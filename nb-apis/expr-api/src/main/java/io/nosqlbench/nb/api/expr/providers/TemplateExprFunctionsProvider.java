package io.nosqlbench.nb.api.expr.providers;

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


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.system.NBEnvironment;
import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import io.nosqlbench.nb.api.expr.ExprRuntimeContext;
import io.nosqlbench.nb.api.expr.annotations.ExprExample;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides special template functions that implement advanced TEMPLATE variable operators.
 * These functions support the := and :+ operators from the legacy StrInterpolator.
 *
 * <p>Functions are prefixed with underscore to indicate they are internal/advanced.</p>
 */
@Service(value = ExprFunctionProvider.class, selector = "template")
public class TemplateExprFunctionsProvider implements ExprFunctionProvider {

    // Thread-local storage for template overrides and tracking
    private static final ThreadLocal<Map<String, String>> TEMPLATE_OVERRIDES =
        ThreadLocal.withInitial(LinkedHashMap::new);

    private static final ThreadLocal<Map<String, String>> TEMPLATE_ACCESSES =
        ThreadLocal.withInitial(LinkedHashMap::new);

    /**
     * Clear template state for the current thread. Should be called after processing
     * a workload to prevent memory leaks.
     */
    public static void clearThreadState() {
        TEMPLATE_OVERRIDES.remove();
        TEMPLATE_ACCESSES.remove();
    }

    /**
     * Get the template accesses tracked for the current thread.
     */
    public static Map<String, String> getTemplateAccesses() {
        return new LinkedHashMap<>(TEMPLATE_ACCESSES.get());
    }

    /**
     * Get the template overrides set for the current thread.
     */
    public static Map<String, String> getTemplateOverrides() {
        return new LinkedHashMap<>(TEMPLATE_OVERRIDES.get());
    }

    @ExprExample(args = {"\"retries\"", "\"3\""}, expect = "\"3\"")
    @ExprFunctionSpec(
        name = "_templateSet",
        synopsis = "_templateSet(name, default)",
        description = "Set parameter if unset, then return it (implements the := operator). " +
            "This mimics the <<key:=default>> behavior where the default is set and used on first access."
    )
    private Object _templateSet(ExprRuntimeContext context, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("_templateSet(name, default) requires exactly two arguments");
        }

        String name = String.valueOf(args[0]);
        String defaultValue = String.valueOf(args[1]);

        Map<String, ?> params = context.parameters();
        Map<String, String> overrides = TEMPLATE_OVERRIDES.get();

        // Check if already overridden
        if (overrides.containsKey(name)) {
            String value = overrides.get(name);
            trackAccess(name, value);
            return value;
        }

        // Check if in parameters
        if (params.containsKey(name)) {
            String value = String.valueOf(params.get(name));
            trackAccess(name, value);
            return value;
        }

        // Check environment
        if (NBEnvironment.INSTANCE.containsKey(name)) {
            String value = NBEnvironment.INSTANCE.get(name);
            trackAccess(name, value);
            return value;
        }

        // Set override and use default
        overrides.put(name, defaultValue);
        trackAccess(name, defaultValue);
        return defaultValue;
    }

    @ExprExample(args = {"\"debug\"", "\"verbose\""}, expect = "\"verbose\"")
    @ExprExample(args = {"\"missing\"", "\"alternate\""}, expect = "\"\"")
    @ExprFunctionSpec(
        name = "_templateAlt",
        synopsis = "_templateAlt(name, alternate)",
        description = "Return alternate value if parameter IS set, empty string otherwise " +
            "(implements the :+ operator). This mimics the <<key:+alternate>> behavior."
    )
    private Object _templateAlt(ExprRuntimeContext context, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("_templateAlt(name, alternate) requires exactly two arguments");
        }

        String name = String.valueOf(args[0]);
        String alternate = String.valueOf(args[1]);

        Map<String, ?> params = context.parameters();
        Map<String, String> overrides = TEMPLATE_OVERRIDES.get();

        // Check if parameter is set (in any location)
        boolean isSet = overrides.containsKey(name) ||
                       params.containsKey(name) ||
                       NBEnvironment.INSTANCE.containsKey(name);

        if (isSet) {
            trackAccess(name, alternate);
            return alternate;
        }

        return "";
    }

    @ExprFunctionSpec(
        name = "_templateTrack",
        synopsis = "_templateTrack(name, value)",
        description = "Track template variable access for validation. This is used internally " +
            "to record which template variables were accessed during workload processing."
    )
    private Object _templateTrack(ExprRuntimeContext context, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("_templateTrack(name, value) requires exactly two arguments");
        }

        String name = String.valueOf(args[0]);
        String value = String.valueOf(args[1]);

        trackAccess(name, value);
        return value;
    }

    @ExprExample(args = {"\"myvar\"", "\"default\""}, expect = "\"default\"")
    @ExprFunctionSpec(
        name = "_templateGet",
        synopsis = "_templateGet(name, default)",
        description = "Get a template variable value if already set, otherwise use default. " +
            "This implements lexical scoping where template variables set in one place are " +
            "accessible in later references without re-specifying the default."
    )
    private Object _templateGet(ExprRuntimeContext context, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("_templateGet(name, default) requires exactly two arguments");
        }

        String name = String.valueOf(args[0]);
        String defaultValue = String.valueOf(args[1]);

        Map<String, ?> params = context.parameters();
        Map<String, String> overrides = TEMPLATE_OVERRIDES.get();

        // Check if already set via _templateSet
        if (overrides.containsKey(name)) {
            String value = overrides.get(name);
            trackAccess(name, value);
            return value;
        }

        // Check if in parameters
        if (params.containsKey(name)) {
            String value = String.valueOf(params.get(name));
            trackAccess(name, value);
            return value;
        }

        // Check environment
        if (NBEnvironment.INSTANCE.containsKey(name)) {
            String value = NBEnvironment.INSTANCE.get(name);
            trackAccess(name, value);
            return value;
        }

        // Use default without setting
        trackAccess(name, defaultValue);
        return defaultValue;
    }

    /**
     * Helper to track parameter access.
     */
    private void trackAccess(String name, String value) {
        TEMPLATE_ACCESSES.get().put(name, value);
    }
}
