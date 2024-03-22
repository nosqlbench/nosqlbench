/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.scenarios;

import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityconfig.yaml.Scenarios;
import io.nosqlbench.adapters.api.templating.StrInterpolator;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdType;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.nbio.NBPathsAPI;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NBCLIScenarioPreprocessor {

    public final static String SILENT_LOCKED = "==";
    public final static String VERBOSE_LOCKED = "===";
    public final static String UNLOCKED = "=";

    private final static Logger logger = LogManager.getLogger("SCENARIOS");
    private static final String SEARCH_IN = "activities";
    public static final String WORKLOAD_SCENARIO_STEP = "STEP";

    public static boolean isFoundWorkload(String workload, String... includes) {
        Optional<Content<?>> found = NBIO.all()
            .searchPrefixes("activities")
            .searchPrefixes(includes)
            .pathname(workload)
            .extensionSet(RawOpsLoader.YAML_EXTENSIONS)
            .first();
        return found.isPresent();
    }

    public static void rewriteScenarioCommands(LinkedList<String> arglist, List<String> includes) {

        if (arglist.isEmpty()) {
            return;
        }

        String workloadName = arglist.peekFirst();
        Optional<Content<?>> found = NBIO.all()
            .searchPrefixes("activities")
            .searchPrefixes(includes.toArray(new String[0]))
            .pathname(workloadName)
            .extensionSet(RawOpsLoader.YAML_EXTENSIONS)
            .first();
//
        if (!found.isPresent()) {
            return;
        }
        arglist.removeFirst();
        Content<?> workloadContent = found.orElseThrow();

//        Optional<Path> workloadPathSearch = NBPaths.findOptionalPath(workloadName, "yaml", false, "activities");
//        Path workloadPath = workloadPathSearch.orElseThrow();

        // Buffer in scenario names from CLI, only counting non-options non-parameters and non-reserved words
        List<String> scenarioNames = new ArrayList<>();
        while (!arglist.isEmpty()
            && !arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")
            && CmdType.valueOfAnyCaseOrIndirect(arglist.peekFirst())==CmdType.indirect) {
            scenarioNames.add(arglist.removeFirst());
        }
        if (scenarioNames.isEmpty()) {
            scenarioNames.add("default");
        }

        // Parse CLI command into keyed parameters, in order
        LinkedHashMap<String, String> userProvidedParams = new LinkedHashMap<>();
        while (!arglist.isEmpty()
            && arglist.peekFirst().contains("=")
            && !arglist.peekFirst().startsWith("-")) {
            String[] arg = arglist.removeFirst().split("=", 2);
            if (userProvidedParams.containsKey(arg[0])) {
                throw new BasicError("duplicate occurrence of option on command line: " + arg[0]);
            }
            userProvidedParams.put(arg[0], arg[1]);
        }

        // This will buffer the new command before adding it to the main arg list
        LinkedList<String> buildCmdBuffer = new LinkedList<>();
        StrInterpolator userParamsInterp = new StrInterpolator(userProvidedParams);


        // Each Scenario

        for (String scenarioName : scenarioNames) {

            // Load in named scenario
            Content<?> yamlWithNamedScenarios = NBIO.all()
                .searchPrefixes(SEARCH_IN)
                .searchPrefixes(includes.toArray(new String[0]))
                .pathname(workloadName)
                .extensionSet(RawOpsLoader.YAML_EXTENSIONS)
                .first().orElseThrow();

            // TODO: The yaml needs to be parsed with arguments from each command independently to support template vars
            OpsDocList scenariosYaml = OpsLoader.loadContent(yamlWithNamedScenarios, new LinkedHashMap<>(userProvidedParams));
            Scenarios scenarios = scenariosYaml.getDocScenarios();

            String[] nameparts = scenarioName.split("\\.",2);
            Map<String,String> namedSteps = new LinkedHashMap<>();
            if (nameparts.length==1) {
                Map<String, String> namedScenario = scenarios.getNamedScenario(scenarioName);
                if (namedScenario==null) {
                    throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                    + "', but you can pick from one of: " + String.join(", ", scenarios.getScenarioNames()));
                }
                namedSteps.putAll(namedScenario);
            } else {
                Map<String, String> selectedScenario = scenarios.getNamedScenario(nameparts[0]);
                if (selectedScenario==null) {
                    throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                        + "', but you can pick from one of: " + String.join(", ", scenarios.getScenarioNames()));
                }
                String stepname = nameparts[1];
                if (stepname.matches("\\d+")) {
                    stepname = String.format("%03d",Integer.parseInt(nameparts[1]));
                }
                if (selectedScenario.containsKey(stepname)) {
                    namedSteps.put(stepname,selectedScenario.get(stepname));
                } else {
                    throw new BasicError("Unable to find named scenario.step '" + scenarioName + "' in workload '" + workloadName
                        + "', but you can pick from one of: " + selectedScenario.keySet().stream().map(n -> nameparts[0].concat(".").concat(n)).collect(Collectors.joining(", ")));
                }
            }

            if (namedSteps.isEmpty()) {
                throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                    + "', but you can pick from one of: " +
                    String.join(", ", scenarios.getScenarioNames()));
            }

            // each command in Context
            for (Map.Entry<String, String> cmdEntry : namedSteps.entrySet()) {

                String stepName = cmdEntry.getKey();
                String cmd = cmdEntry.getValue();

                // here, we should actually parse the command using argv rules

                cmd = userParamsInterp.apply(cmd);
                LinkedHashMap<String, SCNamedParam> parsedStep = parseStep(cmd, stepName, scenarioName);
                LinkedHashMap<String, String> usersCopy = new LinkedHashMap<>(userProvidedParams);
                LinkedHashMap<String, String> buildingCmd = new LinkedHashMap<>();

                // consume each of the parameters from the steps to produce a composited command
                // order is primarily based on the step template, then on user-provided parameters
                for (SCNamedParam cmdarg : parsedStep.values()) {

                    // allow user provided parameter values to override those in the template,
                    // if the assignment operator used in the template allows for it
                    if (usersCopy.containsKey(cmdarg.getName())) {
                        cmdarg = cmdarg.override(usersCopy.remove(cmdarg.getName()));
                    }

                    buildingCmd.put(cmdarg.getName(), cmdarg.toString());
                }
                usersCopy.forEach((k, v) -> buildingCmd.put(k, k + "=" + v));

                // Undefine any keys with a value of 'undef'
                List<String> undefKeys = buildingCmd.entrySet()
                    .stream()
                    .filter(e -> e.getValue().toLowerCase().endsWith("=undef"))
                    .map(Map.Entry::getKey)
                    .toList();
                undefKeys.forEach(buildingCmd::remove);

                if (!buildingCmd.containsKey("workload")) {
                    buildingCmd.put("workload", "workload=" + workloadName);
                }

                if (!buildingCmd.containsKey("alias")) {
                    buildingCmd.put("alias", "alias=" + WORKLOAD_SCENARIO_STEP);
                }

                if (!buildingCmd.containsKey("container")) {
                    buildingCmd.put("container","container="+scenarioName);
                }
                if (!buildingCmd.containsKey("step")) {
                    buildingCmd.put("step","step="+stepName);
                }

                // TODO: simplify this
                String alias = buildingCmd.get("alias");
                String workloadToken = workloadContent.asPath().getFileName().toString();

                alias = alias.replaceAll("WORKLOAD", sanitize(workloadToken));
                alias = alias.replaceAll("SCENARIO", sanitize(scenarioName));
                alias = alias.replaceAll("STEP", sanitize(stepName));
                alias = (alias.startsWith("alias=") ? alias : "alias=" + alias);
                buildingCmd.put("alias", alias);
                buildingCmd.put("labels","labels=workload:"+sanitize(workloadToken)+",scenario:"+scenarioName);

                logger.debug(() -> "rebuilt command: " + String.join(" ", buildingCmd.values()));
                buildCmdBuffer.addAll(buildingCmd.values());
            }
        }
        buildCmdBuffer.descendingIterator().forEachRemaining(arglist::addFirst);
        logger.debug(() -> "composed command line args to fulfill named scenario: " + arglist);

    }

    public static String sanitize(String word) {
        String sanitized = word;
        sanitized = sanitized.replaceAll("\\..+$", "");
        String shortened = sanitized;
        sanitized = sanitized.replaceAll("-","_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]+", "");

        if (!shortened.equals(sanitized)) {
            logger.warn("The identifier or value '" + shortened + "' was sanitized to '" + sanitized + "' to be compatible with monitoring systems. You should probably change this to make diagnostics easier.");
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            StringBuilder stb = new StringBuilder();
            for (StackTraceElement element : elements) {
                    stb.append("\tat ").append(element).append("\n");
            }
            logger.warn("stacktrace: " + stb.toString());
        }
        return sanitized;
    }

    private static final Pattern WordAndMaybeAssignment = Pattern.compile("(?<name>\\w[-_\\d\\w.]+)((?<oper>=+)(?<val>.+))?");

    private static LinkedHashMap<String, SCNamedParam> parseStep(String cmd, String stepName, String scenarioName) {
        LinkedHashMap<String, SCNamedParam> parsedStep = new LinkedHashMap<>();

        String[] namedStepPieces = cmd.split(" +");
        for (String commandFragment : namedStepPieces) {
            Matcher matcher = WordAndMaybeAssignment.matcher(commandFragment);

            if (commandFragment.equalsIgnoreCase("")) {
                logger.debug("Command fragment discovered to be empty.  Skipping this fragment for cmd: {}", cmd);
                continue;
            }

            if (!matcher.matches()) {
                throw new BasicError("Unable to recognize scenario cmd spec in '" + commandFragment + "'");
            }
            String commandName = matcher.group("name");
            String assignmentOp = matcher.group("oper");
            String assignedValue = matcher.group("val");
            if (parsedStep.containsKey(commandName)) {
                String errorMessage = String.format("Duplicate occurrence of parameter \"%s\" on step \"%s\" of scenario \"%s\", step command: \"%s\"", commandName, stepName, scenarioName, cmd);
                throw new BasicError(errorMessage);
            }
            parsedStep.put(commandName, new SCNamedParam(commandName, assignmentOp, assignedValue));
        }
        return parsedStep;
    }

    private static final Pattern templatePattern = Pattern.compile("TEMPLATE\\((.+?)\\)");
    private static final Pattern innerTemplatePattern = Pattern.compile("TEMPLATE\\((.+?)$");
    private static final Pattern templatePattern2 = Pattern.compile("<<(.+?)>>");

    public static List<WorkloadDesc> filterForScenarios(List<Content<?>> candidates) {

        List<Path> yamlPathList = candidates.stream().map(Content::asPath).toList();

        List<WorkloadDesc> workloadDescriptions = new ArrayList<>();

        for (Path yamlPath : yamlPathList) {

            try {

                String referenced = yamlPath.toString();

                if (referenced.startsWith("/")) {
                    if (yamlPath.getFileSystem() == FileSystems.getDefault()) {
                        Path relative = Paths.get(System.getProperty("user.dir")).toAbsolutePath().relativize(yamlPath);
                        if (!relative.toString().contains("..")) {
                            referenced = relative.toString();
                        }
                    }
                }

                Content<?> content = NBIO.all().searchPrefixes(SEARCH_IN)
                    .pathname(referenced).extensionSet(RawOpsLoader.YAML_EXTENSIONS)
                    .one();

                OpsDocList stmts = null;
                try {
                    stmts = OpsLoader.loadContent(content, new LinkedHashMap<>());
                    if (stmts.getStmtDocs().isEmpty()) {
                        logger.warn("Encountered yaml with no docs in '" + referenced + "'");
                        continue;
                    }
                } catch (Exception e) {
                    logger.warn("Error while loading scenario at '" + referenced + "': " + e);
                    continue;
                }

                Map<String, String> templates = new LinkedHashMap<>();
                try {
                    List<String> lines = Files.readAllLines(yamlPath);
                    for (String line : lines) {
                        templates = matchTemplates(line, templates);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Scenarios scenarios = stmts.getDocScenarios();

                List<String> scenarioNames = scenarios.getScenarioNames();

                if (scenarioNames != null && !scenarioNames.isEmpty()) {
//                String path = yamlPath.toString();
//                path = path.startsWith(FileSystems.getDefault().getSeparator()) ? path.substring(1) : path;
                    LinkedHashMap<String, String> sortedTemplates = new LinkedHashMap<>();
                    ArrayList<String> keyNames = new ArrayList<>(templates.keySet());
                    Collections.sort(keyNames);
                    for (String keyName : keyNames) {
                        sortedTemplates.put(keyName, templates.get(keyName));
                    }

                    String description = stmts.getDescription();
                    workloadDescriptions.add(new WorkloadDesc(referenced, scenarioNames, sortedTemplates, description, ""));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while scanning path '" + yamlPath.toString() + "':" + e.getMessage(), e);
            }

        }
        Collections.sort(workloadDescriptions);

        return workloadDescriptions;

    }

    public static List<WorkloadDesc> getWorkloadsWithScenarioScripts(boolean defaultIncludes, Set<String> includes) {
        return getWorkloadsWithScenarioScripts(defaultIncludes, includes.toArray(new String[0]));
    }

    public static List<WorkloadDesc> getWorkloadsWithScenarioScripts(boolean defaultIncludes, String... includes) {

        NBPathsAPI.GetPrefixes searchin = NBIO.all();
        if (defaultIncludes) {
            searchin = searchin.searchPrefixes(SEARCH_IN);
        }

        List<Content<?>> activities = searchin
            .searchPrefixes(includes)
            .extensionSet(RawOpsLoader.YAML_EXTENSIONS)
            .list();

        return filterForScenarios(activities);

    }

    public static List<String> getScripts(boolean defaultIncludes, String... includes) {

        NBPathsAPI.GetPrefixes searchin = NBIO.all();
        if (defaultIncludes) {
            searchin = searchin.searchPrefixes(SEARCH_IN);
        }

        List<Path> scriptPaths = searchin
            .searchPrefixes("scripts/auto")
            .searchPrefixes(includes)
            .extensionSet("js")
            .list().stream().map(Content::asPath).toList();

        List<String> scriptNames = new ArrayList<>();

        for (Path scriptPath : scriptPaths) {
            String name = scriptPath.getFileName().toString();
            name = name.substring(0, name.lastIndexOf('.'));

            scriptNames.add(name);
        }


        return scriptNames;

    }

    public static Map<String, String> matchTemplates(String line, Map<String, String> templates) {
        Matcher matcher = templatePattern.matcher(line);

        while (matcher.find()) {
            String match = matcher.group(1);

            Matcher innerMatcher = innerTemplatePattern.matcher(match);
            String[] matchArray = match.split("[,:]");
            if (matchArray.length==1) {
                matchArray = new String[]{matchArray[0],""};
            }
//            if (matchArray.length!=2) {
//                throw new BasicError("TEMPLATE form must have two arguments separated by a comma, like 'TEMPLATE(a,b), not '" + match +"'");
//            }
            //TODO: support recursive matches
            if (innerMatcher.find()) {
                String[] innerMatch = innerMatcher.group(1).split("[,:]");

                //We want the outer name with the inner default value
                templates.put(matchArray[0], innerMatch[1]);
            } else {
                templates.put(matchArray[0], matchArray[1]);
            }
        }
        matcher = templatePattern2.matcher(line);

        while (matcher.find()) {
            String match = matcher.group(1);
            String[] matchArray = match.split(":");
            if (matchArray.length == 1) {
                templates.put(matchArray[0], "-none-");
            } else {
                templates.put(matchArray[0], matchArray[1]);
            }
        }
        return templates;
    }


}
