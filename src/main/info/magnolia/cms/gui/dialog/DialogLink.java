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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.ContextMessages;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogLink extends DialogEditWithButton {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogLink.class);

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogLink() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        String extension = this.getConfigValue("extension");
        String label = ContextMessages.get(this.getRequest(),"dialog.link.internal"); 
        this.getButton().setLabel(label);
        this.getButton().setSaveInfo(false);
        String repository = this.getConfigValue("repository", ContentRepository.WEBSITE);
        this.getButton().setOnclick(
            "mgnlDialogLinkOpenBrowser('" + this.getName() + "','" + repository + "','" + extension + "');");
    }

}
