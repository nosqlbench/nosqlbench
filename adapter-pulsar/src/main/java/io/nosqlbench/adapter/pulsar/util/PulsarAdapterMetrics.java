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
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.dispensers.PulsarBaseOpDispenser;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerStats;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerStats;

import java.util.function.Function;

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
            ActivityMetrics.counter(this.pulsarBaseOpDispenser,"err_msg_oos");
        msgErrLossCounter =
            ActivityMetrics.counter(this.pulsarBaseOpDispenser, "err_msg_loss");
        msgErrDuplicateCounter =
            ActivityMetrics.counter(this.pulsarBaseOpDispenser, "err_msg_dup");

        // Histogram metrics
        messageSizeHistogram =
            ActivityMetrics.histogram(this.pulsarBaseOpDispenser,
                "message_size", ActivityMetrics.DEFAULT_HDRDIGITS);
        e2eMsgProcLatencyHistogram = ActivityMetrics.histogram(this.pulsarBaseOpDispenser,
            "e2e_msg_latency", ActivityMetrics.DEFAULT_HDRDIGITS);
        payloadRttHistogram = ActivityMetrics.histogram(this.pulsarBaseOpDispenser,
            "payload_rtt", ActivityMetrics.DEFAULT_HDRDIGITS);

        // Timer metrics
        bindTimer =
            ActivityMetrics.timer(this.pulsarBaseOpDispenser,
                "bind", ActivityMetrics.DEFAULT_HDRDIGITS);
        executeTimer =
            ActivityMetrics.timer(this.pulsarBaseOpDispenser,
                "execute", ActivityMetrics.DEFAULT_HDRDIGITS);
        createTransactionTimer =
            ActivityMetrics.timer(this.pulsarBaseOpDispenser,
                "create_transaction", ActivityMetrics.DEFAULT_HDRDIGITS);
        commitTransactionTimer =
            ActivityMetrics.timer(this.pulsarBaseOpDispenser,
                "commit_transaction", ActivityMetrics.DEFAULT_HDRDIGITS);
    }

    public Counter getMsgErrOutOfSeqCounter() { return msgErrOutOfSeqCounter; }
    public Counter getMsgErrLossCounter() { return msgErrLossCounter; }
    public Counter getMsgErrDuplicateCounter() { return msgErrDuplicateCounter; }
    public Histogram getMessageSizeHistogram() { return messageSizeHistogram; }
    public Histogram getE2eMsgProcLatencyHistogram() { return e2eMsgProcLatencyHistogram; }
    public Histogram getPayloadRttHistogram() { return this.payloadRttHistogram; }
    public Timer getBindTimer() { return this.bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Timer getCreateTransactionTimer() { return this.createTransactionTimer; }
    public Timer getCommitTransactionTimer() { return this.commitTransactionTimer; }


    //////////////////////////////////////
    // Pulsar client producer API metrics
    //////////////////////////////////////
    //
    private static class ProducerGaugeImpl implements Gauge<Object> {
        private final Producer<?> producer;
        private final Function<ProducerStats, Object> valueExtractor;

        ProducerGaugeImpl(final Producer<?> producer, final Function<ProducerStats, Object> valueExtractor) {
            this.producer = producer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // we need to synchronize on producer otherwise we could receive corrupted data
            synchronized(this.producer) {
                return this.valueExtractor.apply(this.producer.getStats());
            }
        }
    }
    private static Gauge<Object> producerSafeExtractMetric(final Producer<?> producer, final Function<ProducerStats, Object> valueExtractor) {
        return new ProducerGaugeImpl(producer, valueExtractor);
    }

    public void registerProducerApiMetrics(final Producer<?> producer) {

        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "total_bytes_sent",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> s.getTotalBytesSent() + s.getNumBytesSent()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser,  "total_msg_sent",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> s.getTotalMsgsSent() + s.getNumMsgsSent()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser,  "total_send_failed",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> s.getTotalSendFailed() + s.getNumSendFailed()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser,  "total_ack_received",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> s.getTotalAcksReceived() + s.getNumAcksReceived()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser,  "send_bytes_rate",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser,  "send_msg_rate",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendMsgsRate));
    }


    //////////////////////////////////////
    // Pulsar client consumer API metrics
    //////////////////////////////////////
    //
    private static class ConsumerGaugeImpl implements Gauge<Object> {
        private final Consumer<?> consumer;
        private final Function<ConsumerStats, Object> valueExtractor;

        ConsumerGaugeImpl(final Consumer<?> consumer, final Function<ConsumerStats, Object> valueExtractor) {
            this.consumer = consumer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // - this is a bug report for producer stats.
            // - assume this also applies to consumer stats.
            synchronized(this.consumer) {
                return this.valueExtractor.apply(this.consumer.getStats());
            }
        }
    }
    static Gauge<Object> consumerSafeExtractMetric(final Consumer<?> consumer, final Function<ConsumerStats, Object> valueExtractor) {
        return new ConsumerGaugeImpl(consumer, valueExtractor);
    }

    public void registerConsumerApiMetrics(final Consumer<?> consumer, final String pulsarApiMetricsPrefix) {

        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "total_bytes_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> s.getTotalBytesReceived() + s.getNumBytesReceived()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "total_msg_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> s.getTotalMsgsReceived() + s.getNumMsgsReceived()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "total_recv_failed",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> s.getTotalReceivedFailed() + s.getNumReceiveFailed()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "total_acks_sent",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> s.getTotalAcksSent() + s.getNumAcksSent()));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "recv_bytes_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, ConsumerStats::getRateBytesReceived));
        ActivityMetrics.gauge(this.pulsarBaseOpDispenser, "recv_msg_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, ConsumerStats::getRateMsgsReceived));
    }
}
