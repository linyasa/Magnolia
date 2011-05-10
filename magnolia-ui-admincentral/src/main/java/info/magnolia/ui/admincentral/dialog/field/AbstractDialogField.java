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
package info.magnolia.ui.admincentral.dialog.field;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.ui.admincentral.dialog.support.DialogLocalizationUtil;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * UI component for a field in a dialog.
 *
 * @author tmattsson
 */
public abstract class AbstractDialogField extends CustomComponent implements DialogField {

    private String errorMessage;
    private Label errorLabel;
    private Field field;
    private Editor editor;
    private DialogDefinition dialogDefinition;
    private TabDefinition tabDefinition;
    private FieldDefinition fieldDefinition;
    private Messages messages;

    protected AbstractDialogField(DialogDefinition dialogDefinition, TabDefinition tabDefinition, FieldDefinition fieldDefinition) {

        this.dialogDefinition = dialogDefinition;
        this.tabDefinition = tabDefinition;
        this.fieldDefinition = fieldDefinition;
        this.messages = DialogLocalizationUtil.getMessages(dialogDefinition, tabDefinition, fieldDefinition);

        this.field = getField();

        String label = messages.getWithDefault(fieldDefinition.getLabel(), fieldDefinition.getLabel());
        String description = messages.getWithDefault(fieldDefinition.getDescription(), fieldDefinition.getDescription());

        Label labelLabel = new Label(label);
        errorLabel = new Label();
        errorLabel.setVisible(false);
        Label descriptionLabel = new Label(StringUtils.isNotBlank(description)? description : "(Description not specified)");

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(field);
        layout.addComponent(descriptionLabel);
        layout.addComponent(errorLabel);
        layout.setComponentAlignment(errorLabel, Alignment.BOTTOM_RIGHT);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.addComponent(labelLabel);
        horizontalLayout.setExpandRatio(labelLabel, 1);
        horizontalLayout.addComponent(layout);
        horizontalLayout.setExpandRatio(layout, 5);

        CssLayout box = new CssLayout() {

            @Override
            protected String getCss(Component c) {
                if (errorMessage != null) {
                    return "background-color:#ff8080;";
                }
                return super.getCss(c);
            }
        };
        box.setWidth("100%");
        box.addComponent(horizontalLayout);
        super.setCompositionRoot(box);

        Class<?> type = getTypeFromDialogControl(fieldDefinition);

        this.editor = new VaadinEditorAdapter(field, fieldDefinition, type, this);
    }

    protected abstract Field getField();

    public void setError(String message) {

        // TODO what about multiple errors on the same editor?

        this.errorMessage = message;
        if (message != null) {
            errorLabel.setVisible(true);
            errorLabel.setCaption(message);
            if (field instanceof AbstractComponent) {
                ((AbstractComponent)field).setComponentError(new UserError(message));
            }
        } else {
            errorLabel.setVisible(false);
            if (field instanceof AbstractComponent) {
                ((AbstractComponent)field).setComponentError(null); // ??
            }
        }
        requestRepaintAll();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Editor getEditor() {
        return this.editor;
    }

    private Class<?> getTypeFromDialogControl(FieldDefinition fieldDefinition) {

        // TODO this should look at fieldDefinition.type instead

        if ("edit".equals(fieldDefinition.getControlType())) {
            return String.class;
        }
        if ("date".equals(fieldDefinition.getControlType())) {
            return Calendar.class;
        }
        if ("richText".equals(fieldDefinition.getControlType())) {
            return String.class;
        }
        if ("password".equals(fieldDefinition.getControlType())) {
            return String.class;
        }
        if ("checkboxSwitch".equals(fieldDefinition.getControlType())) {
            return Boolean.class;
        }
        return String.class;
//        throw new IllegalArgumentException("Unsupported type " + dialogControl.getClass());
    }

    public Messages getMessages() {
        return messages;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public TabDefinition getTabDefinition() {
        return tabDefinition;
    }

    public DialogDefinition getDialogDefinition() {
        return dialogDefinition;
    }
}
