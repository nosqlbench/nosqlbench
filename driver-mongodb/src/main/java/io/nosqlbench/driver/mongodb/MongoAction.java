package io.nosqlbench.driver.mongodb;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.concurrent.TimeUnit;

public class MongoAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(MongoAction.class);

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
    public int runCycle(long cycle) {
        ReadyMongoStatement rms;
        Bson queryBson;
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            rms = sequencer.apply(cycle);
            queryBson = rms.bind(cycle);

            // Maybe show the query in log/console - only for diagnostic use
            if (activity.isShowQuery()) {
                logger.info("Query(cycle={}):\n{}", cycle, queryBson);
            }
        }

        long nanoStartTime = System.nanoTime();
        for (int i = 1; i <= activity.getMaxTries(); i++) {
            activity.triesHisto.update(i);

            try (Timer.Context resultTime = activity.resultTimer.time()) {
                // assuming the commands are one of these in the doc:
                // https://docs.mongodb.com/manual/reference/command/nav-crud/
                Document resultDoc = activity.getDatabase().runCommand(queryBson, rms.getReadPreference());

                long resultNanos = System.nanoTime() - nanoStartTime;

                // TODO: perhaps collect the operationTime from the resultDoc if any
                // https://docs.mongodb.com/manual/reference/method/db.runCommand/#command-response
                int ok = Double.valueOf((double) resultDoc.getOrDefault("ok", 0.0d)).intValue();
                if (ok == 1) {
                    // success
                    activity.resultSuccessTimer.update(resultNanos, TimeUnit.NANOSECONDS);
                }
                activity.resultSetSizeHisto.update(resultDoc.getInteger("n", 0));

                return ok == 1 ? 0 : 1;
            } catch (Exception e) {
                logger.error("Failed to runCommand {} on cycle {}, tries {}", queryBson, cycle, i, e);
            }
        }

        throw new RuntimeException(String.format("Exhausted max tries (%s) on cycle %s",
            activity.getMaxTries(), cycle));
    }
}
