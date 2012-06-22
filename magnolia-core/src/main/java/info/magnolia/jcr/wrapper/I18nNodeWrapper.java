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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.ContentUtil;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * A Node wrapper implementation which knows about i18n support and uses it to select child nodes and properties.
 *
 * @version $Id$
 * @see info.magnolia.cms.i18n.I18nContentSupport
 */
public class I18nNodeWrapper extends ChildWrappingNodeWrapper {

    private final I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();

    public I18nNodeWrapper(Node wrapped) {
        super(wrapped);
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        return i18nSupport.getNodeData(ContentUtil.asContent(getWrappedNode()), relPath).isExist();
    }

    @Override
    public Node wrapNode(Node node) {
        if (node == null) {
            return null;
        }
        return new I18nNodeWrapper(node);
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return i18nSupport.getNodeData(ContentUtil.asContent(getWrappedNode()), relPath).getJCRProperty();
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return wrapNode(i18nSupport.getNode(getWrappedNode(), relPath));
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return wrapNode(super.getParent());
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return wrapNode((Node) super.getAncestor(depth));
    }
}
