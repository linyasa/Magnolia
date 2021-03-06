/**
 * This file Copyright (c) 2011-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.jcr.wrapper;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.iterator.ChainedNodeIterator;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang.StringUtils;


/**
 * This wrapper inherits nodes from the parent hierarchy. The method {@link #isAnchor()} defines
 * the anchor to which the inheritance is performed relative to. By default the anchor is of type
 * (mgnl:content).
 * <p>
 * The inheritance is then performed as follows:
 * <ul>
 * <li>try to get the node directly</li>
 * <li>find next anchor</li>
 * <li>try to get the node from the anchor</li>
 * <li>repeat until no anchor can be found anymore (root)</li>
 * </ul>
 * <p>
 * The {@link #getNodes()} and {@link #getNodes(String)} methods merge the direct and inherited children by first adding the
 * inherited children to the iterator and then the direct children.
 */
public class InheritanceNodeWrapper extends ChildWrappingNodeWrapper {

    private final Node start;
    private final AbstractPredicate<Node> filter;

    public InheritanceNodeWrapper(Node node) {
        this(node, node);
    }

    public InheritanceNodeWrapper(Node node, AbstractPredicate<Node> filter) {
        this(node, node, filter);
    }

    public InheritanceNodeWrapper(Node node, Node start, AbstractPredicate<Node> filter) {
        super(node);
        this.start = start;
        this.filter = filter;
    }

    public InheritanceNodeWrapper(Node node, Node start) {
        super(node);
        this.start = start;
        this.filter = new AbstractPredicate<Node>() {

            @Override
            public boolean evaluateTyped(Node t) {
                return true;
            }
        };
    }

    /**
     * Find the anchor for this node.
     */
    protected InheritanceNodeWrapper findAnchor() throws RepositoryException{
        if(this.getDepth() == 0){
            return null;
        }
        if(isAnchor()){
            return this;
        }
        // until the current node is the anchor
        return ((InheritanceNodeWrapper)wrapNode(this.getParent())).findAnchor();
    }

    /**
     * Find next anchor.
     */
    protected InheritanceNodeWrapper findNextAnchor() throws RepositoryException{
        final InheritanceNodeWrapper currentAnchor = findAnchor();
        if(currentAnchor != null && this.getDepth() >0){
            return ((InheritanceNodeWrapper)wrapNode(currentAnchor.getParent())).findAnchor();
        }
        return null;
    }

    /**
     * True if this node is an anchor. By default true if this node is of type {@link info.magnolia.jcr.util.NodeTypes.Content#NAME}.
     */
    protected boolean isAnchor() {
        try {
            return this.isNodeType(NodeTypes.Content.NAME);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * This method returns null if no node has been found.
     */
    protected Node getNodeSafely(String relPath) throws RepositoryException {
        if(getWrappedNode().hasNode(relPath)) {
            return wrapNode(getWrappedNode().getNode(relPath));
        }

        String innerPath = resolveInnerPath() + "/" + relPath;
        innerPath = StringUtils.removeStart(innerPath,"/");

        Node inherited = getNodeSafely(findNextAnchor(), innerPath);
        return inherited;
    }

    /**
     * Returns the inner path of the this node up to the anchor.
     */
    protected String resolveInnerPath() throws RepositoryException {
        final String path;
        InheritanceNodeWrapper anchor = findAnchor();
        // if no anchor left we are relative to the root
        if(anchor == null){
            path = this.getPath();
        }
        else{
            path = StringUtils.substringAfter(this.getPath(), anchor.getPath());
        }
        return StringUtils.removeStart(path,"/");
    }

    /**
     * This method returns null if no node has been found.
     */
    protected Node getNodeSafely(InheritanceNodeWrapper anchor, String path) throws RepositoryException{
        if(anchor == null){
            return null;
        }
        if(StringUtils.isEmpty(path)){
            return anchor;
        }
        return anchor.getNodeSafely(path);
    }


    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return getNodeSafely(relPath) != null;
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        Node inherited = getNodeSafely(relPath);
        if (inherited == null || !filter.evaluateTyped(inherited)) {
            throw new PathNotFoundException("Can't inherit a node [" + relPath + "] on node [" + getWrappedNode().getPath() + "]");
        }
        return wrapNode(inherited);
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        List<NodeIterator> nodes = new ArrayList<NodeIterator>();

        // add inherited children
        try {
            Node inherited = getNodeSafely(findNextAnchor(), resolveInnerPath());
            if(inherited != null && !inherited.getPath().startsWith(this.getPath())){
                nodes.add(inherited.getNodes());
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't inherit children from " + getWrappedNode(), e);
        }
        // add direct children
        nodes.add(getWrappedNode().getNodes());

        return wrapNodeIterator(new FilteringNodeIterator(new ChainedNodeIterator(nodes), filter));
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        List<NodeIterator> nodes = new ArrayList<NodeIterator>();

        // add inherited children
        try {
            Node inherited = getNodeSafely(findNextAnchor(), resolveInnerPath());
            if(inherited != null && !inherited.getPath().startsWith(this.getPath())){
                nodes.add(inherited.getNodes(namePattern));
            }
        }
        catch (RepositoryException e) {
            throw new RepositoryException("Can't inherit children from " + getWrappedNode(), e);
        }
        // add direct children
        nodes.add(getWrappedNode().getNodes(namePattern));

        return wrapNodeIterator(new FilteringNodeIterator(new ChainedNodeIterator(nodes), filter));
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        try {
            if (getWrappedNode().hasProperty(relPath)) {
                return getWrappedNode().getProperty(relPath);
            }
            Node inherited = getNodeSafely(findNextAnchor(), resolveInnerPath());
            if(inherited != null){
                return inherited.getProperty(relPath);
            } else {
                throw new PathNotFoundException("No property exists at " + relPath + " or current Session does not have read access to it.");
            }
        }
        catch (RepositoryException e) {
            throw new RepositoryException("Can't inherit property " + relPath + "  for " + getWrappedNode(), e);
        }
    }

    @Override
    public boolean hasProperty(String name) throws RepositoryException {
        try {
            if (getWrappedNode().hasProperty(name)) {
                return true;
            }
            Node inherited = getNodeSafely(findNextAnchor(), resolveInnerPath());
            if (inherited != null) {
                return inherited.hasProperty(name);
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Can't inherit nodedata " + name + "  for " + getWrappedNode(), e);

        }
        // creates a none existing node data in the standard manner
        return super.hasProperty(name);
    }

    /**
     * True if this is not a sub node of the starting point.
     */
    public boolean isInherited() {
        try {
            return !this.getPath().startsWith(start.getPath());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Node wrapNode(Node node) {
        if(node instanceof InheritanceNodeWrapper) {
            return node;
        }
        return new InheritanceNodeWrapper(node, start, filter);
    }
}
