package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;


public class BasicScriptBuffer implements ScriptBuffer {

    private final static Logger logger = LoggerFactory.getLogger(Cmd.class);

    private final StringBuilder sb = new StringBuilder();
    private final Map<String, String> scriptParams = new HashMap<>();
    private final String createPath;

//    public BasicScriptBuffer() {
//        this.createPath = null;
//    }

    public BasicScriptBuffer() {
        this.createPath =  "_scenario.js";
    }

    public BasicScriptBuffer(String createPath) {
        this.createPath = createPath;
    }

    public ScriptBuffer add(Cmd cmd) {
        Map<String, String> params = cmd.getParams();

        switch (cmd.getCmdType()) {
            case script:
//                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
//                sb.append(";\n");
                combineGlobalParams(scriptParams, cmd);
                String scriptData = loadScript(cmd);
                sb.append(scriptData);
                break;
            case fragment:
//                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
//                sb.append(";\n");
                combineGlobalParams(scriptParams, cmd);
                sb.append(cmd.getArg("script_fragment"));
                if (cmd.getArg("script_fragment").endsWith(";")) {
                    sb.append("\n");
                }
                break;
            case start: // start activity
            case run: // run activity
            case await: // await activity
            case stop: // stop activity
            case waitMillis:

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

    /**
     * Merge the params from the command into the global params map, but ensure that users know
     * if they are overwriting values, which could cause difficult to find bugs in their scripts.
     *
     * @param scriptParams The existing global params map
     * @param cmd          The command containing the new params to merge in
     */
    private void combineGlobalParams(Map<String, String> scriptParams, Cmd cmd) {
        for (String newkey : cmd.getParams().keySet()) {
            String newvalue = cmd.getParams().get(newkey);

            if (scriptParams.containsKey(newkey)) {
                logger.warn("command '" + cmd.getCmdType() + "' overwrote param '" + newkey + " as " + newvalue);
            }
            scriptParams.put(newkey, newvalue);
        }
    }

    @Override
    public ScriptBuffer add(Cmd... cmds) {
        for (Cmd cmd : cmds) {
            add(cmd);
        }
        return this;
    }

    @Override
    public String getParsedScript() {
        String scripttext = sb.toString();

        if (this.createPath != null && !this.createPath.isEmpty()) {
            Path tocreate = Path.of(createPath);

            if (Files.exists(tocreate) && !tocreate.getFileName().toString().startsWith("_")) {
                throw new BasicError("Unable to overwrite file at " + tocreate.toString() + ". If you start the name " +
                    "with _, it will always be overwritten.");
            }
            try {
                String appended = "//@ sourceURL="+tocreate.toString()+"\n\n" + scripttext;
                Files.writeString(tocreate, appended, StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE);
                logger.debug("Wrote script to " + tocreate.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return scripttext;
    }

    @Override
    public Map<String, String> getCombinedParams() {
        return scriptParams;
    }

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


}
