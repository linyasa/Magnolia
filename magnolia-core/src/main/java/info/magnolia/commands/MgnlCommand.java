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
package info.magnolia.commands;

import info.magnolia.commands.chain.Command;
import info.magnolia.commands.chain.Context;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.NestableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * To make the coding of commands as easy as possible the default values set in
 * the config are set and the values of the context are set as properties too if
 * the naming matches.
 */
public abstract class MgnlCommand implements Command {

    public static Logger log = LoggerFactory.getLogger(MgnlCommand.class);

    private boolean isEnabled = true;

    /**
     * Make sure that the context is castable to a magnolia context. DO NOT
     * override this method (with the info.magnolia.commands.chain.Context
     * parameter type) in the descendant classes - unless you know for 100% what
     * you are going to do.
     *
     * @return true on success, false otherwise
     */
    @Override
    public boolean execute(Context ctx) throws Exception {
        if (!(ctx instanceof info.magnolia.context.Context)) {
            throw new IllegalArgumentException("context must be of type " + info.magnolia.context.Context.class);
        }

        MgnlCommand cmd = this;

        boolean success = executeSynchronized(ctx, cmd);
        // convert the confusing true false behavior to fit commons chain
        return !success;
    }

    private boolean executeSynchronized(Context ctx, MgnlCommand cmd) throws Exception {
        boolean success = false; // break execution

        synchronized (cmd) {
            BeanUtils.populate(cmd, ctx);
            try {
                success = cmd.execute((info.magnolia.context.Context) ctx);
            } catch (Exception e) {
                throw new NestableException("Exception during executing command", e);
            } finally {
                cmd.release();
            }
        }
        return success;
    }

    /**
     * This is the actual method to be overridden in descendant classes.
     */
    public abstract boolean execute(info.magnolia.context.Context context) throws Exception;

    /**
     * If a clone is passivated we call this method. Please clean up private
     * properties.
     */
    public void release() {
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }


    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

}
