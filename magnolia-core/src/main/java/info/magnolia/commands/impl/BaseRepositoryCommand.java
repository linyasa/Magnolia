/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.commands.impl;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for the commands that work with the repository.
 * 
 */
public abstract class BaseRepositoryCommand extends MgnlCommand {

    private String path = "/";

    private String repository;

    private String uuid;

    /**
     * @deprecated since 4.5.7 use {@link #getJCRNode(Context)} instead
     */
    @Deprecated
    protected Content getNode(Context ctx) throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(getRepository());
        final Content node;
        if (StringUtils.isNotEmpty(getUuid())) {
            node = hm.getContentByUUID(getUuid());
        } else {
            node = hm.getContent(getPath());
        }
        PermissionUtil.isGranted(node.getJCRNode().getSession(), node.getHandle(), Session.ACTION_READ);
        return node;
    }

    protected Node getJCRNode(Context ctx) throws RepositoryException {
        final Session hm = MgnlContext.getSystemContext().getJCRSession(getRepository());
        final Node node;
        if (StringUtils.isNotEmpty(getUuid())) {
            node = hm.getNodeByIdentifier(getUuid());
        } else {
            node = hm.getNode(getPath());
        }
        return node;
    }

    /**
     * @return the repository
     */
    public String getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public void release() {
        super.release();
        this.uuid = null;
        this.path = null;
        this.repository = null;
    }

}
