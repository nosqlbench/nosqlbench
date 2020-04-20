package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BasicScriptBuffer implements ScriptBuffer {

    private final StringBuilder sb = new StringBuilder();
    private final Map<String, String> scriptParams = new HashMap<>();


    @Override
    public ScriptBuffer add(Cmd cmd) {
        Map<String, String> params = cmd.getParams();

        if (!cmd.getParams().isEmpty()) {
            sb.append(toJSONParams("params", cmd.getParams()));
        }

        switch (cmd.getCmdType()) {
            case script:
                String scriptData = NBCLIScriptAssembly.loadScript(cmd);
                sb.append(scriptData);
                break;
            case fragment:
                sb.append(cmd.getArg("script_fragment"));
                if (!cmd.getArg("script_fragment").endsWith("\n")) {
                    sb.append("\n");
                }
                break;
            case start: // start activity
            case run: // run activity
                // Sanity check that this can parse before using it
                sb.append("scenario.").append(cmd.toString()).append("(")
                    .append(toJSONBlock(cmd.getParams()))
                    .append(");\n");
                break;
            case await: // await activity
                sb.append("scenario.awaitActivity(\"").append(cmd.getArg("alias_name")).append("\");\n");
                break;
            case stop: // stop activity
                sb.append("scenario.stop(\"").append(cmd.getArg("alias_name")).append("\");\n");
                break;
            case waitmillis:
                long millis_to_wait = Long.parseLong(cmd.getArg("millis_to_wait"));
                sb.append("scenario.waitMillis(").append(millis_to_wait).append(");\n");
                break;
        }
        return this;

    }

    @Override
    public String getParsedScript() {
        return sb.toString();
    }

    public static String toJSONBlock(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        List<String> l = new ArrayList<>();
        map.forEach((k, v) -> l.add("'" + k + "': '" + v + "'"));
        return "{" + String.join(",\n  ", l) + "};\n";
    }

    public static String toJSONParams(String varname, Map<?, ?> map) {
        return "// params.size==" + map.size() + "\n" + varname + "="+toJSONBlock(map);
    }

}
