/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.kafka.ops;

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterMetrics;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

public class KafkaOp implements CycleOp<Object> {
    private final KafkaAdapterMetrics kafkaAdapterMetrics;
    protected final KafkaSpace kafkaSpace;
    private final OpTimeTrackKafkaClient opTimeTrackKafkaClient;
    private final Object cycleObj;
    protected final Histogram messageSizeHistogram;


    public KafkaOp(KafkaAdapterMetrics kafkaAdapterMetrics,
                   KafkaSpace kafkaSpace,
                   OpTimeTrackKafkaClient opTimeTrackKafkaClient,
                   Object cycleObj)
    {
        this.kafkaAdapterMetrics = kafkaAdapterMetrics;
        this.kafkaSpace = kafkaSpace;
        this.opTimeTrackKafkaClient = opTimeTrackKafkaClient;
        this.cycleObj = cycleObj;
        this.messageSizeHistogram = kafkaAdapterMetrics.getMessagesizeHistogram();
    }

    @Override
    public Object apply(long value) {
        opTimeTrackKafkaClient.process(value, cycleObj);
        return  null;
    }
}
