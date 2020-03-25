package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import io.nosqlbench.engine.api.util.StrInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NBCLIScriptAssembly {
    private final static Logger logger = LoggerFactory.getLogger(NBCLIScriptAssembly.class);

    public static ScriptData assembleScript(NBCLIOptions options) {
        StringBuilder sb = new StringBuilder();
        Map<String,String> params = new HashMap<>();
        for (NBCLIOptions.Cmd cmd : options.getCommands()) {
            String cmdSpec = cmd.getCmdSpec();
            NBCLIOptions.CmdType cmdType = cmd.getCmdType();
            ActivityDef activityDef;
            switch (cmd.getCmdType()) {
                case script:
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    ScriptData scriptData = loadScript(cmd);
                    if (options.getCommands().size()==1) {
                        sb.append(scriptData.getScriptTextIgnoringParams());
                        params = scriptData.getScriptParams();
                    } else {
                        sb.append(scriptData.getScriptParamsAndText());
                    }
                    break;
                case fragment:
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    sb.append(cmd.getCmdSpec());
                    if (!cmdSpec.endsWith("\n")) {
                        sb.append("\n");
                    }
                    break;
                case start: // start activity
                case run: // run activity
                    // Sanity check that this can parse before using it
                    activityDef = ActivityDef.parseActivityDef(cmdSpec);
                    sb.append("// from CLI as ").append(cmd).append("\n")
                            .append("scenario.").append(cmdType.toString()).append("(\"")
                            .append(cmdSpec)
                            .append("\");\n");
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    // work-around for perf issue
                    if (!cmdSpec.endsWith("\n")) {
                        sb.append("\n");
                    }
                    break;
                case await: // await activity
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    sb.append("scenario.awaitActivity(\"").append(cmdSpec).append("\");\n");
                    break;
                case stop: // stop activity
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    sb.append("scenario.stop(\"").append(cmdSpec).append("\");\n");
                    break;
                case waitmillis:
                    sb.append("// from CLI as ").append(cmd).append("\n");
                    sb.append("scenario.waitMillis(").append(cmdSpec).append(");\n");
                    break;
            }
        }

        return new ScriptData(sb.toString(), params);
    }

    private static ScriptData loadScript(NBCLIOptions.Cmd cmd) {
        String scriptData;

        try {
            logger.debug("Looking for " + new File(".").getCanonicalPath() + File.separator + cmd.getCmdSpec());
        } catch (IOException ignored) {
        }

        InputStream resourceAsStream = NosqlBenchFiles.findRequiredStreamOrFile(cmd.getCmdSpec(), "js", "scripts");

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            scriptData = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to buffer " + cmd.getCmdSpec() + ": " + t);
        }
        StrInterpolator interpolator = new StrInterpolator(cmd.getCmdArgs());
        scriptData = interpolator.apply(scriptData);
        return new ScriptData(scriptData,cmd.getCmdArgs());
    }

    public static class ScriptData {
        private final String scriptText;
        private final Map<String, String> scriptParams;

        public ScriptData(String scriptText, Map<String,String> scriptParams) {

            this.scriptText = scriptText;
            this.scriptParams = scriptParams;
        }

        public String getScriptTextIgnoringParams() {
            return scriptText;
        }

        public Map<String, String> getScriptParams() {
            return scriptParams;
        }

        public String getScriptParamsAndText() {
            return "// params:\n" + toJSON(scriptParams) + "\n// script:\n" + scriptText;
        }
    }

    private static String toJSON(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        List<String> l = new ArrayList<>();
        map.forEach((k,v) -> l.add("'" + k + "': '" + v + "'"));
        return "params={"+String.join(",\n  ",l)+"};\n";
    }
}
