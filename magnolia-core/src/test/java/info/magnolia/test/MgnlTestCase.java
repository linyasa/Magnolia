/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.junit.After;
import org.junit.Before;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.init.properties.ClasspathPropertySource;
import info.magnolia.init.properties.InitPathsPropertySource;
import info.magnolia.init.properties.ModulePropertiesSource;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyCheckerImpl;
import info.magnolia.objectfactory.configuration.ComponentConfiguration;
import info.magnolia.objectfactory.configuration.ComponentConfigurationBuilder;
import info.magnolia.objectfactory.configuration.ComponentFactoryConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ConfiguredComponentConfiguration;
import info.magnolia.objectfactory.configuration.ImplementationConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public abstract class MgnlTestCase {

    @Before
    public void setUp() throws Exception {
        // ignore mapping warnings
        org.apache.log4j.Logger.getLogger(ContentRepository.class).setLevel(org.apache.log4j.Level.ERROR);
        // don't clear all here. tests should be allowed to set their own implementations, fix the tests that do not clean after themselves instead!
        //ComponentsTestUtil.clear();
        setMagnoliaProperties();
        initDefaultImplementations();
        initContext();
    }

    protected void initContext() {
        MockUtil.initMockContext();
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

    protected void setMagnoliaProperties() throws Exception {
        setMagnoliaProperties(getMagnoliaPropertiesStream());
    }

    protected void setMagnoliaProperties(InputStream propertiesStream) throws IOException {
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties(propertiesStream));
    }

    protected InputStream getMagnoliaPropertiesStream() throws IOException {
        return this.getClass().getResourceAsStream(getMagnoliaPropertiesFileName());
    }

    protected String getMagnoliaPropertiesFileName() {
        return "/test-magnolia.properties";
    }

    protected void initDefaultImplementations() throws IOException, ModuleManagementException {
        final List<ModuleDefinition> modules = getModuleDefinitionsForTests();
        final ModuleRegistry mr = new ModuleRegistryImpl();
        ModuleManagerImpl mm = new ModuleManagerImpl(null, new FixedModuleDefinitionReader(modules), mr, new DependencyCheckerImpl());
        mm.loadDefinitions();

        final TestMagnoliaConfigurationProperties configurationProperties = new TestMagnoliaConfigurationProperties(
                new ModulePropertiesSource(mr),
                new ClasspathPropertySource("/test-magnolia.properties"),
                new InitPathsPropertySource(new TestMagnoliaInitPaths())
        );
        SystemProperty.setMagnoliaConfigurationProperties(configurationProperties);

        ComponentsTestUtil.setInstance(ModuleManager.class, mm);
        ComponentsTestUtil.setInstance(ModuleRegistry.class, mr);
        ComponentsTestUtil.setInstance(MagnoliaConfigurationProperties.class, configurationProperties);

        ComponentConfigurationBuilder configurationBuilder = new ComponentConfigurationBuilder();
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(mr, "system");
        configuration.combine(configurationBuilder.getComponentsFromModules(mr, "main"));

        // Content2BeanProcessorImpl uses dependency injection and since we don't have that with MockComponentProvider we
        // need to manually create this object and replace the component configuration read from core.xml
        final TypeMappingImpl typeMapping = new TypeMappingImpl();
        configuration.registerInstance(TypeMapping.class, typeMapping);
        configuration.registerInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));

        for (Map.Entry<Class, ComponentConfiguration> entry : configuration.getComponents().entrySet()) {
            ComponentConfiguration value = entry.getValue();
            if (value instanceof ImplementationConfiguration) {
                ImplementationConfiguration config = (ImplementationConfiguration) value;
                ComponentsTestUtil.setImplementation(config.getType(), config.getImplementation());
            } else if (value instanceof InstanceConfiguration) {
                InstanceConfiguration config = (InstanceConfiguration) value;
                ComponentsTestUtil.setInstance(config.getType(), config.getInstance());
            } else if (value instanceof ComponentFactoryConfiguration) {
                ComponentFactoryConfiguration config = (ComponentFactoryConfiguration) value;
                ComponentsTestUtil.setImplementation(config.getType(), config.getFactoryClass());
            } else if (value instanceof ConfiguredComponentConfiguration) {
                ConfiguredComponentConfiguration config = (ConfiguredComponentConfiguration) value;
                ComponentsTestUtil.setConfigured(config.getType(), config.getWorkspace(), config.getPath(), config.isObserved());
            }
        }

        for (Map.Entry<Class<?>, Class<?>> entry : configuration.getTypeMapping().entrySet()) {
            ComponentsTestUtil.setImplementation((Class)entry.getKey(), (Class)entry.getValue());
        }
    }

    /**
     * Override this method to provide the appropriate list of modules your tests need.
     */
    protected List<ModuleDefinition> getModuleDefinitionsForTests() throws ModuleManagementException {
        final ModuleDefinition core = new BetwixtModuleDefinitionReader().readFromResource("/META-INF/magnolia/core.xml");
        return Collections.singletonList(core);
    }

    protected MockHierarchyManager initMockConfigRepository(String properties) throws IOException, RepositoryException, UnsupportedRepositoryOperationException {

        MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager(ContentRepository.CONFIG, properties);

        return hm;
    }

    /**
     * Utility assertion that will match a String against a regex,
     * <strong>with the DOTALL flag enabled, which means the . character will also match new lines</strong>.
     */
    public static void assertMatches(String message, String s, String regex) {
        assertMatches(message, s, regex, Pattern.DOTALL);
    }

    /**
     * Utility assertion that will match a String against a regex.
     */
    public static void assertMatches(String message, String s, String regex, int flags) {
        final StringBuffer completeMessage = new StringBuffer();
        if (message!=null) {
            completeMessage.append(message).append(":\n");
        }
        completeMessage.append("Input:\n    ");
        completeMessage.append(s);
        completeMessage.append("did not match regex:\n    ");
        completeMessage.append(regex);
        assertTrue(completeMessage.toString(), Pattern.compile(regex, flags).matcher(s).matches());
    }

}
