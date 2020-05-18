package io.nosqlbench.driver.mongodb;

import org.junit.Test;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoActivityTest {

    @Test
    public void testInitOpSequencer() {
        String[] params = {
                "yaml=activities/mongodb-basic.yaml",
                "connection=mongodb://127.0.0.1",
                "database=nosqlbench_testdb"
        };
        ActivityDef activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        MongoActivity mongoActivity = new MongoActivity(activityDef);
        mongoActivity.initActivity();

        OpSequence<ReadyMongoStatement> sequence = mongoActivity.initOpSequencer();
        assertThat(sequence.getOps()).hasSize(3);
    }
}
