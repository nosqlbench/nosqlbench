package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityimpl.motor.ParamsParser;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Use the {@link StmtDef} template form as a property template for parameterized commands. This is a general purpose
 * template which uses a map of named parameters. The {@code command} property designates the verb component of the
 * command.
 * <p>
 * To be valid for use with this template type, the template specifier (the stmt String) must either start with command=
 * or have a single word at the start. In either case, the command will be parsed as if it started with a command=...
 * <p>
 * The semantics of command are meant to be generalized. For example, with HTTP, command might mean the HTTP method like
 * GET or PUT that is used. For web driver, it may be a webdriver command as known by the SIDE file format.
 */
public class CommandTemplate {

    private final static Logger logger = LoggerFactory.getLogger(CommandTemplate.class);

    private final String name;
    private final Map<String, String> statics = new HashMap<>();
    private final Map<String, StringBindings> dynamics = new HashMap<>();

    public CommandTemplate(OpTemplate stmt) {
        this(stmt.getName(), stmt.getStmt(), stmt.getParamsAsValueType(String.class), stmt.getBindings(), List.of());
    }

    public CommandTemplate(OpTemplate stmt, List<Function<String, Map<String, String>>> parsers) {
        this(stmt.getName(), stmt.getStmt(), stmt.getParamsAsValueType(String.class), stmt.getBindings(), parsers);
    }

    /**
     * Create a command template from a set of optional properties.
     *
     * @param name     The name of the command template
     * @param oneline  A oneline version of the parameters. Passed as 'stmt' in the yaml format.
     * @param params   A set of named parameters and values in name:value form.
     * @param bindings A set of named bindings in name:recipe form.
     */
    public CommandTemplate(String name, String oneline, Map<String, String> params, Map<String, String> bindings, List<Function<String, Map<String, String>>> optionalParsers) {

        this.name = name;

        Map<String, String> cmd = new HashMap<>();


        // Only parse and inject the oneline form if it is defined.
        // The first parser to match and return a map will be the last one tried.
        // If none of the suppliemental parsers work, the default params parser is used
        if (oneline != null) {
            List<Function<String,Map<String,String>>> parserlist = new ArrayList<>(optionalParsers);
            parserlist.add(s -> ParamsParser.parse(s,false));
            for (Function<String, Map<String, String>> parser : parserlist) {
                Map<String, String> parsed = parser.apply(oneline);
                if (parsed!=null) {
                    logger.debug("parsed request: " + parsed.toString());
                    cmd.putAll(parsed);
                    break;
                }
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


    public Map<String, String> getCommand(long cycle) {
        HashMap<String, String> map = new HashMap<>(statics);
        dynamics.forEach((k, v) -> {
            map.put(k, v.bind(cycle));
        });
        return map;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return this.dynamics.size() == 0;
    }

    public Set<String> getPropertyNames() {
        return this.statics.keySet();
    }

//    private static List<String> namedGroups(String regex) {
//        List<String> namedGroups = new ArrayList<String>();
//
//        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
//
//        while (m.find()) {
//            namedGroups.add(m.group(1));
//        }
//
//        return namedGroups;
//    }

}
