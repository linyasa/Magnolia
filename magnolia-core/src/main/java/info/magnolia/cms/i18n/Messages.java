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

package info.magnolia.cms.i18n;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides localized strings. You should uses the ContextMessages class if you can provide a request object. Messages
 * will do the job as good as possible without to know the session (user) and all the other contextual things. Endusers
 * will use the MessageManager to resolve messages.
 * @author Philipp Bracher
 */

public class Messages {

    Logger log = LoggerFactory.getLogger(Messages.class);

    /**
     * Name of the javascript object used to make the messages public to the javascripts
     */
    public static final String JS_OBJECTNAME = "mgnlMessages"; //$NON-NLS-1$

    /**
     * The name of the bundle
     */
    protected String basename = MessagesManager.DEFAULT_BASENAME;

    /**
     * The current locale
     */
    protected Locale locale;

    /**
     * The current bundle. Subclasses will overwrite getBundle()
     */
    private ResourceBundle bundle;

    /**
     * Take the message from this object if not found in this instance. One can create a chain.
     */
    private Messages fallBackMessages;

    /**
     * Used by sublcasses. Do not use without knowledge
     */
    protected Messages() {

    }

    /**
     * @param basename name of the bundle
     * @param locale the locale
     */
    protected Messages(String basename, Locale locale) {
        this.locale = locale;
        this.basename = basename;
    }

    /**
     * @return current locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * If no basename is provided this method returns DEFAULT_BASENAME
     * @return current basename
     */
    public String getBasename() {
        return basename;
    }

    /**
     * Get the message from the bundle
     * @param key the key
     * @return message
     */
    public String get(String key) {
        try {
            return getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            if (this.hasFallBackMessages()) {
                return this.getFallBackMessages().get(key);
            }
            return "???" + key + "???"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * @param key the key
     * @param args the replacement strings
     * @return message
     */
    public String get(String key, Object[] args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * You can provide a default value if the key is not found
     * @param key key
     * @param defaultMsg the default message
     * @return the message
     */
    public String getWithDefault(String key, String defaultMsg) {
        String msg;
        try {
            msg = get(key);
            if (msg.startsWith("???")) { //$NON-NLS-1$
                msg = defaultMsg;
            }

        }
        catch (MissingResourceException e) {
            msg = defaultMsg;
        }
        return msg;
    }

    /**
     * With default value and replacement strings
     * @param key key
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public String getWithDefault(String key, Object[] args, String defaultMsg) {
        return MessageFormat.format(getWithDefault(key, defaultMsg), args);
    }

    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public String generateJavaScript() {
        StringBuffer str = new StringBuffer();
        ResourceBundle bundle = getBundle();

        str.append("/* ###################################\n"); //$NON-NLS-1$
        str.append("### Generated Messages\n"); //$NON-NLS-1$
        str.append("################################### */\n\n"); //$NON-NLS-1$

        Enumeration en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();

            if (key.endsWith(".js")) { //$NON-NLS-1$
                String msg = ((String) bundle.getObject(key)).replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                str.append(JS_OBJECTNAME + ".add('" + key + "','" + msg + "','" + getBasename() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                str.append("\n"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * Make the string save for javascript (escape special characters).
     * @param str string to escape
     * @return escaped string
     */
    public static String javaScriptString(String str) {
        return str.replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    /**
     * @return Returns the bundle for the current basename
     */
    public ResourceBundle getBundle() {
        if (bundle == null) {
            try {
                InputStream stream = ClasspathResourcesUtil.getStream("/"
                    + StringUtils.replace(basename, ".", "/")
                    + "_"
                    + getLocale().getLanguage()
                    + "_"
                    + getLocale().getCountry()
                    + ".properties", false);
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + "_"
                        + getLocale().getLanguage()
                        + ".properties", false);
                }
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + "_"
                        + MessagesManager.getDefaultLocale().getLanguage()
                        + ".properties", false);
                }
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + ".properties", false);
                }

                if (stream != null) {
                    bundle = new PropertyResourceBundle(stream);
                }
                else {
                    bundle = ResourceBundle.getBundle(getBasename(), getLocale());
                }
            }
            catch (IOException e) {
                log.error("can't load messages for " + basename);
            }
        }
        return bundle;
    }

    public Messages getFallBackMessages() {
        return fallBackMessages;
    }

    public Messages setFallBackMessages(Messages fallBackMessages) {
        this.fallBackMessages = fallBackMessages;
        return fallBackMessages;
    }

    /**
     * This sets the passed messages as the fallback messages. If there are already fallback messages, the passed messages are appended at the end of the chain.
     * @param messages
     * @return
     */
    public Messages chainMessages(Messages messages) {
        // never create cyclic chains
        if(this.equals(messages)){
            return this;
        }
        if (this.hasFallBackMessages()) {
            Messages fallback = this.getFallBackMessages();
            return fallback.chainMessages(messages);
        }
        else {
            this.setFallBackMessages(messages);
            return messages;
        }
    }

    public boolean hasFallBackMessages() {
        return this.fallBackMessages != null;
    }

    public void reload() throws Exception {
        this.bundle = null;
    }
    
    /**
     * True if the basename and the locale are the same
     */
    public boolean equals(Object arg0) {
        return StringUtils.equals(((Messages) arg0).basename, this.basename) && this.locale.equals(((Messages)arg0).locale);
    }

}