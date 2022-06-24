/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.core.templates;


import io.nosqlbench.engine.api.templating.BindType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: Consider using "Driver Adapter" or "Workload Adapter" instead of ActivityType

/**
 * <p>
 * A ParsedTemplate represents any string provided by a user which is meant to be
 * a prototype for value used in an operation. A ParsedTemplate can represent string
 * values with various levels of interpolation:
 * <UL>
 * <LI>A single literal String value, such as <pre>{@code select * from tablefoo;}</pre>
 * This is the simplest case, as there are no no bindings nor captures.</LI>
 * <LI>A value with named bindings interpolated into the string like
 * <pre>{@code select * from tablefoo where userid={userid};}</pre>
 * In this case, a single named binding point is specified. The <em>userid</em> value will
 * be provided when the string needs to be fully composed before use.</LI>
 * <LI>A value with named captures like
 * <pre>{@code select [username] from tablefoo where userid=32;}</pre>
 * In this case, a single named capture is specified -- The field name <em>username</em>
 * will be read from some native result form and saved into memory for later use.
 * </LI>A single binding reference: <pre>{@code {username}}</pre>
 * This form is the simplest way to specify that a referenced binding should be used in a field.
 * This form does not presume that you want the value to be read as a String. The type of
 * value returned by bindings is used as-is, thus if you need to use a binding reference
 * as a string, make sure your binding recipe returns a String directly. (All of the other forms
 * presume to call {@code .toString()} automatically on any binding values.)</pre></LI>
 * </UL>
 * </p>
 *
 * <p>
 * <H3>Details</H3>
 *
 * Grammars used by native drivers can be decorated
 * with named injection and extraction points for data, known respectively as
 * {@link BindPoint}s and {@link CapturePoint}s.
 *
 * The syntax used for bind points and capture points is standard across all
 * driver adapters. As such, this class captures the definition of
 * decorative syntax and the rules for parsing them out.
 *
 * The key responsibilities of ParsedTemplate are:
 * <UL>
 * <LI>recognize bind points within statement templates</LI>
 * <LI>recognize capture points within statement templates</LI>
 * <LI>render statement templates with bind and capture points elided using a native syntax for variables</LI>
 * <LI>provide metadata to drivers about defined bind and capture points</LI>
 * <LI>provide a text template for re-assembly with injected data</LI>
 * </UL>
 *
 * Once the parsed template is constructed, the method {@link ParsedStringTemplate#orError()}
 * should <em>always</em> called before it is used.
 *
 * <H2>Validity Checks</H2>
 *
 * A parsed template is considered to be valid if and only if the raw template contained only
 * named anchors which were defined in the provided bindings. Extra bindings are not presumed
 * to make a template invalid, but this interpretation is left to the caller for extra checks
 * if needed.
 *
 * <H2>Parsed Details</H2>
 * After parsing, the following details are available:
 *
 * <H3>Parsed Spans</H3>
 * This is an alternating list of the literal sections of the raw template
 * interspersed with the anchor names. This list always starts and ends with a literal section, so
 * will always contain an odd number of elements. (some span sections may be empty if necessary, but
 * not null)
 *
 * <H3>Specific Bindings</H3>
 * These are the binding names and definitions which were used
 * in a named anchor and also found in the provided bindings. If an anchor references
 * a binding which is not provided, then it will <em>not</em> be in this map.
 *
 * <H3>Missing Bindings</H3>
 * This is a list of binding names which were found in the
 * raw template but which were not found in the provided bindings by name.
 *
 * <H3>Extra Bindings</H3>
 * This is a list of binding names which were provided by the user, but which were not used in the raw template by name.
 * </p>
 */
public class ParsedStringTemplate {

    private final static Logger logger = LogManager.getLogger(ParsedStringTemplate.class);

    private final String rawtemplate;
    private final List<CapturePoint> captures;
    private final List<BindPoint> bindpoints;

    public static ParsedStringTemplate of(String rawtemplate, Map<String, String> bindings) {
        return new ParsedStringTemplate(rawtemplate, bindings);
    }

    /**
     * Spans is an even-odd form of (literal, variable, ..., ..., literal)
     * Thus a 1-length span is a single literal, and a 3 length span has a single bind point
     **/
    private final String[] spans;

    /**
     * A map of binding names and recipes (or null)
     */
    private final Map<String, String> bindings = new LinkedHashMap<>();

