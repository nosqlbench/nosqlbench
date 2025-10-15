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
import io.nosqlbench.nb.api.expr.ExprPreprocessor;
import io.nosqlbench.nb.api.expr.ProcessingResult;
import io.nosqlbench.nb.api.expr.TemplateRewriter;
import io.nosqlbench.nb.api.expr.providers.TemplateExprFunctionsProvider;
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
        ExprPreprocessor exprPreprocessor = new ExprPreprocessor();


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
                    List<String> matchingSteps = new LinkedList<>();
                    for (String scenario : scenarios.getScenarioNames()) {
                        Map<String, String> selectedScenario = scenarios.getNamedScenario(scenario);
                        if (selectedScenario.containsKey(scenarioName)) {
                            matchingSteps.add(scenario + "." + scenarioName);
                        }
                    }
                    if (matchingSteps.isEmpty()) {
                        throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                            + "', but you can pick from one of: " + String.join(", ", scenarios.getScenarioNames()));
                    } else {
                        throw new BasicError("Unable to find named scenario '" + scenarioName + "' in workload '" + workloadName
                            + "', you might be looking for one of these scenario steps: " + String.join(", ", matchingSteps));
                    }
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

                // Rewrite TEMPLATE syntax and evaluate expressions
                cmd = TemplateRewriter.rewrite(cmd);
                cmd = exprPreprocessor.process(cmd, null, userProvidedParams);
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

    private static final Pattern WordAndMaybeAssignment =
        Pattern.compile("(?<name>\\w[-_\\d\\w.]*)((?<oper>=+)(?<val>.+))?");
    public static String[] splitCommand(String cmd) {
        // split command by honoring single quotes, double quotes and escape characters
        String[] namedStepPieces = cmd.split(" +(?=([^\"]*\"[^\"]*\")*[^\"]*$)(?=([^']*'[^']*')*[^']*$)");
        Pattern pattern1 = Pattern.compile("(?<!\\\\)\"");
        Pattern pattern2 = Pattern.compile("(?<!\\\\)'");
        for (int i = 0; i < namedStepPieces.length; i++) {
            // check if the quotes are balanced
            String stepPiece = namedStepPieces[i];
            boolean balanced = pattern1.matcher(stepPiece).results().count() % 2 == 0;
            balanced = balanced && (pattern2.matcher(stepPiece).results().count() % 2 == 0);
            if (!balanced) {
                throw new BasicError("Unclosed quote found in scenario cmd '" + cmd + "'");
            }
            // remove single quotes, double quotes and escape character
            stepPiece = pattern1.matcher(stepPiece).replaceAll("");
            stepPiece = pattern2.matcher(stepPiece).replaceAll("");
            namedStepPieces[i] = stepPiece.replaceAll(Matcher.quoteReplacement("\\(?!\\)"), "");
        }
        return namedStepPieces;
    }

    private static LinkedHashMap<String, SCNamedParam> parseStep(String cmd, String stepName, String scenarioName) {
        LinkedHashMap<String, SCNamedParam> parsedStep = new LinkedHashMap<>();

        String[] namedStepPieces = splitCommand(cmd);
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
                ExprPreprocessor templatePreprocessor = new ExprPreprocessor();
                try {
                    String yamlContent = Files.readString(yamlPath);
                    // Rewrite TEMPLATE syntax first
                    String rewritten = TemplateRewriter.rewrite(yamlContent);
                    // Clear previous tracking state
                    TemplateExprFunctionsProvider.clearThreadState();
                    // Process with context to track template variable accesses
                    ProcessingResult result = templatePreprocessor.processWithContext(rewritten, yamlPath.toUri(), Map.of());
                    // Get tracked template variable accesses
                    templates = TemplateExprFunctionsProvider.getTemplateAccesses();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Clean up thread state
                    TemplateExprFunctionsProvider.clearThreadState();
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

}
