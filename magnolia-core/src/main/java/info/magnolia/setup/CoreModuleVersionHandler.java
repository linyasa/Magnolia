/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.setup;

import static info.magnolia.nodebuilder.Ops.*;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.filters.FilterManager;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FalseCondition;
import info.magnolia.module.delta.OrderFilterBeforeTask;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.RemoveDuplicatePermissionTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WebXmlConditionsUtil;
import info.magnolia.module.delta.WorkspaceXmlConditionsUtil;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.NodeBuilderTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.setup.for3_5.GenericTasks;
import info.magnolia.setup.for3_6.CheckMagnoliaDevelopProperty;
import info.magnolia.setup.for3_6.CheckNodeTypesDefinition;
import info.magnolia.setup.for3_6.CheckNodesForMixVersionable;
import info.magnolia.setup.for4_5.RenameACLNodesTask;
import info.magnolia.setup.for4_5.UpdateSecurityFilterClientCallbacksConfiguration;
import info.magnolia.setup.for4_5.UpdateUserManagers;

import java.util.ArrayList;
import java.util.List;

/**
 * Special VersionHandler for the core module. As it does not extend {@link info.magnolia.module.DefaultModuleVersionHandler} it has a special getBasicInstallTasks(InstallContext) that
 * e.g. will not automatically bootstrap xml-files placed in mgnl-bootstrap/core.
 *
 * @version $Id$
 */
public class CoreModuleVersionHandler extends AbstractModuleVersionHandler {
    public static final String BOOTSTRAP_AUTHOR_INSTANCE_PROPERTY = "magnolia.bootstrap.authorInstance";

    // TODO : why is this a BootstrapConditionally and not a BootstrapSingleResource ?
    private final BootstrapConditionally auditTrailManagerTask = new BootstrapConditionally("New auditory log configuration", "Install new configuration for auditory log manager.", "/mgnl-bootstrap/core/config.server.auditLogging.xml");
    private final BootstrapSingleResource bootstrapFreemarker = new BootstrapSingleResource("Freemarker configuration", "Freemarker template loaders can now be configured in Magnolia. Adds default configuration", "/mgnl-bootstrap/core/config.server.rendering.freemarker.xml");
    private final CreateNodeTask addFreemarkerSharedVariables = new CreateNodeTask("Freemarker configuration", "Adds sharedVariables node to the Freemarker configuration",
            RepositoryConstants.CONFIG, "/server/rendering/freemarker", "sharedVariables", MgnlNodeType.NT_CONTENTNODE);
    private final BootstrapSingleResource bootstrapWebContainerResources = new BootstrapSingleResource("Web container resources configuration", "Global configuration which resources are not meant to be handled by Magnolia. For instance JSP files.", "/mgnl-bootstrap/core/config.server.webContainerResources.xml");
    private final BootstrapSingleModuleResource bootstrapChannelManagement = new BootstrapSingleModuleResource("ChannelManagement configuration", "", "config.server.rendering.channelManagement.xml");

    private final BootstrapSingleModuleResource bootstrapChannelFilter = new BootstrapSingleModuleResource("ChannelFilter configuration", "", "config.server.filters.channel.xml");
    private final Task placeChannelBeforeLogout = new OrderFilterBeforeTask("channel", new String[] {"logout"});

