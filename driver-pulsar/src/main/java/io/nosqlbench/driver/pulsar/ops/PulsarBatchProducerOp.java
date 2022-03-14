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

package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerOp extends SyncPulsarOp {

    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final Map<String, String> msgProperties;
    private final String msgPayload;

    public PulsarBatchProducerOp(Schema<?> schema,
                                 String key,
                                 Map<String, String> msgProperties,
                                 String payload) {
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgProperties = msgProperties;
        this.msgPayload = payload;
    }

    @Override
    public void run() {
        if ((msgPayload == null) || msgPayload.isEmpty()) {
            throw new RuntimeException("Message payload (\"msg-value\") can't be empty!");
        }

        List<CompletableFuture<MessageId>> container = PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.get();
        Producer<?> producer = PulsarBatchProducerStartOp.threadLocalProducer.get();
        assert (producer != null) && (container != null);

        TypedMessageBuilder typedMessageBuilder = producer.newMessage(pulsarSchema);
        if ((msgKey != null) && (!msgKey.isEmpty())) {
            typedMessageBuilder = typedMessageBuilder.key(msgKey);
        }
        if (!msgProperties.isEmpty()) {
            typedMessageBuilder = typedMessageBuilder.properties(msgProperties);
        }

        SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
            GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) pulsarSchema,
                pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                msgPayload
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
        } else {
            typedMessageBuilder = typedMessageBuilder.value(msgPayload.getBytes(StandardCharsets.UTF_8));
        }

        container.add(typedMessageBuilder.sendAsync());
    }
}
