package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.NBCLICommandParser;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.ScenarioResult;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import io.nosqlbench.engine.rest.services.WorkspaceService;
import io.nosqlbench.engine.rest.transfertypes.RunScenarioRequest;
import io.nosqlbench.engine.rest.transfertypes.ScenarioInfo;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

@Service(WebServiceObject.class)
@Singleton
@Path("/services/executor/")
public class ScenarioExecutorEndpoint implements WebServiceObject {
    private final static Logger logger = LogManager.getLogger(ScenarioExecutorEndpoint.class);

    private ScenariosExecutor executor = new ScenariosExecutor("executor-service", 1);

    @Context
    private Configuration config;


    @POST
    @Path("cli")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response invokeCommand(RunScenarioRequest rq) {

        String name = rq.getScenarioName();
        if (name.equals("auto")) {
            rq.setScenarioName("scenario"+String.valueOf(System.currentTimeMillis()));
        }

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
        NBCLICommandParser.parse(args, cmdList);
        ScriptBuffer buffer = new BasicScriptBuffer();
        buffer.add(cmdList.toArray(new Cmd[0]));

        Scenario scenario = new Scenario(
                rq.getScenarioName(),
                "",
                Scenario.Engine.Graalvm,
                "disabled",
                false,
                true,
                false
        );
        scenario.addScriptText(buffer.getParsedScript());

        executor.execute(scenario);

        return Response.created(UriBuilder.fromResource(ScenarioExecutorEndpoint.class).path(
                "scenario/" + rq.getScenarioName()).build()).build();

    }

    private LinkedList<String> substituteFilenames(RunScenarioRequest rq, LinkedList<String> args) {
        LinkedList<String> newargs = new LinkedList<>();
        for (String arg : args) {
            for (String s : rq.getFilemap().keySet()) {
                arg = arg.replaceAll(s,rq.getFilemap().get(s));
                newargs.add(arg);
            }
        }
        return newargs;
    }

    private void storeFiles(RunScenarioRequest rq) {
        Map<String, String> filemap = rq.getFilemap();

        WorkspaceService workspaces = new WorkspaceService(config);

        for (String filename : filemap.keySet()) {
            try {

                Files.createDirectories(
                    workspaces.getWorkspace(rq.getWorkspace()).getWorkspacePath(),
                        PosixFilePermissions.asFileAttribute(
                                PosixFilePermissions.fromString("rwxr-x---")
                        ));
                java.nio.file.Path tmpyaml = Files.createTempFile(
                        Paths.get("/tmp/nosqlbench"),
                        rq.getScenarioName(),
                        filename
                );
//            // TODO: Find a better way to do this, like scoping resources to executor
//            tmpyaml.toFile().deleteOnExit();

                Files.write(
                        tmpyaml,
                        filemap.get(filename).getBytes(StandardCharsets.UTF_8)
                );
                rq.getFilemap().put(filename, tmpyaml.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

    @GET
    @Path("scenario/{scenarioName}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ScenarioInfo getScenario(@PathParam("scenarioName") String scenarioName) {
        Optional<Scenario> pendingScenario = executor.getPendingScenario(scenarioName);

        if (pendingScenario.isPresent()) {
            Optional<ScenarioResult> pendingResult = executor.getPendingResult(scenarioName);
            return new ScenarioInfo(pendingScenario.get(),pendingResult.orElse(null));
        } else {
            throw new RuntimeException("Scenario name '" + scenarioName + "' not found.");
        }
    }

    @GET
    @Path("scenarios")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized List<ScenarioInfo> getScenarios() {

        List<ScenarioInfo> scenarioInfos = new ArrayList<>();
        List<String> pendingScenarios = executor.getPendingScenarios();

        for (String pendingScenario : pendingScenarios) {
            ScenarioInfo scenarioInfo = getScenario(pendingScenario);
            scenarioInfos.add(scenarioInfo);
        }
        return scenarioInfos;
    }


}