    public CoreModuleVersionHandler() {
        super();

        register(DeltaBuilder.update("4.4.6", "")
                .addCondition(new FalseCondition("checkPrerequisite", "Updating to 4.5 is only supported from 4.4.6. Please udate to 4.4.6 first.")));

        register(DeltaBuilder.update("4.5", "")
                .addCondition(new SystemTmpDirCondition())
                .addTask(new RenameACLNodesTask())
                .addTask(new ArrayDelegateTask("New security filter", "Adds the securityCallback filter.",
                        new NodeBuilderTask("New security filter", "Adds the securityCallback filter.", ErrorHandling.strict, "config", FilterManager.SERVER_FILTERS,
                                addNode("securityCallback", MgnlNodeType.NT_CONTENT).then(
                                        addProperty("class", "info.magnolia.cms.security.SecurityCallbackFilter"),
                                        addNode("bypasses", MgnlNodeType.NT_CONTENTNODE)
                                )
                        ),
                        new OrderNodeBeforeTask("", "Puts the securityCallback just before the uriSecurity filter.", "config", FilterManager.SERVER_FILTERS + "/securityCallback", "uriSecurity")
                ))

                .addTask(new UpdateSecurityFilterClientCallbacksConfiguration("uriSecurity", "securityCallback"))
                .addTask(new UpdateUserManagers())
                .addCondition(new CheckKeyProperty())
                // TODO addTask( move/backup the callbacks in the contentSecurity filter )
                .addTask(new HashUsersPasswords())
                .addTask(bootstrapChannelManagement)
                .addTask(bootstrapChannelFilter)
                .addTask(placeChannelBeforeLogout)
                .addTask(new RemoveDuplicatePermissionTask("Remove duplicate permission", "Remove duplicate permission in workspace Expression for role superuser", "superuser", "acl_Expressions"))
                .addTask(new RemoveDuplicatePermissionTask("Remove duplicate permission", "Remove duplicate permission in workspace Store for role superuser", "superuser", "acl_Store"))
                .addTask(new RemoveDuplicatePermissionTask("Remove duplicate permission", "Remove duplicate permission in workspace forum for role superuser", "superuser", "acl_forum"))
        );
    }

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext ctx) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.addAll(GenericTasks.genericTasksFor35());
        tasks.add(new CheckMagnoliaDevelopProperty());
        tasks.add(new CheckNodesForMixVersionable());
        tasks.add(auditTrailManagerTask);
        tasks.add(bootstrapFreemarker);
        tasks.add(addFreemarkerSharedVariables);
        tasks.add(bootstrapWebContainerResources);
        tasks.add(new BootstrapConditionally("Security", "Bootstraps security-base role.", "/mgnl-bootstrap/core/userroles.security-base.xml"));
        // always hash passwords. Task will not re-hash so it is safe to run this op at any time, multiple times.
        tasks.add(new HashUsersPasswords());
        tasks.add(bootstrapChannelManagement);
        tasks.add(bootstrapChannelFilter);
        tasks.add(placeChannelBeforeLogout);

        return tasks;
    }

    @Override
    protected List<Condition> getInstallConditions() {
        final ArrayList<Condition> conditions = new ArrayList<Condition>();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsNowWrapped("ActivationHandler");
        u.servletIsNowWrapped("AdminTreeServlet");
        u.servletIsNowWrapped("classpathspool");
        u.servletIsNowWrapped("DialogServlet");
        u.servletIsNowWrapped("PageServlet");
        u.servletIsNowWrapped("log4j");
        u.servletIsNowWrapped("FCKEditorSimpleUploadServlet");
        u.servletIsDeprecated("uuidRequestDispatcher");
        u.filterIsDeprecated("info.magnolia.cms.filters.MagnoliaManagedFilter", "info.magnolia.cms.filters.MgnlMainFilter");
        u.filterMustBeRegisteredWithCorrectDispatchers("info.magnolia.cms.filters.MgnlMainFilter");
        u.listenerIsDeprecated("info.magnolia.cms.servlets.PropertyInitializer", "info.magnolia.cms.servlets.MgnlServletContextListener");
        u.listenerIsDeprecated("info.magnolia.cms.beans.config.ShutdownManager", "info.magnolia.cms.servlets.MgnlServletContextListener");
        final WorkspaceXmlConditionsUtil u2 = new WorkspaceXmlConditionsUtil(conditions);
        u2.workspaceHasOldIndexer();

        conditions.add(new CheckNodeTypesDefinition());
        conditions.add(new SystemTmpDirCondition());
        conditions.add(new CheckKeyProperty());

        return conditions;
    }

}
