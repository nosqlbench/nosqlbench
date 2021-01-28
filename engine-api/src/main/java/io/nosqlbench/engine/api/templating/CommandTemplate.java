package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.nb.api.config.ParamsParser;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.function.Function;

/**
 * This is a general purpose template which uses a map of named parameters.
 * The result is a template which is comprised of a map of names and values, which can
 * be used to create a cycle-specific map of values that can describe a literal operation
 * for some native driver. How this map is used is context dependent.
 *
 * Generally speaking, the properties in this map are taken as parameters or field values,
 * or a command verb. How the keys in the resulting map are used to construct an operation
 * for execution is entirely dependent on how a developer wants to map these fields to
 * a native driver's API.
 *
 * A CommandTemplate can be crated directly, or from an OpTemplate. Additional map parsers
 * may be provided when needed for specialized forms of syntax or variations which should also
 * be supported. See the constructor docs for details on these variations.
 */
public class CommandTemplate {

    private final static Logger logger = LogManager.getLogger(CommandTemplate.class);

    private final String name;
    private final Map<String, String> statics = new HashMap<>();
    private final Map<String, StringBindings> dynamics = new HashMap<>();

    /**
     * Create a CommandTemplate directly from an OpTemplate.
     *
     * In this form, if {@link OpTemplate#getStmt()}
     * is non-null, then it taken as a line-oriented value and parsed according to default {@link ParamsParser} behavior.
     *
     * Additionally, any op params provided are considered as entries to add to the command template's map.
     *
     * @param optpl An OpTemplate
     */
    public CommandTemplate(OpTemplate optpl) {
        this(optpl.getName(), optpl.getStmt(), optpl.getParamsAsValueType(String.class), optpl.getBindings(), List.of());
    }

    /**
     * Create a CommandTemplate directly from an OpTemplate, as in {@link #CommandTemplate(OpTemplate)},
     * with added support for parsing the oneline form with the provided parsers.
     *
     * In this form, if {@link OpTemplate#getStmt()}
     * is non-null, then it taken as a line-oriented value and parsed according to default {@link ParamsParser} behavior.
     * However, the provided parsers (if any) are used first in order to match alternate forms of syntax.
     *
     * See {@link CommandTemplate#CommandTemplate(String, String, Map, Map, List)} for full details on the provided
     * parsers.
     *
     * @param optpl   An OpTemplate
     * @param parsers A list of parser functions
     */
    public CommandTemplate(OpTemplate optpl, List<Function<String, Map<String, String>>> parsers) {
        this(optpl.getName(), optpl.getStmt(), optpl.getParamsAsValueType(String.class), optpl.getBindings(), parsers);
    }

    /**
     * Create a command template from a set of optional properties.
     *
     * <P>The parsers provided should honor these expectations:
     * <UL>
     * <LI>If the one-line format is not recognized, the parser should return null.</LI>
     * <LI>If the one-line format is recognized, and the values provided are valid, then they should be
     * returned as a {@link Map} of {@link String} to {@link String}.</LI>
     * <LI>Otherwise the parser should throw an exception, signifying either an internal parser error or
     * invalid data.</LI>
     * </UL>
     *
     * If none of the provided parsers (if any) return a map of values for the one-line format, then the default
     * behavior of {@link ParamsParser} is used.
     * </P>
     *
     * @param name            The name of the command template
     * @param oneline         A oneline version of the parameters to be parsed by {@link ParamsParser}
     * @param params          A set of named parameters and values in name:value form.
     * @param bindings        A set of named bindings in name:recipe form.
     * @param optionalParsers A set of functions which, if provided, will be used to read the oneline form.
     */
    public CommandTemplate(String name, String oneline, Map<String, String> params, Map<String, String> bindings, List<Function<String, Map<String, String>>> optionalParsers) {

        this.name = name;
        Map<String, String> cmd = new HashMap<>();

        // Only parse and inject the oneline form if it is defined.
        // The first parser to match and return a map will be the last one tried.
        // If none of the supplemental parsers work, the default params parser is used
        if (oneline != null) {
            List<Function<String, Map<String, String>>> parserlist = new ArrayList<>(optionalParsers);
            parserlist.add(s -> ParamsParser.parse(s, false));
            boolean didParse = false;
            for (Function<String, Map<String, String>> parser : parserlist) {
                Map<String, String> parsed = parser.apply(oneline);
                if (parsed != null) {
                    logger.debug("parsed request: " + parsed.toString());
                    cmd.putAll(parsed);
                    didParse = true;
                    break;
                }
            }
            if (!didParse) {
                throw new RuntimeException("A oneline form was provided for the command template, but none of the " +
                    "provided" +
                    " parsers were able to parse it, not even ParamsParser.parse(...)");
            }
        }

        // Always add the named params, but warn if they overwrite any oneline named params
        params.forEach((k, v) -> {
            if (cmd.containsKey(k)) {
                logger.warn("command property override: '" + k + "' superseded by param form with value '" + v + "'");
            }
        });
        cmd.putAll(params);

        cmd.forEach((param, value) -> {
            ParsedTemplate paramTemplate = new ParsedTemplate(value, bindings);
            if (paramTemplate.getBindPoints().size() > 0) {
                BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
                StringBindings paramStringBindings = new StringBindingsTemplate(value, paramBindings).resolve();
                dynamics.put(param, paramStringBindings);
                statics.put(param, null);
            } else {
                statics.put(param, value);
            }
        });
    }


    /**
     * Applyl the provided binding functions to the command template, yielding a map with concrete values
     * to be used by a native command.
     *
     * @param cycle The cycle value which will be used by the binding functions
     * @return A map of specific values
     */
    public Map<String, String> getCommand(long cycle) {
        HashMap<String, String> map = new HashMap<>(statics);
        dynamics.forEach((k, v) -> {
            map.put(k, v.bind(cycle));
        });
        return map;
    }

    /**
     * The name of the operation
     */
    public String getName() {
        return name;
    }

    /**
     * True if the command template contains all static (non-binding) values.
     */
    public boolean isStatic() {
        return this.dynamics.size() == 0;
    }

    public boolean isStatic(String keyname) {
        return this.statics.containsKey(keyname);
    }

    public boolean isDynamic(String keyname) {
        return this.dynamics.containsKey(keyname);
    }

    public boolean containsKey(String keyname) {
        return this.statics.containsKey(keyname) || this.dynamics.containsKey(keyname);
    }

    /**
     * The set of key names known by this command template.
     */
    public Set<String> getPropertyNames() {
        return this.statics.keySet();
    }


    @Override
    public String toString() {
        return "CommandTemplate{" +
            "name='" + name + '\'' +
            ", statics=" + statics +
            ", dynamics=" + dynamics +
            '}';
    }

    public String getStatic(String staticVar) {
        return statics.get(staticVar);
    }

    public String getStaticOr(String staticVar, String defaultVal) {
        if (statics.containsKey(staticVar)) {
            return statics.get(staticVar);
        }
        return defaultVal;
    }
}
