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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites TEMPLATE variable syntax into expr function calls, enabling unified variable
 * substitution through the expr library system.
 *
 * <p>This rewriter transforms two TEMPLATE syntaxes into expr equivalents:</p>
 * <ul>
 *   <li>Function syntax: {@code TEMPLATE(key,default)}</li>
 *   <li>Shell-style syntax: {@code ${key:default}}</li>
 * </ul>
 *
 * <p>Both syntaxes are converted to expr function calls ({@code paramOr}) that are evaluated by
 * the standard expr processing pipeline.</p>
 *
 * <h3>Rewriting Strategy</h3>
 * <p>The rewriter performs pattern-based string substitution BEFORE expr evaluation,
 * converting TEMPLATE syntax into expr function calls. This approach:</p>
 * <ul>
 *   <li>Requires no changes to the expr grammar or parser</li>
 *   <li>Enables full expr capabilities within template values</li>
 *   <li>Provides automatic nesting and recursion via expr evaluator</li>
 * </ul>
 *
 * <h3>Example Transformations</h3>
 * <pre>
 * Input:  "bind: TEMPLATE(dist,Uniform(0,1000))"
 * Output: "bind: {{= paramOr('dist', Uniform(0,1000)) }}"
 *
 * Input:  "host: ${server:localhost}"
 * Output: "host: {{= paramOr('server', 'localhost') }}"
 * </pre>
 */
public final class TemplateRewriter {

    // Pattern for TEMPLATE function syntax: TEMPLATE(...)
    // We'll parse the arguments manually to handle nested parentheses
    private static final Pattern TEMPLATE_FUNCTION_PATTERN =
        Pattern.compile("TEMPLATE\\(");

    // Pattern for shell-style variable syntax START: ${
    private static final Pattern SHELL_VAR_START_PATTERN =
        Pattern.compile("\\$\\{");

    /**
     * Rewrite TEMPLATE variables to expr function calls.
     *
     * <p>Processes both TEMPLATE syntax forms, converting them to equivalent
     * expr function calls that will be evaluated by the expression system.</p>
     *
     * @param source the source text potentially containing TEMPLATE variables
     * @return the rewritten text with TEMPLATE syntax converted to expr calls
     */
    public static String rewrite(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        String result = source;

        // Process each pattern type in order
        result = rewriteTemplateFunctions(result);
        result = rewriteShellVars(result);

        return result;
    }

    /**
     * Rewrite TEMPLATE function syntax {@code TEMPLATE(key,default)} to expr function calls.
     *
     * <p>Transformations:</p>
     * <ul>
     *   <li>{@code TEMPLATE(key)} → {@code paramOr('key', 'UNSET:key')}</li>
     *   <li>{@code TEMPLATE(key,default)} → {@code paramOr('key', 'default')}</li>
     * </ul>
     *
     * <p>This method manually parses the arguments to correctly handle nested parentheses
     * in default values like {@code TEMPLATE(key,Uniform(0,1000))}.</p>
     *
     * @param source the source text
     * @return the text with TEMPLATE function syntax rewritten to expr calls
     */
    private static String rewriteTemplateFunctions(String source) {
        StringBuilder result = new StringBuilder();
        int pos = 0;

        Matcher matcher = TEMPLATE_FUNCTION_PATTERN.matcher(source);
        while (matcher.find(pos)) {
            // Append text before TEMPLATE(
            result.append(source, pos, matcher.start());

            // Find the matching closing parenthesis
            int startPos = matcher.end(); // Position after "TEMPLATE("
            int parenDepth = 1;
            int endPos = startPos;

            while (endPos < source.length() && parenDepth > 0) {
                char c = source.charAt(endPos);
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    parenDepth--;
                }
                endPos++;
            }

            if (parenDepth != 0) {
                // Unmatched parentheses - skip this occurrence
                result.append(source, matcher.start(), endPos);
                pos = endPos;
                continue;
            }

            // Extract the content between parentheses (excluding the final ')')
            String content = source.substring(startPos, endPos - 1);

            // Find the first comma that's not inside parentheses
            String key = null;
            String defaultValue = null;
            boolean hasComma = false;

            int commaPos = findTopLevelComma(content);
            if (commaPos == -1) {
                // No comma found - just a key
                key = content.trim();
            } else {
                // Split at the comma
                hasComma = true;
                key = content.substring(0, commaPos).trim();
                defaultValue = content.substring(commaPos + 1).trim();
            }

            // Generate the replacement
            String replacement;
            if (!hasComma) {
                // TEMPLATE(key) with no comma - use UNSET:key as default
                replacement = String.format("{{= paramOr('%s', 'UNSET:%s') }}", key, key);
            } else if (defaultValue.isEmpty()) {
                // TEMPLATE(key,) with explicit empty default - use null
                replacement = String.format("{{= paramOr('%s', null) }}", key);
            } else {
                // TEMPLATE(key,default) with non-empty default
                replacement = String.format("{{= paramOr('%s', %s) }}",
                    key, quoteValue(rewrite(defaultValue)));
            }

            result.append(replacement);
            pos = endPos;
        }

