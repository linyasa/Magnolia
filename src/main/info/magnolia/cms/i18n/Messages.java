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

package info.magnolia.cms.i18n;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;


/**
 * @author Philipp Bracher Provieds localized strings. You should uses the ContextMessages class if you can provide a
 * request object. Messages will do the job as good as possible without to know the session (user) and all the other
 * contextual things.
 */

public class Messages {

    public static String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages";
    public static String JS_OBJECTNAME = "mgnlMessages";

    protected static Logger log = Logger.getLogger(Messages.class);

    // never use this directly: subclasses can overrite geter
    private String basename;

    // never use this directly: subclasses can overrite geter
    private Locale locale;

    /**
     * Subclasses will overwrite getBundle()
     */
    private ResourceBundle bundle;

    /**
     * Used by sublcasses. Do not use without knowledge
     */
    protected Messages() {
    }

    /**
     * @param basename the name of the bundle
     */
    public Messages(String basename) {
        setLocale(Locale.getDefault());
        setBasename(basename);
    }

    /**
     * @param basename name of the bundle
     * @param locale
     */
    public Messages(String basename, Locale locale) {
        setLocale(locale);
        setBasename(basename);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getBasename() {
        if(basename==null)
            return DEFAULT_BASENAME;
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
        bundle = null;
    }

    public String get(String key) {
        try {
            return getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }

    public String get(String key, String basename) {
        try {
            return getBundle(basename).getString(key);
        }
        catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }

    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * @param key
     * @param args
     * @return
     */
    public String get(String key, Object args[]) {
        return MessageFormat.format(get(key), args);
    }

    public String get(String key, String basename, Object args[]) {
        return MessageFormat.format(get(key, basename), args);
    }

    public String getWithDefault(String key, String defaultMsg) {
        String msg;
        try {
            msg = getBundle().getString(key);
            if(msg.startsWith("???")){
                msg = defaultMsg;
            }
            
        }
        catch (MissingResourceException e) {
            msg = defaultMsg;
        }
        return msg;
    }

    public String getWithDefault(String key, String basename, String defaultMsg) {
        String msg;
        try {
            msg = getBundle(basename).getString(key);
            if(msg.startsWith("???")){
                msg = defaultMsg;
            }
            
        }
        catch (MissingResourceException e) {
            msg = defaultMsg;
        }
        return msg;
    }

    public String getWithDefault(String key,  Object args[], String defaultMsg) {
       return MessageFormat.format(getWithDefault(key, defaultMsg), args);
    }

    public String getWithDefault(String key, String basename, Object args[], String defaultMsg) {
        return MessageFormat.format(getWithDefault(key, basename, defaultMsg), args);
    }

    
    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public String generateJavaScript(){
       StringBuffer str = new StringBuffer();
       ResourceBundle bundle = getBundle();
       
       str.append("/* ###################################\n");
       str.append("### Generated Messages\n");
       str.append("################################### */\n\n");
       
       Enumeration en = bundle.getKeys();
       while(en.hasMoreElements()){
           String key = (String) en.nextElement();
           
           if(key.startsWith("js.")){
               String msg = ((String)bundle.getObject(key)).replaceAll("'", "\\\\'").replaceAll("\n","\\\\n");
               str.append(JS_OBJECTNAME + ".add('"+ key +"','" + msg + "','" + getBasename() + "');");
               str.append("\n");
           }
       }
       return str.toString();
    }
    
    /**
     * @return Returns the bundle.
     */
    public ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = PropertyResourceBundle.getBundle(getBasename(), getLocale());
        }
        return bundle;
    }

    public ResourceBundle getBundle(String basename) {
        return PropertyResourceBundle.getBundle(basename, getLocale());
    }

    public ResourceBundle getBundle(String basename, Locale locale) {
        return PropertyResourceBundle.getBundle(basename, getLocale());
    }
}