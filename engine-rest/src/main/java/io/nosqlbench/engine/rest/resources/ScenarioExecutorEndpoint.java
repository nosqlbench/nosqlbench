/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.NBCLICommandParser;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.scenario.Scenario;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosExecutor;
import io.nosqlbench.engine.rest.services.WorkSpace;
import io.nosqlbench.engine.rest.services.WorkspaceFinder;
import io.nosqlbench.engine.rest.transfertypes.LiveScenarioView;
import io.nosqlbench.engine.rest.transfertypes.RunScenarioRequest;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Maturity;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Future;

@Service(value = WebServiceObject.class, selector = "scenario-executor")
@Singleton
@Path("/services/executor/")
public class ScenarioExecutorEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(ScenarioExecutorEndpoint.class);

    private final ScenariosExecutor executor = new ScenariosExecutor("executor-service", 1);

    @Context
    private Configuration config;


    @DELETE
    @Path("scenario/{scenario}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cancelScenario(@PathParam("scenario") String scenario) {
        try {
            executor.deleteScenario(scenario);
            return Response.ok("canceled '" + scenario + "' and removed it").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("stop/{scenario}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response stopScenario(@PathParam("scenario") String scenario) {
        try {
            executor.stopScenario(scenario, false);
            return Response.ok("stopped '" + scenario + "' without removing it").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


    /**
     * Run a NoSQLBench command just as you would on the command line. Certain parameters are translated
     * (virtualized) into the workspace view for you automatically. That is, any path which would otherwise
     * be resolved on the local file system will now be resolved in that same way but with the designated workspace
     * as the base directory. All filesystem interaction which would otherwise happen in the current working
     * directory should also be done relative to the designated workspace.
     * @param rq
     * @return
     */
    @POST
    @Path("cli")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response invokeCommand(RunScenarioRequest rq) {

        String name = rq.getScenarioName();

        if (name.equals("auto")) {
            rq.setScenarioName("scenario" + System.currentTimeMillis());
        }
        org.joda.time.format.DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");
        name = name.replaceAll("EPOCHMS", String.valueOf(System.currentTimeMillis()));
        name = name.replaceAll("DATESTAMP", dtf.print(new DateTime()));
        name = name.replaceAll("[:/ ]", "");
        rq.setScenarioName(name);

        WorkSpace workspace = new WorkspaceFinder(config).getWorkspace(rq.getWorkspace());

        // First, virtualize files provided
        storeFiles(rq);

        LinkedList<Cmd> cmdList = new LinkedList<>();
        LinkedList<String> args = new LinkedList<>(rq.getCommands());

        for (String arg : args) {
            if (arg.startsWith("-")) {
                throw new RuntimeException("Only commands (verbs and params) can be used here");
            }
        }

        args = substituteFilenames(rq, args);
        Optional<List<Cmd>> parsed = NBCLICommandParser.parse(args, workspace.asIncludes());
        if (!parsed.isPresent()) {
            return Response.serverError().entity("Unable to render command stream from provided command spec.").build();
        }
        ScriptBuffer buffer = new BasicScriptBuffer();
        buffer.add(cmdList.toArray(new Cmd[0]));

        Scenario scenario = new Scenario(
            rq.getScenarioName(),
            "",
            Scenario.Engine.Graalvm,
            "disabled",
                true,
            false,
            "",
            cmdList.toString(),
            (java.nio.file.Path) config.getProperties().get("logpath"),
            Maturity.Unspecified);

        scenario.addScriptText(buffer.getParsedScript());

        executor.execute(scenario);

        return Response.created(UriBuilder.fromResource(ScenarioExecutorEndpoint.class).path(
                "scenario/" + rq.getScenarioName()).build()).entity("started").build();

    }

    private LinkedList<String> substituteFilenames(RunScenarioRequest rq, LinkedList<String> args) {
        LinkedList<String> newargs = new LinkedList<>();
        for (String arg : args) {
            for (String s : rq.getFilemap().keySet()) {
                arg = arg.replaceAll(s, rq.getFilemap().get(s));
            }
            newargs.add(arg);
        }
        return newargs;
    }

    private void storeFiles(RunScenarioRequest rq) {
        Map<String, String> filemap = rq.getFilemap();
        if (filemap == null) {
            return;
        }

        WorkspaceFinder ws = new WorkspaceFinder(config);
        WorkSpace workspace = ws.getWorkspace(rq.getWorkspace());

        Map<String, String> replacements = new HashMap<>();

        for (String filename : filemap.keySet()) {
            java.nio.file.Path targetPath = workspace.storeFile(filename, filemap.get(filename), replacements);
        }
        rq.setFileMap(replacements);
    }

//    /**
//     * Run a single-activity scenario
//     *
//     * @param scenarioName
//     *         The name to install in the executor
//     * @param params
//     *         The params for the activity
//     *
//     * @return
//     */
//    @POST
//    @Path("scenario/{scenarioName}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public synchronized Response invokeScenario(
//            @PathParam("scenarioName") String scenarioName,
//            Map<String, String> params) {
//        Scenario scenario = null;
//        Optional<Scenario> pendingScenario = executor.getPendingScenario(scenarioName);
//        if (pendingScenario.isPresent()) {
//            scenario = pendingScenario.orElseThrow();
//        } else {
//            scenario = new Scenario(scenarioName, Scenario.Engine.Graalvm);
//        }
//        if (params.containsKey("yamldoc")) {
//            try {
//                java.nio.file.Path tmpyaml = Files.createTempFile(Paths.get("/tmp"), scenarioName, ".yaml");
//                // TODO: Find a better way to do this, like scoping resources to executor
//                tmpyaml.toFile().deleteOnExit();
//                Files.write(tmpyaml, params.get("yamldoc").getBytes(StandardCharsets.UTF_8));
//                params.remove("yamldoc");
//                params.put("yaml", tmpyaml.toString());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        scenario.getScenarioController().apply(params);
//        URI scenarioUri = UriBuilder.fromResource(ScenarioExecutorService.class)
//                .build(scenarioName);
//        return Response.created(scenarioUri).build();
//    }

    /**
     * Return a view of a named scenario, just as with {@link #getScenarios()}}.
     * If the named scenario is not present, an error will be returned instead.
     * @param scenarioName
     * @return
     */
    @GET
    @Path("scenario/{scenarioName}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized LiveScenarioView getScenario(@PathParam("scenarioName") String scenarioName) {
        Optional<Scenario> pendingScenario = executor.getPendingScenario(scenarioName);

        if (pendingScenario.isPresent()) {
            Optional<Future<ExecMetricsResult>> pendingResult = executor.getPendingResult(scenarioName);
            Future<ExecMetricsResult> scenarioResultFuture = pendingResult.get();
            return new LiveScenarioView(pendingScenario.get());
        } else {
            throw new RuntimeException("Scenario name '" + scenarioName + "' not found.");
        }
    }

    /**
     * @return a view of all the scenarios known to the scenarios executor, whether starting,
     * running, errored or otherwise. If the scenario is completed, then the result,
     * including the IO log will be provided, otherwise an exception to explain why it failed.
     */
    @GET
    @Path("scenarios")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response getScenarios() {

        try {
            List<LiveScenarioView> liveScenarioViews = new ArrayList<>();
            List<String> pendingScenarios = executor.getPendingScenarios();

            for (String pendingScenario : pendingScenarios) {
                LiveScenarioView liveScenarioView = getScenario(pendingScenario);
                liveScenarioViews.add(liveScenarioView);
            }
            return Response.ok(liveScenarioViews).build();
        } catch (Exception e) {

            CharArrayWriter caw = new CharArrayWriter();
            PrintWriter pw = new PrintWriter(caw);
            e.printStackTrace(pw);
            String trace = caw.toString();
            return Response.serverError().entity(trace).build();
        }
    }


}
