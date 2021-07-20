package io.nosqlbench.engine.api.activityconfig.yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.util.Tagged;
import io.nosqlbench.nb.api.config.params.Element;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
public abstract class OpTemplate implements Tagged {

    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // TODO: coalesce Gson instances to a few statics on a central NB API class

    public final static String FIELD_DESC = "description";
    public final static String FIELD_NAME = "name";
    public final static String FIELD_OP = "op";
    public final static String FIELD_BINDINGS = "bindings";
    public final static String FIELD_PARAMS = "params";
    public final static String FIELD_TAGS = "tags";

    /**
     * @return a description for the op template, or an empty string
     */
    public abstract String getDesc();

    /**
     * @return a name for the op template, user-specified or auto-generated
     */
    public abstract String getName();

    /**
     * Return a map of tags for this statement. Implementations are required to
     * add a tag for "name" automatically when this value is set during construction.
     * @return A map of assigned tags for the op, with the name added as an auto-tag.
     */
    public abstract Map<String, String> getTags();

    public abstract Map<String, String> getBindings();

    public abstract Map<String, Object> getParams();

    public <T> Map<String, T> getParamsAsValueType(Class<? extends T> type) {
        Map<String, T> map = new LinkedHashMap<>();
        for (String pname : getParams().keySet()) {
            Object object = getParams().get(pname);
            if (object != null) {
                if (type.isAssignableFrom(object.getClass())) {
                    map.put(pname, type.cast(object));
                } else {
                    throw new RuntimeException("With param named '" + pname + "" +
                        "' You can't assign an object of type '" + object.getClass().getSimpleName() + "" +
                        "' to '" + type.getSimpleName() + "'. Maybe the YAML format is suggesting the wrong type.");
                }
            }
        }
        return map;
    }


    public <V> V removeParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);

        if (!getParams().containsKey(name)) {
            return defaultValue;
        }

        Object value = getParams().remove(name);

        try {
            return (V) defaultValue.getClass().cast(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to cast type " + value.getClass().getCanonicalName() + " to " + defaultValue.getClass().getCanonicalName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <V> V getParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);

        if (!getParams().containsKey(name)) {
            return defaultValue;
        }
        Object value = getParams().get(name);
        try {
            return (V) defaultValue.getClass().cast(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to cast type " + value.getClass().getCanonicalName() + " to " + defaultValue.getClass().getCanonicalName(), e);
        }
    }



    public <V> V getParam(String name, Class<? extends V> type) {
        Object object = getParams().get(name);
        if (object == null) {
            return null;
        }
        if (type.isAssignableFrom(object.getClass())) {
            V value = type.cast(object);
            return value;
        }
        throw new RuntimeException("Unable to cast type " + object.getClass().getSimpleName() + " to" +
            " " + type.getSimpleName() + ". Perhaps the yaml format is suggesting the wrong type.");
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> getOptionalStringParam(String name, Class<? extends V> type) {
        if (type.isPrimitive()) {
            throw new RuntimeException("Do not use primitive types for the target class here. For example, Boolean.class is accepted, but boolean.class is not.");
        }
        if (getParams().containsKey(name)) {
            Object object = getParams().get(name);
            if (object == null) {
                return Optional.empty();
            }
            try {
                V reified = type.cast(object);
                return Optional.of(reified);
            } catch (Exception e) {
                throw new RuntimeException("Unable to cast type " + object.getClass().getCanonicalName() + " to " + type.getCanonicalName());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getOptionalStringParam(String name) {
        return getOptionalStringParam(name, String.class);
    }

    /**
     * Parse the statement for anchors and return a richer view of the StmtDef which
     * is simpler to use for most statement configuration needs.
     *
     * @return an optional {@link ParsedTemplate}
     */
    public Optional<ParsedTemplate> getParsed(Function<String,String>... rewriters) {
        Optional<String> os = getStmt();
        return os.map(s -> {
            String result = s;
            for (Function<String, String> rewriter : rewriters) {
                result = rewriter.apply(result);
            }
            return result;
        }).map(s -> new ParsedTemplate(s,getBindings()));
    }

    public Optional<ParsedTemplate> getParsed() {
        return getStmt().map(s -> new ParsedTemplate(s, getBindings()));
    }

    public abstract Optional<Map<String, Object>> getOp();

    public Map<String, Object> asData() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

        if (this.getDesc() != null && !this.getDesc().isBlank()) {
            fields.put(FIELD_DESC, this.getDesc());
        }

        if (this.getBindings().size() > 0) {
            fields.put(FIELD_BINDINGS, this.getBindings());
        }

        if (this.getParams().size() > 0) {
            fields.put(FIELD_PARAMS, this.getParams());
        }

        if (this.getTags().size() > 0) {
            fields.put(FIELD_TAGS, this.getTags());
        }

        this.getOp().ifPresent(o -> fields.put(FIELD_OP,o));

        fields.put(FIELD_NAME, this.getName());

        return fields;
    }

    /**
     * Legacy support for String form statements. This is left here as a convenience method,
     * however it is changed to an Optional to force caller refactorings.
     *
     * @return An optional string version of the op, empty if there is no 'stmt' property in the op fields, or no op fields at all.
     */
    public Optional<String> getStmt() {
        return getOp().map(m->m.get("stmt")).map(s->{
            if (s instanceof CharSequence) {
                return s.toString();
            } else {
                return gson.toJson(s);
            }
        });
    }

    public Element getParamReader() {
        return NBParams.one(getName(),getParams());
    }
}
