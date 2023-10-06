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

package io.nosqlbench.adapter.pulsar.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.dispensers.PulsarBaseOpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerStats;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerStats;

import java.util.function.Function;
import java.util.function.Supplier;

public class PulsarAdapterMetrics {

    private static final Logger logger = LogManager.getLogger("PulsarAdapterMetrics");

    private final PulsarBaseOpDispenser pulsarBaseOpDispenser;
    /**
     * Pulsar adapter specific metrics
     */
    // - message out of sequence error counter
    private Counter msgErrOutOfSeqCounter;
    // - message loss counter
    private Counter msgErrLossCounter;
    // - message duplicate (when dedup is enabled) error counter
    private Counter msgErrDuplicateCounter;

    private Histogram messageSizeHistogram;
    // end-to-end latency
    private Histogram e2eMsgProcLatencyHistogram;
    // A histogram that tracks payload round-trip-time, based on a user-defined field in some sender
    // system which can be interpreted as millisecond epoch time in the system's local time zone.
    // This is paired with a field name of the same type to be extracted and reported in a metric
    // named 'payload-rtt'.
    private Histogram payloadRttHistogram;

    private Timer bindTimer;
    private Timer executeTimer;
    private Timer createTransactionTimer;
    private Timer commitTransactionTimer;

    public PulsarAdapterMetrics(final PulsarBaseOpDispenser pulsarBaseOpDispenser) {
        this.pulsarBaseOpDispenser = pulsarBaseOpDispenser;
    }

    public void initPulsarAdapterInstrumentation() {
        // Counter metrics
        msgErrOutOfSeqCounter =
            pulsarBaseOpDispenser.create().counter("err_msg_oos");
        msgErrLossCounter =
            pulsarBaseOpDispenser.create().counter("err_msg_loss");
        msgErrDuplicateCounter =
            pulsarBaseOpDispenser.create().counter("err_msg_dup");

        // Histogram metrics
        messageSizeHistogram = pulsarBaseOpDispenser.create().histogram("message_size");
        e2eMsgProcLatencyHistogram = pulsarBaseOpDispenser.create().histogram("e2e_msg_latency");
        payloadRttHistogram = pulsarBaseOpDispenser.create().histogram("payload_rtt");

        // Timer metrics
        bindTimer = pulsarBaseOpDispenser.create().timer("bind");
        executeTimer = pulsarBaseOpDispenser.create().timer("execute");
        createTransactionTimer = pulsarBaseOpDispenser.create().timer("create_transaction");
        commitTransactionTimer = pulsarBaseOpDispenser.create().timer("commit_transaction");
    }

    public Counter getMsgErrOutOfSeqCounter() {
        return msgErrOutOfSeqCounter;
    }

    public Counter getMsgErrLossCounter() {
        return msgErrLossCounter;
    }

    public Counter getMsgErrDuplicateCounter() {
        return msgErrDuplicateCounter;
    }

    public Histogram getMessageSizeHistogram() {
        return messageSizeHistogram;
    }

    public Histogram getE2eMsgProcLatencyHistogram() {
        return e2eMsgProcLatencyHistogram;
    }

    public Histogram getPayloadRttHistogram() {
        return this.payloadRttHistogram;
    }

    public Timer getBindTimer() {
        return this.bindTimer;
    }

    public Timer getExecuteTimer() {
        return this.executeTimer;
    }

    public Timer getCreateTransactionTimer() {
        return this.createTransactionTimer;
    }

    public Timer getCommitTransactionTimer() {
        return this.commitTransactionTimer;
    }


    //////////////////////////////////////
    // Pulsar client producer API metrics
    //////////////////////////////////////
    //
    private static class ProducerGaugeImpl implements Supplier<Double> {
        private final Producer<?> producer;
        private final Function<ProducerStats, Double> valueExtractor;

        ProducerGaugeImpl(final Producer<?> producer, final Function<ProducerStats, Double> valueExtractor) {
            this.producer = producer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Double get() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // we need to synchronize on producer otherwise we could receive corrupted data
            synchronized (this.producer) {
                return this.valueExtractor.apply(this.producer.getStats());
            }
        }
    }

    private static Supplier<Double> producerSafeExtractMetric(final Producer<?> producer, final Function<ProducerStats, Double> valueExtractor) {
        return new ProducerGaugeImpl(producer, valueExtractor);
    }

    public void registerProducerApiMetrics(final Producer<?> producer) {

        pulsarBaseOpDispenser.create().gauge("total_bytes_sent", PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalBytesSent() + s.getNumBytesSent()));
        pulsarBaseOpDispenser.create().gauge("total_msg_sent", PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalMsgsSent() + s.getNumMsgsSent()));
        pulsarBaseOpDispenser.create().gauge("total_send_failed", PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalSendFailed() + s.getNumSendFailed()));
        pulsarBaseOpDispenser.create().gauge("total_ack_received", PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalAcksReceived() + s.getNumAcksReceived()));
        pulsarBaseOpDispenser.create().gauge("send_bytes_rate", PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate));
        pulsarBaseOpDispenser.create().gauge("send_msg_rate", PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendMsgsRate));
    }


    //////////////////////////////////////
    // Pulsar client consumer API metrics
    //////////////////////////////////////
    //
    private static class ConsumerGaugeImpl implements Supplier<Double> {
        private final Consumer<?> consumer;
        private final Function<ConsumerStats, Double> valueExtractor;

        ConsumerGaugeImpl(final Consumer<?> consumer, final Function<ConsumerStats, Double> valueExtractor) {
            this.consumer = consumer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Double get() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // - this is a bug report for producer stats.
            // - assume this also applies to consumer stats.
            synchronized (this.consumer) {
                return this.valueExtractor.apply(this.consumer.getStats());
            }
        }
    }

    static Supplier<Double> consumerSafeExtractMetric(final Consumer<?> consumer, final Function<ConsumerStats, Double> valueExtractor) {
        return new ConsumerGaugeImpl(consumer, valueExtractor);
    }

    public void registerConsumerApiMetrics(final Consumer<?> consumer, final String pulsarApiMetricsPrefix) {

        pulsarBaseOpDispenser.create().gauge("total_bytes_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalBytesReceived() + s.getNumBytesReceived()));
        pulsarBaseOpDispenser.create().gauge("total_msg_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalMsgsReceived() + s.getNumMsgsReceived()));
        pulsarBaseOpDispenser.create().gauge("total_recv_failed",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalReceivedFailed() + s.getNumReceiveFailed()));
        pulsarBaseOpDispenser.create().gauge("total_acks_sent",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalAcksSent() + s.getNumAcksSent()));
        pulsarBaseOpDispenser.create().gauge("recv_bytes_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getRateBytesReceived()));
        pulsarBaseOpDispenser.create().gauge("recv_msg_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getRateMsgsReceived()));
    }
}
