/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.template.registry;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.rendering.template.TemplateDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The central registry of all {@link TemplateDefinition}s.
 *
 * @version $Id$
 */
public class TemplateDefinitionRegistry {

    // TODO should be an interface

    private static final Logger log = LoggerFactory.getLogger(TemplateDefinitionRegistry.class);

    private static final String DELETED_PAGE_TEMPLATE = "mgnlDeleted";

    private final Map<String, TemplateDefinitionProvider> providers = new HashMap<String, TemplateDefinitionProvider>();

    public void registerTemplateDefinition(TemplateDefinitionProvider provider) throws TemplateDefinitionRegistrationException {
        String id = provider.getId();
        synchronized (providers) {
            if (providers.containsKey(id)) {
                throw new TemplateDefinitionRegistrationException("Template definition already registered for the id [" + id + "]");
            }
            providers.put(id, provider);
        }
    }

    public void unregister(String id) {
        synchronized (providers) {
            providers.remove(id);
        }
    }

    public Set<String> removeAndRegister(Collection<String> remove, Collection<TemplateDefinitionProvider> providers2) {
        synchronized (providers) {
            final Set<String> ids = new HashSet<String>();
            for (String id : remove) {
                providers.remove(id);
            }
            for (TemplateDefinitionProvider provider : providers2) {
                String id = provider.getId();
                if (providers.containsKey(id)) {
                    // TODO log
                } else {
                    providers.put(id, provider);
                }
                ids.add(provider.getId());
            }
            return ids;
        }
    }

    public TemplateDefinition getTemplateDefinition(String id) throws TemplateDefinitionRegistrationException {

        TemplateDefinitionProvider templateDefinitionProvider;
        synchronized (providers) {
            templateDefinitionProvider = providers.get(id);
        }
        if (templateDefinitionProvider == null) {
            throw new TemplateDefinitionRegistrationException("No TemplateDefinition registered for id: " + id);
        }
        TemplateDefinition templateDefinition = templateDefinitionProvider.getTemplateDefinition();
        templateDefinition.setId(id);
        return templateDefinition;
    }

    public Collection<TemplateDefinition> getTemplateDefinitions() {
        Collection<TemplateDefinition> templateDefinitions = new ArrayList<TemplateDefinition>();
        synchronized (providers) {
            for (Map.Entry<String, TemplateDefinitionProvider> entry : providers.entrySet()) {
                String id = entry.getKey();
                TemplateDefinitionProvider provider = entry.getValue();
                try {
                    TemplateDefinition templateDefinition = provider.getTemplateDefinition();
                    templateDefinition.setId(id);
                    templateDefinitions.add(templateDefinition);
                } catch (TemplateDefinitionRegistrationException e) {
                    // one failing provider is no reason to not show any templates
                    log.error("Failed to read template definition from " + provider + ".", e);
                }
            }
        }
        return templateDefinitions;
    }

    // TODO move this to an independent template availability component
    public Collection<TemplateDefinition> getAvailableTemplates(Node content) {

        try {
            if (content != null && NodeUtil.hasMixin(content, MgnlNodeType.MIX_DELETED)) {
                return Collections.singleton(getTemplateDefinition(DELETED_PAGE_TEMPLATE));
            }
        } catch (RepositoryException e) {
            log.error("Failed to check node for deletion status.", e);
        } catch (TemplateDefinitionRegistrationException e) {
            log.error("Deleted content template is not correctly registered.", e);
        }

        final ArrayList<TemplateDefinition> availableTemplateDefinitions = new ArrayList<TemplateDefinition>();
        final Collection<TemplateDefinition> templateDefinitions = getTemplateDefinitions();
        for (TemplateDefinition templateDefinition : templateDefinitions) {
            if(templateDefinition.isAvailable(content)){
                availableTemplateDefinitions.add(templateDefinition);
            }
        }
        return availableTemplateDefinitions;
    }

    /**
     * Get the Template that could be used for the provided content as a default.
     */
    // TODO move this to an independent template availability component
    public TemplateDefinition getDefaultTemplate(Node content) {

        // try to use the same as the parent
        TemplateDefinition definition = null;
        try {
            definition = this.getTemplateDefinition(MetaDataUtil.getTemplate(content));
        } catch (TemplateDefinitionRegistrationException e) {
            log.warn("Can't resolve default template for node " + content, e);
        }
        if (definition != null && definition.isAvailable(content)){
            return definition;
        }

        // otherwise use the first available template
        Collection<TemplateDefinition> templates = getAvailableTemplates(content);
        if (!templates.isEmpty()) {
            return templates.iterator().next();
        }

        return null;
    }

}