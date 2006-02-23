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
package info.magnolia.cms.servlets;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.Dispatcher;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.VirtualMap;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Cache;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.beans.runtime.WebContextImpl;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.CacheProcess;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the main http servlet which will be called for any resource request this servlet will dispacth or process
 * requests according to their nature -- all resource requests will go to ResourceDispatcher -- all page requests will
 * be handed over to the defined JSP or Servlet (template). Updated to allow caching of virtual URI's
 * @author Sameer Charles
 * @version 2.1
 */
public class EntryServlet extends HttpServlet {

    /**
     * Request parameter: the INTERCEPT holds the name of an administrative action to perform.
     */
    public static final String INTERCEPT = "mgnlIntercept"; //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(EntryServlet.class);

    /**
     * The default request interceptor path, defined in web.xml.
     */
    private static final String REQUEST_INTERCEPTOR = "/RequestInterceptor"; //$NON-NLS-1$

    /**
     * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     * @param request HttpServletRequest
     * @return last modified time in miliseconds since 1st Jan 1970 GMT
     */
    public long getLastModified(HttpServletRequest request) {
        return info.magnolia.cms.beans.runtime.Cache.getCreationTime(request);
    }

    /**
     * Allow caching of this specific resource. This method always returns <code>true</code>, and it's here to allow
     * an easy plug-in of application-specific logic by extending EntrySrvlet. If you need to disable cache for specific
     * requests (not based on the request URI, since this is configurable from adminCentral) you can override this
     * method.
     * @param req HttpServletRequest
     * @return <code>true</code> if the page returned by this request can be cached, <code>false</code> if cache
     * should not be used.
     */
    protected boolean allowCaching(HttpServletRequest req) {
        return true;
    }

    /**
     * All HTTP/s requests are handled here.
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if (ConfigLoader.isBootstrapping()) {
            // @todo a nice page, with the log content...
            res.getWriter().write("Magnolia bootstrapping has failed, check bootstrap.log in magnolia/logs"); //$NON-NLS-1$
            return;
        }

        try {
            // Initialize magnolia context
            initializeContext(req);
            if (isAuthorized(req, res)) {

                // allowCaching allows users to plug-in application specific logic
                boolean cacheable = allowCaching(req);

                // try to stream from cache first
                if (cacheable && Cache.isCached(req)) {
                    if (CacheHandler.streamFromCache(req, res)) {
                        return; // if success return
                    }
                }

                if (redirect(req, res)) {

                    // it's a valid request cache it
                    if (cacheable) {
                        this.cacheRequest(req);
                    }
                    return;
                }
                intercept(req, res);
                // aggregate content
                Aggregator aggregator = new Aggregator(req, res);
                boolean success = aggregator.collect();
                if (success) {
                    try {
                        Dispatcher.dispatch(req, res, getServletContext());
                    }
                    catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    this.cacheRequest(req);
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("Resource not found, redirecting request for [" //$NON-NLS-1$
                            + req.getRequestURI()
                            + "] to 404 URI"); //$NON-NLS-1$
                    }

                    if (!res.isCommitted()) {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                    else {
                        log.info("Unable to redirect to 404 page, response is already committed"); //$NON-NLS-1$
                    }
                }
            }
        }
        catch (AccessDeniedException e) {
            // don't log AccessDenied as errors, it can happen...
            log.warn(e.getMessage());
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * All requests are handles by get handler.
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doGet(req, res);
    }

    /**
     * Initialize Magnolia context. It creates a context and initialize the user only if these do not exist yet. <b>Note</b>:
     * the implementation may get changed
     * @param request the current request
     */
    protected void initializeContext(HttpServletRequest request) {
        //if (MgnlContext.getInstance() == null) {
            Context ctx = new WebContextImpl(request);
            MgnlContext.setInstance(ctx);
        //}
    }

