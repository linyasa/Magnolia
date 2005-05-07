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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Cache {

    private static final String CONFIG_PATH = "server/cache/level1";

    private static final String CACHE_MAPPING_NODE = "URI";

    private static final String COMPRESSION_LIST_NODE = "compression";

    private static final String ALLOW_LIST = "allow";

    private static final String DENY_LIST = "deny";

    private static final String ACTIVE = "active";

    private static final String DOMAIN = "domain";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Cache.class);

    private static Map cachedCacheableURIMapping = new Hashtable();

    /**
     * Compression wont work for these pre compressed formats.
     */
    private static final Map COMPRESSION_LIST = new Hashtable();

    private static boolean isCacheable;

    private static String domain;

    /**
     * Utility class, don't instantiate.
     */
    private Cache() {
        // unused
    }

    protected static void init() {
        cachedCacheableURIMapping.clear();
        COMPRESSION_LIST.clear();
        log.info("Config : loading cache mapping");
        try {
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(CONFIG_PATH);
            isCacheable = startPage.getNodeData(ACTIVE).getBoolean();
            if (isCacheable) {
                domain = startPage.getNodeData(DOMAIN).getString();
                Content contentNode = startPage.getContent(CACHE_MAPPING_NODE + "/" + ALLOW_LIST);
                cacheCacheableURIMappings(contentNode, true);
                contentNode = startPage.getContent(CACHE_MAPPING_NODE + "/" + DENY_LIST);
                cacheCacheableURIMappings(contentNode, false);
                Content compressionListNode = startPage.getContent(COMPRESSION_LIST_NODE);
                updateCompressionList(compressionListNode);
                // todo sort assending so there wont be too much work on comparing
            }
            log.info("Config : cache mapping loaded");
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load cache mapping or no mapping defined");
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : reloading cache mapping");
        Cache.init();
    }

    /**
     * @param nodeList to be added in cache
     */
    private static void cacheCacheableURIMappings(Content nodeList, boolean allow) {
        if (nodeList == null) {
            return;
        }
        Iterator it = nodeList.getChildren().iterator();
        while (it.hasNext()) {
            Content container = (Content) it.next();
            NodeData uri = container.getNodeData("URI");
            UrlPattern p = new SimpleUrlPattern(uri.getString());
            cachedCacheableURIMapping.put(p, BooleanUtils.toBooleanObject(allow));
        }
        try {
            CacheHandler.validatePath(CacheHandler.CACHE_DIRECTORY);
        }
        catch (Exception e) {
            log.error("Failed to validate cache directory location");
            log.error(e.getMessage(), e);
        }
    }

    private static void updateCompressionList(Content list) {
        if (list == null) {
            return;
        }
        Iterator it = list.getChildren().iterator();
        while (it.hasNext()) {
            Content node = (Content) it.next();
            COMPRESSION_LIST.put(node.getNodeData("extension").getString(), node.getNodeData("type").getString());
        }
    }

    public static boolean applyCompression(String key) {
        return COMPRESSION_LIST.containsKey(key.trim().toLowerCase());
    }

    /**
     * If this instance can be cached. todo check for Level1 and Level2 caching.
     * @return <code>true</code> if this instance can be cached
     */
    public static boolean isCacheable() {
        return isCacheable;
    }

    public static String getDomain() {
        return domain;
    }

    /**
     * @return true if the requested URI can be added to cache
     */
    public static boolean isCacheable(HttpServletRequest request) {
        // first check for MIMEMappings, extension must exist otherwise its a fake request

        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(Path.getExtension(request)))) {
            return false;
        }
        Iterator listEnum = cachedCacheableURIMapping.keySet().iterator();

        String uri = Path.getURI(request);
        boolean isAllowed = false;
        int lastMatchedPatternlength = 0;

        while (listEnum.hasNext()) {
            UrlPattern p = (UrlPattern) listEnum.next();
            if (p.match(uri)) {

                int patternLength = p.getLength();
                if (lastMatchedPatternlength < patternLength) {
                    lastMatchedPatternlength = patternLength;
                    isAllowed = ((Boolean) cachedCacheableURIMapping.get(p)).booleanValue();
                }
            }
        }
        return isAllowed;
    }

}
