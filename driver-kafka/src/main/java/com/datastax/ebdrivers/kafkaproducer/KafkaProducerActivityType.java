package com.datastax.ebdrivers.kafkaproducer;

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


import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "kafkaproducer")
public class KafkaProducerActivityType implements ActivityType<KafkaProducerActivity> {

    @Override
    public KafkaProducerActivity getActivity(ActivityDef activityDef) {
        return new KafkaProducerActivity(activityDef);
    }

    private static class Dispenser implements ActionDispenser {
        private final KafkaProducerActivity activity;

        private Dispenser(KafkaProducerActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new KafkaAction(this.activity, slot);
        }
    }

    @Override
    public ActionDispenser getActionDispenser(KafkaProducerActivity activity) {
        return new Dispenser(activity);
    }
}
