package io.nosqlbench.engine.rest.resources;

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.NBCLICommandParser;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.ScenarioResult;
import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import io.nosqlbench.engine.rest.services.WorkSpace;
import io.nosqlbench.engine.rest.services.WorkspaceFinder;
import io.nosqlbench.engine.rest.transfertypes.LiveScenarioView;
import io.nosqlbench.engine.rest.transfertypes.RunScenarioRequest;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.*;

@Service(WebServiceObject.class)
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
        NBCLICommandParser.parse(args, cmdList, workspace.asIncludes());
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

    @GET
    @Path("scenario/{scenarioName}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized LiveScenarioView getScenario(@PathParam("scenarioName") String scenarioName) {
        Optional<Scenario> pendingScenario = executor.getPendingScenario(scenarioName);

        if (pendingScenario.isPresent()) {
            Optional<ScenarioResult> pendingResult = executor.getPendingResult(scenarioName);
            return new LiveScenarioView(pendingScenario.get(), pendingResult.orElse(null));
        } else {
            throw new RuntimeException("Scenario name '" + scenarioName + "' not found.");
        }
    }

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
