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
package info.magnolia.jcr.node2bean.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.objectfactory.ComponentProvider;

/**
 * A transformer which "hides" a collection node. Extend or pass the type and node name in the constructor.
 */
public class CollectionPropertyHidingTransformer extends Node2BeanTransformerImpl {

    private static final Logger log = LoggerFactory.getLogger(CollectionPropertyHidingTransformer.class);

    private Class<?> beanClass;

    private String collectionName;

    private TypeDescriptor type;

    private PropertyTypeDescriptor propertyDescriptor;

    private Method writeMethod;

    private TypeDescriptor propertyType;

    /**
     *
     * @param beanClass class which collection will be hidden
     * @param collectionName name of collection to hide
     */
    public CollectionPropertyHidingTransformer(Class<?> beanClass, String collectionName) {
        this.beanClass = beanClass;
        this.collectionName = collectionName;
    }

    @Override
    protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
        // lazy init, we need TypeMapping
        if (type == null) {
            type = typeMapping.getTypeDescriptor(beanClass);
            propertyDescriptor = type.getPropertyTypeDescriptor(collectionName, typeMapping);
            writeMethod = propertyDescriptor.getWriteMethod();
            propertyType = propertyDescriptor.getCollectionEntryType();
        }

        if (resolvedType == null) {
            // if we are transforming a child node which does not define
            // the class to be used, return the type of the collection entries

            // if the parent type is of the handled type
            // this is the case when we are transforming children nodes)
            if(state.getLevel() > 1 && state.getCurrentType().equals(type)) {
                // make it the default
                // use property descriptor
                resolvedType = getPropertyType();
            }
        }
        return resolvedType;
    }

    @Override
    public void setProperty(TypeMapping mapping, TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) throws RepositoryException {
        if (descriptor.getName().equals(collectionName)) {
            Object bean = state.getCurrentBean();

            Map<String, Object> value = Maps.filterValues(values, new Predicate<Object>() {
                @Override
                public boolean apply(Object input) {
                    return getPropertyType().getType().isInstance(input);
                }
            });

            try {
                if (propertyDescriptor.isMap()) {
                    writeMethod.invoke(bean, value);
                } else if (propertyDescriptor.isArray()) {
                    Class<?> entryClass = getPropertyType().getType();
                    Collection<Object> list = new LinkedList<Object>(value.values());

                    Object[] arr = (Object[]) Array.newInstance(entryClass, list.size());

                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = Iterables.get(list, i);
                    }
                    writeMethod.invoke(bean, new Object[] {arr});
                } else if (propertyDescriptor.isCollection()) {
                    Collection<?> collection = createCollectionFromMap(value, propertyDescriptor.getType().getType());
                    writeMethod.invoke(bean, collection);
                }
            } catch (Exception e) {
                log.error("Can't call set method " + propertyDescriptor.getWriteMethod(), e);
            }
        } else {
            super.setProperty(mapping, state, descriptor, values);
        }
    }

    public TypeDescriptor getPropertyType() {
        return propertyType;
    }
}
