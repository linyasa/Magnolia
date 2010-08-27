/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.module.admincentral.AdminCentralVaadinApplication;

/**
 * A column that displays icons for permissions and activation status.
 */
public class StatusColumn extends TreeColumn {

    private boolean activation = true;
    private boolean permissions = false;

    public boolean isActivation() {
        return activation;
    }

    public void setActivation(boolean activation) {
        this.activation = activation;
    }

    public boolean isPermissions() {
        return permissions;
    }

    public void setPermissions(boolean permissions) {
        this.permissions = permissions;
    }

    @Override
    public Class<?> getType() {
        return HorizontalLayout.class;
    }

    @Override
    public Object getValue(Content content) {

        HorizontalLayout layout = new HorizontalLayout();

        if (activation) {
            layout.addComponent(createIcon("/mgnl-resources/icons/16/" + MetaDataUtil.getActivationStatusIcon(content)));
        }

        if (permissions && !content.isGranted(info.magnolia.cms.security.Permission.WRITE)) {
            layout.addComponent(createIcon("/mgnl-resources/icons/16/" + "pen_blue_canceled.gif"));
        }

        return layout;
    }

    private Component createIcon(String resource) {
        Embedded embedded = new Embedded();
        embedded.setType(Embedded.TYPE_IMAGE);
        embedded.setSource(new ClassResource(resource, AdminCentralVaadinApplication.application));
        embedded.setWidth("16px");
        embedded.setHeight("16px");
        return embedded;
    }

    @Override
    public Object getValue(Content content, NodeData nodeData) {
        return null;
    }
}