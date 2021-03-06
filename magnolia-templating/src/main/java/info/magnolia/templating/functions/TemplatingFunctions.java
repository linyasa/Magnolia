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
package info.magnolia.templating.functions;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.jcr.inheritance.InheritanceNodeWrapper;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.jcr.wrapper.HTMLEscapingNodeWrapper;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.configured.ConfiguredInheritance;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.templating.inheritance.DefaultInheritanceContentDecorator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object exposing several methods useful for templates. It is exposed in templates as <code>cmsfn</code>.
 *
 * @version $Id$
 */
public class TemplatingFunctions {

    private static final Logger log = LoggerFactory.getLogger(TemplatingFunctions.class);

    private final Provider<AggregationState> aggregationStateProvider;

    //TODO: To review with Philipp. Should not use Provider, but has deep impact on CategorizationSupportImpl PageSyndicator CategorySyndicator....
    @Inject
    public TemplatingFunctions(Provider<AggregationState> aggregationStateProvider) {
        this.aggregationStateProvider = aggregationStateProvider;
    }


    public Node asJCRNode(ContentMap contentMap) {
        return contentMap == null ? null : contentMap.getJCRNode();
    }

    public ContentMap asContentMap(Node content) {
        return content == null ? null : new ContentMap(content);
    }

    public List<Node> children(Node content) throws RepositoryException {
        return content == null ? null : asNodeList(NodeUtil.getNodes(content, NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<Node> children(Node content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : asNodeList(NodeUtil.getNodes(content, nodeTypeName));
    }

    public List<ContentMap> children(ContentMap content) throws RepositoryException {
        return content == null ? null : asContentMapList(NodeUtil.getNodes(asJCRNode(content), NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<ContentMap> children(ContentMap content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : asContentMapList(NodeUtil.getNodes(asJCRNode(content), nodeTypeName));
    }

    public ContentMap root(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode()));
    }

    public ContentMap root(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode(), nodeTypeName));
    }

    public Node root(Node content) throws RepositoryException {
        return this.root(content, null);
    }

    public Node root(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (nodeTypeName == null) {
            return (Node) content.getAncestor(0);
        }
        if (isRoot(content) && content.isNodeType(nodeTypeName)) {
            return content;
        }

        Node parentNode = this.parent(content, nodeTypeName);
        while (parent(parentNode, nodeTypeName) != null) {
            parentNode = this.parent(parentNode, nodeTypeName);
        }
        return parentNode;
    }

    public ContentMap parent(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.parent(contentMap.getJCRNode()));
    }

    public ContentMap parent(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.parent(contentMap.getJCRNode(), nodeTypeName));
    }

    public Node parent(Node content) throws RepositoryException {
        return this.parent(content, null);
    }

