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
package info.magnolia.rendering.template.configured;

import info.magnolia.rendering.generator.Generator;
import info.magnolia.rendering.template.AutoGenerationConfiguration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Implementation of {@link AutoGenerationConfiguration}.
 * @version $Id$
 *
 */
public class ConfiguredAutoGeneration implements AutoGenerationConfiguration<Map<?, ?>> {
    private Map<String, Map<?, ?>> content = new HashMap<String, Map<?, ?>>();
    private Class<Generator<AutoGenerationConfiguration>> generatorClass;

    @Override
    public Map<String, Map<?, ?>> getContent() {
        return content;
    }

    @Override
    public Class<Generator<AutoGenerationConfiguration>> getGeneratorClass() {
        return generatorClass;
    }

    public void setContent(Map<String, Map<?, ?>> content) {
        this.content = content;
    }

    public void setGeneratorClass(Class<Generator<AutoGenerationConfiguration>> generatorClass) {
        this.generatorClass = generatorClass;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
