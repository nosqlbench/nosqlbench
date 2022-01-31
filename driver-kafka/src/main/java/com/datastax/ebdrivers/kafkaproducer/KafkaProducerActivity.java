package com.datastax.ebdrivers.kafkaproducer;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KafkaProducerActivity extends SimpleActivity {
    private final static Logger logger = LogManager.getLogger(KafkaProducerActivity.class);
    private String yamlLoc;
    private String clientId;
    private String servers;
    private OpSequence<KafkaStatement> opSequence;
    private String schemaRegistryUrl;
    Timer resultTimer;
    Timer resultSuccessTimer;


    public KafkaProducerActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        // sanity check
        yamlLoc = activityDef.getParams().getOptionalString("yaml", "workload")
            .orElseThrow(() -> new IllegalArgumentException("yaml is not defined"));
        servers = Arrays.stream(activityDef.getParams().getOptionalString("host","hosts")
            .orElse("localhost" + ":9092")
            .split(","))
            .map(x ->  x.indexOf(':') == -1 ? x + ":9092" : x)
            .collect(Collectors.joining(","));
        clientId = activityDef.getParams().getOptionalString("clientid","client.id","client_id")
            .orElse("TestProducerClientId");
        schemaRegistryUrl = activityDef.getParams()
            .getOptionalString("schema_registry_url", "schema.registry.url")
            .orElse("http://localhost:8081");
    }

    @Override
    public void initActivity() {
        logger.debug("initializing activity: " + this.activityDef.getAlias());
        onActivityDefUpdate(activityDef);

        opSequence = initOpSequencer();
        setDefaultsFromOpSequence(opSequence);

        resultTimer = ActivityMetrics.timer(activityDef, "result");
        resultSuccessTimer = ActivityMetrics.timer(activityDef, "result-success");
    }

    private OpSequence<KafkaStatement> initOpSequencer() {
        SequencerType sequencerType = SequencerType.valueOf(
            getParams().getOptionalString("seq").orElse("bucket")
        );
        SequencePlanner<KafkaStatement> sequencer = new SequencePlanner<>(sequencerType);

        String tagFilter = activityDef.getParams().getOptionalString("tags").orElse("");
        StmtsDocList stmtsDocList = StatementsLoader.loadPath(logger, yamlLoc, activityDef.getParams(), "activities");
        List<OpTemplate> statements = stmtsDocList.getStmts(tagFilter);

        String format = getParams().getOptionalString("format").orElse(null);

        if (statements.size() > 0) {
            for (OpTemplate statement : statements) {
                sequencer.addOp(
                    new KafkaStatement(statement,
                                       servers,
                                       clientId,
                                       schemaRegistryUrl),
                    statement.getParamOrDefault("ratio", 1)
                );
            }
        } else {
            logger.error("Unable to create a Kafka statement if you have no active statements.");
        }

        return sequencer.resolve();
    }

    protected OpSequence<KafkaStatement> getOpSequencer() {
        return opSequence;
    }
}
