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

package io.nosqlbench.adapter.pulsar.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.dispensers.PulsarBaseOpDispenser;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerStats;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerStats;

import java.util.function.Function;

public class PulsarAdapterMetrics {

    private final static Logger logger = LogManager.getLogger("PulsarAdapterMetrics");

    private final PulsarBaseOpDispenser pulsarBaseOpDispenser;
    private final String defaultAdapterMetricsPrefix;

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

    public PulsarAdapterMetrics(PulsarBaseOpDispenser pulsarBaseOpDispenser, String defaultMetricsPrefix) {
        this.pulsarBaseOpDispenser = pulsarBaseOpDispenser;
        this.defaultAdapterMetricsPrefix = defaultMetricsPrefix;
    }

    public void initPulsarAdapterInstrumentation() {
        // Counter metrics
        this.msgErrOutOfSeqCounter =
            ActivityMetrics.counter(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "err_msg_oos");
        this.msgErrLossCounter =
            ActivityMetrics.counter(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "err_msg_loss");
        this.msgErrDuplicateCounter =
            ActivityMetrics.counter(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "err_msg_dup");

        // Histogram metrics
        this.messageSizeHistogram =
            ActivityMetrics.histogram(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "message_size",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.e2eMsgProcLatencyHistogram =
            ActivityMetrics.histogram(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "e2e_msg_latency",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.payloadRttHistogram =
            ActivityMetrics.histogram(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "payload_rtt",
                ActivityMetrics.DEFAULT_HDRDIGITS);

        // Timer metrics
        this.bindTimer =
            ActivityMetrics.timer(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "bind",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.executeTimer =
            ActivityMetrics.timer(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "execute",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.createTransactionTimer =
            ActivityMetrics.timer(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "create_transaction",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.commitTransactionTimer =
            ActivityMetrics.timer(
                pulsarBaseOpDispenser,
                defaultAdapterMetricsPrefix + "commit_transaction",
                ActivityMetrics.DEFAULT_HDRDIGITS);
    }

    public Counter getMsgErrOutOfSeqCounter() { return this.msgErrOutOfSeqCounter; }
    public Counter getMsgErrLossCounter() { return this.msgErrLossCounter; }
    public Counter getMsgErrDuplicateCounter() { return this.msgErrDuplicateCounter; }
    public Histogram getMessageSizeHistogram() { return this.messageSizeHistogram; }
    public Histogram getE2eMsgProcLatencyHistogram() { return this.e2eMsgProcLatencyHistogram; }
    public Histogram getPayloadRttHistogram() { return payloadRttHistogram; }
    public Timer getBindTimer() { return bindTimer; }
    public Timer getExecuteTimer() { return executeTimer; }
    public Timer getCreateTransactionTimer() { return createTransactionTimer; }
    public Timer getCommitTransactionTimer() { return commitTransactionTimer; }


    //////////////////////////////////////
    // Pulsar client producer API metrics
    //////////////////////////////////////
    //
    private static class ProducerGaugeImpl implements Gauge<Object> {
        private final Producer<?> producer;
        private final Function<ProducerStats, Object> valueExtractor;

        ProducerGaugeImpl(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
            this.producer = producer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // we need to synchronize on producer otherwise we could receive corrupted data
            synchronized(producer) {
                return valueExtractor.apply(producer.getStats());
            }
        }
    }
    private static Gauge<Object> producerSafeExtractMetric(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
        return new ProducerGaugeImpl(producer, valueExtractor);
    }

    public void registerProducerApiMetrics(Producer<?> producer, String pulsarApiMetricsPrefix) {
        String metricsPrefix = defaultAdapterMetricsPrefix;
        if (!StringUtils.isBlank(pulsarApiMetricsPrefix)) {
            metricsPrefix = pulsarApiMetricsPrefix;
        }

        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_bytes_sent",
            producerSafeExtractMetric(producer, (s -> s.getTotalBytesSent() + s.getNumBytesSent())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_msg_sent",
            producerSafeExtractMetric(producer, (s -> s.getTotalMsgsSent() + s.getNumMsgsSent())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_send_failed",
            producerSafeExtractMetric(producer, (s -> s.getTotalSendFailed() + s.getNumSendFailed())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_ack_received",
            producerSafeExtractMetric(producer,(s -> s.getTotalAcksReceived() + s.getNumAcksReceived())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "send_bytes_rate",
            producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "send_msg_rate",
            producerSafeExtractMetric(producer, ProducerStats::getSendMsgsRate));
    }


    //////////////////////////////////////
    // Pulsar client consumer API metrics
    //////////////////////////////////////
    //
    private static class ConsumerGaugeImpl implements Gauge<Object> {
        private final Consumer<?> consumer;
        private final Function<ConsumerStats, Object> valueExtractor;

        ConsumerGaugeImpl(Consumer<?> consumer, Function<ConsumerStats, Object> valueExtractor) {
            this.consumer = consumer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // - this is a bug report for producer stats.
            // - assume this also applies to consumer stats.
            synchronized(consumer) {
                return valueExtractor.apply(consumer.getStats());
            }
        }
    }
    static Gauge<Object> consumerSafeExtractMetric(Consumer<?> consumer, Function<ConsumerStats, Object> valueExtractor) {
        return new ConsumerGaugeImpl(consumer, valueExtractor);
    }

    public void registerConsumerApiMetrics(Consumer<?> consumer, String pulsarApiMetricsPrefix) {
        String metricsPrefix = defaultAdapterMetricsPrefix;
        if (!StringUtils.isBlank(pulsarApiMetricsPrefix)) {
            metricsPrefix = pulsarApiMetricsPrefix;
        }

        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_bytes_recv",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalBytesReceived() + s.getNumBytesReceived())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_msg_recv",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalMsgsReceived() + s.getNumMsgsReceived())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_recv_failed",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalReceivedFailed() + s.getNumReceiveFailed())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "total_acks_sent",
            consumerSafeExtractMetric(consumer,(s -> s.getTotalAcksSent() + s.getNumAcksSent())));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "recv_bytes_rate",
            consumerSafeExtractMetric(consumer, ConsumerStats::getRateBytesReceived));
        ActivityMetrics.gauge(pulsarBaseOpDispenser, metricsPrefix + "recv_msg_rate",
            consumerSafeExtractMetric(consumer, ConsumerStats::getRateMsgsReceived));
    }
}
