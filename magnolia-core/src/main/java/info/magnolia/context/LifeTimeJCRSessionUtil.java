/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.util.HierarchyManagerUtil;
import info.magnolia.objectfactory.Components;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Returns HierarchyManagers which use a life time session. If the SystemContext uses single session the same session
 * will be used. Otherwise an own instance of SystemRepositoryStrategy is used.
 *
 * @version $Id$
 */
public class LifeTimeJCRSessionUtil {

    private static final Logger log = LoggerFactory.getLogger(LifeTimeJCRSessionUtil.class);

    private static SystemRepositoryStrategy repositoryStrategy;

    private static boolean useSystemContext;

    static {
        SystemContext ctx = MgnlContext.getSystemContext();
        useSystemContext = !(ctx instanceof ThreadDependentSystemContext);
        if (!useSystemContext) {
            log.info("Will handle lifetime sessions because the system context is of type {}", ThreadDependentSystemContext.class);
            repositoryStrategy = Components.newInstance(SystemRepositoryStrategy.class);
        }
    }

    @Deprecated
    public static HierarchyManager getHierarchyManager(String workspaceName) {
        try {
            return HierarchyManagerUtil.asHierarchyManager(getSession(workspaceName));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

    }

    public static Session getSession(String workspaceName) throws RepositoryException {
        if (useSystemContext) {
            return MgnlContext.getSystemContext().getJCRSession(workspaceName);
        }
        // we handle the session
        return repositoryStrategy.getSession(workspaceName);
    }

    @Deprecated
    public static QueryManager getQueryManager(String workspaceName) {
        return getHierarchyManager(workspaceName).getQueryManager();
    }

    public static void release() {
        if (useSystemContext) {
            MgnlContext.getSystemContext().release();
        }
        else {
            // we handle the session
            repositoryStrategy.release();
        }
    }
}
