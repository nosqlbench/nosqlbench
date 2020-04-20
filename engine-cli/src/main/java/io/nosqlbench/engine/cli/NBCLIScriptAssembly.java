package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NBCLIScriptAssembly {
    private final static Logger logger = LoggerFactory.getLogger(NBCLIScriptAssembly.class);

//    public static ScriptData assembleScript(NBCLIOptions options) {
//
//        ScriptBuffer script = new BasicScriptBuffer();
////        Map<String,String> params = new HashMap<>();
//
//        for (Cmd cmd : options.getCommands()) {
//            script.add(cmd);
//        }
//        return script.getParsedScript();
//    }

    public static String assemble(NBCLIOptions options) {
        ScriptBuffer script = new BasicScriptBuffer();
        for (Cmd command : options.getCommands()) {
            script.add(command);
        }
        return script.getParsedScript();
    }

    public static String loadScript(Cmd cmd) {
        String scriptData;
        String script_path = cmd.getArg("script_path");

        logger.debug("Looking for " + script_path);

        Content<?> one = NBIO.all().prefix("scripts").name(script_path).extension("js").one();
        scriptData = one.asString();

        StrInterpolator interpolator = new StrInterpolator(cmd.getParams());
        scriptData = interpolator.apply(scriptData);
        return scriptData;
    }

    public static String toJSON(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        List<String> l = new ArrayList<>();
        map.forEach((k,v) -> l.add("'" + k + "': '" + v + "'"));
        return "params={"+String.join(",\n  ",l)+"};\n";
    }
}
