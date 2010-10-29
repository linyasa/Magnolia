/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.mail;

import info.magnolia.module.mail.templates.MailAttachment;
import info.magnolia.module.mail.util.MailUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Mail template used to send emails with Magnolia.
 *
 */
public class MailTemplate {

    public static String MAIL_CONTENT_TYPE = "contentType";

    public static String MAIL_FROM = "from";

    public static String MAIL_SUBJECT = "subject";

    public static String MAIL_TO = "to";

    public static String MAIL_TO_WORKFLOW = "mailTo";

    public static String MAIL_CC = "cc";

    public static String MAIL_TYPE = "type";

    public static String MAIL_PARAMETERS = "parameters";

    public static String MAIL_ATTACHMENTS = "attachments";

    public static String MAIL_BODY = "body";

    public static final String MAIL_HTML = "html";

    public static final String MAIL_TEMPLATE_FILE = "templateFile";

    private static final String MAIL_REPLY_TO = "replyTo";

    private static final String MAIL_BCC = "bcc";

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private List<MailAttachment> attachments = new ArrayList<MailAttachment>();

    private String from;

    private String to;

    private String cc;

    private String subject;

    private String type;

    private String contentType;

    private String name;

    private String text;

    private String templateFile;

    private String bcc;

    private String replyTo;

    public MailTemplate() {

    }

    public Map<String, Object> getParameters() {
        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        return new HashMap<String, Object>(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    public List<MailAttachment> getAttachments() {
        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        return new ArrayList<MailAttachment>(attachments);
    }

    public void setAttachments(List<MailAttachment> attachments) {
        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        this.attachments.clear();
        this.attachments.addAll(attachments);
    }

    public void addAttachment(MailAttachment attachment) {
        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        this.attachments.add(attachment);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getCc() {
        return cc;
    }


    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setValues(Map<String, Object> params, List<MailAttachment> attachments) {

        if(params.containsKey(MAIL_TEMPLATE_FILE)) {
            this.templateFile = (String) params.get(MAIL_TEMPLATE_FILE);
        }

        if(params.containsKey(MAIL_CONTENT_TYPE)) {
            this.contentType = (String) params.get(MAIL_CONTENT_TYPE);
        }

        if(params.containsKey(MAIL_FROM)) {
            this.from = (String) params.get(MAIL_FROM);
        }

        if(params.containsKey(MAIL_SUBJECT)) {
            this.subject = (String) params.get(MAIL_SUBJECT);
        }

        if(params.containsKey(MAIL_TO)) {
            this.to = (String) params.get(MAIL_TO);
        }
        if(params.containsKey(MailTemplate.MAIL_TO_WORKFLOW)) {
            this.to = (String) params.get(MailTemplate.MAIL_TO_WORKFLOW);
        }

        if(params.containsKey(MAIL_CC)) {
            this.cc = (String) params.get(MAIL_CC);
        }

        if(params.containsKey(MAIL_TYPE)) {
            this.type = (String) params.get(MAIL_TYPE);
        }

        if(params.containsKey(MAIL_BODY)) {
            this.text = (String) params.get(MAIL_BODY);
        }

        if(params.containsKey(MAIL_REPLY_TO)) {
            this.replyTo = (String) params.get(MAIL_REPLY_TO);
        }

        if(params.containsKey(MAIL_BCC)) {
            this.bcc = (String) params.get(MAIL_BCC);
        }

        // instance of this class will be re-used. Do not let references to internal variables escape the instance
        this.parameters.clear();
        this.parameters.putAll(params);

        if(!CollectionUtils.isEmpty(attachments)) {
            // instance of this class will be re-used. Do not let references to internal variables escape the instance
            this.attachments.clear();
            this.attachments.addAll(attachments);
        }
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public Session initSession() {

        Map<String, String> smtp = MailModule.getInstance().getSmtp();
        Properties props = new Properties(); // System.getProperties(); should I try to use the system properties ?

        props.put("mail.smtp.host", MailUtil.getParameter(getParameters(), MailConstants.SMTP_SERVER, smtp.get(MailConstants.SMTP_SERVER)));
        props.put("mail.smtp.port", MailUtil.getParameter(getParameters(), MailConstants.SMTP_PORT, smtp.get(MailConstants.SMTP_PORT)));

        final String starttls = (String) MailUtil.getParameter(getParameters(), MailConstants.SMTP_TLS, smtp.get(MailConstants.SMTP_TLS));
        if ("true".equals(starttls)) {
            //MAGNOLIA-2420
            props.put("mail.smtp.starttls.enable", starttls);
        }
        final String ssl = (String) MailUtil.getParameter(getParameters(), MailConstants.SMTP_SSL, smtp.get(MailConstants.SMTP_SSL));
        if ("true".equals(ssl)) {
            //MAGNOLIA-2420
            props.put("mail.smtp.socketFactory.port", MailUtil.getParameter(getParameters(), MailConstants.SMTP_PORT, smtp.get(MailConstants.SMTP_PORT)));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        Authenticator auth = null;
        final String smtpUser = (String) MailUtil.getParameter(getParameters(), MailConstants.SMTP_USER, smtp.get(MailConstants.SMTP_USER));
        final String smtpPassword = (String) MailUtil.getParameter(getParameters(), MailConstants.SMTP_PASSWORD, smtp.get(MailConstants.SMTP_PASSWORD));
        if (StringUtils.isNotBlank(smtpUser)) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", smtpUser);
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            };
        }
        props.put("mail.smtp.sendpartial", StringUtils.defaultString(smtp.get(MailConstants.SMTP_SEND_PARTIAL)));
        return Session.getInstance(props, auth);
    }

}
