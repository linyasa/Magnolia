/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.jsp;

import com.meterware.httpunit.WebResponse;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.Context;
import info.magnolia.context.WebContext;
import info.magnolia.templatinguicomponents.components.SingletonParagraphBar;
import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditParagraphBarTagTest extends AbstractJspTest {
    @Override
    protected void setupExpectations(WebContext ctx, AccessManager accessManager) {
        expect(ctx.getAttribute(SingletonParagraphBar.class.getName(), Context.LOCAL_SCOPE)).andReturn(null).once();
    }

    @Override
    void check(WebResponse response) throws Exception {
        final String result = response.getText();

        // TODO - how to setup the rendering context attribute (which Jsp*Renderer do) such as content etc
        assertFalse(result.contains("<ui:"));

        // System.out.println(result);

        // TODO assertEquals("not testing yet", "...", result);
    }
}
