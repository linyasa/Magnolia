/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.freemarker.models;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import info.magnolia.context.Context;

import java.util.Map;

/**
 * Exposes Context instances as different MapModels.
 * <ul>
 *  <li>SimpleMapModel would prevent us from using Context's specific methods</li>
 *  <li>SimpleHash (which seems to be the default in 2.3.14) also prevents using specific methods.</li>
 * </ul>
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class ContextModelFactory implements MagnoliaModelFactory {
    static final ContextModelFactory INSTANCE = new ContextModelFactory();

    @Override
    public Class factoryFor() {
        return Context.class;
    }

    @Override
    public AdapterTemplateModel create(Object object, ObjectWrapper wrapper) {
        return new MapModel((Map) object, (BeansWrapper) wrapper);
    }
}
