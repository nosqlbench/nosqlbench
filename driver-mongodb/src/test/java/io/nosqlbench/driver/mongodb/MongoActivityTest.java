package io.nosqlbench.driver.mongodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoActivityTest {

    private ActivityDef activityDef;

    @BeforeEach
    public void setup() {
        String[] params = {
                "yaml=activities/mongodb-basic.yaml",
                "connection=mongodb://127.0.0.1",
                "database=nosqlbench_testdb"
        };
        activityDef = ActivityDef.parseActivityDef(String.join(";", params));
    }

    @Test
    public void testInitOpSequencer() {
        MongoActivity mongoActivity = new MongoActivity(activityDef);
        mongoActivity.initActivity();

        OpSequence<ReadyMongoStatement> sequence = mongoActivity.initOpSequencer();
        assertThat(sequence.getOps()).hasSize(3);
    }
}
