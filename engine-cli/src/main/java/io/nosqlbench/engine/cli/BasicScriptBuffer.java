package io.nosqlbench.engine.cli;

import java.util.HashMap;
import java.util.Map;


public class BasicScriptBuffer implements ScriptBuffer {

    private final StringBuilder sb = new StringBuilder();
    private final Map<String, String> scriptParams = new HashMap<>();


    @Override
    public ScriptBuffer add(Cmd cmd) {
        Map<String, String> params = cmd.getParams();

        switch (cmd.getCmdType()) {
            case script:
                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
                sb.append(";\n");
                String scriptData = NBCLIScriptAssembly.loadScript(cmd);
                sb.append(scriptData);
                break;
            case fragment:
                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
                sb.append(";\n");
                sb.append(cmd.getArg("script_fragment"));
                if (!cmd.getArg("script_fragment").endsWith("\n")) {
                    sb.append("\n");
                }
                break;
            case start: // start activity
            case run: // run activity
            case await: // await activity
            case stop: // stop activity
            case waitmillis:

            sb.append("scenario.").append(cmd).append("\n");
////                // Sanity check that this can parse before using it
////                sb.append("scenario.").append(cmd.toString()).append("(")
////                    .append(Cmd.toJSONBlock(cmd.getParams(), false))
////                    .append(");\n");
////                break;
//                sb.append("scenario.awaitActivity(\"").append(cmd.getArg("alias_name")).append("\");\n");
//                break;
//                sb.append("scenario.stop(\"").append(cmd.getArg("alias_name")).append("\");\n");
//                break;
//                long millis_to_wait = Long.parseLong(cmd.getArg("millis_to_wait"));
//                sb.append("scenario.waitMillis(").append(millis_to_wait).append(");\n");
                break;
        }
        return this;

    }

    @Override
    public String getParsedScript() {
        return sb.toString();
    }


}
