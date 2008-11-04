/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.templating.renderers;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspTemplateRendererTest extends TestCase {

    public void testExposesNodesAsMaps() {
        final WebContext magnoliaCtx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);
        // the page node is exposed twice, once as "actpage", once as "content"
        final Content page = createStrictMock(Content.class);
        expect(page.getHandle()).andReturn("/myPage").times(2);

        final AggregationState aggState = new AggregationState();
        aggState.setMainContent(page);
        expect(magnoliaCtx.getAggregationState()).andReturn(aggState);

        replay(magnoliaCtx, page);
        final Map templateCtx = new HashMap();
        new JspTemplateRenderer().setupContext(templateCtx, page, null, null, null);

        // other tests should verify the other objects !
        assertEquals("Unexpected amount of objects in context", 7, templateCtx.size());
        assertTrue(templateCtx.get("actpage") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("actpage")));

        assertTrue(templateCtx.get("content") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("content")));

        verify(magnoliaCtx, page);
    }

    private Content unwrap(Content c) {
        if (c instanceof ContentWrapper) {
            return unwrap(((ContentWrapper) c).getWrappedContent());
        }
        return c;
    }
}
