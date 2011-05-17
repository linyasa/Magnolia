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
package info.magnolia.ui.admincentral.column;

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.column.client.VEditable;
import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.tree.view.JcrBrowser;
import info.magnolia.ui.framework.editor.Editor;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;


/**
 * UI component that displays a label and on double click opens it for editing by switching the
 * label to save text field. Implements {@link Comparable} to allow sorting of columns holding this
 * component with Vaadin. Default implementation for <code>compareTo(..),</code> method uses jcr's
 * item name for comparison. Subclasses may use more specific properties.
 * FIXME: no longer need to implement comparable as we use jcr sorting by default now.
 *
 * @author tmattsson
 * @author mrichert
 */
@ClientWidget(value = VEditable.class)
public abstract class AbstractEditable extends CustomComponent implements Comparable<AbstractEditable> {

    private static final Logger log = LoggerFactory.getLogger(AbstractEditable.class);

    /**
     * Presenter for AbstractEditable.
     */
    public interface Presenter {

        void edit(Item item, Editor editor) throws RepositoryException;

        boolean save(Item item) throws RepositoryException;

        void onClick(Item item) throws RepositoryException;
    }

    /**
     * Result object used by subclasses to return the component to switch to on double click and an editor which is to
     * be used by the editor driver.
     */
    public static class ComponentAndEditor {

        private Component component;

        private Editor editor;

        public ComponentAndEditor(Component component, Editor editor) {
            this.component = component;
            this.editor = editor;
        }

        public Component getComponent() {
            return component;
        }

        public Editor getEditor() {
            return editor;
        }
    }

    private final String workspace;

    private final String nodeIdentifier;

    private final String propertyName;
    private CssLayout layout;
    private Presenter presenter;

    protected AbstractEditable(Item item, Presenter presenter) throws RepositoryException {

        this.presenter = presenter;

        workspace = item.getSession().getWorkspace().getName();
        nodeIdentifier = item.isNode() ? ((Node) item).getIdentifier() : item.getParent().getIdentifier();
        propertyName = item.isNode() ? null: item.getName();

        layout = new CssLayout();

        // TODO the double click event should be removed when the text field is visible, otherwise its not possible to double click to mark words
        layout.addListener(new LayoutEvents.LayoutClickListener() {

            @Override
            public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
                Component parent = AbstractEditable.this.getParent();
                while (!(parent instanceof JcrBrowser)) {
                    parent = parent.getParent();
                    if (parent == null) {
                        return;
                    }
                }
                JcrBrowser browser = (JcrBrowser) parent;
                String path;
                try {
                    path = getItem().getPath();
                }
                catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
                JcrContainer container = browser.getContainer();
                ContainerItemId itemId = container.getItemByPath(path);

                if (browser.isSelected(itemId)) {
                    try {
                        Item item = getItem();
                        ComponentAndEditor componentAndEditor = getComponentAndEditor(item);
                        AbstractEditable.this.presenter.edit(item, componentAndEditor.getEditor());
                        layout.removeAllComponents();
                        layout.addComponent(componentAndEditor.getComponent());
                    }
                    catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                }
            }
        });
        layout.addComponent(new Label(getLabelText(item)));
        layout.setSizeFull();
        setCompositionRoot(layout);
        setSizeFull();

        //FIXME this is a hack to show the label which is a div inline with the preceding icons. It probably breaks on IE as it uses display: inline-block.
        addStyleName("m-inline-div");
    }

    protected void onCancel() {
        try {
            layout.removeAllComponents();
            layout.addComponent(new Label(getLabelText(getItem())));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected void onSave() {
        try {
            Item item = getItem();
            if (presenter.save(item)) {
                layout.removeAllComponents();
                layout.addComponent(new Label(getLabelText(getItem())));
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public int compareTo(AbstractEditable o) {
        try {
            log.debug("comparing {} and {}", getItem().getName().toLowerCase(), o.getItem().getName().toLowerCase());
            return getItem().getName().toLowerCase().compareTo(o.getItem().getName().toLowerCase());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected abstract String getLabelText(Item item) throws RepositoryException;

    protected abstract ComponentAndEditor getComponentAndEditor(Item item) throws RepositoryException;

    protected Item getItem() throws RepositoryException {
        Node node = MgnlContext.getJCRSession(workspace).getNodeByIdentifier(nodeIdentifier);
        return propertyName == null ? node : node.getProperty(propertyName);
    }
}
