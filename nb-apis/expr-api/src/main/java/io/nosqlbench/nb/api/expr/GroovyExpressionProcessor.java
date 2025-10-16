package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.params.Element;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.nb.api.expr.ExprFunctionParamsAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Applies Groovy backed expression substitution to workload sources. Expressions are
 * discovered using the {@code {{= ... }}} delimiter and are evaluated against a per-file
 * Groovy shell that is enriched through {@link ExprFunctionProvider} services.
 */
public class GroovyExpressionProcessor {

    private static final Logger LOGGER = LogManager.getLogger(GroovyExpressionProcessor.class);
    private static final Pattern SIGIL_PATTERN = Pattern.compile("\\{\\{(.+?)}}", Pattern.DOTALL);
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("^([A-Za-z_][\\w.-]*)\\s*(===|==|=)\\s*(.+)$", Pattern.DOTALL);
    private static final Pattern LVAR_REFERENCE_PATTERN = Pattern.compile("^@([A-Za-z_][\\w.-]*)([!?])?$");

    // Memoized ServiceLoader to avoid repeated classpath scanning
    private static final ServiceLoader<ExprFunctionProvider> SERVICE_LOADER =
        ServiceLoader.load(ExprFunctionProvider.class);

    private final List<ExprFunctionProvider> providers;
    private final CompilerConfiguration compilerConfiguration;

    public GroovyExpressionProcessor() {
        this(loadProviders(), new CompilerConfiguration());
    }

