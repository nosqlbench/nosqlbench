/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.parser;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when a MetricsQL query cannot be parsed.
 *
 * <p>This exception encapsulates both the error message and the list of specific
 * parse errors encountered, allowing callers to access detailed error information.</p>
 */
public class MetricsQLParseException extends RuntimeException {

    private final List<String> errors;

    /**
     * Constructs a parse exception with a message and list of errors.
     *
     * @param message The error message
     * @param errors List of specific parse errors
     */
    public MetricsQLParseException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
    }

    /**
     * Constructs a parse exception with a message and cause.
     *
     * @param message The error message
     * @param cause The underlying cause
     */
    public MetricsQLParseException(String message, Throwable cause) {
        super(message, cause);
        this.errors = Collections.emptyList();
    }

    /**
     * Constructs a parse exception with a message.
     *
     * @param message The error message
     */
    public MetricsQLParseException(String message) {
        super(message);
        this.errors = Collections.emptyList();
    }

    /**
     * Returns the list of specific parse errors.
     *
     * @return Unmodifiable list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns true if this exception contains multiple errors.
     *
     * @return true if there are multiple errors, false otherwise
     */
    public boolean hasMultipleErrors() {
        return errors.size() > 1;
    }

    /**
     * Returns the number of parse errors.
     *
     * @return The error count
     */
    public int getErrorCount() {
        return errors.size();
    }
}
