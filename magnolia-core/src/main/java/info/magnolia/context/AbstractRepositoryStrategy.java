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
package info.magnolia.context;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.repository.RepositoryManager;


/**
 * Basic implementation of the <code>RepositoryAcquiringStrategy</code> providing storage of JCR sessions and hierarchy manager instances to extending classes.
 *
 * @version $Id$
 */
public abstract class AbstractRepositoryStrategy implements JCRSessionStrategy {
    private static final Logger log = LoggerFactory.getLogger(AbstractRepositoryStrategy.class);

    private final Map<String, Session> jcrSessions = new HashMap<String, Session>();

    protected final RepositoryManager repositoryManager;

    @Inject
    protected AbstractRepositoryStrategy(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    public Session getSession(String workspaceName) throws LoginException, RepositoryException {
        Session jcrSession = jcrSessions.get(workspaceName);

        if (jcrSession == null) {
            log.debug("creating jcr session {} by thread {}", workspaceName, Thread.currentThread().getName());

            jcrSession = internalGetSession(workspaceName);
            jcrSessions.put(workspaceName, jcrSession);
        }

        return jcrSession;
    }

    protected abstract Session internalGetSession(String workspaceName) throws RepositoryException;

    protected void release(boolean checkObservation) {
        log.debug("releasing jcr sessions");
        for (Session session : jcrSessions.values()) {
            releaseSession(session, checkObservation);
        }
        jcrSessions.clear();
    }

    protected void releaseSession(final Session session, boolean checkObservation) {
        final String workspaceName = session.getWorkspace().getName();
        if (session.isLive()) {
            try {
                final ObservationManager observationManager = session.getWorkspace().getObservationManager();
                final EventListenerIterator listeners = observationManager.getRegisteredEventListeners();
                if (!checkObservation || !listeners.hasNext()) {
                    session.logout();
                    log.debug("logged out jcr session: {} by thread {}", session, Thread.currentThread().getName());

                } else {
                    log.warn("won't close session because of registered observation listener {}", workspaceName);
                    if (log.isDebugEnabled()) {
                        while (listeners.hasNext()) {
                            EventListener listener = listeners.nextEventListener();
                            log.debug("registered listener {}", listener);
                        }
                    }
                }
            }
            catch (RepositoryException e) {
                log.error("can't check if event listeners are registered", e);
            }
        } else {
            log.warn("session has been already closed {}", workspaceName);
        }
    }

    /**
     * Returns the number of sessions managed by this strategy.
     */
    protected int getLocalSessionCount() {
        return jcrSessions.size();
    }
}