    /**
     * Parse the given raw template, check the bind points against the provide bindings, and
     * provide detailed template checks for validity.
     *
     * @param rawtemplate       A string template which contains optionally embedded named anchors
     * @param availableBindings The bindings which are provided by the user to fulfill the named anchors in this raw template
     */
    public ParsedStringTemplate(String rawtemplate, Map<String, String> availableBindings) {
        this.bindings.putAll(availableBindings);
        this.rawtemplate = rawtemplate;

        CapturePointParser capturePointParser = new CapturePointParser();
        CapturePointParser.Result captureData = capturePointParser.apply(rawtemplate);
        this.captures = captureData.getCaptures();

        BindPointParser bindPointParser = new BindPointParser();
        BindPointParser.Result bindPointsResult = bindPointParser.apply(captureData.getRawTemplate(), availableBindings);
        this.spans = bindPointsResult.getSpans().toArray(new String[0]);
        this.bindpoints = bindPointsResult.getBindpoints();
    }

    public BindType getType() {
        if (this.spans.length == 1) {
            return BindType.literal;
        } else if (this.spans[0].isEmpty() && this.spans[2].isEmpty()) {
            return BindType.bindref;
        } else {
            return BindType.concat;
        }
    }

    public ParsedStringTemplate orError() {
        if (hasError()) {
            throw new RuntimeException("Unable to parse statement: " + this);
        }
        return this;
    }

    public String toString() {
        String sb = "parsed: " +
            StreamSupport.stream(Arrays.spliterator(spans), false)
                .map(s -> "[" + s + "]").collect(Collectors.joining(",")) +
            (getMissing().size() > 0 ? "\n missing bindings: " +
                getMissing().stream().collect(Collectors.joining(",", "[", "]")) : "");
        return sb;
    }

    /**
     * @return true if the parsed statement is not usable.
     */
    public boolean hasError() {
        return getMissing().size() > 0;
    }

    /**
     * Returns a list of binding names which were referenced
     * in either <pre>{anchor}</pre> or <pre>?anchor</pre> form,
     * but which were not present in the provided bindings map.
     * If any binding names are present in the returned set, then
     * this binding will not be usable.
     *
     * @return A list of binding names which were referenced but not defined*
     */
    public Set<String> getMissing() {
        if (spans.length == 1) {
            return Set.of();
        }

        HashSet<String> missing = new HashSet<>();
        for (int i = 1; i < spans.length; i += 2) {
            if (!bindings.containsKey(spans[i])) {
                missing.add(spans[i]);
            }
        }

        return missing;
    }

    /**
     * @return a list of anchors as found in the raw template.
     */
    public List<String> getAnchors() {
        List<String> anchors = new ArrayList<>();

        for (int i = 1; i < spans.length; i += 2) {
            anchors.add(spans[i]);
        }
        return anchors;

    }

    /**
     * Get the named anchors and their associated binding specifiers as found
     * in the raw template.
     *
     * @return A list of bind points
     * @throws InvalidParameterException if the template has an error,
     *                                   such as an anchor which has no provided binding.
     */
    public List<BindPoint> getBindPoints() {
        bindpoints.forEach(b -> {
            if (b.getBindspec() == null || b.getBindspec().isEmpty()) {
                throw new RuntimeException("No binding spec was provided for bind point '" + b + "'");
            }
        });
        return bindpoints;
    }

    /**
     * Return the statement that can be used as-is by any driver specific version.
     * This uses the anchor token as provided to yield a version of the statement
     * which contains positional anchors, but no named bindings.
     *
     * @param tokenFormatter The mapping from a token name to a place holder
     * @return A driver or usage-specific format of the statement, with anchors
     */
    public String getPositionalStatement(Function<String, String> tokenFormatter) {
        StringBuilder sb = new StringBuilder(spans[0]);

        for (int i = 1; i < spans.length; i += 2) {
            sb.append(tokenFormatter != null ? tokenFormatter.apply(spans[i]) : spans[i]);
            sb.append(spans[i + 1]);
        }
        return sb.toString();
    }
    public String getPositionalStatement() {
        return getPositionalStatement(s -> s);
    }

    /**
     * Return the parsed template in (<em>literal, variable, ..., ..., literal</em>) form.
     *
     * @return the sections of spans within the template
     */
    public String[] getSpans() {
        return spans;
    }


    /**
     * Returns the parsed template as a single binding spec if and only if the pattern matches
     * a single binding anchor with no prefix or suffix.
     *
     * @return A single binding spec if that is all that was specified.
     */
    public Optional<BindPoint> asBinding() {
        if (spans.length == 3 && spans[0].isEmpty() && spans[2].isEmpty()) {
            return Optional.of(bindpoints.get(0));
        } else {
            return Optional.empty();
        }
    }

    public List<CapturePoint> getCaptures() {
        return this.captures;
    }

    public String getStmt() {
        return rawtemplate;
    }

}
