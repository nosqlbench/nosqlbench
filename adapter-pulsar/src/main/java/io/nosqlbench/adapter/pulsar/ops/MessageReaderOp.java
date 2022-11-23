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

import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;

public class MessageReaderOp extends PulsarClientOp {

    private final static Logger logger = LogManager.getLogger(MessageReaderOp.class);

    private final Reader<?> reader;

    public MessageReaderOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                           PulsarClient pulsarClient,
                           Schema<?> pulsarSchema,
                           boolean asyncApi,
                           Reader<?> reader) {
        super(pulsarAdapterMetrics, pulsarClient, pulsarSchema, asyncApi);

        this.reader = reader;
    }

    @Override
    public Object apply(long value) {
        // TODO: implement the Pulsar reader logic when needed
        //       at the moment, the reader API support from the NB Pulsar driver is disabled
        return null;
    }
}
