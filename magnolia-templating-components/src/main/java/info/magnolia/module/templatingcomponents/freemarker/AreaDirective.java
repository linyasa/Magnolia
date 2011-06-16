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
package info.magnolia.module.templatingcomponents.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.module.templatingcomponents.components.AreaComponent;
import info.magnolia.templating.template.AreaDefinition;

import java.io.IOException;
import java.util.Map;

/**
 * A freemarker directive for rendering an area.
 *
 * @version $Id$
 */
public class AreaDirective extends AbstractDirective<AreaComponent> {

    @Override
    protected void prepareUIComponent(AreaComponent uiComponent, Environment env, Map<String, TemplateModel> params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateModelException, IOException {

        initContentComponent(params, uiComponent);

        AreaDefinition area = (AreaDefinition) object(params, "area");
        String name = string(params, "name", null);
        String paragraphs = string(params, "paragraphs", null);
        String dialog = string(params, "dialog", null);
        String type = string(params, "type", AreaComponent.DEFAULT_TYPE);

        uiComponent.setArea(area);
        uiComponent.setName(name);
        uiComponent.setAvailableComponents(paragraphs);
        uiComponent.setDialog(dialog);
        uiComponent.setType(type);
    }
}
