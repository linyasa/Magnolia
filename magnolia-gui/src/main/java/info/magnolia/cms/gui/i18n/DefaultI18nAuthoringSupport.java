/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.cms.gui.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Control;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.BooleanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class DefaultI18nAuthoringSupport implements I18nAuthoringSupport {

    private final I18nContentSupport i18nContentSupport;
    private boolean enabled = false;

    public DefaultI18nAuthoringSupport() {
        this.i18nContentSupport = I18nContentSupportFactory.getI18nSupport();
    }

    @Override
    public Control getLanguageChooser() {
        if (isEnabled() && i18nContentSupport.isEnabled() && i18nContentSupport.getLocales().size()>1){
            Select select = new Select();

            select.setName("locale");
            select.setEvent("onchange", "window.location = this.value");

            Content currentPage = MgnlContext.getAggregationState().getMainContent();
            String currentUri = createURI(currentPage, i18nContentSupport.getLocale());
            select.setValue(currentUri);

            for (Locale locale : i18nContentSupport.getLocales()) {
                String uri = createURI(currentPage, locale);
                String label = StringUtils.capitalize(locale.getDisplayLanguage(locale));
                if(StringUtils.isNotEmpty(locale.getCountry())){
                    label += " (" + StringUtils.capitalize(locale.getDisplayCountry()) + ")";
                }
                select.setOptions(label, uri);
            }

            return select;
        }
        return null;
    }

    protected String createURI(Content currentPage, Locale locale) {
        // we are going to change the context language, this is ugly but is safe as only the current Thread is modified
        Locale currentLocale = i18nContentSupport.getLocale();
        String uri=null;
        try {
            // this is going to set the local in the aggregation state and hence wont change the i18nSupport object itself
            i18nContentSupport.setLocale(locale);
            uri = LinkUtil.createAbsoluteLink(currentPage);
        }
        // make sure that we always reset to the original locale
        finally{
            i18nContentSupport.setLocale(currentLocale);
        }
        return uri;
    }

    @Override
    public void i18nIze(Dialog dialog) {
        // TODO: should this be set in the aggregation state?
        Locale locale = LocaleUtils.toLocale(dialog.getConfigValue("locale", null));
        boolean isFallbackLanguage = i18nContentSupport.getFallbackLocale().equals(locale);

        if (isEnabled() && i18nContentSupport.isEnabled() && locale != null) {
            List<DialogControlImpl> tabs = dialog.getSubs();
            for (DialogControlImpl tab : tabs) {
                List<DialogControlImpl> controls = tab.getSubs();
                for (DialogControlImpl control : controls) {
                    boolean i18n = BooleanUtil.toBoolean(control.getConfigValue("i18n"), false);
                    if (i18n) {
                        if(!isFallbackLanguage){
                            String newName = control.getName() + "_" + locale.toString();
                            control.setName(newName);
                        }
                        String translatedLabel = control.getMessage(control.getLabel());
                        control.setLabel(translatedLabel + " (" + locale.toString() + ")");
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
