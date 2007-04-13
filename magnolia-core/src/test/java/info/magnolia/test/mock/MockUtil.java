/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.api.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * Util to create mock objects. Use createHierarchyManager() to build mock content based on a property file. property
 * values can have prefixes like boolean: int: for creating typed nodedatas.
 * @author philipp
 * @version $Id$
 */
public class MockUtil {

    /**
     * Ordered properties. Uses to keep the order in the mocked content
     * @author philipp
     * @version $Id$
     */
    public static final class OrderedProperties extends Properties {

        private final LinkedHashMap map = new LinkedHashMap();

        public Object put(Object key, Object value) {
            return map.put(key, value);
        }

        public Object get(Object key) {
            return map.get(key);
        }

        public String getProperty(String key) {
            return (String) get(key);
        }

        public Set entrySet() {
            return this.map.entrySet();
        }

        public Set keySet() {
            return this.map.keySet();
        }
    }

    /**
     * Mocks the current and system context
     */
    public static MockContext initMockContext() {
        MockContext ctx = new MockContext();
        MgnlContext.setInstance(ctx);
        // and system context as well
        FactoryUtil.setInstance(SystemContext.class, ctx);
        return ctx;
    }

    public static HierarchyManager createHierarchyManager(InputStream propertiesStream) throws IOException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        Content root = hm.getRoot();
        createContent(root, propertiesStream);
        return hm;
    }

    public static HierarchyManager createHierarchyManager(String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createHierarchyManager(in);
    }

    public static void createContent(Content root, InputStream propertiesStream) throws IOException, RepositoryException {
        Properties properties = new OrderedProperties();

        properties.load(propertiesStream);

        for (Object o : properties.keySet()) {
            String key = (String) o;
            String name = StringUtils.substringAfterLast(key, ".");
            String valueStr = properties.getProperty(key);
            String path = StringUtils.substringBeforeLast(key, ".");
            path = StringUtils.replace(path, ".", "/");

            MockContent c = (MockContent) ContentUtil.createPath(root, path, ItemType.CONTENTNODE);
            populateContent(c, name, valueStr);
        }
    }

    public static void populateContent(MockContent c, String name, String valueStr) {
        if (name.equals("@type")) {
            c.setNodeTypeName(valueStr);
        }
        else if (name.equals("@uuid")) {
            c.setUUID(valueStr);
        }
        else {
            Object valueObj = convertNodeDataStringToObject(valueStr);
            c.addNodeData(new MockNodeData(name, valueObj));
        }
    }

    public static Object convertNodeDataStringToObject(String valueStr) {
        Object valueObj = valueStr;

        if (valueStr.contains(":")) {
            String type = StringUtils.substringBefore(valueStr, ":");
            if (type.equals("int")) {
                type = "integer";
            }
            String value = StringUtils.substringAfter(valueStr, ":");
            try {
                valueObj = ConvertUtils.convert(value, Class.forName("java.lang." + StringUtils.capitalize(type)));
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("can't convert value [" + valueStr + "]", e);
            }
        }
        return valueObj;
    }

    public static Content createContent(final String name, Object[][] data, Content[] children) throws RepositoryException {
        OrderedMap nodeDatas = MockUtil.createNodeDatas(data);
        OrderedMap childrenMap = new ListOrderedMap();

        for (Content child : children) {
            childrenMap.put(child.getName(), child);
        }

        return new MockContent(name, nodeDatas, childrenMap);
    }

    public static Content createNode(String name, Object[][] data) throws RepositoryException {
        return createContent(name, data, new Content[]{});
    }

    public static OrderedMap createNodeDatas(Object[][] data) {
        OrderedMap nodeDatas = new ListOrderedMap();
        for (Object[] aData : data) {
            String name = (String) aData[0];
            Object value = aData[1];
            nodeDatas.put(name, new MockNodeData(name, value));
        }
        return nodeDatas;
    }
}
