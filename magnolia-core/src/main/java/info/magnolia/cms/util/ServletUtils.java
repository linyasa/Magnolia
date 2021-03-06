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
package info.magnolia.cms.util;

import java.util.LinkedHashMap;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods for operations related to Servlet API.
 *
 * @author tmattsson
 * @see info.magnolia.cms.util.RequestHeaderUtil
 * @deprecated since 4.5 - use {@link info.magnolia.cms.util.ServletUtil} instead.
 */
@Deprecated
public abstract class ServletUtils {
    
    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = ServletUtil.FORWARD_REQUEST_URI_ATTRIBUTE;
    public static final String FORWARD_QUERY_STRING_ATTRIBUTE = ServletUtil.FORWARD_QUERY_STRING_ATTRIBUTE; 
    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE;
    public static final String ERROR_REQUEST_STATUS_CODE_ATTRIBUTE = ServletUtil.ERROR_REQUEST_STATUS_CODE_ATTRIBUTE;

    /**
     * Returns the init parameters for a {@link javax.servlet.ServletConfig} object as a Map, preserving the order in which they are exposed
     * by the {@link javax.servlet.ServletConfig} object.
     */
    public static LinkedHashMap<String, String> initParametersToMap(ServletConfig config) {
        return ServletUtil.initParametersToMap(config);
    }

    /**
     * Returns the init parameters for a {@link javax.servlet.FilterConfig} object as a Map, preserving the order in which they are exposed
     * by the {@link javax.servlet.FilterConfig} object.
     */
    public static LinkedHashMap<String, String> initParametersToMap(FilterConfig config) {
        return ServletUtil.initParametersToMap(config);
    }

    /**
     * Finds a request wrapper object inside the chain of request wrappers.
     */
    public static <T extends ServletRequest> T getWrappedRequest(ServletRequest request, Class<T> clazz) {
        return ServletUtil.getWrappedRequest(request, clazz);
    }

    /**
     * Returns true if the request has a content type that indicates that is a multipart request.
     */
    public static boolean isMultipart(HttpServletRequest request) {
        return ServletUtil.isMultipart(request);
    }

    /**
     * Returns true if the request is currently processing a forward operation. This method will return false after
     * an include operation has begun and will return true after that include operations has completed.
     */
    public static boolean isForward(HttpServletRequest request) {
        return ServletUtil.isForward(request);
    }

    /**
     * Returns true if the request is currently processing an include operation.
     */
    public static boolean isInclude(HttpServletRequest request) {
        return request.getAttribute(ServletUtil.INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
    }

    /**
     * Returns true if the request is rendering an error page, either due to a call to sendError() or an exception
     * being thrown in a filter or a servlet that reached the container. Will return true also after an include() or
     * forward() while rendering the error page.
     */
    public static boolean isError(HttpServletRequest request) {
        return ServletUtil.isError(request);
    }

    /**
     * Returns the dispatcher type of the request, the dispatcher type is defined to be identical to the semantics of
     * filter mappings in web.xml.
     */
    public static DispatcherType getDispatcherType(HttpServletRequest request) {
        return ServletUtil.getDispatcherType(request);
    }

    /**
     * Returns the original request uri. The If the request has been forwarded it finds the original request uri from
     * request attributes. The returned uri is not decoded.
     */
    public static String getOriginalRequestURI(HttpServletRequest request) {
        return ServletUtil.getOriginalRequestURI(request);
    }

    /**
     * Returns the original request url. If the request has been forwarded it reconstructs the url from  request
     * attributes. The returned url is not decoded.
     */
    public static String getOriginalRequestURLIncludingQueryString(HttpServletRequest request) {
        return ServletUtil.getOriginalRequestURLIncludingQueryString(request);
    }

    /**
     * Returns the request uri for the request. If the request is an include it will return the uri being included. The
     * returned uri is not decoded.
     */
    public static String getRequestUri(HttpServletRequest request) {
        return ServletUtil.getRequestUri(request);
    }
}
