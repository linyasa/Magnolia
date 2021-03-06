/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * An HttpClientCallback implementation which redirects to a configured path or URL.
 * This can be useful, for instance, in SSO contexts where the login screen is handled by
 * a different application, or if one wants to simply hide the login form from a public instance
 * using a fronting server configuration.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RedirectClientCallback extends AbstractHttpClientCallback {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedirectClientCallback.class);

    private String location = "/.magnolia";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        String target;
        if (location.startsWith("/")) {
            target = request.getContextPath() + location;
        } else {
            target = location;
        }
        if (request.getRequestURI().equals(target)) {
            log.debug("Unauthorized, can't redirect further, we're already at {}", target);
            return;
        }
        log.debug("Unauthorized, will redirect to {}", target);

        try {
            // formats the target location with the request url, to allow passing it has a parameter, for instance.
            String url = request.getRequestURL().toString();
            if (MgnlContext.getParameters() != null && !MgnlContext.getParameters().isEmpty()) {
                Set<String> keys = MgnlContext.getParameters().keySet();
                String parameterString = "";
                String[] values;
                for (String key : keys) {
                    // we don't want to pass along the mgnlLogut parameter on a
                    // login action
                    if (!key.equals("mgnlLogout")) {
                        values = MgnlContext.getParameterValues(key);
                        for (int i = 0; i < values.length; i++) {
                            parameterString += key + "=" + values[i] + "&";
                        }
                    }
                }
                if (StringUtils.isNotBlank(parameterString)) {
                    // cut off trailing "&"
                    url += "?" + StringUtils.substringBeforeLast(parameterString, "&");
                    target  += "?" + StringUtils.substringBeforeLast(parameterString, "&");
                }
            }
            final String encodedUrl = URLEncoder.encode(url, "UTF-8");
            


            final String formattedTarget = MessageFormat.format(target, encodedUrl);
            response.sendRedirect(formattedTarget);
        } catch (IOException e) {
            throw new RuntimeException("Can't redirect to " + target + " : " + e.getMessage(), e);
        }
    }

    // ------- configuration methods

    /**
     * The location field as sent to the browser. If the value starts with a /, it is preceded
     * by the context path of the current request. The default value is "/.magnolia".
     * If you need to the current request location in an external login form, you can use the {0} tag:
     * a value of "http://sso.mycompany.com/login/?backto={0}" will pass the current request url as the "backto"
     * parameter to the location url.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    protected String getLocation() {
        return location;
    }
}
