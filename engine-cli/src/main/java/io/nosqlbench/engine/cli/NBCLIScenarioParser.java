package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.Scenarios;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.exceptions.BasicError;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBCLIScenarioParser {

    public final static String SILENT_LOCKED = "==";
    public final static String VERBOSE_LOCKED = "===";
    public final static String UNLOCKED = "=";

    private final static Logger logger = LoggerFactory.getLogger(NBCLIScenarioParser.class);

    public static boolean isFoundWorkload(String word) {
        Optional<Path> workloadPath = NosqlBenchFiles.findOptionalPath(word, "yaml", "activities");
        return workloadPath.isPresent();
    }

    public static void parseScenarioCommand(LinkedList<String> arglist) {

        String workloadName = arglist.removeFirst();
        Optional<Path> workloadPathSearch = NosqlBenchFiles.findOptionalPath(workloadName, "yaml", "activities");
        Path workloadPath = workloadPathSearch.orElseThrow();

        List<String> scenarioNames = new ArrayList<>();
        while (arglist.size() > 0
            && !arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")
            && !NBCLIOptions.RESERVED_WORDS.contains(arglist.peekFirst())) {
            scenarioNames.add(arglist.removeFirst());
        }
        if (scenarioNames.size() == 0) {
            scenarioNames.add("default");
        }

        // Load in user's CLI options
        LinkedHashMap<String, String> userCli = new LinkedHashMap<>();
        while (arglist.size() > 0
            && arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")) {
            String[] arg = arglist.removeFirst().split("=");
            arg[0] = Synonyms.canonicalize(arg[0], logger);
            if (userCli.containsKey(arg[0])) {
                throw new BasicError("duplicate occurence of option on command line: " + arg[0]);
            }
            userCli.put(arg[0], arg[1]);
        }

        // This will hold the command to be prepended to the main arglist
        LinkedList<String> buildCmdBuffer = new LinkedList<>();

        for (String scenarioName : scenarioNames) {

            // Load in named scenario
            StmtsDocList stmts = StatementsLoader.load(logger, workloadPath.toString());
            Scenarios scenarios = stmts.getDocScenarios();
            List<String> cmds = scenarios.getNamedScenario(scenarioName);
            if (cmds == null) {
                throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                    + "', but you can pick from " + String.join(",", scenarios.getScenarioNames()));
            }

            Pattern cmdpattern = Pattern.compile("(?<name>\\w+)((?<oper>=+)(?<val>.+))?");
            for (String cmd : cmds) {  // each command line of the named scenario
                LinkedHashMap<String, String> usersCopy = new LinkedHashMap<>(userCli);
                LinkedHashMap<String, CmdArg> cmdline = new LinkedHashMap<>();

                String[] cmdparts = cmd.split(" ");
                for (String cmdpart : cmdparts) {
                    Matcher matcher = cmdpattern.matcher(cmdpart);
                    if (!matcher.matches()) {
                        throw new BasicError("Unable to recognize scenario cmd spec in '" + cmdpart + "'");
                    }
                    String name = Synonyms.canonicalize(matcher.group("name"), logger);
                    String oper = matcher.group("oper");
                    String val = matcher.group("val");
                    cmdline.put(name, new CmdArg(name, oper, val));
                }

                LinkedHashMap<String, String> builtcmd = new LinkedHashMap<>();

                for (CmdArg cmdarg : cmdline.values()) {
                    if (usersCopy.containsKey(cmdarg.getName())) {
                        cmdarg = cmdarg.override(usersCopy.remove(cmdarg.getName()));
                    }
                    builtcmd.put(cmdarg.getName(), cmdarg.toString());
                }
                usersCopy.forEach((k, v) -> builtcmd.put(k, k + "=" + v));
                if (!builtcmd.containsKey("workload")) {
                    builtcmd.put("workload", "workload=" + workloadPath.toString());
                }

                logger.debug("Named scenario built command: " + String.join(" ", builtcmd.values()));
                buildCmdBuffer.addAll(builtcmd.values());
            }

        }
        buildCmdBuffer.descendingIterator().forEachRemaining(arglist::addFirst);

    }

    private final static class CmdArg {
        private final String name;
        private final String operator;
        private final String value;
        private String scenarioName;

        public CmdArg(String name, String operator, String value) {
            this.name = name;
            this.operator = operator;
            this.value = value;
        }

        public boolean isReassignable() {
            return "=".equals(operator);
        }
        public boolean isFinalSilent() {
            return "==".equals(operator);
        }
        public boolean isFinalVerbose() {
            return "===".equals(operator);
        }


        public CmdArg override(String value) {
            if (isReassignable()) {
                return new CmdArg(this.name, this.operator, value);
            } else if (isFinalSilent()) {
                return this;
            } else if (isFinalVerbose()) {
                throw new BasicError("Unable to reassign value for locked param '" + name + operator + value + "'");
            } else {
                throw new RuntimeException("impossible!");
            }
        }

        @Override
        public String toString() {
            return name + (operator != null ? "=" : "") + (value != null ? value : "");
        }

        public String getName() {
            return name;
        }
    }

//    private static void parseWorkloadYamlCmds(String yamlPath, LinkedList<String> arglist, String scenarioName) {
//        StmtsDocList stmts = StatementsLoader.load(logger, yamlPath);
//
//        Scenarios scenarios = stmts.getDocScenarios();
//
//        String scenarioName = "default";
//        if (scenarioName != null) {
//            scenarioName = scenarioName;
//        }
//
//        List<String> cmds = scenarios.getNamedScenario(scenarioName);
//
//
//        Map<String, String> paramMap = new HashMap<>();
//        while (arglist.size() > 0 && arglist.peekFirst().contains("=")) {
//            String arg = arglist.removeFirst();
//            String oldArg = arg;
//            arg = Synonyms.canonicalize(arg, logger);
//
//            for (int i = 0; i < cmds.size(); i++) {
//                String yamlCmd = cmds.get(i);
//                String[] argArray = arg.split("=");
//                String argKey = argArray[0];
//                String argValue = argArray[1];
//                if (!yamlCmd.contains(argKey)) {
//                    cmds.set(i, yamlCmd + " " + arg);
//                } else {
//                    paramMap.put(argKey, argValue);
//                }
//            }
//        }
//
//
//        if (cmds == null) {
//            List<String> names = scenarios.getScenarioNames();
//            throw new RuntimeException("Unknown scenario name, make sure the scenario name you provide exists in the workload definition (yaml):\n" + String.join(",", names));
//        }
//
//        for (String cmd : cmds) {
//            String[] cmdArray = cmd.split(" ");
//
//            for (String parameter : cmdArray) {
//                if (parameter.contains("=")) {
//                    if (!parameter.contains("TEMPLATE(") && !parameter.contains("<<")) {
//                        String[] paramArray = parameter.split("=");
//                        paramMap.put(paramArray[0], paramArray[1]);
//                    }
//                }
//            }
//
//            StrSubstitutor sub1 = new StrSubstitutor(paramMap, "<<", ">>", '\\', ",");
//            StrSubstitutor sub2 = new StrSubstitutor(paramMap, "TEMPLATE(", ")", '\\', ",");
//
//            cmd = sub2.replace(sub1.replace(cmd));
//
//            if (cmd.contains("yaml=") || cmd.contains("workload=")) {
//                parse(cmd.split(" "));
//            } else {
//                parse((cmd + " workload=" + yamlPath).split(" "));
//            }
//
//            // Is there a better way to do this than regex?
//
//        }
//    }

}
