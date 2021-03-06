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
package info.magnolia.importexport;

import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Test;

/**
 * @version $Id$
 */
public class PropertiesImportExportTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }


    @Test
    public void testConvertsToStringByDefault() throws IOException, RepositoryException {
        final PropertiesImportExport pie = new PropertiesImportExport();

        assertEquals("foo", pie.convertNodeDataStringToObject("foo"));
        assertEquals("bar", pie.convertNodeDataStringToObject("string:bar"));
        assertEquals("2009-10-14T08:59:01.227-04:00", pie.convertNodeDataStringToObject("string:2009-10-14T08:59:01.227-04:00"));
    }

    @Test
    public void testConvertsToWrapperType() {
        final PropertiesImportExport pie = new PropertiesImportExport();

        assertEquals(Boolean.TRUE, pie.convertNodeDataStringToObject("boolean:true"));
        assertEquals(Boolean.FALSE, pie.convertNodeDataStringToObject("boolean:false"));
        assertEquals(Integer.valueOf(5), pie.convertNodeDataStringToObject("integer:5"));
        final Object dateConvertedObject = pie.convertNodeDataStringToObject("date:2009-10-14T08:59:01.227-04:00");
        assertTrue(dateConvertedObject instanceof Calendar);
        assertEquals(1255525141227L, ((Calendar) dateConvertedObject).getTimeInMillis());
        // It's null if it doesn't match the exact format string
        final Object dateOnlyObject = pie.convertNodeDataStringToObject("date:2009-12-12");
        assertNull(dateOnlyObject);
    }

    @Test
    public void testCanUseIntShortcutForConvertingIntegers() {
        final PropertiesImportExport pie = new PropertiesImportExport();

        assertEquals(Integer.valueOf(37), pie.convertNodeDataStringToObject("int:37"));
    }

    // This test uses the import then immediately exports and makes sure that the number of properties
    // at least matches.

    @Test
    public void testPropertiesExport() throws Exception {
        final HierarchyManager hm = initHM();
        Properties baseProperties = new Properties();
        baseProperties.load(this.getClass().getResourceAsStream("propertiesimportexport-testrepo.properties"));

        Properties exportedProperties = PropertiesImportExport.contentToProperties(hm.getRoot(), ContentUtil.ALL_NODES_EXCEPT_JCR_CONTENT_FILTER);
        assertEquals(baseProperties.keySet().size(), exportedProperties.keySet().size());
        assertEquals(baseProperties, exportedProperties);

        Properties legacyExportedProperties = PropertiesImportExport.toProperties(hm);
        assertEquals("Legacy mode export doesn't contain @uuid, metadata, or @type nodes",
                10, legacyExportedProperties.keySet().size()
        );
    }

    @Test
    public void testImportMetadata() throws Exception {
        // TODO - some of these tests, and it's very visible with this one, depend on MockUtil.createHM
        // ... so in essence, we're testing that, and not PIE

        final HierarchyManager hm = initHM();
        Content uuidLinkNode = hm.getContentByUUID("2");
        assertNotNull("Content retrievable by its UUID", uuidLinkNode);
        MetaData nodeMetaData = uuidLinkNode.getMetaData();
        assertNotNull("Metadata of node should not be null when it is set explicitly in the properties", nodeMetaData);
        assertEquals("Template node is populated properly", "someParagraphName", NodeTypes.Renderable.getTemplate(uuidLinkNode.getJCRNode()));
        assertTrue("activation matches status set in the properties", NodeTypes.Activatable.isActivated(uuidLinkNode.getJCRNode()));

    }

    protected HierarchyManager initHM() throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(this.getClass().getResourceAsStream("propertiesimportexport-testrepo.properties"));
    }

}
