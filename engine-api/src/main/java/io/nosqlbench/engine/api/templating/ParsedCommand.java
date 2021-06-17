package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.nb.api.config.ParamsParser;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Parse an OpTemplate into a ParsedCommand
 */
public class ParsedCommand {

    private final static Logger logger = LogManager.getLogger(ParsedCommand.class);

    /** the name of this operation **/
    private final String name;

    /** The fields which are statically assigned **/
    private final Map<String,Object> statics = new LinkedHashMap<>();

    /**
     * The fields which are dynamic, and must be realized via functions.
     * This map contains keys which identify the field names, and values, which may be null or undefined.
     */
    private final Map<String,String> dynamics = new LinkedHashMap<>();

    /**
     * The names of payload values in the result of the operation which should be saved.
     * The keys in this map represent the name of the value as it would be found in the native
     * representation of a result. If the values are defined, then each one represents the name
     * that the found value should be saved as instead of the original name.
     */
    private final Map<String,String> captures = new LinkedHashMap<>();

    /**
     * Create a parsed command from an Op template. The op template is simply the normalized view of
     * op template structure which is uniform regardless of the original format.
     * @param ot An OpTemplate representing an operation to be performed in a native driver.
     */
    ParsedCommand(OpTemplate ot) {
        this(ot,List.of());
    }

    ParsedCommand(OpTemplate ot, List<Function<String, Map<String, String>>> optionalParsers) {
        this.name = ot.getName();

        Map<String,Object> cmd = new LinkedHashMap<>();

        if (ot.getOp() instanceof CharSequence) {

            String oneline = ot.getOp().toString();
            List<Function<String, Map<String, String>>> parserlist = new ArrayList<>(optionalParsers);
            boolean didParse = false;
            parserlist.add(s -> ParamsParser.parse(s, false));

            for (Function<String, Map<String, String>> parser : parserlist) {
                Map<String, String> parsed = parser.apply(oneline);
                if (parsed != null) {
                    logger.debug("parsed request: " + parsed);
                    cmd.putAll(parsed);
                    didParse = true;
                    break;
                }
            }
        } else if (ot.getOp() instanceof Map) {
            Map<?,?> map = ot.getOp();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                cmd.put(entry.getKey().toString(),entry.getValue());
            }
        } else {
            throw new BasicError("op template has op type of " + ot.getOp().getClass().getCanonicalName() + ", which is not supported.");
        }
        resolveCmdMap(cmd,ot.getBindings());

//        ArrayList<Function<String, Map<String, String>>> _parsers = new ArrayList<>(parsers);

    }

    private void resolveCmdMap(Map<String, Object> cmd, Map<String, String> bindings) {
        Map<String,Object> resolved = new LinkedHashMap<>();
        cmd.forEach((k,v) -> {
            if (v instanceof CharSequence) {
                ParsedTemplate parsed = new ParsedTemplate(v.toString(), bindings);
                switch (parsed.getForm()) {
                    case literal:
                        break;
                    case rawbind:
                    case template:
                        this.dynamics.put(k,v.toString());
                        break;
                }
            } else if (v instanceof Map) {
                Map<String,Object> m = (Map<String, Object>) v;
//                ((Map<?, ?>) v).forEach((k,v) -> {
//
//                });
                resolved.put(k,Map.of("type","Map"));
            } else {

            }
        });
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getStatics() {
        return statics;
    }

    public Map<String, String> getDynamics() {
        return dynamics;
    }
}
