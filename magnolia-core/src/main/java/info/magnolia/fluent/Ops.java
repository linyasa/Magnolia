/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.fluent;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;

import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ItemNotFoundException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class Ops {
    public static NodeOperation add(final String name) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                return context.createContent(name);
            }
        };

    }

    public static NodeOperation add(final String name, final String type) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                return context.createContent(name, type);
            }
        };
    }


    public static NodeOperation getNode(final String name) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                return context.getContent(name);
            }
        };
    }

    /**
     * Can remove either a node or property.
     */
    public static NodeOperation remove(final String name) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                context.delete(name);
                return context;
            }
        };
    }

    // TODO - do we really want to differentiate between set and add property ?

    public static NodeOperation addProperty(final String name, final Object value) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                if (context.hasNodeData(name)) {
                    // throw new ItemExistsException("Property " + name + " already exists at " + context.getHandle());
                    throw new ItemExistsException(name);
                }
                context.createNodeData(name, value);
                return context;
            }
        };
    }

    public static NodeOperation setProperty(final String name, final Object newValue) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                context.setNodeData(name, value);
                return context;
            }
        };
    }

    public static NodeOperation setProperty(final String name, final Object expectedCurrentValue, final Object newValue) {
        return new A() {
            Content doExec(Content context) throws RepositoryException {
                if (!context.hasNodeData(name)) {
                    throw new ItemNotFoundException(name);
                }
                final NodeData current = context.getNodeData(name);
                if (!expectedCurrentValue.equals(NodeDataUtil.getValueObject(current))) {
                    throw new RepositoryException("Expected " + expectedCurrentValue + " and found " + current.getString() + " instead.");   
                }
                final Value value = NodeDataUtil.createValue(newValue, context.getJCRNode().getSession().getValueFactory());
                context.setNodeData(name, value);
                return context;
            }
        };

    }

    abstract static class A implements NodeOperation {
        private NodeOperation[] childrenOps = {};

        public void exec(Content context) throws RepositoryException {
            context = doExec(context);
            for (NodeOperation childrenOp : childrenOps) {
                childrenOp.exec(context);
            }
        }

        /**
         * @param context the node that should now be used as the context for subsequent operations
         */
        abstract Content doExec(Content context) throws RepositoryException;

        public NodeOperation then(NodeOperation... childrenOps) {
            this.childrenOps = childrenOps;
            return this;
        }
    }
}
