package io.nosqlbench.nb.api.expr;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        ServiceLoader.load(ExprFunctionProvider.class).forEach(discovered::add);
        return discovered;
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
        if (source == null || source.isEmpty()) {
            return source;
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

        providers.forEach(provider -> {
            try {
                provider.contribute(context);
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception ex) {
                throw new RuntimeException("Error contributing expression functions from provider "
                    + provider.getClass().getName(), ex);
            }
        });

        GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
        return replaceExpressions(source, shell, Optional.ofNullable(sourceUri), variableStore);
    }

    private String replaceExpressions(String source, GroovyShell shell, Optional<URI> sourceUri, ExpressionVariableStore variables) {
        Matcher matcher = SIGIL_PATTERN.matcher(source);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String sigilBody = matcher.group(1);
            Optional<String> replacement = processSigil(sigilBody, shell, sourceUri, variables);
            if (replacement.isPresent()) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.get()));
            } else {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Optional<String> processSigil(String body, GroovyShell shell, Optional<URI> sourceUri, ExpressionVariableStore variables) {
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        if (trimmed.startsWith("=")) {
            String expression = trimmed.substring(1).trim();
            Object result = evaluateExpression(shell, expression, sourceUri);
            return Optional.of(formatResult(result));
        }

        Matcher assignmentMatcher = ASSIGNMENT_PATTERN.matcher(trimmed);
        if (assignmentMatcher.matches()) {
            String name = assignmentMatcher.group(1);
            String operator = assignmentMatcher.group(2);
            String expression = assignmentMatcher.group(3).trim();
            return Optional.of(handleAssignment(name, operator, expression, shell, sourceUri, variables));
        }

        Matcher referenceMatcher = LVAR_REFERENCE_PATTERN.matcher(trimmed);
        if (referenceMatcher.matches()) {
            String name = referenceMatcher.group(1);
            String modifier = referenceMatcher.group(2);
            return Optional.of(handleReference(name, modifier, sourceUri, variables));
        }

        return Optional.empty();
    }

    private String handleAssignment(String name, String operator, String expression, GroovyShell shell, Optional<URI> sourceUri, ExpressionVariableStore variables) {
        switch (operator) {
            case "=":
                Object overwrite = evaluateExpression(shell, expression, sourceUri);
                variables.set(name, overwrite);
                return formatResult(overwrite);
            case "==":
                if (variables.contains(name)) {
                    return formatResult(variables.get(name));
                }
                Object setOnce = evaluateExpression(shell, expression, sourceUri);
                variables.set(name, setOnce);
                return formatResult(setOnce);
            case "===":
                if (variables.contains(name)) {
                    throw new RuntimeException("Expression variable '" + name + "' is already set for " + describeSource(sourceUri));
                }
                Object strict = evaluateExpression(shell, expression, sourceUri);
                variables.set(name, strict);
                return formatResult(strict);
            default:
                throw new IllegalStateException("Unsupported operator '" + operator + "'");
        }
    }

    private String handleReference(String name, String modifier, Optional<URI> sourceUri, ExpressionVariableStore variables) {
        boolean exists = variables.contains(name);
        if ("?".equals(modifier)) {
            Object value = exists ? variables.get(name) : null;
            return formatResult(value);
        }
        if (!exists) {
            throw new RuntimeException("Expression variable '" + name + "' has not been set for " + describeSource(sourceUri));
        }
        Object value = variables.get(name);
        if ("!".equals(modifier) && value == null) {
            throw new RuntimeException("Expression variable '" + name + "' was null when a non-null value was required for " + describeSource(sourceUri));
        }
        return formatResult(value);
    }

    private Object evaluateExpression(GroovyShell shell, String expression, Optional<URI> sourceUri) {
        try {
            Script script = shell.parse(expression);
            return script.run();
        } catch (Exception ex) {
            String location = sourceUri.map(Object::toString).orElse("<inline>");
            LOGGER.error("Error evaluating Groovy expression '{}' for {}", expression, location, ex);
            throw new RuntimeException("Error evaluating Groovy expression '" + expression + "' for " + location, ex);
        }
    }

    private String formatResult(Object value) {
        return value == null ? "" : value.toString();
    }

    private String describeSource(Optional<URI> sourceUri) {
        return sourceUri.map(Object::toString).orElse("<inline>");
    }

    private static final class ExpressionVariableStore {
        private static final String BINDING_PREFIX = "__expr_lvar_";
        private static final String BINDING_MAP_NAME = "__expr_lvars";

        private final Binding binding;
        private final Map<String, Object> values = new LinkedHashMap<>();

        ExpressionVariableStore(Binding binding) {
            this.binding = binding;
        }

        void set(String name, Object value) {
            values.put(name, value);
            binding.setVariable(BINDING_PREFIX + name, value);
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
