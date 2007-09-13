/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.util.NodeNameComparator;
import info.magnolia.module.admininterface.AbstractTreeConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class RolesTreeConfiguration extends AbstractTreeConfiguration {

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.setSortComparator(new NodeNameComparator());

        tree.setDrawShifter(false);

        tree.setIcon(ItemType.ROLE.getSystemName(), Tree.ICONDOCROOT + "hat_white.gif"); //$NON-NLS-1$
        tree.setIconOndblclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/roleedit.html');"); //$NON-NLS-1$
        tree.addItemType(ItemType.ROLE);

        final Messages msgs = getMessages();
        TreeColumn column0 = TreeColumn.createLabelColumn(tree, msgs.get("tree.roles.name"), !browseMode);
        column0.setWidth(2);

        TreeColumn column1 = TreeColumn.createNodeDataColumn(tree, msgs.get("tree.roles.fullname"), "title", !browseMode);
        column1.setWidth(2);

        TreeColumn columnIcons = TreeColumn.createActivationColumn(tree);        

        TreeColumn column2 = TreeColumn.createMetaDataColumn(tree, msgs.get("tree.roles.date"), MetaData.LAST_MODIFIED, "yyyy-MM-dd, HH:mm");
        column2.setWidth(2);

        tree.addColumn(column0);
        if (!browseMode) {
            tree.addColumn(column1);
            if (Server.isAdmin() || ActivationManagerFactory.getActivationManager().hasAnyActiveSubscriber()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();

        ContextMenuItem menuOpen = new ContextMenuItem("edit");
        menuOpen.setLabel(msgs.get("tree.roles.menu.edit")); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/hat_white.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/roleedit.html');"); //$NON-NLS-1$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuNewPage = new ContextMenuItem("new");
        menuNewPage.setLabel(msgs.get("tree.roles.menu.new")); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/hat_white_add.gif"); //$NON-NLS-1$
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createRootNode('" //$NON-NLS-1$
            + ItemType.ROLE.getSystemName()
            + "');"); //$NON-NLS-1$
        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel(msgs.get("tree.roles.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuMove = new ContextMenuItem("move");
        menuMove.setLabel(msgs.get("tree.roles.menu.move")); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel(msgs.get("tree.roles.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel(msgs.get("tree.roles.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuDeactivate = new ContextMenuItem("deactivate");
        menuDeactivate.setLabel(msgs.get("tree.roles.menu.deactivate")); //$NON-NLS-1$
        menuDeactivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeactivate.setOnclick(tree.getJavascriptTree() + ".deactivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        if (!ActivationManagerFactory.getActivationManager().hasAnyActiveSubscriber()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!browseMode) {
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuNewPage);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuDelete);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeactivate);
        }
        else {
            tree.addMenuItem(ContextMenuItem.getRefreshMenuItem(tree, msgs, request));
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareFunctionBar(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.addFunctionBarItemFromContextMenu("edit");
        tree.addFunctionBarItemFromContextMenu("new");
        tree.addFunctionBarItem(null);
        tree.addFunctionBarItemFromContextMenu("activate");
        tree.addFunctionBarItemFromContextMenu("deactivate");
        tree.addFunctionBarItem(null);
        tree.addFunctionBarItem(FunctionBarItem.getRefreshFunctionBarItem(tree, getMessages(), request));
    }

}