    /**
     * Uses access manager to authorise this request.
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean true if read access is granted
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    protected boolean isAuthorized(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (MgnlContext.getAccessManager(ContentRepository.WEBSITE) != null) {
            String path = StringUtils.substringBefore(Path.getURI(req), "."); //$NON-NLS-1$
            if (!MgnlContext.getAccessManager(ContentRepository.WEBSITE).isGranted(path, Permission.READ)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        return true;
    }

    /**
     * Redirect based on the mapping in config/server/.node.xml
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if request has been redirected, <code>false</code> otherwise
     */
    private boolean redirect(HttpServletRequest request, HttpServletResponse response) {
        String uri = this.getURIMap(request);
        if (StringUtils.isNotEmpty(uri)) {
            try {
                request.getRequestDispatcher(uri).forward(request, response);
            }
            catch (Exception e) {
                log.error("Failed to forward - " + uri); //$NON-NLS-1$
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    /**
     * Attach Interceptor servlet if interception needed
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    private void intercept(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter(INTERCEPT) != null) {
            try {
                request.getRequestDispatcher(REQUEST_INTERCEPTOR).include(request, response);
            }
            catch (Exception e) {
                log.error("Failed to Intercept"); //$NON-NLS-1$
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return URI mapping as in ServerInfo
     * @param request HttpServletRequest
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualMap.getURIMapping(StringUtils.substringAfter(request.getRequestURI(), request.getContextPath()));
    }

    /**
     * Caches this request if level-1 cache is active and request is part of cacheable mapping
     * @param request HttpServletRequest
     */
    private void cacheRequest(HttpServletRequest request) {
        if (!Cache.isInCacheProcess(request) && info.magnolia.cms.beans.config.Cache.isCacheable(request)) {
            CacheProcess cache = new CacheProcess(new ClonedRequest(request));
            cache.start();
        }
    }

    /**
     * Simply a copy of the original request used by CacheProcess.
     */
    private static class ClonedRequest implements HttpServletRequest {

        /**
         * Request attributes.
         */
        private Map attributes = new HashMap();

        /**
         * Request headers.
         */
        private Map headers = new HashMap();

        /**
         * Parameters.
         */
        private Map parameters;

        /**
         * Request URI.
         */
        private String uri;

        /**
         * Request context path.
         */
        private String contextPath;

        /**
         * Character encoding.
         */
        private String characterEncoding;

        /**
         * Server port.
         */
        private int serverPort;

        /**
         * Request scheme (http or https).
         */
        private String scheme;

        /**
         * Server name.
         */
        private String serverName;

        /**
         * Server name.
         */
        private String method;

        /**
         * Instantiate a new ClonedRequest, copying needed attributes from the original request.
         * @param originalRequest wrapped HttpServletRequest
         */
        public ClonedRequest(HttpServletRequest originalRequest) {
            this.contextPath = originalRequest.getContextPath();
            // remember URI
            this.uri = originalRequest.getRequestURI();
            this.characterEncoding = originalRequest.getCharacterEncoding();
            // copy neccessary attributes
            this.attributes.put(Aggregator.EXTENSION, originalRequest.getAttribute(Aggregator.EXTENSION));
            this.attributes.put(Aggregator.ACTPAGE, originalRequest.getAttribute(Aggregator.ACTPAGE));

            // copy headers
            String authHeader = originalRequest.getHeader("Authorization"); //$NON-NLS-1$
            if (authHeader != null) {
                this.headers.put("Authorization", authHeader); //$NON-NLS-1$
            }

            // needed if cacheDomain is not set
            this.serverPort = originalRequest.getServerPort();
            this.scheme = originalRequest.getScheme();
            this.serverName = originalRequest.getServerName();
            this.method = originalRequest.getMethod();
            this.parameters = originalRequest.getParameterMap();
        }

        /**
         * @see javax.servlet.http.HttpServletRequest#getRequestURI()
         */
        public String getRequestURI() {
            return uri;
        }

        /**
         * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
         */
        public String getHeader(String key) {
            return (String) this.headers.get(key);
        }

        /**
         * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
         */
        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getContextPath()
         */
        public String getContextPath() {
            return this.contextPath;
        }

        /**
         * @see javax.servlet.ServletRequest#getCharacterEncoding()
         */
        public String getCharacterEncoding() {
            return this.characterEncoding;
        }

        /**
         * @see javax.servlet.ServletRequest#getScheme()
         */
        public String getScheme() {
            return this.scheme;
        }

        /**
         * @see javax.servlet.ServletRequest#getServerName()
         */
        public String getServerName() {
            return this.serverName;
        }

        /**
         * @see javax.servlet.ServletRequest#getServerPort()
         */
        public int getServerPort() {
            return this.serverPort;
        }

        /**
         * @see javax.servlet.http.HttpServletRequest#getMethod()
         */
        public String getMethod() {
            return this.method;
        }

        /**
         * @see javax.servlet.ServletRequest#getParameterMap()
         */
        public Map getParameterMap() {
            return this.parameters;
        }

        /**
         * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
         */
        public String getParameter(String s) {
            return (String) this.parameters.get(s);
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getRequestURL()
         */
        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getServletPath()
         */
        public String getServletPath() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
         */
        public HttpSession getSession(boolean b) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getSession()
         */
        public HttpSession getSession() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
         */
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
         */
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
         */
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
         */
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getAuthType()
         */
        public String getAuthType() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getCookies()
         */
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
         */
        public long getDateHeader(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
         */
        public Enumeration getHeaders(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
         */
        public Enumeration getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
         */
        public int getIntHeader(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getPathInfo()
         */
        public String getPathInfo() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
         */
        public String getPathTranslated() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getQueryString()
         */
        public String getQueryString() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
         */
        public String getRemoteUser() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
         */
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
         */
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getAttributeNames()
         */
        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
         */
        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getContentLength()
         */
        public int getContentLength() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getContentType()
         */
        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getInputStream()
         */
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getParameterNames()
         */
        public Enumeration getParameterNames() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
         */
        public String[] getParameterValues(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getProtocol()
         */
        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getReader()
         */
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getRemoteAddr()
         */
        public String getRemoteAddr() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getRemoteHost()
         */
        public String getRemoteHost() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String s, Object o) {
            this.attributes.put(s, o);
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String s) {
            this.attributes.remove(s);
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getLocale()
         */
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getLocales()
         */
        public Enumeration getLocales() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#isSecure()
         */
        public boolean isSecure() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
         */
        public RequestDispatcher getRequestDispatcher(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
         */
        public String getRealPath(String s) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getRemotePort()
         */
        public int getRemotePort() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getLocalName()
         */
        public String getLocalName() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getLocalAddr()
         */
        public String getLocalAddr() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws UnsupportedOperationException.
         * @see javax.servlet.ServletRequest#getLocalPort()
         */
        public int getLocalPort() {
            throw new UnsupportedOperationException();
        }

    }

}
