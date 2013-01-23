/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.commands.impl;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;

import org.apache.commons.lang.StringUtils;


/**
 * A command setting a message using the AlertUtil.
 * 
 * @author Philipp Bracher
 * @version $Revision:6423 $ ($Author:scharles $)
 */
public class MessageCommand extends MgnlCommand {

    /**
     * The message.
     */
    private String message = "";

    private String i18nBasename = MessagesManager.DEFAULT_BASENAME;

    /**
     * @see info.magnolia.commands.MgnlCommand#execute(info.magnolia.context.Context)
     */
    @Override
    public boolean execute(Context context) throws Exception {
        if (StringUtils.isNotEmpty(message)) {
            Messages msgs = MessagesUtil.chainWithDefault(this.getI18nBasename());
            AlertUtil.setMessage(msgs.getWithDefault(message, message));
        }
        return true;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return Returns the i18nBasename.
     */
    public String getI18nBasename() {
        return this.i18nBasename;
    }

    /**
     * @param basename The i18nBasename to set.
     */
    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    @Override
    public void release() {
        super.release();
        i18nBasename = MessagesManager.DEFAULT_BASENAME;
        message = null;
    }
}