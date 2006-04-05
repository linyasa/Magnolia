/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;

/**
 * @author Sameer Charles
 * $Id:ListModelIteratorImpl.java 2492 2006-03-30 08:30:43Z scharles $
 */
public class ListModelIteratorImpl implements ListModelIterator {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(ListModelIteratorImpl.class);

    /**
     * list holding all objects/records
     * */
    private final List list;

    /**
     *  next position
     * */
    private int pos;

    /**
     * next content object (prefetched)
     * */
    private Content next;

    /**
     * object on current pointer
     * */
    private Content current;

    /**
     *  key name on which provided list is grouped
     * */
    private String groupKey;

    /**
     * creates a new ListModelIterator
     * @param list of content objects
     * */
    public ListModelIteratorImpl(List list, String groupKey) {
        this.list = new ArrayList(list);
        this.groupKey = groupKey;
        this.pos = 0;
        // prefetch next object
        prefetchNext();
    }

    /**
     * prefetch object for the list
     *
     * */
    private void prefetchNext() {
        this.next = null;
        while (this.next == null && this.pos < this.list.size()) {
            try {
                this.next = (Content) this.list.get(pos);
            } catch (ClassCastException e) {
                // invalid object, remove it and try again
                this.list.remove(pos); // will try again
            }
        }
    }

    /**
     * get named value
     *
     * @param name its a key to which value is attached in this record
     */
    public Object getValue(String name) {
        return this.getValue(name, this.current);
    }

    /**
     * get value from a specified object
     * @param name its a key to which value is attached in this record
     * @param node
     * */
    private Object getValue(String name, Content node) {
        return this.internalGetValue(name, node);
    }

    /**
     * get internal value
     * - first check for property in  this object
     * - then look for the getter for this name
     * - else search in MetaData
     * @param name
     * @param node
     * */
    private Object internalGetValue(String name, Content node) {
        try {
            if (node.hasNodeData(name)) {
                return node.getNodeData(name).getString();
            } else {
                // check if getter exist for this name
                try {
                    String methodName =
                            "get"+StringUtils.substring(name,0,1).toUpperCase() + StringUtils.substring(name,1);
                    Method method = getClass().getMethod(methodName, new Class[] {node.getClass()});
                    return method.invoke(this, new Object[] {node});
                } catch (NoSuchMethodException e) {
                    // finally check MetaData
                    return node.getMetaData().getStringProperty(name);
                } catch (Exception e) {
                    log.error("Unable to locate property or method for - "+name);
                }
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get name
     * @return name of the current object
     * */
    public String getName() {
        return this.getName(this.current);
    }

    /**
     * get name
     * @return name of the current object
     * */
    public String getName(Content node) {
        return node.getName();
    }

    /**
     * get node type
     * @return node type
     * */
    public String getType() {
        return this.getType(this.current);
    }

    /**
     * get node type
     * @return node type
     * */
    public String getType(Content node) {
        try {
            return node.getNodeType().getName();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return StringUtils.EMPTY;
    }

    /**
     * get path
     * @return handle for the ciurrent object
     * */
    public String getPath() {
        return this.getPath(this.current);
    }

    /**
     * get path
     * @return handle for the ciurrent object
     * */
    public String getPath(Content node) {
        return node.getHandle();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModelIterator#getValueObject()
     */
    public Object getValueObject() {
        return this.current;
    }

    /**
     * get group name
     *
     * @return name of the group of the current record
     */
    public String getGroupName() {
        if (StringUtils.isEmpty(this.groupKey)) return StringUtils.EMPTY;
        return (String) this.getValue(this.groupKey, this.current);
    }

    /**
     * move next
     */
    public Object next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        this.current = this.next;
        this.pos++;
        prefetchNext();

        return this.current;
    }

    /**
     * jump to next group
     */
    public Object nextGroup() {
        Object tmp = null;
        while (this.hasNextInGroup()) {
            tmp = this.next();
        }
        return tmp;
    }

    /**
     * checks if there is next record
     *
     * @return true if not EOF
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * checks if there are more records in the current group
     *
     * @return true if not EOF
     */
    public boolean hasNextInGroup() {
        if (StringUtils.isEmpty(this.groupKey)) return this.hasNext(); // no group key defined, its all one group
        else if (this.hasNext()) {
            if (this.current != null) {
                String currentValue = (String) this.getValue(this.groupKey, this.current);
                String nextValue = (String) this.getValue(this.groupKey, this.next);
                return StringUtils.equalsIgnoreCase(currentValue, nextValue);
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        // not implemented
    }

}