    public GroovyExpressionProcessor(List<ExprFunctionProvider> providers, CompilerConfiguration compilerConfiguration) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        this.compilerConfiguration = Objects.requireNonNull(compilerConfiguration, "compilerConfiguration");
    }

    private static List<ExprFunctionProvider> loadProviders() {
        List<ExprFunctionProvider> discovered = new ArrayList<>();
        // Using stream() on ServiceLoader returns a Stream<Provider<T>> for lazy loading
        SERVICE_LOADER.stream()
            .map(ServiceLoader.Provider::get)  // Lazy instantiation happens here
            .forEach(discovered::add);
        return discovered;
    }

    /**
     * Get a list of all available expression function provider names (selectors).
     * These names come from the {@code selector} attribute of the {@link Service} annotation.
     * This method uses lazy Provider inspection to avoid instantiating providers.
     *
     * @return list of provider selector names
     */
    public static List<String> getAvailableProviderNames() {
        List<String> names = new ArrayList<>();
        // Use Provider.type() to get the class without instantiating
        SERVICE_LOADER.stream()
            .map(provider -> {
                Class<?> providerType = provider.type();
                Service annotation = providerType.getAnnotation(Service.class);
                return annotation != null ? annotation.selector() : null;
            })
            .filter(selector -> selector != null && !selector.isEmpty())
            .forEach(names::add);
        return names;
    }

    /**
     * Load providers in a specific order based on selector names.
     * Providers not in the list are excluded. This allows users to control
     * function precedence by specifying provider order.
     *
     * @param orderedSelectors comma-separated list of provider selectors in desired order
     * @return list of providers in the specified order
     */
    public static List<ExprFunctionProvider> loadProvidersInOrder(String orderedSelectors) {
        if (orderedSelectors == null || orderedSelectors.trim().isEmpty()) {
            return loadProviders();
        }

        List<String> selectorList = Arrays.stream(orderedSelectors.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        return loadProvidersInOrder(selectorList);
    }

    /**
     * Load providers in a specific order based on selector names.
     * Providers not in the list are excluded. Uses lazy Provider instantiation
     * so only requested providers are actually created.
     *
     * @param orderedSelectors list of provider selectors in desired order
     * @return list of providers in the specified order
     */
    public static List<ExprFunctionProvider> loadProvidersInOrder(List<String> orderedSelectors) {
        if (orderedSelectors == null || orderedSelectors.isEmpty()) {
            return loadProviders();
        }

        // Create a map of selector -> Provider for lazy instantiation
        Map<String, ServiceLoader.Provider<ExprFunctionProvider>> providerMap = new LinkedHashMap<>();
        SERVICE_LOADER.stream()
            .forEach(provider -> {
                Class<?> providerType = provider.type();
                Service annotation = providerType.getAnnotation(Service.class);
                if (annotation != null && annotation.selector() != null && !annotation.selector().isEmpty()) {
                    providerMap.put(annotation.selector(), provider);
                }
            });

        // Build ordered list, instantiating ONLY the requested providers
        List<ExprFunctionProvider> ordered = new ArrayList<>();
        for (String selector : orderedSelectors) {
            ServiceLoader.Provider<ExprFunctionProvider> provider = providerMap.get(selector);
            if (provider != null) {
                ordered.add(provider.get());  // Lazy instantiation happens here
            } else {
                LOGGER.warn("Expression function provider '{}' was requested but not found", selector);
            }
        }

        return ordered;
    }

    /**
     * Get metadata about available providers without instantiating them.
     * Returns a map of selector -> provider class for inspection.
     *
     * @return map of provider selectors to their class types
     */
    public static Map<String, Class<?>> getProviderMetadata() {
        Map<String, Class<?>> metadata = new LinkedHashMap<>();
        SERVICE_LOADER.stream()
            .forEach(provider -> {
                Class<?> providerType = provider.type();
                Service annotation = providerType.getAnnotation(Service.class);
                if (annotation != null && annotation.selector() != null && !annotation.selector().isEmpty()) {
                    metadata.put(annotation.selector(), providerType);
                }
            });
        return metadata;
    }

    /**
     * Get function metadata from a provider class without instantiating it.
     * This allows inspection of available functions before loading.
     *
     * @param providerClass the provider class
     * @return list of function metadata
     */
    public static List<ExprFunctionMetadata> getProviderFunctionMetadata(Class<?> providerClass) {
        return new ArrayList<>(ExprFunctionAnnotations.extractMetadata(providerClass));
    }

    /**
     * Extract the selector from a provider's {@link Service} annotation.
     *
     * @param provider the provider instance
     * @return the selector string, or null if not found
     */
    private static String getProviderSelector(ExprFunctionProvider provider) {
        Service annotation = provider.getClass().getAnnotation(Service.class);
        return annotation != null ? annotation.selector() : null;
    }

    /**
     * Evaluate and substitute expressions within the provided workload source.
     *
     * @param source raw workload text
     * @param sourceUri workload origin if available
     * @param parameters parameters supplied alongside the workload
     * @return the transformed workload with Groovy expressions resolved
     */
    public String process(String source, URI sourceUri, Map<String, ?> parameters) {
        return processWithContext(source, sourceUri, parameters).getOutput();
    }

    /**
     * Evaluate and substitute expressions within the provided workload source,
     * and return both the transformed output and the binding context.
     *
     * @param source raw workload text
     * @param sourceUri workload origin if available
     * @param parameters parameters supplied alongside the workload
     * @return ProcessingResult containing both output and binding context
     */
    public ProcessingResult processWithContext(String source, URI sourceUri, Map<String, ?> parameters) {
        if (source == null || source.isEmpty()) {
            return new ProcessingResult(source, new Binding());
        }

        Map<String, ?> safeParams = parameters == null ? Map.of() : Map.copyOf(parameters);
        Binding binding = new Binding();
        GroovyExprRuntimeContext context = new GroovyExprRuntimeContext(
            binding,
            Optional.ofNullable(sourceUri),
            safeParams
        );

        context.setVariable("_parameters", safeParams);
        context.setVariable("_sourceUri", Optional.ofNullable(sourceUri).map(URI::toString).orElse(""));

        ExpressionVariableStore variableStore = new ExpressionVariableStore(binding);
        context.setVariable(ExpressionVariableStore.BINDING_MAP_NAME, variableStore.getBackingMap());

        // Find the GroovyLibraryAutoLoader provider if it exists
        GroovyLibraryAutoLoader libraryLoader = null;

        providers.forEach(provider -> {
            try {
                if (provider instanceof ExprFunctionParamsAware paramsAware) {
                    Element paramsElement = safeParams.isEmpty()
                        ? NBParams.one(Map.of("parameters", Map.of()))
                        : NBParams.one(safeParams);
                    paramsAware.setParams(paramsElement);
                }
                ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception ex) {
                throw new RuntimeException("Error contributing expression functions from provider "
                    + provider.getClass().getName(), ex);
            }
        });

        // Create the GroovyShell FIRST, then load libraries using the same shell
        GroovyShell shell = new GroovyShell(binding, compilerConfiguration);

        // Load Groovy libraries from lib/groovy/ directory and extract metadata
        for (ExprFunctionProvider provider : providers) {
            if (provider instanceof GroovyLibraryAutoLoader loader) {
                libraryLoader = loader;
                loader.loadLibrariesWithShell(shell);
                // The library functions are already in the binding (they're script methods)
                // We just need to manually add the metadata to the context
                loader.getLibraryMetadata().forEach((name, metadata) -> {
                    // Get the registered metadata map and add this entry
                    // Note: The actual function is already in the binding via the script
                    if (context instanceof GroovyExprRuntimeContext groovyContext) {
                        // Access the metadata map directly since functions are already loaded
                        groovyContext.registerMetadataOnly(metadata);
                    }
                });
                break;
            }
        }

        context.setVariable(ExpressionVariableStore.FUNCTION_METADATA_NAME, context.getRegisteredMetadata());

        // Capture initial variables (after providers and libraries load, before user scripts run)
        Set<String> initialVariables = Set.copyOf(binding.getVariables().keySet());

        String output = replaceExpressions(source, shell, Optional.ofNullable(sourceUri), variableStore);

        return new ProcessingResult(output, binding, initialVariables);
    }

    private String replaceExpressions(String source, GroovyShell shell, Optional<URI> sourceUri, ExpressionVariableStore variables) {
        String[] lines = source.split("\n", -1);
        Matcher matcher = SIGIL_PATTERN.matcher(source);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String sigilBody = matcher.group(1);
            int matchStart = matcher.start();
            int matchEnd = matcher.end();

            // Calculate line number and column position
            int lineNumber = 1;
            int lineStart = 0;
            int currentPos = 0;

            for (String line : lines) {
                int lineEnd = currentPos + line.length();
                if (matchStart >= currentPos && matchStart <= lineEnd) {
                    break;
                }
                lineNumber++;
                currentPos = lineEnd + 1; // +1 for newline
                lineStart = currentPos;
            }

            int columnStart = matchStart - lineStart;
            int columnEnd = matchEnd - lineStart;
            String templateLine = lines[lineNumber - 1];

            ExpressionContext context = new ExpressionContext(
                sourceUri,
                sigilBody,
                lineNumber,
                columnStart,
                columnEnd,
                templateLine
            );

            Optional<String> replacement = processSigil(sigilBody, shell, context, variables);
            if (replacement.isPresent()) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.get()));
            } else {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Optional<String> processSigil(String body, GroovyShell shell, ExpressionContext context, ExpressionVariableStore variables) {
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        if (trimmed.startsWith("=")) {
            String expression = trimmed.substring(1).trim();
            Object result = evaluateExpression(shell, expression, context);
            return Optional.of(formatResult(result));
        }

        Matcher assignmentMatcher = ASSIGNMENT_PATTERN.matcher(trimmed);
        if (assignmentMatcher.matches()) {
            String name = assignmentMatcher.group(1);
            String operator = assignmentMatcher.group(2);
            String expression = assignmentMatcher.group(3).trim();
            return Optional.of(handleAssignment(name, operator, expression, shell, context, variables));
        }

        Matcher referenceMatcher = LVAR_REFERENCE_PATTERN.matcher(trimmed);
        if (referenceMatcher.matches()) {
            String name = referenceMatcher.group(1);
            String modifier = referenceMatcher.group(2);
            return Optional.of(handleReference(name, modifier, context, variables));
        }

        return Optional.empty();
    }

    private String handleAssignment(String name, String operator, String expression, GroovyShell shell, ExpressionContext context, ExpressionVariableStore variables) {
        switch (operator) {
            case "=":
                Object overwrite = evaluateExpression(shell, expression, context);
                variables.set(name, overwrite);
                return formatResult(overwrite);
            case "==":
                if (variables.contains(name)) {
                    return formatResult(variables.get(name));
                }
                Object setOnce = evaluateExpression(shell, expression, context);
                variables.set(name, setOnce);
                return formatResult(setOnce);
            case "===":
                if (variables.contains(name)) {
                    throw new RuntimeException("Expression variable '" + name + "' is already set for " + context.getSourceDescription());
                }
                Object strict = evaluateExpression(shell, expression, context);
                variables.set(name, strict);
                return formatResult(strict);
            default:
                throw new IllegalStateException("Unsupported operator '" + operator + "'");
        }
    }

    private String handleReference(String name, String modifier, ExpressionContext context, ExpressionVariableStore variables) {
        boolean exists = variables.contains(name);
        if ("?".equals(modifier)) {
            Object value = exists ? variables.get(name) : null;
            return formatResult(value);
        }
        if (!exists) {
            throw new RuntimeException("Expression variable '" + name + "' has not been set for " + context.getSourceDescription());
        }
        Object value = variables.get(name);
        if ("!".equals(modifier) && value == null) {
            throw new RuntimeException("Expression variable '" + name + "' was null when a non-null value was required for " + context.getSourceDescription());
        }
        return formatResult(value);
    }

    private Object evaluateExpression(GroovyShell shell, String expression, ExpressionContext context) {
        try {
            // Use shell.evaluate() instead of parse().run() to ensure proper variable resolution
            // from the binding, including closures defined in library scripts
            return shell.evaluate(expression);
        } catch (Exception ex) {
            LOGGER.error("Error evaluating Groovy expression at {}:{}",
                context.getSourceDescription(),
                context.getTemplateLineNumber(),
                ex);

            // Extract line and column info from Groovy exception
            var locationInfo = ExpressionEvaluationException.extractLocationInfo(ex);

            throw new ExpressionEvaluationException(
                "Error evaluating expression",
                ex,
                context,
                locationInfo.getLineNumber(),
                locationInfo.getColumnNumber()
            );
        }
    }

    private String formatResult(Object value) {
        return value == null ? "" : value.toString();
    }

    private static final class ExpressionVariableStore {
        private static final String BINDING_PREFIX = "__expr_lvar_";
        static final String BINDING_MAP_NAME = "__expr_lvars";
        static final String FUNCTION_METADATA_NAME = "__expr_function_metadata";

        private final Binding binding;
        private final Map<String, Object> values = new LinkedHashMap<>();

        ExpressionVariableStore(Binding binding) {
            this.binding = binding;
        }

        void set(String name, Object value) {
            values.put(name, value);
            binding.setVariable(BINDING_PREFIX + name, value);
            binding.setVariable(name, value);
        }

        boolean contains(String name) {
            return values.containsKey(name);
        }

        Object get(String name) {
            return values.get(name);
        }

        Map<String, Object> getBackingMap() {
            return values;
        }
    }
}
