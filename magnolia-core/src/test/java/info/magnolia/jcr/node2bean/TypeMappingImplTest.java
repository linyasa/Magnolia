/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.node2bean;

import static org.junit.Assert.*;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Version $Id$.
 */
public class TypeMappingImplTest {

    @Before
    public void setUp() {
        ComponentsTestUtil.setInstance(SimpleBeanTransformer.class, new SimpleBeanTransformer());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetPropertyTypeDescriptorWithSimpleType() {
        // GIVEN
        TypeMapping mapping = new TypeMappingImpl();

        // WHEN
        PropertyTypeDescriptor ptd = mapping.getPropertyTypeDescriptor(SimpleBean.class, "integer");

        // THEN
        assertNotNull(ptd);
        assertEquals("integer", ptd.getName());
        assertEquals(int.class, ptd.getType().getType());

        // WHEN
        ptd = mapping.getPropertyTypeDescriptor(SimpleBean.class, "string");

        // THEN
        assertNotNull(ptd);
        assertEquals("string", ptd.getName());
        assertEquals(String.class, ptd.getType().getType());
    }

    @Test
    public void testGetPropertyTypeDescriptorWithCollection() {
        // GIVEN
        TypeMapping mapping = new TypeMappingImpl();

        // WHEN
        PropertyTypeDescriptor ptd = mapping.getPropertyTypeDescriptor(BeanWithCollectionOfSimpleBean.class, "beans");

        // THEN
        assertNotNull(ptd);

        assertTrue(ptd.isCollection());

        assertEquals("beans", ptd.getName());
        assertEquals(SimpleBean.class, ptd.getCollectionEntryType().getType());
    }

    @Test
    public void testGetPropertyTypeDescriptorWithMap() {
        // GIVEN
        TypeMapping mapping = new TypeMappingImpl();

        // WHEN
        PropertyTypeDescriptor ptd = mapping.getPropertyTypeDescriptor(BeanWithMapWithGenerics.class, "beans");

        // THEN
        assertNotNull(ptd);

        assertTrue(ptd.isMap());

        assertEquals("beans", ptd.getName());
        assertEquals(String.class, ptd.getCollectionKeyType().getType());
        assertEquals(SimpleBean.class, ptd.getCollectionEntryType().getType());
    }

    @Test
    public void testBeanPropertyTypeDescriptorHasTransformer() {
        // GIVEN
        TypeMapping mapping = new TypeMappingImpl();

        // WHEN
        PropertyTypeDescriptor ptd = mapping.getPropertyTypeDescriptor(BeanWithCollectionOfSimpleBean.class, "beans");

        // THEN
        assertNotNull(ptd);
        assertNotNull(ptd.getCollectionEntryType().getTransformer());

        assertEquals(SimpleBeanTransformer.class, ptd.getCollectionEntryType().getTransformer().getClass());
    }

    @Test
    public void testGetPropertyTypeDescriptorWithArray() {
        // GIVEN
        TypeMapping mapping = new TypeMappingImpl();

        // WHEN
        PropertyTypeDescriptor ptd = mapping.getPropertyTypeDescriptor(BeanWithArrayOfSimpleBean.class, "beans");

        // THEN
        assertNotNull(ptd);
        assertEquals(SimpleBean.class, ptd.getCollectionEntryType().getType());
    }

}