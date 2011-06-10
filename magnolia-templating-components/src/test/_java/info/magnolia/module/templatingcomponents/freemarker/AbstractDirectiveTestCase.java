/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.freemarker.AbstractFreemarkerTestCase;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Provides setup/teardown for testing Directives. Has to be Junit3-style for now as AbstractFreemarkerTestCase is not
 * yet migrated to JUnit4.
 *
 * @version $Id$
 */
public abstract class AbstractDirectiveTestCase extends AbstractFreemarkerTestCase {

    private WebContext ctx;
    protected MockHierarchyManager hm;
    private HttpServletRequest req;
    private HttpServletResponse res;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        hm = MockUtil.createHierarchyManager(
                StringUtils.join(Arrays.asList(
                        "/foo/bar@type=mgnl:content",
                        "/foo/bar/MetaData@type=mgnl:metadata",
                        "/foo/bar/MetaData/mgnl\\:template=testPageTemplate",
                        "/foo/bar/paragraphs@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/0@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/0@uuid=100",
                        "/foo/bar/paragraphs/0/text=hello 0",
                        "/foo/bar/paragraphs/0/MetaData@type=mgnl:metadata",
                        "/foo/bar/paragraphs/0/MetaData/mgnl\\:template=testParagraph0",
                        "/foo/bar/paragraphs/1@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/1@uuid=101",
                        "/foo/bar/paragraphs/1/text=hello 1",
                        "/foo/bar/paragraphs/1/MetaData@type=mgnl:metadata",
                        "/foo/bar/paragraphs/1/MetaData/mgnl\\:template=testParagraph1",
                        "/foo/bar/paragraphs/2@type=mgnl:contentNode",
                        "/foo/bar/paragraphs/2@uuid=102",
                        "/foo/bar/paragraphs/2/text=hello 2",
                        "/foo/bar/paragraphs/2/MetaData@type=mgnl:metadata",
                        "/foo/bar/paragraphs/2/MetaData/mgnl\\:template=testParagraph2",
                        ""
                ), "\n"));

        final AggregationState aggState = new AggregationState();
        // depending on tests, we'll set the main content and current content to the same or a different node
        aggState.setMainContent(hm.getContent("/foo/bar"));
        aggState.setCurrentContent(hm.getContent("/foo/bar/paragraphs/1"));

        // let's make sure we render stuff on an author instance
        aggState.setPreviewMode(false);
        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());

        Paragraph testParagraph0 = new Paragraph();
        testParagraph0.setName("testParagraph0");
        testParagraph0.setTitle("Test Paragraph 0");
        Paragraph testParagraph1 = new Paragraph();
        testParagraph1.setName("testParagraph1");
        testParagraph1.setTitle("Test Paragraph 1");
        Paragraph testParagraph2 = new Paragraph();
        testParagraph2.setName("testParagraph2");
        testParagraph2.setTitle("Test Paragraph 2");

        ParagraphManager paragraphManager = mock(ParagraphManager.class);
        when(paragraphManager.getParagraphDefinition("testParagraph0")).thenReturn(testParagraph0);
        when(paragraphManager.getParagraphDefinition("testParagraph1")).thenReturn(testParagraph1);
        when(paragraphManager.getParagraphDefinition("testParagraph2")).thenReturn(testParagraph2);
        ComponentsTestUtil.setInstance(ParagraphManager.class, paragraphManager);

        req = mock(HttpServletRequest.class);
        req.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);

        res = mock(HttpServletResponse.class);
        when(res.getWriter()).thenReturn(null);

        ctx = mock(WebContext.class);
        when(ctx.getAggregationState()).thenReturn(aggState);
        when(ctx.getLocale()).thenReturn(Locale.US);
        when(ctx.getHierarchyManager(hm.getName())).thenReturn(hm);
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.getRequest()).thenReturn(req);

        setupExpectations(ctx, req);

        MgnlContext.setInstance(ctx);
    }

    /**
     * Hook method - overwrite if you want to set up special expectations.
     */
    protected void setupExpectations(WebContext ctx, HttpServletRequest req) {
    }

    @Override
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
        super.tearDown();
    }

    public String renderForTest(final String templateSource) throws Exception {
        tplLoader.putTemplate("test.ftl", templateSource);

        final Map<String, Object> map = contextWithDirectives();
        map.put("content", hm.getContent("/foo/bar/"));

        final StringWriter out = new StringWriter();
        fmHelper.render("test.ftl", map, out);

        return out.toString();
    }

    protected Map<String, Object> contextWithDirectives() {
        // this is the only thing we expect rendering engines to do: added the directives to the rendering context so
        // they can be refered to via "@cms"
        return createSingleValueMap("cms", new Directives());
    }

}