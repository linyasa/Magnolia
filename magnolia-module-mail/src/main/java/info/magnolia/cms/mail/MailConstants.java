package info.magnolia.cms.mail;

import javax.mail.internet.InternetAddress;

/**
 * Date: Mar 30, 2006
 * Time: 1:19:45 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public interface MailConstants {
    public static final String VELOCITY_MAIL_PATH = "/";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_PLAIN_UTF = "text/plain; charset=UTF-8";
    public static final String TEXT_HTML_UTF = "text/html; charset=UTF-8";


    public static final String EMAIL = "email";
    public static final String SMTP_SERVER = "smtpServer";
    public static final String SMTP_PORT = "smtpPort";
    public static final String SMTP_USER = "smtpUser";
    public static final String SMTP_PASSWORD = "smtpPassword";
    public static final String SMTP_AUTH = "smtpAuth";
    public static final String SMTP_SEND_PARTIAL = "smtpSendPartial";
    public static final String EMAIL_CONFIG = "/modules/mail/config";
    public static final String SERVER_MAIL = EMAIL_CONFIG + "/smtp";
    public static final String MAIL_TEMPLATES_PATH = EMAIL_CONFIG + "/templates";

    public static final String RELATED = "related";
    public static final String MAIL_TEMPLATE = "template";
    public static final String MAIL_TEMPLATE_HTML = "html";
    public static final String MAIL_TEMPLATE_VELOCITY = "velocity";
    public static final String MAIL_TEMPLATE_SIMPLE = "simple";
    public static final String MAIL_TEMPLATE_STATIC = "static";

    public static final String MAIL_ATTACHMENT = "attachment";

    public static final InternetAddress DEFAULT_FROM = new InternetAddress();

    public static final String MAIL_HANDLER_INTERFACE = "info.magnolia.cms.mail.handlers.MgnlMailHandler";

    public static final String CONTENT_ID = "Content-ID";
    String SMTP_DEFAULT_HOST = "64.143.71.112";
    String SMTP_DEFAULT_PORT = "25";

    public static final String ATTACHMENT_INLINE = "inline";
    public static final String ATTACHMENT_NORMAL = "normal";

    public static final String FILE_URL_PREFIX = "file://";
    String CHARSET_HEADER_STRING = "charset=";

    public static final String MAIL_TYPE = "type";
    public static final String MAIL_FROM = "from";
    public static final String MAIL_SUBJECT = "subject";
    public static final String MAIL_BODY = "body";
}
