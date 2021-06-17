package com.datastax.ebdrivers.dsegraph;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphProtocol;
import com.datastax.ebdrivers.dsegraph.statements.BindableGraphStatement;
import com.datastax.ebdrivers.dsegraph.statements.ReadyGraphStatementTemplate;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.engine.api.scripting.GraalJsEvaluator;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.engine.api.util.TagFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("Duplicates")
public class GraphActivity extends SimpleActivity implements ActivityDefObserver {
    private final static Logger logger = LogManager.getLogger(GraphActivity.class);

    public Timer bindTimer;
    public Timer executeTimer;
    public Timer resultTimer;
    public Timer logicalGraphOps;
    public Histogram triesHisto;
    protected List<OpTemplate> stmts;
    private int stride;
    private DseSession session;
    private DseCluster cluster;
    private ExceptionMeterMetrics exceptionMeterMetrics;
    private OpSequence<ReadyGraphStatementTemplate> opsequence;

    public GraphActivity(ActivityDef activityDef) {
        super(activityDef);
        StrInterpolator interp = new StrInterpolator(activityDef);
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
    }

    @Override
    public void initActivity() {
        logger.debug("initializing activity: " + this.activityDef.getAlias());

        stride = activityDef.getParams().getOptionalInteger("stride").orElse(1);
        cluster = createCluster();
        session = createSession();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");
        logicalGraphOps = ActivityMetrics.timer(activityDef, "graphops");

        this.opsequence = initSequencer();
        setDefaultsFromOpSequence(this.opsequence);

        onActivityDefUpdate(activityDef);
    }

    private OpSequence<ReadyGraphStatementTemplate> initSequencer() {
        SequencerType sequencerType = SequencerType.valueOf(
                getParams().getOptionalString("seq").orElse("bucket")
        );
        SequencePlanner<ReadyGraphStatementTemplate> planner = new SequencePlanner<>(sequencerType);

        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
        StrInterpolator interp = new StrInterpolator(activityDef);
        StmtsDocList unfiltered = StatementsLoader.loadPath(logger, yaml_loc, interp, "activities");

        // log tag filtering results
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");
        TagFilter tagFilter = new TagFilter(tagfilter);
        unfiltered.getStmts().stream().map(tagFilter::matchesTaggedResult).forEach(r -> logger.info(r.getLog()));

        stmts = unfiltered.getStmts(tagfilter);

        if (stmts.size() == 0) {
            throw new RuntimeException("There were no unfiltered statements found for this activity.");
        }

        for (OpTemplate stmtDef : stmts) {

            ParsedStmt parsed = stmtDef.getParsed().orError();

            ReadyGraphStatementTemplate readyGraphStatement;
            long ratio = Long.valueOf(stmtDef.getParams().getOrDefault("ratio", "1").toString());
            Optional<Integer> repeat = Optional.ofNullable(stmtDef.getParams().get("repeat"))
                    .map(String::valueOf)
                    .map(Integer::valueOf);
            if (repeat.isPresent()) {
                readyGraphStatement = new ReadyGraphStatementTemplate(
                        stmtDef.getName(),
                        GraphStmtParser.getCookedRepeatedStatement(stmtDef.getStmt(), repeat.get()),
                        stmtDef.getParsed().getBindPoints(),
                        GraphStmtParser.getFields(stmtDef.getStmt(), stmtDef.getBindings()).toArray(new String[0]),
                        repeat.get());
            } else {
                readyGraphStatement = new ReadyGraphStatementTemplate(
                        stmtDef.getName(),
                        GraphStmtParser.getCookedStatement(stmtDef.getStmt()),
                        stmtDef.getParsed().getBindPoints(),
                        GraphStmtParser.getFields(stmtDef.getStmt(), stmtDef.getBindings()).toArray(new String[0]));
            }
            planner.addOp(readyGraphStatement, ratio);
        }

        if (getActivityDef().getCycleCount() == 0) {
            getActivityDef().setCycles(String.valueOf(stmts.size()));
        }

        OpSequence<ReadyGraphStatementTemplate> sequencer = planner.resolve();
        return sequencer;
    }

    public DseSession getSession() {
        return session;
    }

