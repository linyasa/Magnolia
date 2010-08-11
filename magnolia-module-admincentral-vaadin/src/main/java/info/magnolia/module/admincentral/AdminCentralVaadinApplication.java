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
package info.magnolia.module.admincentral;

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import com.vaadin.Application;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Demo Application - simple AdressBook.
 *
 * Added Tree Table Add on to WEB-Inf/lib...
 *
 * TODO show website tree instead of dummy address book data. Started to work on that but it's clearly a mess as you can see.
 *
 * @author dan
 * @author fgrilli
 */
public class AdminCentralVaadinApplication extends Application {

    private static String[] fields = { "First Name", "Last Name", "Company",
            "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
            "Home Email", "Street", "Zip", "City", "State", "Country" };
    private static final long serialVersionUID = 5773744599513735815L;
    private static String[] visibleCols = new String[] { "Last Name",
            "First Name", "Company" };

    private static final String[] websiteFields = { "page", "title", "status", "template", "modificationDate" };

    private static final String[] websiteVisibleCols = { "Page", "Title", "Status", "Template", "Mod. Date" };

    public static final String WINDOW_TITLE = "Magnolia AdminCentral";

    private Accordion accordion = createAccordion();

    private IndexedContainer addressBookData = createDummyData();
    //private IndexedContainer addressBookData = createWebsiteTreeData("/", ItemType.CONTENTNODE.getSystemName());
    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();
    private Form contactEditor = new Form();
    private TreeTable contactList = createContactList();
    private Button contactRemovalButton;

