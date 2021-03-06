/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.module.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of components as defined in module descriptor.
 *
 * @version $Id$
 * @see ModuleDefinition
 */
public class ComponentsDefinition {

    private String id;
    private List<ComponentDefinition> components = new ArrayList<ComponentDefinition>();
    private List<ConfigurerDefinition> configurers = new ArrayList<ConfigurerDefinition>();
    private List<TypeMappingDefinition> typeMappings = new ArrayList<TypeMappingDefinition>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ComponentDefinition> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentDefinition> components) {
        this.components = components;
    }

    public boolean addComponent(ComponentDefinition component) {
        return components.add(component);
    }

    public List<ConfigurerDefinition> getConfigurers() {
        return configurers;
    }

    public void setConfigurers(List<ConfigurerDefinition> configurers) {
        this.configurers = configurers;
    }

    public boolean addConfigurer(ConfigurerDefinition configurerDefinition) {
        return configurers.add(configurerDefinition);
    }

    public List<TypeMappingDefinition> getTypeMappings() {
        return typeMappings;
    }

    public void setTypeMappings(List<TypeMappingDefinition> typeMappings) {
        this.typeMappings = typeMappings;
    }

    public boolean addTypeMapping(TypeMappingDefinition typeMapping) {
        return typeMappings.add(typeMapping);
    }
}
