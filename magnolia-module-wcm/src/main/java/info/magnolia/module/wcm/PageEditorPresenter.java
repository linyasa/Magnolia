/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.wcm;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import com.vaadin.Application;
import info.magnolia.cms.core.MetaData;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.wcm.editor.SelectionChangedEvent;
import info.magnolia.module.wcm.editor.SelectionChangedHandler;
import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.module.wcm.toolbox.ToolboxActionFactory;
import info.magnolia.module.wcm.toolbox.ToolboxView;
import info.magnolia.ui.admincentral.MainActivityMapper;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerBasedViewPort;

/**
 * Presenter logic for page editor.
 */
public class PageEditorPresenter implements ToolboxView.Presenter, SelectionChangedHandler, PageChangedHandler {

    private Shell shell;
    private EventBus eventBus;
    private PlaceController placeController;
    private MainActivityMapper mainActivityMapper;
    private PageEditorView pageEditorView;
    private ToolboxView toolboxView;
    private WcmModule wcmModule;
    private Application application;
    private DialogPresenterFactory dialogPresenterFactory;
    private ToolboxActionFactory toolboxActionFactory;

    // TODO should not depend on wcmModule but rather on a configuration provider

    public PageEditorPresenter(Shell shell, EventBus eventBus, PlaceController placeController, MainActivityMapper mainActivityMapper, PageEditorView pageEditorView, Application application, DialogPresenterFactory dialogPresenterFactory, WcmModule wcmModule, ToolboxActionFactory toolboxActionFactory) {
        this.shell = shell;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.mainActivityMapper = mainActivityMapper;
        this.pageEditorView = pageEditorView;
        this.wcmModule = wcmModule;
        this.application = application;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.toolboxActionFactory = toolboxActionFactory;

        eventBus.addHandler(PageChangedEvent.class, this);
    }

    public void init() {

        PlaceHistoryMapper historyMapper = new PlaceHistoryMapperImpl(PageEditorPlace.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper, shell);
        Place defaultPlace = Place.NOWHERE;
        historyHandler.register(placeController, eventBus, defaultPlace);

        ActivityManager activityManager = new ActivityManager(mainActivityMapper, eventBus);
        activityManager.setViewPort(new ComponentContainerBasedViewPort("main", pageEditorView.getEditorContainer()));

        historyHandler.handleCurrentHistory();

        toolboxView = pageEditorView.getToolboxView();
        toolboxView.setPresenter(this);

        eventBus.addHandler(SelectionChangedEvent.class, this);

        toolboxView.showRack(wcmModule.getToolboxConfiguration().getPage());
    }

    private String type; // "page" "area" "paragraph"
    private ContentSelection contentSelection;

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {

        this.type = event.getType();

        this.contentSelection = new ContentSelection();
        this.contentSelection.setWorkspace(event.getWorkspace());
        this.contentSelection.setPath(event.getPath());
        this.contentSelection.setCollectionName(event.getCollectionName());
        this.contentSelection.setNodeName(event.getNodeName());

        if ("page".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getPage());
        } else if ("area".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getArea());
        } else if ("paragraph".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getParagraph());
        }
    }

    @Override
    public void onMenuItemSelected(MenuItemDefinition menuItem) {
        System.out.println("Command clicked " + menuItem);

        ContentSelection selection = this.contentSelection;
        if (selection == null) {
            // TODO this should mean the page itself, but only the activity knows about the page...
            return;
        }

        Node node;
        try {
            node = MgnlContext.getJCRSession(selection.getWorkspace()).getNode(selection.getPath());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        Action action = toolboxActionFactory.createAction(menuItem.getActionDefinition(), node, selection);
        System.out.println("Executing " + action.getClass());
        try {
            action.execute();
        } catch (ActionExecutionException e) {
            // TODO present error to user
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void openDialog(String dialogName, String workspace, String path, String collectionName) {
        DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
        dialogPresenter.setWorkspace(workspace);
        dialogPresenter.setPath(path);
        dialogPresenter.setCollectionName(collectionName);
        dialogPresenter.showDialog();
    }

    public void addParagraph(final String workspace, final String path, final String collectionName, String paragraphs) {
        final ContentSelection selection = new ContentSelection();
        selection.setWorkspace(workspace);
        selection.setPath(path);
        selection.setCollectionName(collectionName);
        ParagraphSelectionDialog paragraphSelectionDialog = new ParagraphSelectionDialog(StringUtils.split(paragraphs, ", \t\n")) {
            @Override
            protected void onClosed(final Paragraph paragraph) {
                String dialogName = PageEditorHacks.getDialogUsedByParagraph(paragraph);
                if (dialogName != null) {

                    DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
                    dialogPresenter.setWorkspace(selection.getWorkspace());
                    dialogPresenter.setPath(selection.getPath());
                    dialogPresenter.setCollectionName(selection.getCollectionName());
                    dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
                        @Override
                        public void onSave(Node node) {
                            try {
                                MetaData metaData = JCRMetadataUtil.getMetaData(node);
                                metaData.setTemplate(paragraph.getName());
                                node.getSession().save();
                            } catch (RepositoryException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    });
                    dialogPresenter.showDialog();
                }
            }
        };

        application.getMainWindow().addWindow(paragraphSelectionDialog);
    }

    public void selectionChanged(String type, String workspace, String path, String collectionName, String nodeName) {
        // TODO we fire the event from this class and receives it in this class, not really necessary
        eventBus.fireEvent(new SelectionChangedEvent(type, workspace, path, collectionName, nodeName));
    }

    @Override
    public void onPageChanged() {
        this.toolboxView.showRack(wcmModule.getToolboxConfiguration().getPage());
    }
}