    /**
     * Used for deciding, whether a table column gets editable or not (only
     * the selected one will be...).
     *
     * Hint: not yet properly working
     */
    private Object selectedContactId = null;
    private Accordion createAccordion() {
        Label l1 = new Label("There are no previous Website actions.");
        Label l2 = new Label("There are no saved Configs.");

        Accordion a = new Accordion();
        a.addTab(l1, "Website", null);
        a.addTab(l2, "Config", null);
        a.addListener(new Accordion.SelectedTabChangeListener() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void selectedTabChange(SelectedTabChangeEvent event) {
                TabSheet tabsheet = event.getTabSheet();
                Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
                if (tab != null) {
                    getMainWindow().showNotification(
                            "Selected tab: " + tab.getCaption());
                }
            }
        });
        return a;
    }
    /**
     * @return TreeTable
     */
    private TreeTable createContactList() {
        TreeTable table = new TreeTable();
        table.setSizeFull();
        table.setTableFieldFactory(new TableFieldFactory() {
            private static final long serialVersionUID = 3299668916157924056L;

            public Field createField(Container container, Object itemId,
                    Object propertyId, Component uiContext) {
                /**
                 * Cell is editable if it is the selected one...
                 */
                return (selectedContactId != null && selectedContactId
                        .equals(itemId)) ? new TextField() : null;
            }
        });
        table.setEditable(true);
        return table;
    }

    /**
     * @return a container with 1000 First/Lastname combinations (randomly generated)
     */
    private IndexedContainer createDummyData() {

        String[] fnames = { "Peter", "Alice", "Joshua", "Mike", "Olivia",
                "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
                "Lisa", "Marge" };
        String[] lnames = { "Smith", "Gordon", "Simpson", "Brown", "Clavel",
                "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
                "Barks", "Ross", "Schneider", "Tate" };

        IndexedContainer ic = new IndexedContainer();

        for (String p : fields) {
            ic.addContainerProperty(p, String.class, "");
        }

        for (int i = 0; i < 1000; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, "First Name").setValue(
                    fnames[(int) (fnames.length * Math.random())]);
            ic.getContainerProperty(id, "Last Name").setValue(
                    lnames[(int) (lnames.length * Math.random())]);
        }

        return ic;
    }

    private IndexedContainer createWebsiteTreeData(String path, String itemType){
        Content parent = null;
        try {
            parent = MgnlContext.getHierarchyManager("website").getContent(path);
        } catch (RepositoryException e) {
            //getMainWindow().showNotification("Something bad happened", e.getMessage(), Notification.TYPE_WARNING_MESSAGE);
            throw new RuntimeException(e);
        }
        IndexedContainer ic = new IndexedContainer();

        for (String p : websiteFields) {
            ic.addContainerProperty(p, String.class, "");
        }
        Collection<Content> nodes = parent.getChildren(itemType);
        /*Comparator comp = this.getSortComparator();
        if(comp != null){
            Collection sortedNodes = new TreeSet(comp);
            sortedNodes.addAll(nodes);
            nodes = sortedNodes;
        }*/
       Iterator<Content> it = nodes.iterator();
        while(it.hasNext()){
            Content content = it.next();
            Object id = ic.addItem();
            ic.getContainerProperty(id, "page").setValue(content.getName());
            ic.getContainerProperty(id, "title").setValue(content.getTitle());
            ic.getContainerProperty(id, "status").setValue(content.getMetaData().getActivationStatus());
            ic.getContainerProperty(id, "template").setValue(content.getTemplate());
            ic.getContainerProperty(id, "modificationDate").setValue(DateFormat.getInstance().format(content.getMetaData().getModificationDate().getTime()));

        }
        return ic;
    }

    @Override
    public void init() {
        /**
         * dan: simply remove next in order to get the default theme
         * ("reindeer")
         */
        setTheme("runo");
        initLayout();
        initContactAddRemoveButtons();
        initContactDetailsView();
        initContactList();
        initFilteringControls();
    }

    private void initContactAddRemoveButtons() {
        // New item button
        bottomLeftCorner.addComponent(new Button("+",
                new Button.ClickListener() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {
                        Object id = contactList.addItem();
                        contactList.setValue(id);
                    }
                }));

        // Remove item button
        contactRemovalButton = new Button("-", new Button.ClickListener() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
                contactList.removeItem(contactList.getValue());
                contactList.select(null);
            }
        });
        contactRemovalButton.setVisible(false);
        bottomLeftCorner.addComponent(contactRemovalButton);
    }

    /**
     * Demonstrate Dialog-Handling.
     */
    private void initContactDetailsView() {
        final Window dialog = new Window("Address Details");
        dialog.addComponent(contactEditor);
        contactList.addListener(new ItemClickEvent.ItemClickListener() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent e) {
                if (e.getButton() == MouseEvents.ClickEvent.BUTTON_RIGHT) {
                    getMainWindow().addWindow(dialog);
                }
            }
        });
    }

    private void initContactList() {
        contactList.setContainerDataSource(addressBookData);
        contactList.setVisibleColumns(visibleCols);
        contactList.setSelectable(true);
        //contactList.setEditable(true);
        // contactList.setImmediate(true);

        /**
         * dan: For the showcase we need a Tree-Structure - so loop over the contacts and create an simple tree structure
         * (depth 3)
         */
        Object[] ids = contactList.getItemIds().toArray();
        Object current = null;
        Object next = null;
        Object nextNext = null;
        for (int i = 0; i < ids.length - 2; i = i + 3) {
            current = ids[i];
            next = ids[i + 1];
            nextNext = ids[i + 2];
            contactList.setParent(next, current);
            contactList.setParent(nextNext, next);
        }

        contactList.addListener(new Property.ValueChangeListener() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {
                Object id = contactList.getValue();
                contactEditor.setItemDataSource(id == null ? null : contactList
                        .getItem(id));
                contactRemovalButton.setVisible(id != null);
                selectedContactId = id;
            }
        });
    }

    private void initFilteringControls() {
        for (final String pn : visibleCols) {
            final TextField sf = new TextField();
            bottomLeftCorner.addComponent(sf);
            sf.setWidth("100%");
            sf.setInputPrompt(pn);
            sf.setImmediate(true);
            bottomLeftCorner.setExpandRatio(sf, 1);
            sf.addListener(new Property.ValueChangeListener() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                public void valueChange(ValueChangeEvent event) {
                    addressBookData.removeContainerFilters(pn);
                    if (sf.toString().length() > 0 && !pn.equals(sf.toString())) {
                        addressBookData.addContainerFilter(pn, sf.toString(),
                                true, false);
                    }
                    getMainWindow().showNotification(
                            "" + addressBookData.size() + " matches found");
                }
            });
        }
    }

    /**
     * package-private modifier is used for better testing
     * possibilities...
     */
    void initLayout() {
        SplitPanel splitPanel = new SplitPanel(
                SplitPanel.ORIENTATION_HORIZONTAL);
        setMainWindow(new Window(WINDOW_TITLE, splitPanel));
        splitPanel.setSplitPosition(20);
        VerticalLayout vertical = new VerticalLayout();
        vertical.setSizeFull();

        accordion.setSizeFull();

        contactEditor.setSizeFull();
        contactEditor.getLayout().setMargin(true);
        // contactEditor.setImmediate(true);

        bottomLeftCorner.setWidth("100%");

        splitPanel.addComponent(accordion);
        splitPanel.addComponent(vertical);
        vertical.addComponent(contactList);
        vertical.addComponent(bottomLeftCorner);

        vertical.setExpandRatio(contactList, 10);
        vertical.setExpandRatio(bottomLeftCorner, 1);
    }
}