    public Node parent(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (isRoot(content)) {
            return null;
        }
        if (nodeTypeName == null) {
            return content.getParent();
        }
        Node parent = content.getParent();
        while (!parent.isNodeType(nodeTypeName)) {
            if (isRoot(parent)) {
                return null;
            }
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Returns the page's {@link ContentMap} of the passed {@link ContentMap}. If the passed {@link ContentMap} represents a page, the passed {@link ContentMap} will be returned.
     * If the passed {@link ContentMap} has no parent page at all, null is returned.
     *
     * @param content the {@link ContentMap} to get the page's {@link ContentMap} from.
     * @return returns the page {@link ContentMap} of the passed content {@link ContentMap}.
     * @throws RepositoryException
     */
    public ContentMap page(ContentMap content) throws RepositoryException {
        return content == null ? null : asContentMap(page(content.getJCRNode()));
    }

    /**
     * Returns the page {@link Node} of the passed node. If the passed {@link Node} is a page, the passed {@link Node} will be returned.
     * If the passed Node has no parent page at all, null is returned.
     *
     * @param content the {@link Node} to get the page from.
     * @return returns the page {@link Node} of the passed content {@link Node}.
     * @throws RepositoryException
     */
    public Node page(Node content) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (content.isNodeType(NodeTypes.Page.NAME)) {
            return content;
        }
        return parent(content, NodeTypes.Page.NAME);
    }

    public List<ContentMap> ancestors(ContentMap contentMap) throws RepositoryException {
        return ancestors(contentMap, null);
    }

    public List<ContentMap> ancestors(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        List<Node> ancestorsAsNodes = this.ancestors(contentMap.getJCRNode(), nodeTypeName);
        return asContentMapList(ancestorsAsNodes);
    }

    public List<Node> ancestors(Node content) throws RepositoryException {
        return content == null ? null : this.ancestors(content, null);
    }

    public List<Node> ancestors(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        List<Node> ancestors = new ArrayList<Node>();
        int depth = content.getDepth();
        for (int i = 1; i < depth; ++i) {
            Node possibleAncestor = (Node) content.getAncestor(i);
            if (nodeTypeName == null) {
                ancestors.add(possibleAncestor);
            } else {
                if (possibleAncestor.isNodeType(nodeTypeName)) {
                    ancestors.add(possibleAncestor);
                }
            }
        }
        return ancestors;
    }

    public Node inherit(Node content) throws RepositoryException {
        return inherit(content, null);
    }

    public Node inherit(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        Node inheritedNode = wrapForInheritance(content);

        if (StringUtils.isBlank(relPath)) {
            return inheritedNode;
        }

        try {
            Node subNode = inheritedNode.getNode(relPath);
            return NodeUtil.unwrap(subNode);
        } catch (PathNotFoundException e) {
            // TODO fgrilli: rethrow exception?
        }
        return null;
    }

    public ContentMap inherit(ContentMap content) throws RepositoryException {
        return inherit(content, null);
    }

    public ContentMap inherit(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        Node node = inherit(content.getJCRNode(), relPath);
        return node == null ? null : new ContentMap(node);
    }


    public Property inheritProperty(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        try {
            Node inheritedNode = wrapForInheritance(content);
            return inheritedNode.getProperty(relPath);

        } catch (PathNotFoundException e) {
            // TODO fgrilli: rethrow exception?
        } catch (RepositoryException e) {
            // TODO fgrilli:rethrow exception?
        }

        return null;
    }

    public Property inheritProperty(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        return inheritProperty(content.getJCRNode(), relPath);
    }

    public List<Node> inheritList(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        Node inheritedNode = wrapForInheritance(content);
        Node subNode = inheritedNode.getNode(relPath);
        return children(subNode);
    }

    public List<ContentMap> inheritList(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        Node node = asJCRNode(content);
        Node inheritedNode = wrapForInheritance(node);
        Node subNode = inheritedNode.getNode(relPath);
        return children(new ContentMap(subNode));
    }

    public boolean isInherited(Node content) {
        if (content instanceof InheritanceNodeWrapper) {
            return ((InheritanceNodeWrapper) content).isInherited();
        }
        return false;
    }

    public boolean isInherited(ContentMap content) {
        return isInherited(asJCRNode(content));
    }

    public boolean isFromCurrentPage(Node content) {
        return !isInherited(content);
    }

    public boolean isFromCurrentPage(ContentMap content) {
        return isFromCurrentPage(asJCRNode(content));
    }

    /**
     * Create link for the Node identified by nodeIdentifier in the specified workspace.
     */
    public String link(String workspace, String nodeIdentifier) {
        try {
            return LinkUtil.createLink(workspace, nodeIdentifier);
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * There should be no real reason to use this method except to produce link to binary content stored in jcr:data property in which case one should call {@link #link(Node)} while passing parent node as a parameter. In case you find other valid reason to use this method, please raise it in a forum discussion or create issue. Otherwise this method will be removed in the future.
     * 
     * @deprecated since 4.5.4. There is no valid use case for this method.
     */
    @Deprecated
    public String link(Property property) {
        try {
            Node parentNode = null;
            String propertyName = null;
            if (property.getType() == PropertyType.BINARY) {
                parentNode = property.getParent().getParent();
                propertyName = property.getParent().getName();
            } else {
                parentNode = property.getParent();
                propertyName = property.getName();
            }
            NodeData equivNodeData = ContentUtil.asContent(parentNode).getNodeData(propertyName);
            return LinkUtil.createLink(equivNodeData);
        } catch (Exception e) {
            return null;
        }
    }

    public String link(Node content) {
        return content == null ? null : LinkUtil.createLink(content);
    }

    public String link(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : this.link(asJCRNode(contentMap));
    }

    /**
     * Get the language used currently.
     * @return The language as a String.
     */
    public String language(){
        return I18nContentSupportFactory.getI18nSupport().getLocale().toString();
    }
    /**
     * Returns an external link prepended with <code>http://</code> in case the protocol is missing or an empty String
     * if the link does not exist.
     *
     * @param content The node where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @return The link prepended with <code>http://</code>
     */
    public String externalLink(Node content, String linkPropertyName) {
        String externalLink = PropertyUtil.getString(content, linkPropertyName);
        if (StringUtils.isBlank(externalLink)) {
            return StringUtils.EMPTY;
        }
        if (!hasProtocol(externalLink)) {
            externalLink = "http://" + externalLink;
        }
        return externalLink;
    }

    /**
     * Returns an external link prepended with <code>http://</code> in case the protocol is missing or an empty String
     * if the link does not exist.
     *
     * @param content The node's map representation where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @return The link prepended with <code>http://</code>
     */
    public String externalLink(ContentMap content, String linkPropertyName) {
        return externalLink(asJCRNode(content), linkPropertyName);
    }

    /**
     * Return a link title based on the @param linkTitlePropertyName. When property @param linkTitlePropertyName is
     * empty or null, the link itself is provided as the linkTitle (prepended with <code>http://</code>).
     *
     * @param content The node where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @param linkTitlePropertyName The property where the link title value is stored
     * @return the resolved link title value
     */
    public String externalLinkTitle(Node content, String linkPropertyName, String linkTitlePropertyName) {
        String linkTitle = PropertyUtil.getString(content, linkTitlePropertyName);
        if (StringUtils.isNotEmpty(linkTitle)) {
            return linkTitle;
        }
        return externalLink(content, linkPropertyName);
    }

    /**
     * Return a link title based on the @param linkTitlePropertyName. When property @param linkTitlePropertyName is
     * empty or null, the link itself is provided as the linkTitle (prepended with <code>http://</code>).
     *
     * @param content The node where the link property is stored on.
     * @param linkPropertyName The property where the link value is stored in.
     * @param linkTitlePropertyName The property where the link title value is stored
     * @return the resolved link title value
     */
    public String externalLinkTitle(ContentMap content, String linkPropertyName, String linkTitlePropertyName) {
        return externalLinkTitle(asJCRNode(content), linkPropertyName, linkTitlePropertyName);
    }

    public boolean isEditMode() {
        // TODO : see CmsFunctions.isEditMode, which checks a couple of other properties.
        return isAuthorInstance() && !isPreviewMode();
    }

    public boolean isPreviewMode() {
        return this.aggregationStateProvider.get().isPreviewMode();
    }

    public boolean isAuthorInstance() {
        return Components.getComponent(ServerConfiguration.class).isAdmin();
    }

    public boolean isPublicInstance() {
        return !isAuthorInstance();
    }

    /**
     * Util method to create html attributes <code>name="value"</code>. If the value is empty an empty string will be returned.
     * This is mainly helpful to avoid empty attributes.
     */
    public String createHtmlAttribute(String name, String value) {
        value = StringUtils.trim(value);
        if (StringUtils.isNotEmpty(value)) {
            return new StringBuffer().append(name).append("=\"").append(value).append("\"").toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns an instance of SiblingsHelper for the given node.
     */
    public SiblingsHelper siblings(Node node) throws RepositoryException {
        return SiblingsHelper.of(ContentUtil.asContent(node));
    }

    public SiblingsHelper siblings(ContentMap node) throws RepositoryException {
        return siblings(asJCRNode(node));
    }

    /**
     * Return the Node for the Given Path
     * from the website repository.
     */
    public Node content(String path){
        return content(RepositoryConstants.WEBSITE, path);
    }

    /**
     * Return the Node for the Given Path
     * from the given repository.
     */
    public Node content(String repository, String path){
        return SessionUtil.getNode(repository, path);
    }

    /**
     * Return the Node by the given identifier
     * from the website repository.
     */
    public Node contentByIdentifier(String id){
        return contentByIdentifier(RepositoryConstants.WEBSITE, id);
    }

    /**
    * Return the Node by the given identifier
    * from the given repository.
    */
    public Node contentByIdentifier(String repository, String id){
        return SessionUtil.getNodeByIdentifier(repository, id);
    }

    public List<ContentMap> asContentMapList(Collection<Node> nodeList) {
        if (nodeList != null) {
            List<ContentMap> contentMapList = new ArrayList<ContentMap>();
            for (Node node : nodeList) {
                contentMapList.add(asContentMap(node));
            }
            return contentMapList;
        }
        return null;
    }

    public List<Node> asNodeList(Collection<ContentMap> contentMapList) {
        if (contentMapList != null) {
            List<Node> nodeList = new ArrayList<Node>();
            for (ContentMap node : contentMapList) {
                nodeList.add(node.getJCRNode());
            }
            return nodeList;
        }
        return null;
    }

    // TODO fgrilli: should we unwrap children?
    protected List<Node> asNodeList(Iterable<Node> nodes) {
        List<Node> childList = new ArrayList<Node>();
        for (Node child : nodes) {
            childList.add(child);
        }
        return childList;
    }

    // TODO fgrilli: should we unwrap children?
    protected List<ContentMap> asContentMapList(Iterable<Node> nodes) {
        List<ContentMap> childList = new ArrayList<ContentMap>();
        for (Node child : nodes) {
            childList.add(new ContentMap(child));
        }
        return childList;
    }

    /**
     * Checks if passed string has a <code>http://</code> protocol.
     *
     * @param link The link to check
     * @return If @param link contains a <code>http://</code> protocol
     */
    private boolean hasProtocol(String link) {
        return link != null && link.contains("://");
    }

    /**
     * Checks if the passed {@link Node} is the jcr root '/' of the workspace.
     * @param content {@link Node} to check if its root.
     * @return if @param content is the jcr workspace root.
     * @throws RepositoryException
     */
    private boolean isRoot(Node content) throws RepositoryException {
        return content.getDepth() == 0;
    }

    /**
     * Removes escaping of HTML on properties.
     */
    public ContentMap decode(ContentMap content){
        return asContentMap(decode(content.getJCRNode()));
    }

    /**
     * Removes escaping of HTML on properties.
     */
    public Node decode(Node content){
        return NodeUtil.deepUnwrap(content, HTMLEscapingNodeWrapper.class);
    }

    /**
     * Adds escaping of HTML on properties as well as changing line breaks into &lt;br/&gt; tags.
     */
    public Node encode(Node content){
        return content != null ? new HTMLEscapingNodeWrapper(content, true) : null;
    }

    private Node wrapForInheritance(Node destination) throws RepositoryException {
        ConfiguredInheritance inheritanceConfiguration = new ConfiguredInheritance();
        inheritanceConfiguration.setEnabled(true);
        return new DefaultInheritanceContentDecorator(destination, inheritanceConfiguration).wrapNode(destination);
    }

    /**
     * Returns the string representation of a property from the metaData of the node or <code>null</code> if the node has no Magnolia metaData or if no matching property is found.
     */
    public String metaData(Node content, String property){

        Object returnValue;
        try {
            if (property.equals(NodeTypes.Created.CREATED)) {
                returnValue = NodeTypes.Created.getCreated(content);
            } else if (property.equals(NodeTypes.Created.CREATED_BY)) {
                returnValue = NodeTypes.Created.getCreatedBy(content);
            } else if (property.equals(NodeTypes.LastModified.LAST_MODIFIED)) {
                returnValue = NodeTypes.LastModified.getLastModified(content);
            } else if (property.equals(NodeTypes.LastModified.LAST_MODIFIED_BY)) {
                returnValue = NodeTypes.LastModified.getLastModifiedBy(content);
            } else if (property.equals(NodeTypes.Renderable.TEMPLATE)) {
                returnValue = NodeTypes.Renderable.getTemplate(content);
            } else if (property.equals(NodeTypes.Activatable.LAST_ACTIVATED)) {
                returnValue = NodeTypes.Activatable.getLastActivated(content);
            } else if (property.equals(NodeTypes.Activatable.LAST_ACTIVATED_BY)) {
                returnValue = NodeTypes.Activatable.getLastActivatedBy(content);
            } else if (property.equals(NodeTypes.Activatable.ACTIVATION_STATUS)) {
                returnValue = NodeTypes.Activatable.getActivationStatus(content);
            } else if (property.equals(NodeTypes.Deleted.DELETED)) {
                returnValue = NodeTypes.Deleted.getDeleted(content);
            } else if (property.equals(NodeTypes.Deleted.DELETED_BY)) {
                returnValue = NodeTypes.Deleted.getDeletedBy(content);
            } else if (property.equals(NodeTypes.Deleted.COMMENT)) {
                // Since NodeTypes.Deleted.COMMENT and NodeTypes.Versionable.COMMENT have identical names this will work for both
                returnValue = NodeTypes.Deleted.getComment(content);
            } else {

                // Try to get the value using one of the deprecated names in MetaData.
                // This throws an IllegalArgumentException if its not one of those constants
                returnValue = MetaDataUtil.getMetaData(content).getStringProperty(property);

                // If no exception was thrown then warn that a legacy constant was used
                log.warn("Deprecated constant [" + property+"] used to query for meta data property on node [" + NodeUtil.getPathIfPossible(content) + "]");
            }
        } catch (RepositoryException e) {
            return "";
        }

        return returnValue instanceof Calendar ? ISO8601.format((Calendar) returnValue) : returnValue.toString();
    }

    /**
     * @see {@link TemplatingFunctions#metaData(Node, String)}.
     */
    public String metaData(ContentMap content, String property){
        return metaData(content.getJCRNode(), property);
    }
    
    /**
     * Executes query and returns result as Collection of Nodes.
     * 
     * @param workspace
     * @param statement has to be in formal form for chosen language
     * @param language
     * @param returnItemType
     */
    public Collection<Node> search(String workspace, String statement, String language, String returnItemType){
        try {
            return NodeUtil.getCollectionFromNodeIterator(QueryUtil.search(workspace, statement, language, returnItemType));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Executes simple SQL2 query and returns result as Collection of Nodes.
     * 
     * @param workspace
     * @param statement should be set of labels target has to contain inserted as one string each separated by comma
     * @param returnItemType
     * @param startPath can be inserted, for results without limitation set it to slash
     */
    public Collection<Node> simpleSearch(String workspace, String statement, String returnItemType, String startPath){
        if(StringUtils.isEmpty(statement)){
            log.error("Cannot search with empty statement.");
            return null;
        }
        String query = QueryUtil.buildQuery(statement, startPath);
        try {
            return NodeUtil.getCollectionFromNodeIterator(QueryUtil.search(workspace, query, "JCR-SQL2", returnItemType));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