    private DseCluster createCluster() {

        String host = activityDef.getParams().getOptionalString("host").orElse("localhost");
        int port = activityDef.getParams().getOptionalInteger("port").orElse(9042);

        DseCluster.Builder builder = DseCluster.builder()
                .withPort(port)
                .withCompression(ProtocolOptions.Compression.NONE);

        DseCluster.Builder finalBuilder = builder;
        List<String> hosts = activityDef.getParams().getOptionalString("host", "hosts")
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(List.of("localhost"));

        for (String h : hosts) {
            logger.debug("adding host as contact point: " + h);
            builder.addContactPoint(h);
        }

        Optional<String> usernameOpt = activityDef.getParams().getOptionalString("username");
        Optional<String> passwordOpt = activityDef.getParams().getOptionalString("password");
        Optional<String> passfileOpt = activityDef.getParams().getOptionalString("passfile");

        if (usernameOpt.isPresent()) {
            String username = usernameOpt.get();
            String password;
            if (passwordOpt.isPresent()) {
                password = passwordOpt.get();
            } else if (passfileOpt.isPresent()) {
                Path path = Paths.get(passfileOpt.get());
                try {
                    password = Files.readAllLines(path).get(0);
                } catch (IOException e) {
                    String error = "Error while reading password from file:" + passfileOpt;
                    logger.error(error, e);
                    throw new RuntimeException(e);
                }
            } else {
                String error = "username is present, but neither password nor passfile are defined.";
                logger.error(error);
                throw new RuntimeException(error);
            }
            builder.withCredentials(username, password);
        }

        Optional<String> clusteropts = activityDef.getParams().getOptionalString("cbopts");
        if (clusteropts.isPresent()) {
            try {
                logger.info("applying cbopts:" + clusteropts.get());
                GraalJsEvaluator<DseCluster.Builder> clusterEval = new GraalJsEvaluator<>(DseCluster.Builder.class);
                clusterEval.put("builder", builder);
                String importEnv =
                        "load(\"nashorn:mozilla_compat.js\");\n" +
                                " importPackage(com.google.common.collect.Lists);\n" +
                                " importPackage(com.google.common.collect.Maps);\n" +
                                " importPackage(com.datastax.driver);\n" +
                                " importPackage(com.datastax.driver.core);\n" +
                                " importPackage(com.datastax.driver.core.policies);\n" +
                                "builder" + clusteropts.get() + "\n";
                clusterEval.script(importEnv);
                builder = clusterEval.eval();
                logger.info("successfully applied:" + clusteropts.get());
            } catch (Exception e) {
                logger.error("Unable to evaluate: " + clusteropts.get() + " in script context:" + e.getMessage());
                throw e;
            }
        }

        try {
            cluster = builder.build();
        } catch (Exception e) {
            logger.error("Error while instantiating cluster from builder: " + e.toString(), e);
            throw e;
        }
        activityDef.getParams().getOptionalBoolean("defaultidempotence").ifPresent(
                b -> cluster.getConfiguration().getQueryOptions().setDefaultIdempotence(b)
        );

        String graphson_version = activityDef.getParams().getOptionalString("graphson").orElse("2");
        switch (Integer.valueOf(graphson_version)) {
            case 1:
                cluster.getConfiguration().getGraphOptions().setGraphSubProtocol(GraphProtocol.GRAPHSON_1_0);
                break;
            case 2:
                cluster.getConfiguration().getGraphOptions().setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0);
                break;
        }
        cluster.getConfiguration().getGraphOptions().setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0);
        return cluster;
    }

    private DseSession createSession() {

        try {
            DseSession session = cluster.newSession();
            logger.info("cluster-metadata-allhosts:\n" + session.getCluster().getMetadata().getAllHosts());
            return session;
        } catch (Exception e) {
            logger.error("Error while creating a session for dsegraph: " + e.toString(), e);
            throw e;
        }

    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        ParameterMap params = activityDef.getParams();
        GraphOptions options = cluster.getConfiguration().getGraphOptions();

        params.getOptionalString("graphlanguage").ifPresent(options::setGraphLanguage);
        params.getOptionalString("graphname").ifPresent(options::setGraphName);
        params.getOptionalString("graphsource").ifPresent(options::setGraphSource);

        params.getOptionalString("graph_read_cl").ifPresent(
                s -> options.setGraphReadConsistencyLevel(ConsistencyLevel.valueOf(s))
        );

        params.getOptionalString("graph_write_cl").ifPresent(
                s -> options.setGraphWriteConsistencyLevel(ConsistencyLevel.valueOf(s))
        );

        params.getOptionalLong("graph_write_cl").ifPresent(
                i -> options.setReadTimeoutMillis(i.intValue())
        );

    }

    /**
     * Return the stride as configured in the activity parameters. This only
     * available activity init()
     *
     * @return long stride
     */
    public long getStride() {
        return stride;
    }

    public OpSequence<BindableGraphStatement> getOpSequence() {
        return this.opsequence.transform(ReadyGraphStatementTemplate::resolve);
    }
}
