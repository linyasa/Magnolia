/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.util.Resource;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class ContentNodeIterator extends TagSupport
{

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentNodeIterator.class);

    protected static final String CURRENT_INDEX = "currentIndex";

    protected static final String SIZE = "size";

    public static final String CONTENT_NODE_COLLECTION_NAME = "contentNodeCollectionName";

    private String contentNodeCollectionName;

    private Iterator contentNodeIterator;

    private Content page;

    private int beginIndex;

    private int endIndex;

    private int step = 1;

    private int size;

    private int currentIndex;

    /**
     * <p>
     * starts loop tag
     * </p>
     * @return int
     */
    public int doStartTag()
    {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        this.page = Resource.getCurrentActivePage(request);
        try
        {
            Collection children = this.page.getContentNode(this.contentNodeCollectionName).getChildren();
            this.size = children.size();
            if (this.size == 0)
                return SKIP_BODY;
            pageContext.setAttribute(ContentNodeIterator.SIZE, new Integer(this.getEnd()), PageContext.REQUEST_SCOPE);
            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.currentIndex),
                PageContext.REQUEST_SCOPE);
            pageContext.setAttribute(
                ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
                this.contentNodeCollectionName,
                PageContext.REQUEST_SCOPE);
            this.contentNodeIterator = children.iterator();
            Resource.setLocalContentNodeCollectionName(request, this.contentNodeCollectionName);
            for (; this.beginIndex > -1; --this.beginIndex)
                Resource.setLocalContentNode(request, (ContentNode) this.contentNodeIterator.next());
        }
        catch (RepositoryException re)
        {
            log.debug(re.getMessage());
            return SKIP_BODY;
        }
        this.page = null;
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @return int
     */
    public int doAfterBody()
    {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        if (this.contentNodeIterator.hasNext() && (this.currentIndex < this.getEnd()))
        {
            this.currentIndex++;
            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.currentIndex),
                PageContext.REQUEST_SCOPE);
            for (int i = 0; i < this.step; i++)
                Resource.setLocalContentNode(request, (ContentNode) this.contentNodeIterator.next());
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }

    /**
     * @param name , container list name on which this tag will iterate
     * @deprecated
     */
    public void setContainerListName(String name)
    {
        this.setContentNodeCollectionName(name);
    }

    /**
     * @param name , content node name on which this tag will iterate
     */
    public void setContentNodeCollectionName(String name)
    {
        this.contentNodeCollectionName = name;
    }

    public String getContentNodeCollectionName()
    {
        return this.contentNodeCollectionName;
    }

    /**
     * @param index , to begin with
     */
    public void setBegin(String index)
    {
        this.beginIndex = (new Integer(index)).intValue();
    }

    /**
     * @param index , to end at
     */
    public void setEnd(String index)
    {
        this.endIndex = (new Integer(index)).intValue();
    }

    /**
     * @return end index
     */
    private int getEnd()
    {
        if (this.endIndex == 0)
        {
            return this.size;
        }
        return this.endIndex;
    }

    /**
     * @param step to jump to
     */
    public void setStep(String step)
    {
        this.step = (new Integer(step)).intValue();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag()
    {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Resource.removeLocalContentNode(request);
        Resource.removeLocalContentNodeCollectionName(request);
        pageContext.removeAttribute(ContentNodeIterator.CURRENT_INDEX);
        pageContext.removeAttribute(ContentNodeIterator.SIZE);
        pageContext.removeAttribute(ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME);
        this.beginIndex = 0;
        this.endIndex = 0;
        this.step = 1;
        this.size = 0;
        this.currentIndex = 0;
        return EVAL_PAGE;
    }

}