        // Append any remaining text
        result.append(source.substring(pos));
        return result.toString();
    }

    /**
     * Find the position of the first comma that's not inside parentheses.
     *
     * @param content the string to search
     * @return the position of the comma, or -1 if not found
     */
    private static int findTopLevelComma(String content) {
        int parenDepth = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            } else if (c == ',' && parenDepth == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Rewrite shell-style variable syntax {@code ${key:default}} to expr function calls.
     *
     * <p>Transformations:</p>
     * <ul>
     *   <li>{@code ${key}} → {@code paramOr('key', 'UNSET:key')}</li>
     *   <li>{@code ${key:default}} → {@code paramOr('key', 'default')}</li>
     * </ul>
     *
     * <p>This method manually parses shell variables to correctly handle nested
     * expressions like {@code ${key:{{=expr}}}} that contain `}` characters.</p>
     *
     * @param source the source text
     * @return the text with shell-style syntax rewritten to expr calls
     */
    private static String rewriteShellVars(String source) {
        StringBuilder result = new StringBuilder();
        int pos = 0;

        Matcher matcher = SHELL_VAR_START_PATTERN.matcher(source);
        while (matcher.find(pos)) {
            // Append text before ${
            result.append(source, pos, matcher.start());

            // Find the matching closing brace, accounting for nested ${ } and {{ }} expressions
            int startPos = matcher.end(); // Position after "${"
            int braceDepth = 1; // Track nesting of { }
            int endPos = startPos;
            boolean foundColon = false;
            int colonPos = -1;

            while (endPos < source.length() && braceDepth > 0) {
                char c = source.charAt(endPos);

                if (c == '{') {
                    braceDepth++;
                } else if (c == '}') {
                    braceDepth--;
                    if (braceDepth == 0) {
                        break;
                    }
                } else if (c == ':' && braceDepth == 1 && colonPos == -1) {
                    foundColon = true;
                    colonPos = endPos;
                }

                endPos++;
            }

            if (endPos >= source.length() || source.charAt(endPos) != '}') {
                // Unmatched braces - skip this occurrence
                result.append(source, matcher.start(), endPos);
                pos = endPos;
                continue;
            }

            // Extract key and default value
            String key;
            String defaultValue = null;

            if (foundColon && colonPos != -1) {
                key = source.substring(startPos, colonPos).trim();
                defaultValue = source.substring(colonPos + 1, endPos).trim();
            } else {
                key = source.substring(startPos, endPos).trim();
            }

            // Generate the replacement
            String replacement;
            if (defaultValue == null || defaultValue.isEmpty()) {
                // ${key} with no default - use UNSET:key
                replacement = String.format("{{= paramOr('%s', 'UNSET:%s') }}", key, key);
            } else {
                // ${key:default} with default value
                replacement = String.format("{{= paramOr('%s', %s) }}",
                    key, quoteValue(rewrite(defaultValue)));
            }

            result.append(replacement);
            pos = endPos + 1; // Skip past the closing }
        }

        // Append any remaining text
        result.append(source.substring(pos));
        return result.toString();
    }

    /**
     * Quote a value for use in an expr function call.
     *
     * <p>This method intelligently handles different value types:</p>
     * <ul>
     *   <li>Numeric literals (integers, floats) are returned unquoted</li>
     *   <li>Boolean literals are returned unquoted</li>
     *   <li>Expr function calls (containing parentheses) are returned unquoted</li>
     *   <li>Expr references (already using {{}} syntax) are unwrapped to avoid nesting</li>
     *   <li>VirtData binding chains (containing ->) are quoted as strings</li>
     *   <li>All other strings are single-quoted with proper escaping</li>
     * </ul>
     *
     * @param value the value to quote
     * @return the properly quoted or unquoted value
     */
    private static String quoteValue(String value) {
        if (value == null || value.isEmpty()) {
            return "''";
        }

        // Check if it contains VirtData binding chain syntax (->)
        // This should be treated as a string, not an expression
        if (value.contains("->")) {
            return "'" + value.replace("'", "\\'") + "'";
        }

        // Check if it contains expr references - unwrap to avoid nested delimiters
        // Transform {{=expression}} to just expression
        boolean hadExprReference = value.contains("{{") && value.contains("}}");
        if (hadExprReference) {
            value = unwrapExprReference(value);
            // Continue with other checks to see if unwrapped value needs quoting
        }

        // Check if it's a numeric literal (integer or float)
        if (value.matches("^-?\\d+(\\.\\d+)?$")) {
            return value;
        }

        // Check if it's a boolean literal
        if (value.equals("true") || value.equals("false")) {
            return value;
        }

        // Check if it contains function call syntax (likely an expr function)
        if (value.contains("(") && value.contains(")")) {
            return value;
        }

        // If we unwrapped an expr reference, check if the result needs quoting
        if (hadExprReference) {
            // Check if it looks like a simple identifier (variable name)
            // Simple identifiers: letters, numbers, underscores - no spaces or special chars
            if (value.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                return value;
            }

            // Check if it looks like an expression with operators
            // If it contains expr operators, treat it as an expression (don't quote)
            if (value.matches(".*[+\\-*/|&<>=!].*")) {
                return value;
            }

            // Otherwise (mixed text like "prefix name suffix"), quote it
            return "'" + value.replace("'", "\\'") + "'";
        }

        // For values that didn't have expr references, quote them as strings
        return "'" + value.replace("'", "\\'") + "'";
    }

    /**
     * Unwrap expr references to avoid nested delimiters.
     *
     * <p>Transforms {@code {{=expression}}} to {@code expression} to prevent
     * creating nested delimiters when used inside another expr context.</p>
     *
     * <p>This method handles both single and multiple expr references:</p>
     * <ul>
     *   <li>{@code {{=base_vectors}}} → {@code base_vectors}</li>
     *   <li>{@code {{= paramOr('x', 10) }}} → {@code paramOr('x', 10)}</li>
     *   <li>{@code {{=x}} + {{=y}}} → {@code x + y}</li>
     *   <li>{@code prefix {{=x}} suffix} → {@code prefix x suffix}</li>
     * </ul>
     *
     * @param value the value potentially containing expr delimiters
     * @return the value with all {{...}} expressions unwrapped
     */
    private static String unwrapExprReference(String value) {
        if (value == null || !value.contains("{{")) {
            return value;
        }

        // Pattern to match {{= ... }} or {{ ... }}
        Pattern exprPattern = Pattern.compile("\\{\\{\\s*=?\\s*(.+?)\\s*\\}\\}", Pattern.DOTALL);
        Matcher matcher = exprPattern.matcher(value);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Extract just the expression content, removing the delimiters
            String expression = matcher.group(1).trim();
            matcher.appendReplacement(result, Matcher.quoteReplacement(expression));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
