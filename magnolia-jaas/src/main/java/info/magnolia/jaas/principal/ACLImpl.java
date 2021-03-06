/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.ACL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * This class represents access control list as a principal.
 * @author Sameer Charles $Id$
 * @deprecated since 4.5 use {@link info.magnolia.cms.security.ACLImpl} instead
 */
@Deprecated
public class ACLImpl implements ACL {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NAME = "acl";

    private String name;

    private final List list;

    public ACLImpl() {
        this.list = new ArrayList();
    }

    /**
     * Get name given to this principal.
     * @return name
     */
    @Override
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return NAME;
        }
        return this.name;
    }

    /**
     * Set this principal name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds permission in to the list of permissions for this principal.
     * @param permission
     */
    public void addPermission(Object permission) {
        this.list.add(permission);
    }

    /**
     * Initialize access control list with provided permissions it will overwrite any existing permissions set before.
     * @param list
     */
    public void setList(List list) {
        this.list.clear();
        this.list.addAll(list);
    }

    /**
     * Returns list of permissions for this principal. Returned list is not a copy and should be treated as read only!
     */
    @Override
    public List getList() {
        return this.list;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", this.name)
                .append("list", this.list)
                .toString();
    }

}
