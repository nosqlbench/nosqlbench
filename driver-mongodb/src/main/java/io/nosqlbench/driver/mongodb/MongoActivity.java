package io.nosqlbench.driver.mongodb;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.util.TagFilter;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(MongoActivity.class);

    private String yamlLoc;
    private String connectionString;
    private String databaseName;

    private MongoClient client;
    private MongoDatabase mongoDatabase;
    private boolean showQuery;

    private OpSequence<ReadyMongoStatement> opSequence;

    Timer bindTimer;
    Timer resultTimer;
    Timer resultSuccessTimer;
    Histogram triesHisto;
    Histogram resultSetSizeHisto;

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

        client = createMongoClient(connectionString);
        mongoDatabase = client.getDatabase(databaseName);
        showQuery = activityDef.getParams().getOptionalBoolean("showquery")
                               .orElse(false);
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

        StmtsDocList stmtsDocList = StatementsLoader.loadPath(logger, yamlLoc, activityDef.getParams(), "activities");

        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");

        TagFilter tagFilter = new TagFilter(tagfilter);
        stmtsDocList.getStmts().stream().map(tagFilter::matchesTaggedResult).forEach(r -> logger.info(r.getLog()));

        List<OpTemplate> stmts = stmtsDocList.getStmts(tagfilter);
        if (stmts.isEmpty()) {
            logger.error("No statements found for this activity");
        } else {
            for (OpTemplate stmt : stmts) {
                ParsedTemplate parsed = stmt.getParsed().orElseThrow();
                String statement = parsed.getPositionalStatement(Function.identity());
                Objects.requireNonNull(statement);

                sequencer.addOp(new ReadyMongoStatement(stmt), stmt.getParamOrDefault("ratio",1));
            }
        }

        return sequencer.resolve();
    }

    MongoClient createMongoClient(String connectionString) {
        CodecRegistry codecRegistry = fromRegistries(fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                                                     MongoClientSettings.getDefaultCodecRegistry());
        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .applyConnectionString(new ConnectionString(connectionString))
                                                          .codecRegistry(codecRegistry)
                                                          .uuidRepresentation(UuidRepresentation.STANDARD)
                                                          .build();
        return MongoClients.create(settings);
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

}
