package io.nosqlbench.driver.webdriver;

import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityimpl.motor.ParamsParser;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Use the {@link StmtDef} template form as a property template for parameterized
 * commands. This is a general purpose template which uses a map of named parameters.
 * The {@code command} property designates the verb component of the command.
 */
public class CommandTemplate {

    private final static Logger logger = LoggerFactory.getLogger(CommandTemplate.class);
    private final String name;
    private LinkedHashMap<String, StringBindings> cmdspec = new LinkedHashMap<>();

    public CommandTemplate(StmtDef stmt, boolean canonicalize) {
        this.name = stmt.getName();
        String prefixed = "command=" + stmt.getStmt();
        Map<String,String> cmdMap = ParamsParser.parse(prefixed, canonicalize);
        Map<String, String> paramsMap = stmt.getParams();
        paramsMap.forEach((k,v) -> {
            if (cmdMap.containsKey(k)) {
                logger.warn("command property override: '" + k + "' superseded by param form with value '" + v + "'");
            }
            cmdMap.put(k,v);
        });

        cmdMap.forEach((param,value) -> {
            ParsedTemplate paramTemplate = new ParsedTemplate(value, stmt.getBindings());
            BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
            StringBindings paramStringBindings = new StringBindingsTemplate(value, paramBindings).resolve();
            cmdspec.put(param,paramStringBindings);
        });

    }

    public CommandTemplate(String command, Map<String,String> bindings, String name, boolean canonicalize) {
        this.name = name;
        Map<String, String> cmdMap = ParamsParser.parse(command, canonicalize);
        cmdMap.forEach((param,value) -> {
            ParsedTemplate paramTemplate = new ParsedTemplate(command,bindings);
            BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
            StringBindings paramStringBindings = new StringBindingsTemplate(value, paramBindings).resolve();
            cmdspec.put(param,paramStringBindings);
        });

    }

    public Map<String,String> getCommand(long cycle) {
        LinkedHashMap<String, String> cmd = new LinkedHashMap<>(cmdspec.size());
        cmdspec.forEach((k,v) -> {
            cmd.put(k,v.bind(cycle));
        });
        return cmd;
    }

    public String getName() {
        return name;
    }
}
