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
package info.magnolia.cms.core;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * @version 2.0
 */
public class CacheProcess extends Thread {

    private static Logger log = Logger.getLogger(CacheProcess.class);

    private HttpServletRequest request;

    public CacheProcess(HttpServletRequest request) {
        this.request = request;
    }

    public void run() {
        try {
            CacheHandler.cacheURI(this.request);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
