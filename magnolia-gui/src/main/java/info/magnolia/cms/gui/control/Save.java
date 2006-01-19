/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Digester;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.LinkUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * This class handels the saving in the dialogs. It uses the mgnlSaveInfo parameters sendend from the browser to store
 * the data in the node.The structure of the parameter is the following: <br>
 * <code>name, type, valueType, isRichEditValue, encoding</code>
 * <p>
 * To find the consts see ControlSuper <table>
 * <tr>
 * <td>name</td>
 * <td>the name of the field</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>string, boolean, ...</td>
 * </tr>
 * <tr>
 * <td>valueType</td>
 * <td>single, multiple</td>
 * </tr>
 * <tr>
 * <td>isRichEditValue</td>
 * <td>value from an editor</td>
 * </tr>
 * <tr>
 * <td>encoding</td>
 * <td>base64, unix, none</td>
 * </tr>
 * </table>
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Save extends ControlSuper {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(Save.class);

    /**
     * The from, containing all the fields and files. This form is generated by magnolia.
     */
    private MultipartForm form;

    /**
     * creates the node if it is not present
     */
    private boolean create;

    private ItemType creationItemType = ItemType.CONTENT;

    /**
     * The name of the repository to store the data. Website is default.
     */
    private String repository = ContentRepository.WEBSITE;

    /**
     * Do not use this without a reason.
     */
    public Save() {
    }

    /**
     * Initialize the Save control.
     * @param form the form generated from the request due to handle multipart forms
     * @param request request
     */
    public Save(MultipartForm form, HttpServletRequest request) {
        this.setForm(form);
        this.setRequest(request);
        this.setPath(form.getParameter("mgnlPath")); //$NON-NLS-1$
        this.setNodeCollectionName(form.getParameter("mgnlNodeCollection")); //$NON-NLS-1$
        this.setNodeName(form.getParameter("mgnlNode")); //$NON-NLS-1$
        this.setParagraph(form.getParameter("mgnlParagraph")); //$NON-NLS-1$
        this.setRepository(form.getParameter("mgnlRepository")); //$NON-NLS-1$
    }

    /**
     * Uses the mgnlSageInfo parameters to save the data.
     */
    public void save() {
        String[] saveInfos = getForm().getParameterValues("mgnlSaveInfo"); // name,type,propertyOrNode //$NON-NLS-1$
        String nodeCollectionName = this.getNodeCollectionName(null);
        String nodeName = this.getNodeName(null);
        String path = this.getPath();
        HttpServletRequest request = this.getRequest();

        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, this.getRepository());
        try {
            Content page = null;
            try {
                page = hm.getContent(path);
            }
            catch (RepositoryException e) {
                if (isCreate()) {
                    String parentPath = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
                    String label = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(parentPath)) {
                        page = hm.getRoot();
                    }
                    else {
                        page = hm.getContent(parentPath);
                    }
                    page = page.createContent(label, creationItemType);
                }
                else {
                    log.error("tried to save a not existing node. use create = true to force creation"); //$NON-NLS-1$
                }
            }

            // get or create nodeCollection
            Content nodeCollection = null;
            if (nodeCollectionName != null) {
                try {
                    nodeCollection = page.getContent(nodeCollectionName);
                }
                catch (RepositoryException re) {
                    // nodeCollection does not exist -> create
                    nodeCollection = page.createContent(nodeCollectionName, ItemType.CONTENTNODE);
                    if (log.isDebugEnabled()) {
                        log.debug("Create - " + nodeCollection.getHandle()); //$NON-NLS-1$
                    }
                }
            }
            else {
                nodeCollection = page;
            }

            // get or create node
            Content node = null;
            if (nodeName != null) {
                try {
                    node = nodeCollection.getContent(nodeName);
                }
                catch (RepositoryException re) {
                    // node does not exist -> create
                    if (nodeName.equals("mgnlNew")) { //$NON-NLS-1$
                        nodeName = Path.getUniqueLabel(hm, nodeCollection.getHandle(), "0"); //$NON-NLS-1$
                        // this value can get used later on to find this node
                        this.setNodeName(nodeName);
                    }
                    node = nodeCollection.createContent(nodeName, ItemType.CONTENTNODE);
                    node.createNodeData("paragraph").setValue(this.getParagraph()); //$NON-NLS-1$
                    node.getMetaData().setSequencePosition();
                }
            }
            else {
                node = nodeCollection;
            }
            // update meta data (e.g. last modified) of this paragraph and the page
            node.updateMetaData(request);
            page.updateMetaData(request);
            // loop all saveInfo controls; saveInfo format: name, type, valueType(single|multiple, )
            for (int i = 0; i < saveInfos.length; i++) {
                String saveInfo = saveInfos[i];
                processSaveInfo(node, saveInfo);
            }
            if (log.isDebugEnabled()) {
                log.debug("Saving - " + path); //$NON-NLS-1$
            }
            hm.save();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        this.removeSessionAttributes();
    }

    /**
     * This method cears about one mgnlSaveInfo. It adds the value to the node
     * @param node node to add data
     * @param saveInfo <code>name, type, valueType, isRichEditValue, encoding</code>
     * @throws PathNotFoundException exception
     * @throws RepositoryException exception
     * @throws AccessDeniedException no access
     */
    protected void processSaveInfo(Content node, String saveInfo) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {

        String name;
        int type = PropertyType.STRING;
        int valueType = ControlSuper.VALUETYPE_SINGLE;
        int isRichEditValue = 0;
        int encoding = ControlSuper.ENCODING_NO;
        String[] values = {StringUtils.EMPTY};
        if (StringUtils.contains(saveInfo, ',')) {
            String[] info = StringUtils.split(saveInfo, ',');
            name = info[0];
            if (info.length >= 2) {
                type = PropertyType.valueFromName(info[1]);
            }
            if (info.length >= 3) {
                valueType = Integer.valueOf(info[2]).intValue();
            }
            if (info.length >= 4) {
                isRichEditValue = Integer.valueOf(info[3]).intValue();
            }
            if (info.length >= 5) {
                encoding = Integer.valueOf(info[4]).intValue();
            }
        }
        else {
            name = saveInfo;
        }
        if (type == PropertyType.BINARY) {
            processBinary(node, name);
        }
        else {
            values = getForm().getParameterValues(name);
            if (valueType == ControlSuper.VALUETYPE_MULTIPLE) {
                processMultiple(node, name, type, values);
            }
            else {
                processCommon(node, name, type, isRichEditValue, encoding, values);
            }
        }
    }

    /**
     * Process a common value
     * @param node node where the data must be stored
     * @param name name of the field
     * @param type type
     * @param isRichEditValue is it a return value of a richt edit field
     * @param encoding must we encode (base64)
     * @param values all values belonging to this field
     * @throws PathNotFoundException exception
     * @throws RepositoryException exception
     * @throws AccessDeniedException exception
     */
    protected void processCommon(Content node, String name, int type, int isRichEditValue, int encoding, String[] values)
        throws PathNotFoundException, RepositoryException, AccessDeniedException {
        String valueStr = StringUtils.EMPTY;
        if (values != null) {
            valueStr = values[0]; // values is null when the expected field would not exis, e.g no
        }
        // checkbox selected
        if (isRichEditValue != ControlSuper.RICHEDIT_NONE) {
            valueStr = this.getRichEditValueStr(valueStr, isRichEditValue);
        }
        // actualy encoding does only work for control password
        boolean remove = false;
        boolean write = false;
        if (encoding == ControlSuper.ENCODING_BASE64) {
            if (StringUtils.isNotBlank(valueStr)) {
                valueStr = new String(Base64.encodeBase64(valueStr.getBytes()));
                write = true;
            }
        }
        else if (encoding == ControlSuper.ENCODING_UNIX) {
            if (StringUtils.isNotEmpty(valueStr)) {
                valueStr = Digester.getSHA1Hex(valueStr);
                write = true;
            }
        }
        else {
            // no encoding
            if (values == null || StringUtils.isEmpty(valueStr)) {
                remove = true;
            }
            else {
                write = true;
            }
        }
        if (remove) {
            processRemoveCommon(node, name, type, isRichEditValue, encoding, values);
        }
        else if (write) {
            processWriteCommon(node, name, valueStr, type);
        }
    }

    /**
     * Remove the specified property on the node.
     * 
     * @param node the node
     * @param name the property name
     * @param type the property type
     * @param isRichEditValue is it a return value of a richt edit field
     * @param encoding must we encode (base64)
     * @param values all values belonging to this field
     * 
     * @throws PathNotFoundException thrown if the property name does not correspond to a
     *  valid property name
     * @throws RepositoryException thrown if other repository exception is thrown 
     */
    protected void processRemoveCommon(Content node, String name, int type, int isRichEditValue, int encoding, String[] values) 
        throws PathNotFoundException, RepositoryException {
        NodeData data = node.getNodeData(name);
    
        if (data.isExist()) {
            node.deleteNodeData(name);
        }
    }

    /**
     * Writes a property value.
     * 
     * @param node the node
     * @param name the property name to be written
     * @param valueStr the value of the property
     * @throws AccessDeniedException thrown if the write access is not granted
     * @throws RepositoryException thrown if other repository exception is thrown
     */
    protected void processWriteCommon(Content node, String name, String valueStr, int type) 
        throws AccessDeniedException, RepositoryException {
        Value value = this.getValue(valueStr, type);
        
        NodeData data = node.getNodeData(name);
        
        if (null != value) {
            if (data.isExist()) {
                data.setValue(value);
            }
            else {
                node.createNodeData(name, value);
            }
        }
    }
    
    /**
     * @param node
     * @param name
     * @param type
     * @param values
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws AccessDeniedException
     */
    protected void processMultiple(Content node, String name, int type, String[] values) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {
        // remove entire content node and (re-)write each
        try {
            node.delete(name);
        }
        catch (PathNotFoundException e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        if (values != null && values.length != 0) {
            Content multiNode = node.createContent(name, ItemType.CONTENTNODE);
            try {
                // MetaData.CREATION_DATE has private access; no method to delete it so far...
                multiNode.deleteNodeData("creationdate"); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                log.debug("Exception caught: " + re.getMessage(), re); //$NON-NLS-1$
            }
            for (int j = 0; j < values.length; j++) {
                String valueStr = values[j];
                Value value = this.getValue(valueStr, type);
                multiNode.createNodeData(Integer.toString(j)).setValue(value);
            }
        }
    }

    /**
     * Process binary data. File- or imageupload.
     * @param node
     * @param name
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    protected void processBinary(Content node, String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Document doc = getForm().getDocument(name);
        if (doc == null && getForm().getParameter(name + "_" + File.REMOVE) != null) { //$NON-NLS-1$
            try {
                node.deleteNodeData(name);
            }
            catch (RepositoryException re) {
                log.debug("Exception caught: " + re.getMessage(), re); //$NON-NLS-1$
            }

        }
        else {
            NodeData data = null;
            if (doc != null) {
                data = node.getNodeData(name);
                if (!data.isExist()) {
                    data = node.createNodeData(name, PropertyType.BINARY);
                    if (log.isDebugEnabled()) {
                        log.debug("creating under - " + node.getHandle()); //$NON-NLS-1$
                        log.debug("creating node data for binary store - " + name); //$NON-NLS-1$
                    }
                }
                data.setValue(doc.getStream());
                log.debug("Node data updated"); //$NON-NLS-1$
            }
            if (data != null) {
                String fileName = getForm().getParameter(name + "_" + FileProperties.PROPERTY_FILENAME); //$NON-NLS-1$
                if (fileName == null || fileName.equals(StringUtils.EMPTY)) {
                    fileName = doc.getFileName();
                }
                data.setAttribute(FileProperties.PROPERTY_FILENAME, fileName);
                if (doc != null) {
                    data.setAttribute(FileProperties.PROPERTY_CONTENTTYPE, doc.getType());

                    Calendar value = new GregorianCalendar(TimeZone.getDefault());
                    data.setAttribute(FileProperties.PROPERTY_LASTMODIFIES, value);

                    data.setAttribute(FileProperties.PROPERTY_SIZE, Long.toString(doc.getLength()));

                    data.setAttribute(FileProperties.PROPERTY_EXTENSION, doc.getExtension());

                    String template = getForm().getParameter(name + "_" + FileProperties.PROPERTY_TEMPLATE); //$NON-NLS-1$

                    data.setAttribute(FileProperties.PROPERTY_TEMPLATE, template);

                    doc.delete();
                }
            }
        }
    }

    public void removeSessionAttributes() {
        HttpSession session = this.getRequest().getSession();
        MultipartForm form = getForm();
        String[] toRemove = form.getParameterValues(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE);
        if (toRemove != null) {
            for (int i = 0; i < toRemove.length; i++) {
                session.removeAttribute(toRemove[i]);
                // log.debug("removed: "+toRemove[i]);
            }
        }
    }

    public Value getValue(String s) {
        return this.getValue(s, PropertyType.STRING);
    }

    public Value getValue(long l) {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
        ValueFactory valueFactory;
        try {
            valueFactory = hm.getWorkspace().getSession().getValueFactory();
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }
        return valueFactory.createValue(l);
    }

    public Value getValue(String valueStr, int type) {

        ValueFactory valueFactory = null;

        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.getRequest(), this.getRepository());
        try {
            valueFactory = hm.getWorkspace().getSession().getValueFactory();
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }

        Value value = null;
        if (type == PropertyType.STRING) {
            value = valueFactory.createValue(valueStr);
        }
        else if (type == PropertyType.BOOLEAN) {
            value = valueFactory.createValue(BooleanUtils.toBoolean(valueStr));
        }
        else if (type == PropertyType.DOUBLE) {
            try {
                value = valueFactory.createValue(Double.parseDouble(valueStr));
            }
            catch (NumberFormatException e) {
                value = valueFactory.createValue(0d);
            }
        }
        else if (type == PropertyType.LONG) {
            try {
                value = valueFactory.createValue(Long.parseLong(valueStr));
            }
            catch (NumberFormatException e) {
                value = valueFactory.createValue(0L);
            }
        }
        else if (type == PropertyType.DATE) {
            try {
                Calendar date = new GregorianCalendar();
                try {
                    String newDateAndTime = valueStr;
                    String[] dateAndTimeTokens = newDateAndTime.split("T"); //$NON-NLS-1$
                    String newDate = dateAndTimeTokens[0];
                    String[] dateTokens = newDate.split("-"); //$NON-NLS-1$
                    int hour = 0;
                    int minute = 0;
                    int second = 0;
                    int year = Integer.parseInt(dateTokens[0]);
                    int month = Integer.parseInt(dateTokens[1]) - 1;
                    int day = Integer.parseInt(dateTokens[2]);
                    if (dateAndTimeTokens.length > 1) {
                        String newTime = dateAndTimeTokens[1];
                        String[] timeTokens = newTime.split(":"); //$NON-NLS-1$
                        hour = Integer.parseInt(timeTokens[0]);
                        minute = Integer.parseInt(timeTokens[1]);
                        second = Integer.parseInt(timeTokens[2]);
                    }
                    date.set(year, month, day, hour, minute, second);
                    // this is used in the searching
                    date.set(Calendar.MILLISECOND, 0);
                    date.setTimeZone(TimeZone.getTimeZone("GMT"));
                }
                // todo time zone??
                catch (Exception e) {
                    // ignore, it sets the current date / time
                }
                value = valueFactory.createValue(date);
            }
            catch (Exception e) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        else if (type == PropertyType.REFERENCE) {
            try {
                Node referencedNode = hm.getWorkspace().getSession().getNodeByUUID(valueStr);
                
                value = valueFactory.createValue(referencedNode);
            }
            catch(RepositoryException re) {
                log.debug("Cannot retrieve the referenced node by UUID: " + valueStr,
                          re);
            }
        }
        
        return value;
    }

    /**
     * Manipulates the value returned from html editors (kupu, fck). It encodes the internal links.
     * @param value
     * @param isRichEditValue
     * @return todo configurable regexp on save?
     */
    protected String getRichEditValueStr(String value, int isRichEditValue) {

        // encode the internal links to avoid dependences from the contextpath, position of the page
        String valueStr = LinkUtil.convertAbsoluteLinksToUUIDs(value);
        switch (isRichEditValue) {
            case ControlSuper.RICHEDIT_KUPU:
            case ControlSuper.RICHEDIT_FCK:
                valueStr = StringUtils.replace(valueStr, "\r\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
                valueStr = StringUtils.replace(valueStr, "\n", " "); //$NON-NLS-1$ //$NON-NLS-2$

                // ie inserts some strange br...
                valueStr = StringUtils.replace(valueStr, "</br>", StringUtils.EMPTY); //$NON-NLS-1$
                valueStr = StringUtils.replace(valueStr, "<P><BR>", "<P>"); //$NON-NLS-1$ //$NON-NLS-2$

                valueStr = StringUtils.replace(valueStr, "<br>", "\n "); //$NON-NLS-1$ //$NON-NLS-2$
                valueStr = StringUtils.replace(valueStr, "<BR>", "\n "); //$NON-NLS-1$ //$NON-NLS-2$
                valueStr = StringUtils.replace(valueStr, "<br/>", "\n "); //$NON-NLS-1$ //$NON-NLS-2$

                // replace <P>
                valueStr = replacePByBr(valueStr, "p"); //$NON-NLS-1$

                // TODO remove it definitly: the method seams not to work
                // replace <a class="...></a> by <span class=""></span>
                // valueStr = replaceABySpan(valueStr, "a");
                break;
            default:
                break;
        }
        return valueStr;

    }

    /**
     * @param value
     * @param tagName
     */
    protected static String replacePByBr(final String value, String tagName) {

        if (StringUtils.isBlank(value)) {
            return value;
        }

        String fixedValue = value;

        String pre = "<" + tagName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        String post = "</" + tagName + ">"; //$NON-NLS-1$ //$NON-NLS-2$

        // get rid of last </p>
        if (fixedValue.endsWith(post)) {
            fixedValue = StringUtils.substringBeforeLast(fixedValue, post);
        }

        fixedValue = StringUtils.replace(fixedValue, pre + "&nbsp;" + post, "\n "); //$NON-NLS-1$ //$NON-NLS-2$
        fixedValue = StringUtils.replace(fixedValue, pre, StringUtils.EMPTY);
        fixedValue = StringUtils.replace(fixedValue, post, "\n\n "); //$NON-NLS-1$

        if (!tagName.equals(tagName.toUpperCase())) {
            fixedValue = replacePByBr(fixedValue, tagName.toUpperCase());
        }
        return fixedValue;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public ItemType getCreationItemType() {
        return creationItemType;
    }

    public void setCreationItemType(ItemType creationItemType) {
        this.creationItemType = creationItemType;
    }

    /**
     * @return the form containing the values passed
     */
    protected MultipartForm getForm() {
        return form;
    }

    /**
     * set the from
     * @param form containing the sended values
     */
    protected void setForm(MultipartForm form) {
        this.form = form;
    }

    /**
     * set the name of the repository saving to
     * @param repository the name of the repository
     */
    protected void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * get the name of thre repository saving to
     * @return name
     */
    protected String getRepository() {
        return repository;
    }
}