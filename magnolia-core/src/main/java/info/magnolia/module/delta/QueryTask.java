/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

/**
 * An abstract task to perform operations on nodes returned by a given query.
 * Keep in mind that results returned by a query will reflect the current
 * content of the repository, not of your session; i.e if a previous task
 * modified a property, the query might still return the node as if it had
 * the previous value.
 *
 * @version $Id$
 */
public abstract class QueryTask extends AbstractRepositoryTask {
    private final String repositoryName;
    private final String query;

    public QueryTask(String name, String description, String repositoryName, String query) {
        super(name, description);
        this.repositoryName = repositoryName;
        this.query = query;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final NodeIterator nodeIterator = QueryUtil.search(repositoryName, query, Query.JCR_SQL2);
        while(nodeIterator.hasNext()){
            operateOnNode(installContext, nodeIterator.nextNode());
        }
    }

    /**
     * @deprecated Since 4.5.4 use info.magnolia.module.delta.QueryTask.operateOnNode(InstallContext, Node)
     */
    protected abstract void operateOnNode(InstallContext installContext, Content node);

    protected abstract void operateOnNode(InstallContext installContext, Node node);
}
