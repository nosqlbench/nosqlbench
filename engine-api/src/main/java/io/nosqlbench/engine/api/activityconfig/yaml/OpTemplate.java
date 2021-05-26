package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.util.Tagged;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>The OpTemplate is the developer's view of the operational templates that users
 * provide in YAML or some other structured format.</p>
 *
 * <H2>Terms</H2>
 * Within this documentation, the word <i>OpTemplate</i> will refer to the template API and
 * semantics. The word <i>user template</i> will refer to the configuration data as provided
 * by a user.
 *
 * <p>OpTemplates are the native Java representation of the user templates that specify how to
 * make an executable operation. OpTemplates are not created for each operation, but are used
 * to create an mostly-baked intermediate form commonly known as a <i>ready op</i>.
 * It is the intermediate form which is used to create an instance of an executable
 * op in whichever way is the most appropriate and efficient for a given driver.</p>
 *
 * <p>This class serves as the canonical documentation and API for how user templates
 * are mapped into a fully resolved OpTemplate. User-provided op templates can be
 * any basic data structure, and are often provided  as part of a YAML workload file.
 * The description below will focus on structural rules rather than any particular
 * encoding format. The types used are fairly universal and easy to map from one
 * format to another.</p>
 *
 *
 * <p>A long-form introduction to this format is included in the main NoSQLBench docs
 * at <a href="http://docs.nosqlbench.io">docs.nosqlbench.io</a>
 * under the <I>Designing Workloads</I> section.</p>
 *
 * <p>A few structural variations are allowed -- No specific form enforced. The reasons for this are:
 * 1) It is generally obvious what as user wants to do from a given layout. 2) Data structure
 * markup is generally frustrating and difficult to troubleshoot. 3) The conceptual domain of
 * NB op construction is well-defined enough to avoid ambiguity.</p>
 *
 * <H2>Type Conventions</H2>
 *
 * For the purposes of simple interoperability, the types used at this interface boundary should
 * be limited to common scalar types -- numbers and strings, and simple structures like maps and lists.
 * The basic types defined for ECMAScript should eventually be supported, but no domain-specific
 * objects which would require special encoding or decoding rules should be used.
 *
 * <H2>Standard Properties</H2>
 *
 * Each op template can have these standard properties:
 * <UL>
 * <LI>name - every op template has a name, even if it is auto generated for you. This is used to
 * name errors in the log, to name metrics in telemetry, and so on.</LI>
 * <LI>description - an optional description, defaulted to "".</LI>
 * <LI>statement - An optional string value which represents an opaque form of the body of
 * an op template</LI>
 * <LI>params - A string-object map of zero or more named parameters, where the key is taken as the parameter
 * name and the value is any simple object form as limited by type conventions above.
 * <LI>bindings - A map of binding definitions, where the string key is taken as the anchor name, and the
 * string value is taken as the binding recipe.</LI>
 * <LI>tags - A map of tags, with string names and values</LI>
 * </UL>
 *
 * The user-provided definition of an op template should capture a blueprint of an operation to be executed by
 * a native driver. As such, you need either a statement or a set of params which can describe what
 * specific type should be constructed. The rules on building an executable operation are not enforced
 * by this API. Yet, responsible NB driver developers will clearly document what the rules
 * are for specifying each specific type of operation supported by an NB driver with examples in YAML format.
 *
 * <H2>OpTemplate Construction Rules</H2>
 *
 * <p>The available structural forms follow a basic set of rules for constructing the OpTemplate in a consistent way.
 * <OL>
 * <LI>A collection of user-provided op templates is provided as a string, a list or a map.</LI>
 * <LI>All maps are order-preserving, like {@link java.util.LinkedHashMap}</LI>
 * <LI>For maps, the keys are taken as the names of the op template instances.</LI>
 * <LI>The content of each op template can be provided as a string or as a map.</LI>
 * <OL>
 * <LI>If the op template entry is provided as a string, then the OpTemplate is constructed as having only a single
 * <i>statement</i> property (in addition to defaults within scope).
 * as provided by OpTemplate API.</LI>
 * <LI>If the op template entry is provided as a map, then the OpTemplate is constructed as having all of the
 * named properties defined in the standard properties above.
 * Any entry in the template which is not a reserved word is assigned to the params map as a parameter, in whatever structured
 * type is appropriate (scalar, lists, maps).</LI>
 * </LI>
 * </p>
 * </OL>
 *
 * <H2>Example Forms</H2>
 * The valid forms are shown below as examples.
 *
 * <H3>One String Statement</H3>
 * <pre>{@code
 * statement: statement
 * }</pre>
 *
 * <H3>List of Templates</H3>
 * <pre>{@code
 * statements:
 *   - statement1
 *   - statement2
 * }</pre>
 *
 * <H3>List of Maps</H3>
 * <pre>{@code
 * statements:
 *   - name: name1
 *     stmt: statement body
 *     params:
 *       p1: v1
 *       p2: v2
 * }</pre>
 *
 * <H3>List Of Condensed Maps</H3>
 * <pre>{@code
 * statements:
 *   - name1: statement body
 *     p1: v1
 *     p2: v2
 * }</pre>
 */
public interface OpTemplate extends Tagged {

    String getName();

    Object getOp();

    Map<String, String> getBindings();

    Map<String, Object> getParams();

    <T> Map<String, T> getParamsAsValueType(Class<? extends T> type);

    <V> V removeParamOrDefault(String name, V defaultValue);

    @SuppressWarnings("unchecked")
    <V> V getParamOrDefault(String name, V defaultValue);

    <V> V getParam(String name, Class<? extends V> type);

    @SuppressWarnings("unchecked")
    <V> Optional<V> getOptionalStringParam(String name, Class<? extends V> type);

    Optional<String> getOptionalStringParam(String name);

    Map<String, String> getTags();

    /**
     * Parse the statement for anchors and return a richer view of the StmtDef which
     * is simpler to use for most statement configuration needs.
     *
     * @return a new {@link ParsedStmt}
     */
    ParsedStmt getParsed(Function<String, String>... transforms);

    String getDesc();

    Map<String, Object> asData();

    /**
     * Legacy support for String form statements. This will be replaced after refactoring.
     * @return A string version of the op
     * @throws io.nosqlbench.nb.api.errors.BasicError if the op is not a CharSequence
     */
    String getStmt();
}
