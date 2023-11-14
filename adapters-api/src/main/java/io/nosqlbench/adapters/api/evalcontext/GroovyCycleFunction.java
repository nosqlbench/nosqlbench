/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapters.api.evalcontext;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.nb.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.nb.api.loaders.BundledExtensionsLoader;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroovyCycleFunction<T> implements CycleFunction<T> {
    private final static Logger logger = LogManager.getLogger(GroovyBooleanCycleFunction.class);
    private final String name;
    private final List<String> imports;
//    private final List<String> enabledServices = new ArrayList<>(List.of("vectormath"));
    private final List<String> enabledServices = new ArrayList<>();

    protected String scriptText; // Groovy script as provided
    protected final Script script; // Groovy Script as compiled
    protected final Binding variableBindings; // Groovy binding layer
    protected final Bindings bindingFunctions; // NB bindings
    private final List<Class<?>> staticImports;

    /**
     * Instantiate a cycle function from basic types
     *
     * @param scriptText
     *     The raw script text, not including any bind point or capture point syntax
     * @param bindingSpecs
     *     The names and recipes of bindings which are referenced in the scriptText
     * @param imports
     *     The package imports to be installed into the execution environment
     */
    public GroovyCycleFunction(String name, String scriptText, Map<String, String> bindingSpecs, List<String> imports, List<Class<?>> staticImports, Binding binding) {
        this.name = name;
        this.scriptText = scriptText;
        this.imports = imports;
        this.staticImports = staticImports;

        // scripting env variable bindings
        this.variableBindings = binding!=null? binding : new Binding();

        // virtdata bindings to be evaluated at cycle time
        this.bindingFunctions = new BindingsTemplate().addFieldBindings(bindingSpecs).resolveBindings();

        this.script = compileScript(this.scriptText, imports, staticImports, binding);
        addServices();
    }

    private void addServices() {
        for (final ScriptingExtensionPluginInfo<?> extensionDescriptor : BundledExtensionsLoader.findAll()) {
            staticImports.addAll(extensionDescriptor.autoImportStaticMethodClasses());
            if (!extensionDescriptor.isAutoLoading()) {
                logger.info(() -> "Not loading " + extensionDescriptor + ", autoloading is false");
                continue;
            }
            if (!enabledServices.isEmpty() && !enabledServices.contains(extensionDescriptor.getBaseVariableName())) {
                logger.info(()->"Not loading " + extensionDescriptor + ", not included in subset.");
                continue;
            }
            final Logger extensionLogger =
                LogManager.getLogger("extensions." + extensionDescriptor.getBaseVariableName());
            final Object extensionObject = extensionDescriptor.getExtensionObject(
                extensionLogger,
                null
            );
            logger.trace(() -> "Adding extension object:  name=" + extensionDescriptor.getBaseVariableName() +
                " class=" + extensionObject.getClass().getSimpleName());
            setVariable(extensionDescriptor.getBaseVariableName(), extensionObject);
        }
    }

    public GroovyCycleFunction(String name, ParsedTemplateString template, List<String> imports, List<Class<?>> staticImports, Binding binding) {
        this(
            name,
            template.getPositionalStatement(),
            resolveBindings(template.getBindPoints()),
            imports,
            staticImports,
            binding
        );
    }

    private Script compileScript(String scriptText, List<String> imports, List<Class<?>> staticImports, Binding binding) {
        // add classes which are in the imports to the groovy evaluation context
        String[] verifiedClasses = expandClassNames(imports);
        String[] verifiedStaticImports = expandStaticImports(staticImports);

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        ImportCustomizer importer = new ImportCustomizer().addImports(verifiedClasses);
        importer.addStaticStars(verifiedStaticImports);
        compilerConfiguration.addCompilationCustomizers(importer);

        GroovyShell gshell = new GroovyShell(binding!=null? binding:new Binding(), compilerConfiguration);
        return gshell.parse(scriptText);
    }

    private String[] expandStaticImports(List<Class<?>> staticImports) {
        return staticImports.stream().map(Class::getCanonicalName).toArray(String[]::new);
    }

    private static Map<String, String> resolveBindings(List<BindPoint> bindPoints) {
        return new BindingsTemplate(bindPoints).getMap();
    }


    protected String[] expandClassNames(List<String> groovyImportedClasses) {
        ClassLoader loader = BaseOpDispenser.class.getClassLoader();

        List<String> classNames = new ArrayList<>();
        for (String candidateName : groovyImportedClasses) {
            if (candidateName.endsWith(".*")) {
                throw new RuntimeException("You can not use wildcard package imports like '" + candidateName + "'");
            }
            try {
                loader.loadClass(candidateName);
                classNames.add(candidateName);
                logger.debug(() -> "added import " + candidateName);
            } catch (Exception e) {
                throw new RuntimeException("Class '" + candidateName + "' was not found for groovy imports.");
            }
        }
        return classNames.toArray(new String[0]);
    }

    @Override
    public String getExpressionDetails() {
        return this.scriptText;
    }


    @Override
    public <V> void setVariable(String name, V value) {
        this.variableBindings.setVariable(name, value);
    }

    @Override
    public T apply(long value) {
        Map<String, Object> values = bindingFunctions.getAllMap(value);
        values.forEach((k, v) -> variableBindings.setVariable(k, v));
        T result = (T) script.run();
        return result;
    }

    /**
     * Create an instance of an executable function which is based on the current one, with all of
     * the per-cycle bindings as well as the variable bindings duplicated (shared).
     * @return
     */
    @Override
    public CycleFunction<T> newInstance() {
        return new GroovyCycleFunction<T>(name, scriptText, bindingFunctions, imports, staticImports, this.variableBindings.getVariables());
    }

    private GroovyCycleFunction(String name, String scriptText, Bindings bindingFunctions, List<String> imports, List<Class<?>> staticImports, Map originalBinding) {
        this.name = name;
        this.scriptText = scriptText;
        this.bindingFunctions = bindingFunctions;
        this.imports = imports;
        this.staticImports = staticImports;

        this.script = compileScript(scriptText, imports, staticImports, new Binding());
        this.variableBindings = script.getBinding();
        originalBinding.forEach((k,v) -> variableBindings.setVariable(k.toString(),v));
    }


}
