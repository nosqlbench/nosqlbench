/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.pulsar.ops;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import io.nosqlbench.adapter.pulsar.util.PulsarAvroSchemaUtil;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.KeyValueSchema;
import org.apache.pulsar.common.schema.SchemaType;

public abstract class PulsarClientOp extends PulsarOp {
    protected final PulsarClient pulsarClient;
    protected  final Schema<?> pulsarSchema;

    // Pulsar KeyValue schema
    private org.apache.avro.Schema avroSchema;
    private org.apache.avro.Schema avroKeySchema;

    protected final Histogram messageSizeHistogram;
    protected final Histogram payloadRttHistogram;
    protected final Histogram e2eMsgProcLatencyHistogram;

    protected final Timer transactionCommitTimer;

    public PulsarClientOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                          PulsarClient pulsarClient,
                          Schema<?> pulsarScheam,
                          boolean asyncApi) {
        super (pulsarAdapterMetrics, asyncApi);

        this.pulsarClient = pulsarClient;
        this.pulsarSchema = pulsarScheam;

        this.messageSizeHistogram = pulsarAdapterMetrics.getMessageSizeHistogram();
        this.payloadRttHistogram = pulsarAdapterMetrics.getPayloadRttHistogram();
        this.e2eMsgProcLatencyHistogram = pulsarAdapterMetrics.getE2eMsgProcLatencyHistogram();
        this.transactionCommitTimer = pulsarAdapterMetrics.getCommitTransactionTimer();
    }

    protected org.apache.avro.Schema getAvroSchemaFromConfiguration() {
        // no need for synchronization, this is only a cache
        // in case of the race we will parse the string twice, not a big
        if (avroSchema == null) {
            if (pulsarSchema.getSchemaInfo().getType() == SchemaType.KEY_VALUE) {
                KeyValueSchema kvSchema = (KeyValueSchema) pulsarSchema;
                Schema valueSchema = kvSchema.getValueSchema();
                String avroDefStr = valueSchema.getSchemaInfo().getSchemaDefinition();
                avroSchema = PulsarAvroSchemaUtil.GetSchema_ApacheAvro(avroDefStr);
            } else {
                String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                avroSchema = PulsarAvroSchemaUtil.GetSchema_ApacheAvro(avroDefStr);
            }
        }
        return avroSchema;
    }

    protected org.apache.avro.Schema getKeyAvroSchemaFromConfiguration() {
        // no need for synchronization, this is only a cache
        // in case of the race we will parse the string twice, not a big
        if (avroKeySchema == null) {
            if (pulsarSchema.getSchemaInfo().getType() == SchemaType.KEY_VALUE) {
                KeyValueSchema kvSchema = (KeyValueSchema) pulsarSchema;
                Schema keySchema = kvSchema.getKeySchema();
                String avroDefStr = keySchema.getSchemaInfo().getSchemaDefinition();
                avroKeySchema = PulsarAvroSchemaUtil.GetSchema_ApacheAvro(avroDefStr);
            } else {
                throw new RuntimeException("We are not using KEY_VALUE schema, so no Schema for the Key!");
            }
        }
        return avroKeySchema;
    }
}
