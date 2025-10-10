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

import java.net.URI;
import java.util.Map;

/**
 * Lightweight facade that prescans text for Groovy expression sigils before invoking the
 * heavyweight {@link GroovyExpressionProcessor}. This avoids building the expression runtime
 * when workloads contain no substitutions.
 */
public final class ExprPreprocessor {

    private static final String SIGIL = "{{=";

    private volatile GroovyExpressionProcessor processor;

    /**
     * Render the provided source through the expression system only when a substitution sigil is
     * detected. Otherwise, the original source is returned unchanged.
     */
    public String process(String source, URI sourceUri, Map<String, ?> parameters) {
        if (!containsExpressions(source)) {
            return source;
        }
        return getProcessor().process(source, sourceUri, parameters);
    }

    /**
     * @return {@code true} when the source contains the {@code {{=}}} sigil that requires
     * expression expansion.
     */
    public boolean containsExpressions(String source) {
        return source != null && source.contains(SIGIL);
    }

    private GroovyExpressionProcessor getProcessor() {
        GroovyExpressionProcessor local = processor;
        if (local == null) {
            synchronized (this) {
                local = processor;
                if (local == null) {
                    local = new GroovyExpressionProcessor();
                    processor = local;
                }
            }
        }
        return local;
    }
}
