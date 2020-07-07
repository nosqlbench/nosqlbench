/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.virtdata.core.templates;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A parsed template is a form of a raw template which has been parsed for its
 * named anchors and sanity checked against a set of provided bindings.
 *
 * Once the parsed template is constructed, the method {@link ParsedTemplate#orError()}
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
 */
public class ParsedTemplate {

    /**
     * The default patterns match one of two forms:
     * <UL>
     * <LI>an opening curly brace, followed by a word character, followed by any contiguous
     * combination of dashes, underscores, digits, words, and dots, followed by
     * a closing curly brace.</LI>
     * <LI>A question mark, followed by a word character, followed by any contiguous
     * combination of dashes, underscores, digits, word characters, or dots.</LI>
     * </UL>
     *
     * <H2>Examples</H2>
     * <pre>
     * {var1}
     * {var2.var3__-var5}
     * ?var6
     * ?var7.__var8-var9
     * </pre>
     */
    private final static Pattern[] DEFAULT_PATTERNS = new Pattern[]{
            Pattern.compile("\\{(?<anchor>\\w+[-_\\d\\w.]*)}"),
            Pattern.compile("\\?(?<anchor>\\w+[-_\\d\\w.]*)")
    };
    private final static Logger logger  = LogManager.getLogger(ParsedTemplate.class);
    private final Pattern[] patterns;
    // Spans is an even-odd form of (literal, variable, ..., ..., literal)
    private final String rawtemplate;
    private final String[] spans;

    private final Set<String> missingBindings = new HashSet<>();
    private final Set<String> extraBindings = new HashSet<>();
    private final Map<String, String> bindings = new LinkedHashMap<>();
    private final Map<String, String> specificBindings = new LinkedHashMap<>();

    /**
     * Construct a new ParsedTemplate from the provided statement template.
     *
     * @param rawtemplate The string that contains literal sections and anchor sections interspersed
     * @param providedBindings The bindings that are provided for the template to be parsed
     */
    public ParsedTemplate(String rawtemplate, Map<String, String> providedBindings) {
        this(rawtemplate, providedBindings, DEFAULT_PATTERNS);
    }

    /**
     * Parse the given raw template, check the bind points against the provide bindings, and
     * provide detailed template checks for validity.
     *
     * <H4>Overriding Patterns</H4>
     * <P>
     * If patterns are not provided then {@link ParsedTemplate#DEFAULT_PATTERNS} are used, which includes
     * the ability to match {var1} and ?var1 style anchors. If patterns are
     * provided, then they must be compatible with the {@link Matcher#find()} method, and must also
     * have a named group with the name 'anchor', as in (?&lt;anchor&gt;...)
     * </P>
     *
     * @param rawtemplate      A string template which contains optionally embedded named anchors
     * @param providedBindings The bindings which are provided by the user to fulfill the named anchors in this raw template
     * @param providedPatterns The patterns which match the named anchor format and extract anchor names from the raw template
     */
    public ParsedTemplate(String rawtemplate, Map<String, String> providedBindings, Pattern... providedPatterns) {
        this.rawtemplate = rawtemplate;
        this.bindings.putAll(providedBindings);
        this.patterns = providedPatterns;
        this.spans = parse();
    }

    public ParsedTemplate orError() {
        if (hasError()) {
            throw new RuntimeException("Unable to parse statement: " + this.toString());
        }
        return this;
    }

    /**
     * After this method runs, the following conditions should apply:
     * <ul>
     * <li>spans will contain all the literal and variable sections in order, starting a literal, even if it is empty</li>
     * <li>spans will be an odd number in length, meaning that the last section will also be a literal, even if it is empty</li>
     * <li>specificBindings will contain an ordered map of the binding definitions</li>
     * </ul>
     */
    private String[] parse() {
        List<String> spans = new ArrayList<>();
        Set<String> usedAnchors = new HashSet<>();

        extraBindings.addAll(bindings.keySet());
        String statement = rawtemplate;
        int patternsMatched = 0;

        int lastMatch = 0;

        for (Pattern pattern : patterns) {
            if (!pattern.toString().contains("?<anchor>")) {
                throw new InvalidParameterException("The provided pattern '" + pattern.toString() + "' must contain a named group called anchor," +
                        "as in '(?<anchor>...)'");
            }

            Matcher m = pattern.matcher(rawtemplate);

            if (!m.find()) { // sanity check that this matcher works at all or go to the next pattern
                continue;
            }

            while (m.find(lastMatch)) {
                String pre = statement.substring(lastMatch, m.start());
                spans.add(pre);

                String tokenName = m.group("anchor");
                lastMatch = m.end();

                spans.add(tokenName);

                if (extraBindings.contains(tokenName)) {
                    usedAnchors.add(tokenName);
                    specificBindings.put(tokenName, bindings.get(tokenName));
                } else {
                    missingBindings.add(tokenName);
                }
            }
            usedAnchors.forEach(extraBindings::remove);

            break; // If the last matcher worked at all, only do one cycle
        }

        if (lastMatch >= 0) {
            spans.add(statement.substring(lastMatch));
        } else {
            spans.add(statement);
        }

        return spans.toArray(new String[0]);
//
//        //Matcher m = stmtToken.matcher(statement);
//        int lastMatch = 0;
//        String remainder = "";
//        while (m.find(lastMatch)) {
//            String pre = statement.substring(lastMatch, m.start());
//
//            String form1 = m.group(1);
//            String form2 = m.group(2);
//            String tokenName = (form1 != null && !form1.isEmpty()) ? form1 : form2;
//            lastMatch = m.end();
//            spans.add(pre);
//
//            if (extraBindings.contains(tokenName)) {
//                anchors.add(tokenName);
//                bindspecs.add(stmtDef.getBindings().get(tokenName));
//                usedAnchors.add(tokenName);
////                specificBindings.put(tokenName, stmtDef.getBindings().get(tokenName));
//            } else {
//                missingBindings.add(tokenName);
//            }
//        }
//        usedAnchors.forEach(extraBindings::remove);
//
//        if (lastMatch >= 0) {
//            spans.add(statement.substring(lastMatch));
//        } else {
//            spans.add(statement);
//        }
//
//        return spans.toArray(new String[0]);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("template: '").append(rawtemplate).append("'");
        sb.append("\n parsed: ");
        sb.append(StreamSupport.stream(Arrays.spliterator(spans), false)
                .map(s -> "[" + s + "]").collect(Collectors.joining(",")));
        sb.append("\n missing bindings: ")
                .append(missingBindings.stream().collect(Collectors.joining(",", "[", "]")));
        sb.append(" extra bindings: ");
        sb.append("\n extra bindings: ")
                .append(extraBindings.stream().collect(Collectors.joining(",", "[", "]")));
        return sb.toString();
    }

    /**
     * @return true if the parsed statement is not usable.
     */
    public boolean hasError() {
        return missingBindings.size() > 0;
    }

    /**
     * The list of binding names returned by this method does not
     * constitute an error. They may be used for
     * for informational purposes in error handlers, for example.
     *
     * @return a set of bindings names which were provided to
     * this parsed statement, but which were not referenced
     * in either <pre>{anchor}</pre> or <pre>?anchor</pre> form.
     */
    public Set<String> getExtraBindings() {
        return extraBindings;
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
    public Set<String> getMissingBindings() {
        return missingBindings;
    }

    /**
     * Return a map of bindings which were referenced in the statement.
     * This is an easy way to get the list of effective bindings for
     * a statement for diagnostic purposes without including a potentially
     * long list of library bindings. This method does <em>not</em>
     * represent all of the binding points, as when anchor names are
     * used more than once.
     *
     * @return a bindings map of referenced bindings in the statement
     */
    public Map<String, String> getSpecificBindings() {
        return specificBindings;
    }

    /**
     * @return a list of anchors as fou nd in the raw template.
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
        List<BindPoint> bindpoints = new ArrayList<>();
        for (int i = 1; i < spans.length; i += 2) {
            if (!bindings.containsKey(spans[i])) {
                throw new InvalidParameterException("Binding named '" + spans[i] + "' is not provided for template '" + rawtemplate + "'");
            }
            bindpoints.add(new BindPoint(spans[i], bindings.get(spans[i])));
        }

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
            sb.append(tokenFormatter!=null ? tokenFormatter.apply(spans[i]) : spans[i]);
            sb.append(spans[i + 1]);
        }
        return sb.toString();
    }

    /**
     * Return the parsed template in (<em>literal, variable, ..., ..., literal</em>) form.
     *
     * @return the sections of spans within the template
     */
    public String[] getSpans() {
        return spans;
    }


}
