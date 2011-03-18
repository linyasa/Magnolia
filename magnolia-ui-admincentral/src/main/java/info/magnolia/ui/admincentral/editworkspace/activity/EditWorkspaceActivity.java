/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.editworkspace.activity;

import info.magnolia.ui.admincentral.editworkspace.place.EditWorkspacePlace;
import info.magnolia.ui.admincentral.editworkspace.view.EditWorkspaceView;
import info.magnolia.ui.admincentral.tree.activity.TreeActivityMapper;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;


/**
 * Edit a workspace. Shows the structure view.
 */
public class EditWorkspaceActivity extends AbstractActivity {
    private String workspace;
    private TreeActivityMapper treeActivityMapper;
    private DetailViewActivityMapper detailViewActivityMapper;
    private EditWorkspaceView view;

    public EditWorkspaceActivity(EditWorkspacePlace place, EditWorkspaceView view, TreeActivityMapper treeActivityMapper, DetailViewActivityMapper detailViewActivityMapper) {
        this.workspace = place.getWorkspace();
        this.view = view;
        this.treeActivityMapper = treeActivityMapper;
        this.detailViewActivityMapper = detailViewActivityMapper;
    }

    @Override
    public String mayStop() {
        return "Are you sure you want to leave this page?";
    }

    public void start(ViewPort display, EventBus eventBus) {

        final ActivityManager treeActivityManager = new ActivityManager(treeActivityMapper, eventBus);
        final ActivityManager detailViewActivityManager = new ActivityManager(detailViewActivityMapper, eventBus);

        treeActivityManager.setViewPort(view.getTreeDisplay());
        detailViewActivityManager.setViewPort(view.getDetailDisplay());

        display.setView(view);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((workspace == null) ? 0 : workspace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EditWorkspaceActivity other = (EditWorkspaceActivity) obj;
        if (workspace == null) {
            if (other.workspace != null)
                return false;
        } else if (!workspace.equals(other.workspace))
            return false;
        return true;
    }


}