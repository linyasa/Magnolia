/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ConfigUtil;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNameMap;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * $Id$
 */
public final class ContentRepository {

    /**
     * default repository ID's.
     */
    public static final String WEBSITE = "website"; //$NON-NLS-1$

    public static final String USERS = "users"; //$NON-NLS-1$

    public static final String USER_ROLES = "userroles"; //$NON-NLS-1$

    public static final String USER_GROUPS = "usergroups"; //$NON-NLS-1$

    public static final String CONFIG = "config"; //$NON-NLS-1$

    public static final String DEFAULT_WORKSPACE = "default"; //$NON-NLS-1$

    public static final String VERSION_STORE = "mgnlVersion";

    /**
     * magnolia namespace.
     */
    public static final String NAMESPACE_PREFIX = "mgnl"; //$NON-NLS-1$

    public static final String NAMESPACE_URI = "http://www.magnolia.info/jcr/mgnl"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentRepository.class);

    /**
     * repository element string.
     */
    private static final String ELEMENT_REPOSITORY = "Repository"; //$NON-NLS-1$

    private static final String ELEMENT_REPOSITORYMAPPING = "RepositoryMapping"; //$NON-NLS-1$

    private static final String ELEMENT_PARAM = "param"; //$NON-NLS-1$

    private static final String ELEMENT_WORKSPACE = "workspace"; //$NON-NLS-1$

    /**
     * Attribute names.
     */
    private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup"; //$NON-NLS-1$

    private static final String ATTRIBUTE_PROVIDER = "provider"; //$NON-NLS-1$

    private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

    private static final String ATTRIBUTE_REPOSITORY_NAME = "repositoryName"; //$NON-NLS-1$

    private static final String ATTRIBUTE_WORKSPACE_NAME = "workspaceName"; //$NON-NLS-1$

    /**
     * repository user.
     */
    public static final String REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");

    /**
     * repository default password
     */
    public static final String REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");

    /**
     * All available repositories store.
     */
    private static Map repositories = new Hashtable();

    /**
     * JCR providers as mapped in repositories.xml.
     */
    private static Map repositoryProviders = new Hashtable();

    /**
     * Repositories configuration as defined in repositories mapping file via attribute
     * <code>magnolia.repositories.config</code>.
     */
    private static Map repositoryMapping = new Hashtable();

    /**
     * holds all repository names as configured in repositories.xml
     */
    private static Map repositoryNameMap = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private ContentRepository() {
        // unused constructor
    }

    /**
     * loads all configured repository using ID as Key, as configured in repositories.xml.
     *
     * <pre>
     * &lt;Repository name="website"
     *                id="website"
     *                provider="info.magnolia.jackrabbit.ProviderImpl"
     *                loadOnStartup="true" >
     *   &lt;param name="configFile"
     *             value="WEB-INF/config/repositories/website.xml"/>
     *   &lt;param name="repositoryHome"
     *             value="repositories/website"/>
     *   &lt;param name="contextFactoryClass"
     *             value="org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory"/>
     *   &lt;param name="providerURL"
     *             value="localhost"/>
     *   &lt;param name="id" value="website"/>
     * &lt;/Repository>
     *</pre>
     */
    public static void init() {
        log.info("System : loading JCR"); //$NON-NLS-1$
        repositories.clear();
        try {
            loadRepositories();
            log.info("System : JCR loaded"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Verify the initialization state of all the repositories. This methods returns <code>false</code> only if
     * <strong>all</strong> the repositories are empty (no node else than the root one).
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     * content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     */
    public static boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        Iterator repositoryNames = getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
            if (checkIfInitialized(repository)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param repository
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    public static boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        if (log.isDebugEnabled()) {
            log.debug("Checking [" + repository + "] repository."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        HierarchyManager hm = getHierarchyManager(repository);

        if (hm == null) {
            throw new RuntimeException("Repository [" + repository + "] not loaded"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Content startPage = hm.getRoot();

        // return any kind of children
        Collection children = startPage.getChildren(new Content.ContentFilter(){
            public boolean accept(Content content) {
                return (!content.getName().startsWith("jcr:") && !content.getName().startsWith("rep:"));
            }
        });

        if (!children.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Content found in [" + repository + "]."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return true;
        }
        return false;
    }

    /**
     * Re-load all configured repositories.
     * @see #init()
     */
    public static void reload() {
        log.info("System : reloading JCR"); //$NON-NLS-1$
        ContentRepository.init();
    }

    /**
     * Load repository mappings and params using repositories.xml
     * @throws Exception
     */
    private static void loadRepositories() throws Exception {
        Document document = buildDocument();
        Element root = document.getRootElement();
        loadRepositoryNameMap(root);
        Collection repositoryElements = root.getChildren(ContentRepository.ELEMENT_REPOSITORY);
        Iterator children = repositoryElements.iterator();
        while (children.hasNext()) {
            Element element = (Element) children.next();
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            String load = element.getAttributeValue(ATTRIBUTE_LOAD_ON_STARTUP);
            String provider = element.getAttributeValue(ATTRIBUTE_PROVIDER);
            RepositoryMapping map = new RepositoryMapping();
            map.setName(name);
            map.setProvider(provider);
            boolean loadOnStartup = BooleanUtils.toBoolean(load);
            map.setLoadOnStartup(loadOnStartup);
            /* load repository parameters */
            Iterator params = element.getChildren(ELEMENT_PARAM).iterator();
            Map parameters = new Hashtable();
            while (params.hasNext()) {
                Element param = (Element) params.next();
                String value = param.getAttributeValue(ATTRIBUTE_VALUE);
                parameters.put(param.getAttributeValue(ATTRIBUTE_NAME), value);
            }
            map.setParameters(parameters);
            List workspaces = element.getChildren(ELEMENT_WORKSPACE);
            if (workspaces != null && !workspaces.isEmpty()) {
                Iterator wspIterator = workspaces.iterator();
                while (wspIterator.hasNext()) {
                    Element workspace = (Element) wspIterator.next();
                    String wspName = workspace.getAttributeValue(ATTRIBUTE_NAME);
                    log.info("Loading workspace:" + wspName);
                    map.addWorkspace(wspName);
                }
            }
            else {
                map.addWorkspace(DEFAULT_WORKSPACE);
            }
            ContentRepository.repositoryMapping.put(name, map);
            try {
                loadRepository(map);
            }
            catch (Exception e) {
                log.error("System : Failed to load JCR \"" + map.getName() + "\" " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * load repository name mapping
     * @param root element of repositories.xml
     */
    private static void loadRepositoryNameMap(Element root) {
        Element repositoryMapping = root.getChild(ContentRepository.ELEMENT_REPOSITORYMAPPING);
        Iterator children = repositoryMapping.getChildren().iterator();
        while (children.hasNext()) {
            Element nameMap = (Element) children.next();
            addMappedRepositoryName(
                    nameMap.getAttributeValue(ATTRIBUTE_NAME),
                    nameMap.getAttributeValue(ATTRIBUTE_REPOSITORY_NAME),
                    nameMap.getAttributeValue(ATTRIBUTE_WORKSPACE_NAME));
        }
    }

    /**
     * This method initializes the repository. You must not call this method twice.
     * @param map
     * @throws RepositoryNotInitializedException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("System : loading JCR {}", map.getName()); //$NON-NLS-1$
        Provider handlerClass = (Provider) ClassUtil.newInstance(map.getProvider());
        handlerClass.init(map);
        Repository repository = handlerClass.getUnderlineRepository();
        ContentRepository.repositories.put(map.getName(), repository);
        ContentRepository.repositoryProviders.put(map.getName(), handlerClass);
        if (map.isLoadOnStartup()) {
            // load hierarchy managers for each workspace
            Iterator workspaces = map.getWorkspaces().iterator();
            while (workspaces.hasNext()) {
                String wspID = (String) workspaces.next();
                registerNameSpacesAndNodeTypes(repository, wspID, map, handlerClass);
            }
        }
    }

    /**
     * @param repositoryId
     * @param workspaceId
     * @throws RepositoryException
     * */
    public static void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        log.info("System : loading workspace {}", workspaceId); //$NON-NLS-1$
        if(!repositoryNameMap.containsKey(workspaceId)){
            addMappedRepositoryName(workspaceId, repositoryId, workspaceId);
        }
        RepositoryMapping map = getRepositoryMapping(repositoryId);
        if(!map.getWorkspaces().contains(workspaceId)){
            map.addWorkspace(workspaceId);
        }
        Provider provider = getRepositoryProvider(repositoryId);
        provider.registerWorkspace(workspaceId);
        registerNameSpacesAndNodeTypes(getRepository(repositoryId),  workspaceId, map, provider);
    }

    /**
     * Load hierarchy manager for the specified repository and workspace
     */
    private static void registerNameSpacesAndNodeTypes(Repository repository, String wspID, RepositoryMapping map,
        Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(REPOSITORY_USER, REPOSITORY_PSWD.toCharArray());
            Session session = WorkspaceAccessUtil.getInstance().createRepositorySession(sc, wspID);
            provider.registerNamespace(NAMESPACE_PREFIX, NAMESPACE_URI, session.getWorkspace());
            provider.registerNodeTypes();
        }
        catch (RepositoryException re) {
            log.error("System : Failed to initialize hierarchy manager for JCR {}", map.getName()); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    /**
     * Configures and returns a AccessManager with system permissions
     * @return The system AccessManager
     * @deprecated uses MglnContext.getSystemContext()
     */
    public static AccessManager getAccessManager() {
        return MgnlContext.getSystemContext().getAccessManager(CONFIG);
    }

    /**
     * Builds JDOM document.
     * @return document
     * @throws IOException
     * @throws JDOMException
     */
    private static Document buildDocument() throws JDOMException, IOException {
        File source = Path.getRepositoriesConfigFile();
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate magnolia repositories config file at " //$NON-NLS-1$
                + source.getAbsolutePath());
        }

        return ConfigUtil.string2JDOM(ConfigUtil.replaceTokens(new FileInputStream(source)));
    }

    /**
     * Returns magnolia specific Repository name where this workspace is registered
     * within <Repository/>
     * */
    public static String getParentRepositoryName(String workspaceName) throws RepositoryException {
        Iterator values = repositoryNameMap.values().iterator();
        while (values.hasNext()) {
            RepositoryNameMap map = (RepositoryNameMap) values.next();
            if (workspaceName.equalsIgnoreCase(map.getWorkspaceName())) {
                return map.getRepositoryName();
            }
        }
        throw new RepositoryException("No mapping found for this repository in magnolia repositories.xml");
    }

    /**
     * Get mapped repository name
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public static String getMappedRepositoryName(String name) {
        RepositoryNameMap nameMap = (RepositoryNameMap) ContentRepository.repositoryNameMap.get(name);
        if(nameMap==null){
            return name;
        }
        return nameMap.getRepositoryName();
    }

    /**
     * Get mapped workspace name
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public static String getMappedWorkspaceName(String name) {
        RepositoryNameMap nameMap = (RepositoryNameMap) ContentRepository.repositoryNameMap.get(name);
        if (nameMap == null) {
            return name;
        }
        return nameMap.getWorkspaceName();
    }

    /**
     * Add a mapped repository name
     * @param name
     * @param repositoryName
     */
    public static void addMappedRepositoryName(String name, String repositoryName) {
        addMappedRepositoryName(name, repositoryName, null);
    }

    /**
     * Add a mapped repository name
     * @param name
     * @param repositoryName
     * @param workspaceName
     */
    public static void addMappedRepositoryName(String name, String repositoryName, String workspaceName) {
        if (StringUtils.isEmpty(workspaceName)) workspaceName = name;
        RepositoryNameMap nameMap = new RepositoryNameMap(repositoryName, workspaceName);
        ContentRepository.repositoryNameMap.put(name, nameMap);
    }

    /**
     * Get default workspace name
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    public static String getDefaultWorkspace(String repositoryId) {
        RepositoryMapping mapping = getRepositoryMapping(repositoryId);
        if (mapping == null) {
            return DEFAULT_WORKSPACE;
        }
        Collection workspaces = mapping.getWorkspaces();
        if (workspaces.contains(getMappedWorkspaceName(repositoryId))) {
            return repositoryId;
        }
        return DEFAULT_WORKSPACE;
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     * @deprecated uses MglnContext.getSystemContext.getHierarchyManager()
     */
    public static HierarchyManager getHierarchyManager(String repositoryID) {
        return getHierarchyManager(repositoryID, getDefaultWorkspace(repositoryID));
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     * @deprecated uses MglnContext.getSystemContext.getHierarchyManager()
     */
    public static HierarchyManager getHierarchyManager(String repositoryID, String workspaceID) {
        return MgnlContext.getSystemContext().getHierarchyManager(repositoryID, workspaceID);
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public static Repository getRepository(String repositoryID) {
        Repository repository = (Repository) ContentRepository.repositories.get(repositoryID);
        if (repository == null) {
            String mappedRepositoryName = getMappedRepositoryName(repositoryID);
            if (mappedRepositoryName != null) {
                return (Repository) ContentRepository.repositories.get(mappedRepositoryName);
            }
        }
        return repository;
    }

    /**
     * Returns repository provider specified by the <code>repositoryID</code> as configured in repository config.
     */
    public static Provider getRepositoryProvider(String repositoryID) {

        Provider provider = (Provider) ContentRepository.repositoryProviders.get(repositoryID);

        if (provider == null) {
            String mappedRepositoryName = getMappedRepositoryName(repositoryID);
            if (mappedRepositoryName != null) {
                provider = (Provider) ContentRepository.repositoryProviders.get(mappedRepositoryName);
            }
        }
        return provider;
    }

    /**
     * returns repository mapping as configured.
     */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        String name = getMappedRepositoryName(repositoryID);
        if (name != null && ContentRepository.repositoryMapping.containsKey(name)) {
            return (RepositoryMapping) ContentRepository.repositoryMapping.get(getMappedRepositoryName(repositoryID));
        }
        log.debug("no mapping for the repository {}", repositoryID);
        return null;
    }

    /**
     * Returns <code>true</code> if a mapping for the given repository name does exist.
     */
    public static boolean hasRepositoryMapping(String repositoryID) {
        String name = getMappedRepositoryName(repositoryID);
        return name != null && ContentRepository.repositoryMapping.containsKey(name);
    }

    /**
     * Gets repository names array as configured in repositories.xml
     * @return repository names
     */
    public static Iterator getAllRepositoryNames() {
        return ContentRepository.repositoryNameMap.keySet().iterator();
    }

    /**
     * get internal workspace name
     * @param workspaceName
     * @return workspace name as configured in magnolia repositories.xml
     * */
    public static String getInternalWorkspaceName(String workspaceName) {
        Iterator keys = repositoryNameMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            RepositoryNameMap nameMap = (RepositoryNameMap) repositoryNameMap.get(key);
            if (nameMap.getWorkspaceName().equalsIgnoreCase(workspaceName)) return key;
        }
        log.error("No Repository/Workspace name mapping defined for "+workspaceName);
        return workspaceName;
    }

}
