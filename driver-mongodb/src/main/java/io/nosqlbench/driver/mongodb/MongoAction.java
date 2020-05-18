package io.nosqlbench.driver.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Timer;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoAction implements SyncAction {

    private final static Logger logger = LoggerFactory.getLogger(MongoAction.class);

    private final MongoActivity activity;
    private final int slot;

    private OpSequence<ReadyMongoStatement> sequencer;

    public MongoAction(MongoActivity activity, int slot) {
        this.activity = activity;
        this.slot = slot;
    }

    @Override
    public void init() {
        this.sequencer = activity.getOpSequencer();
    }

    @Override
    public int runCycle(long cycleValue) {
        ReadyMongoStatement rms;
        Bson queryBson;
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            rms = sequencer.get(cycleValue);
            queryBson = rms.bind(cycleValue);

            if (activity.isShowQuery()) {
                logger.info("Query(cycle={}):\n{}", cycleValue, queryBson);
            }
        }

        for (int i = 0; i < activity.getMaxTries(); i++) {
            activity.triesHisto.update(i);
            try (Timer.Context executeTime = activity.executeTimer.time()) {
                MongoDatabase database = activity.getDatabase();
                ReadPreference readPreference = rms.getReadPreference();
                Document resultDoc = database.runCommand(queryBson, readPreference);

                double ok = resultDoc.getDouble("ok");
                activity.resultSetSizeHisto.update(resultDoc.getInteger("n", 0));
                return ok == 1.0d ? 0 : 1;
            } catch (Exception e) {
                logger.error("Failed to runCommand {} on cycle {}, tries {}", queryBson, cycleValue, i, e);
            }
        }

        throw new RuntimeException(String.format("Exhausted max tries (%s) on cycle %s",
                                                 cycleValue, activity.getMaxTries()));
    }
}
