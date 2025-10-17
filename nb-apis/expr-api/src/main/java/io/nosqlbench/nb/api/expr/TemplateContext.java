/*
 * Copyright (c) 2025 nosqlbench
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

package io.nosqlbench.nb.api.expr;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-local context for tracking template variable accesses and overrides during workload processing.
 *
 * <p>This class manages the lifecycle of template state using the try-with-resources pattern,
 * ensuring automatic cleanup and preventing memory leaks in long-running processes.</p>
 *
 * <h3>Usage</h3>
 * <pre>
 * try (TemplateContext ctx = TemplateContext.enter()) {
 *     // Process workload - template accesses are tracked automatically
 *     String processed = processor.process(workload, uri, params);
 *
 *     // Retrieve tracked variables
 *     Map&lt;String, String&gt; accesses = ctx.getAccesses();
 * } // Automatic cleanup
 * </pre>
 *
 * <h3>Thread Safety</h3>
 * <p>Each thread has its own independent context. Contexts are not inherited by child threads.</p>
 */
public class TemplateContext implements AutoCloseable {

    /**
     * Template variable overrides set via _templateSet() function.
     * These take precedence over parameter defaults but not over provided parameters.
     */
    private final Map<String, String> overrides = new LinkedHashMap<>();

    /**
     * Template variables accessed during workload processing.
     * Used for workload validation and to identify required parameters.
     */
    private final Map<String, String> accesses = new LinkedHashMap<>();

    /**
     * Thread-local storage for the current context.
     */
    private static final ThreadLocal<TemplateContext> CURRENT = new ThreadLocal<>();

    /**
     * Private constructor - use {@link #enter()} to create contexts.
     */
    private TemplateContext() {
    }

    /**
     * Enter a new template context for the current thread.
     *
     * <p>This method should be called in a try-with-resources block to ensure
     * automatic cleanup:</p>
     * <pre>
     * try (TemplateContext ctx = TemplateContext.enter()) {
     *     // Process workload
     * }
     * </pre>
     *
     * @return a new template context
     * @throws IllegalStateException if a context is already active on this thread
     */
    public static TemplateContext enter() {
        if (CURRENT.get() != null) {
            throw new IllegalStateException(
                "Template context already active on this thread. " +
                "Nested contexts are not supported."
            );
        }
        TemplateContext ctx = new TemplateContext();
        CURRENT.set(ctx);
        return ctx;
    }

    /**
     * Get the current template context for this thread.
     *
     * @return the current context
     * @throws IllegalStateException if no context is active on this thread
     */
    public static TemplateContext current() {
        TemplateContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException(
                "No active template context on this thread. " +
                "Use TemplateContext.enter() in a try-with-resources block."
            );
        }
        return ctx;
    }

    /**
     * Check if a template context is currently active on this thread.
     *
     * @return true if a context is active, false otherwise
     */
    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    /**
     * Track a template variable access.
     *
     * <p>This records that a template variable was accessed during workload processing,
     * along with the value that was resolved. This information is used for:</p>
     * <ul>
     *   <li>Workload validation (identifying required parameters)</li>
     *   <li>Documentation generation</li>
     *   <li>Debugging and diagnostics</li>
     * </ul>
     *
     * @param name the template variable name
     * @param value the resolved value
     */
    public void trackAccess(String name, String value) {
        accesses.put(name, value);
    }

    /**
     * Get all template variable accesses tracked in this context.
     *
     * @return an immutable copy of the access tracking map
     */
    public Map<String, String> getAccesses() {
        return new LinkedHashMap<>(accesses);
    }

    /**
     * Set a template variable override.
     *
     * <p>Overrides are used by the _templateSet() function to implement lexical scoping
     * for template variables. Once set, the override value takes precedence over defaults
     * but not over explicitly provided parameters.</p>
     *
     * @param name the template variable name
     * @param value the override value
     */
    public void setOverride(String name, String value) {
        overrides.put(name, value);
    }

    /**
     * Get a template variable override value.
     *
     * @param name the template variable name
     * @return the override value, or null if not set
     */
    public String getOverride(String name) {
        return overrides.get(name);
    }

    /**
     * Check if a template variable override is set.
     *
     * @param name the template variable name
     * @return true if an override exists, false otherwise
     */
    public boolean hasOverride(String name) {
        return overrides.containsKey(name);
    }

    /**
     * Get all template variable overrides in this context.
     *
     * @return an immutable copy of the overrides map
     */
    public Map<String, String> getOverrides() {
        return new LinkedHashMap<>(overrides);
    }

    /**
     * Close this context and remove it from the current thread.
     *
     * <p>This method is called automatically when using try-with-resources.
     * After closing, the context is no longer accessible and all tracked state is discarded.</p>
     */
    @Override
    public void close() {
        CURRENT.remove();
    }
}
