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
