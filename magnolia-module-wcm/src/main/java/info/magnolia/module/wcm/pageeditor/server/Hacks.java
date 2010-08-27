/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.wcm.pageeditor.server;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.EditParagraphWindow;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * Collection of hacks just done for the PoC.
 * FIXME get rid of it
 */
public class Hacks {

    /**
     * Copied from DialogSandboxPage.
     */
    public static EditParagraphWindow getDialogWindow(final String uuid) throws RepositoryException {
        Content paragraph = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContentByUUID(uuid);
        String paragraphTemplate = paragraph.getMetaData().getTemplate();
        Paragraph paragraphDef = ParagraphManager.getInstance().getParagraphDefinition(paragraphTemplate);
        String pageHandle = getPageHandle(paragraph);
        String nodeCollection = StringUtils.substringBeforeLast(StringUtils.substringAfter(paragraph.getHandle(), pageHandle + "/"), "/" + paragraph.getName());
        if ("/".equals(nodeCollection)) {
            // no collection at all
            nodeCollection = null;
        }

        EditParagraphWindow dialog = new EditParagraphWindow(paragraphDef.getDialog(),
            ContentRepository.WEBSITE, pageHandle, nodeCollection, paragraph.getName());
        dialog.setScrollable(true);
        return dialog;
    }

    private static String getPageHandle(Content content) throws RepositoryException {
        while (content != null) {
            if (content.getItemType().equals(ItemType.CONTENT)) {
                return content.getHandle();
            }
            content = content.getParent();
        }
        return null;
    }
}