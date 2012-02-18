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
package info.magnolia.templating.editor.client.widget.controlbar;

import java.util.Map;

import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.widget.dnd.DragAndDrop;
import static info.magnolia.templating.editor.client.jsni.JavascriptUtils.*;



import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;




/**
 * Edit bar.
 */
public class ComponentBar extends AbstractBar  {

    private String workspace;
    public String path;
    private String dialog;
    public String id;
    private boolean isInherited;

    public ComponentBar(MgnlElement mgnlElement) throws IllegalArgumentException {

        super(mgnlElement);

        checkMandatories(mgnlElement.getComment().getAttributes());
        addStyleName("component");

        if(DragDropEventBase.isSupported()) {
            getStyle().setCursor(Cursor.MOVE);
            createButtons(false);
            createDragAndDropHandlers();
        } else {
            createButtons(true);
            createMouseEventsHandlers();
        }

        setVisible(false);
        attach();

        PageEditor.model.addEditBar(getMgnlElement(), this);
    }

    private void checkMandatories(Map<String, String> attributes) {
        String content = attributes.get("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.id = path.substring(path.lastIndexOf("/") + 1);

        setId("__"+id);

        this.dialog = attributes.get("dialog");

        this.isInherited = Boolean.parseBoolean(attributes.get("inherited"));

        if (this.isInherited) {
            throw new IllegalArgumentException();
        }

    }

    private void createDragAndDropHandlers() {
        DragAndDrop.dragAndDrop(this);
    }

    private void createMouseEventsHandlers() {

        addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                String parentPath = path.substring(0, path.lastIndexOf("/"));
                PageEditor.moveComponentEnd((ComponentBar)event.getSource(), parentPath);
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                PageEditor.moveComponentOver((ComponentBar)event.getSource());

            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                PageEditor.moveComponentOut((ComponentBar)event.getSource());
            }
        }, MouseOutEvent.getType());
    }

    private void createButtons(boolean createMoveButton) {

        if (dialog != null && !this.isInherited) {
            final Button edit = new Button(getI18nMessage("buttons.edit.js"));
            edit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PageEditor.openDialog(dialog, workspace, path, null, null);

                }
            });
            addPrimaryButton(edit, Float.RIGHT);
        }

        if(createMoveButton && !this.isInherited) {
            final Button move = new Button(getI18nMessage("buttons.move.js"));
            move.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    toggleButtons(false);
                    PageEditor.moveComponentStart(id);
                }
            });
            addPrimaryButton(move, Float.RIGHT);
        }

        if (!this.isInherited) {
            final Button removeButton = new Button(getI18nMessage("buttons.delete.js"));
            removeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PageEditor.deleteComponent(path);
                }
            });
            addSecondaryButton(removeButton, Float.RIGHT);
        }
    }
}