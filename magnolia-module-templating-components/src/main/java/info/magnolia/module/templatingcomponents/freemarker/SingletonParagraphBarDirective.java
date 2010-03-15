/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.templatingcomponents.AuthoringUiComponent;
import info.magnolia.module.templatingcomponents.components.SingletonParagraphBar;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SingletonParagraphBarDirective extends AbstractDirective {
    @Override
    protected AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState, Environment env, Map<String, TemplateModel> params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateModelException, IOException {
        checkBody(body, true);
        final String enableButtonLabel = string(params, "enableLabel", null);
        /*
        final Content target = content(params, "target", null);
        final String containerNodeName = string(params, "container", null);
        if (target == null && containerNodeName == null) {
            // TODO check
            throw new TemplateModelException("At least target or container must be specified.");
        }
        */
        final String contentName = string(params, "content", null);
        final List<String> allowedParagraphs = mandatoryStringList(params, "paragraphs");

        return SingletonParagraphBar.make(serverCfg, aggState, contentName, allowedParagraphs, enableButtonLabel);

    }

}