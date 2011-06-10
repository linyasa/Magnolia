/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.templating;

import java.lang.reflect.InvocationTargetException;

import info.magnolia.cms.core.Content;

/**
 * Abstract rendering definition used for templates and paragraphs.
 *
 * @version $Id$
 * @deprecated since 5.0, replaced by {@link info.magnolia.templating.template.configured.RenderableDefinition}
 */
public interface RenderableDefinition extends info.magnolia.templating.template.RenderableDefinition{

    /**
     * @deprecated since 5.0 - use {@link #getRenderType()} instead
     */
    public String getType();

    /**
     * @deprecated since 5.0 - use {@link #getTemplateScript()} instead
     */
    public String getTemplatePath();


    /**
     * The modules execute() method can return a string which is passed to this method to determine the template to use.
     * @deprecated  since 5.0 - without replacement
     */
    public String determineTemplatePath(String actionResult, RenderingModel<?> model);


    /**
     * @deprecated since 5.0 - use {@link info.magnolia.templating.template.RenderableDefinition#newModel(javax.jcr.Node, info.magnolia.templating.template.RenderableDefinition, info.magnolia.templating.model.RenderingModel)} instead
     */
    public RenderingModel<?> newModel(Content wrappedContent, RenderableDefinition definition, RenderingModel<?> parentModel)  throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;
}