package io.nosqlbench.driver.mongodb;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.engine.api.util.TagFilter;

public class MongoActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LoggerFactory.getLogger(MongoActivity.class);

    private String yamlLoc;
    private String connectionString;
    private String databaseName;

    private MongoClient client;
    private MongoDatabase mongoDatabase;
    private boolean showQuery;
    private int maxTries;

    private OpSequence<ReadyMongoStatement> opSequence;

    Timer bindTimer;
    Timer resultTimer;
    Timer resultSuccessTimer;
    Histogram resultSetSizeHisto;
    Histogram triesHisto;

    public MongoActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        // sanity check
        yamlLoc = activityDef.getParams().getOptionalString("yaml", "workload")
                             .orElseThrow(() -> new IllegalArgumentException("yaml is not defined"));
        connectionString = activityDef.getParams().getOptionalString("connection")
                                      .orElseThrow(() -> new IllegalArgumentException("connection is not defined"));
        // TODO: support multiple databases
        databaseName = activityDef.getParams().getOptionalString("database")
                                  .orElseThrow(() -> new IllegalArgumentException("database is not defined"));
    }

    @Override
    public void initActivity() {
        logger.debug("initializing activity: " + this.activityDef.getAlias());
        onActivityDefUpdate(activityDef);

        opSequence = initOpSequencer();
        setDefaultsFromOpSequence(opSequence);

        client = MongoClients.create(connectionString);
        mongoDatabase = client.getDatabase(databaseName);
        showQuery = activityDef.getParams().getOptionalBoolean("showquery")
                               .orElse(false);
        maxTries = activityDef.getParams().getOptionalInteger("maxtries")
                              .orElse(10);

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        resultSuccessTimer = ActivityMetrics.timer(activityDef, "result-success");
        resultSetSizeHisto = ActivityMetrics.histogram(activityDef, "resultset-size");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");
    }

    @Override
    public void shutdownActivity() {
        logger.debug("shutting down activity: " + this.activityDef.getAlias());
        if (client != null) {
            client.close();
        }
    }

    OpSequence<ReadyMongoStatement> initOpSequencer() {
        SequencerType sequencerType = SequencerType.valueOf(
                activityDef.getParams().getOptionalString("seq").orElse("bucket")
        );
        SequencePlanner<ReadyMongoStatement> sequencer = new SequencePlanner<>(sequencerType);

        StmtsDocList stmtsDocList = StatementsLoader.load(logger, yamlLoc, new StrInterpolator(activityDef), "activities");

        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");

        TagFilter tagFilter = new TagFilter(tagfilter);
        stmtsDocList.getStmts().stream().map(tagFilter::matchesTaggedResult).forEach(r -> logger.info(r.getLog()));

        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);
        for (StmtDef stmt : stmts) {
            ParsedStmt parsed = stmt.getParsed().orError();
            String statement = parsed.getPositionalStatement(Function.identity());
            Objects.requireNonNull(statement);

            sequencer.addOp(new ReadyMongoStatement(stmt),
                            Long.parseLong(stmt.getParams().getOrDefault("ratio","1")));
        }

        return sequencer.resolve();
    }

    protected MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    protected OpSequence<ReadyMongoStatement> getOpSequencer() {
        return opSequence;
    }

    protected boolean isShowQuery() {
        return showQuery;
    }

    protected int getMaxTries() {
        return maxTries;
    }
}
