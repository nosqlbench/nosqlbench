package io.nosqlbench.engine.api.scenarios;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.Scenarios;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.nb.api.config.Synonyms;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NBCLIScenarioParser {

    public final static String SILENT_LOCKED = "==";
    public final static String VERBOSE_LOCKED = "===";
    public final static String UNLOCKED = "=";

    private final static Logger logger = LoggerFactory.getLogger(NBCLIScenarioParser.class);
    private static final String SEARCH_IN = "activities";

    public static boolean isFoundWorkload(String workload,
                                          String... includes) {
        Optional<Content<?>> found = NBIO.all()
            .prefix("activities")
            .prefix(includes)
            .name(workload)
            .extension("yaml")
            .first();
        return found.isPresent();
    }

    public static void parseScenarioCommand(LinkedList<String> arglist,
                                            Set<String> RESERVED_WORDS,
                                            String... includes) {

        String workloadName = arglist.removeFirst();
        Optional<Content<?>> found = NBIO.all()
            .prefix("activities")
            .prefix(includes)
            .name(workloadName)
            .extension("yaml")
            .first();
//
        Content<?> workloadContent = found.orElseThrow();

//        Optional<Path> workloadPathSearch = NBPaths.findOptionalPath(workloadName, "yaml", false, "activities");
//        Path workloadPath = workloadPathSearch.orElseThrow();

        // Buffer in CLI word from user, but only until the next command
        List<String> scenarioNames = new ArrayList<>();
        while (arglist.size() > 0
            && !arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")
            && !RESERVED_WORDS.contains(arglist.peekFirst())) {
            scenarioNames.add(arglist.removeFirst());
        }
        if (scenarioNames.size() == 0) {
            scenarioNames.add("default");
        }

        // Parse CLI command into keyed parameters, in order
        LinkedHashMap<String, String> userParams = new LinkedHashMap<>();
        while (arglist.size() > 0
            && arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")) {
            String[] arg = arglist.removeFirst().split("=");
            arg[0] = Synonyms.canonicalize(arg[0], logger);
            if (userParams.containsKey(arg[0])) {
                throw new BasicError("duplicate occurrence of option on command line: " + arg[0]);
            }
            userParams.put(arg[0], arg[1]);
        }

        StrInterpolator userParamsInterp = new StrInterpolator(userParams);

        // This will hold the command to be prepended to the main arglist
        LinkedList<String> buildCmdBuffer = new LinkedList<>();

        for (String scenarioName : scenarioNames) {

            // Load in named scenario
            Content<?> yamlWithNamedScenarios = NBIO.all()
                .prefix(SEARCH_IN)
                .prefix(includes)
                .name(workloadName)
                .extension("yaml")
                .one();

//            // TODO: ugly hack remove this
//            workloadName = (workloadName.endsWith(".yaml")) ? workloadName : workloadName + ".yaml";
//            StmtsDocList stmts = StatementsLoader.load(logger, workloadName, SEARCH_IN);


            StmtsDocList stmts = StatementsLoader.load(logger,yamlWithNamedScenarios);


            Scenarios scenarios = stmts.getDocScenarios();
            List<String> cmds = scenarios.getNamedScenario(scenarioName);
            if (cmds == null) {
                throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                    + "', but you can pick from " + String.join(",", scenarios.getScenarioNames()));
            }

            Pattern cmdpattern = Pattern.compile("(?<name>\\w+)((?<oper>=+)(?<val>.+))?");
            for (String cmd : cmds) {  // each command line of the named scenario
                cmd = userParamsInterp.apply(cmd);
                LinkedHashMap<String, String> usersCopy = new LinkedHashMap<>(userParams);
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
                    builtcmd.put("workload", "workload=" + workloadName);
                }

                // Undefine any keys with a value of 'undef'
                List<String> undefKeys = builtcmd.entrySet()
                    .stream()
                    .filter(e -> e.getValue().toLowerCase().endsWith("=undef"))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                undefKeys.forEach(builtcmd::remove);

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
            return UNLOCKED.equals(operator);
        }
        public boolean isFinalSilent() {
            return SILENT_LOCKED.equals(operator);
        }
        public boolean isFinalVerbose() {
            return VERBOSE_LOCKED.equals(operator);
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

    private static Pattern templatePattern = Pattern.compile("TEMPLATE\\((.+?)\\)");
    private static Pattern innerTemplatePattern = Pattern.compile("TEMPLATE\\((.+?)$");
    private static Pattern templatePattern2 = Pattern.compile("<<(.+?)>>");


    public static List<WorkloadDesc> getWorkloadsWithScenarioScripts(String... includes) {

        List<Content<?>> activities = NBIO.all()
            .prefix(SEARCH_IN)
            .prefix(includes)
            .extension("yaml")
            .list();

        List<Path> yamlPathList = activities.stream().map(Content::asPath).collect(Collectors.toList());

        List<WorkloadDesc> workloadDescriptions = new ArrayList<>();

        for (Path yamlPath : yamlPathList) {
            String referenced = yamlPath.toString();
            referenced = referenced.startsWith("/") ? referenced.substring(1) :
                referenced;

            Content<?> content = NBIO.all().prefix(SEARCH_IN)
                .name(referenced).extension("yaml")
                .one();

            StmtsDocList stmts = StatementsLoader.load(logger,content);

            Map<String, String> templates = new HashMap<>();
            try {
                List<String> lines = Files.readAllLines(yamlPath);
                for (String line : lines) {
                    templates = matchTemplates(line, templates);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            Scenarios scenarios = stmts.getDocScenarios();

            List<String> scenarioNames = scenarios.getScenarioNames();

            if (scenarioNames != null && scenarioNames.size() >0){
                String path = yamlPath.toString();
                path = path.startsWith(FileSystems.getDefault().getSeparator()) ? path.substring(1) : path;
                workloadDescriptions.add(new WorkloadDesc(path, scenarioNames, templates));
            }
        }

        return workloadDescriptions;
    }

    public static Map<String, String> matchTemplates(String line, Map<String, String> templates) {
        Matcher matcher = templatePattern.matcher(line);

        while (matcher.find()) {
            String match = matcher.group(1);

            Matcher innerMatcher = innerTemplatePattern.matcher(match);
            String[] matchArray = match.split(",");
            //TODO: support recursive matches
            if (innerMatcher.find()) {
                String[] innerMatch = innerMatcher.group(1).split(",");

                //We want the outer name with the inner default value
                templates.put(matchArray[0], innerMatch[1]);

            }else{
                templates.put(matchArray[0], matchArray[1]);
            }
        }
        matcher = templatePattern2.matcher(line);

        while (matcher.find()) {
            String match = matcher.group(1);
            String[] matchArray = match.split(":");
            templates.put(matchArray[0],matchArray[1]);
        }
        return templates;
    }


}
