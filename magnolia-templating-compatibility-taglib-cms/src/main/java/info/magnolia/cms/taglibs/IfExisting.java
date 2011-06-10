/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is exactly the opposite of the ifNotExisting Tag.
 * @jsp.tag name="ifExisting" body-content="JSP"
 *
 * @author Michael Aemisegger
 * @version $Revision $ ($Author $)
 */
public class IfExisting extends BaseConditionalContentTag {
    private static final Logger log = LoggerFactory.getLogger(IfExisting.class);

    private String nodeDataName = StringUtils.EMPTY;

    /**
     * @param name , antom name to evaluate
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    @Override
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    @Override
    protected boolean condition() {
        Content node = this.getFirstMatchingNode();

        // if the tag is checking for a nodes existance
        if (StringUtils.isEmpty(this.nodeDataName)) {
            return node != null;
        }
        else {
            NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(node, this.nodeDataName);
            return (nodeData != null) && nodeData.isExist();
        }
    }

}