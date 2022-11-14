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

    private final PulsarSpace pulsarSpace;
    private final String defaultAdapterMetricsPrefix;

    /**
     * Pulsar adapter specific metrics
     */
    protected Counter bytesCounter;
    // - message out of sequence error counter
    protected Counter msgErrOutOfSeqCounter;
    // - message loss counter
    protected Counter msgErrLossCounter;
    // - message duplicate (when dedup is enabled) error counter
    protected Counter msgErrDuplicateCounter;

    protected Histogram messageSizeHistogram;
    // end-to-end latency
    protected Histogram e2eMsgProcLatencyHistogram;
    // A histogram that tracks payload round-trip-time, based on a user-defined field in some sender
    // system which can be interpreted as millisecond epoch time in the system's local time zone.
    // This is paired with a field name of the same type to be extracted and reported in a metric
    // named 'payload-rtt'.
    protected Histogram payloadRttHistogram;

    protected Timer bindTimer;
    protected Timer executeTimer;
    protected Timer createTransactionTimer;
    protected Timer commitTransactionTimer;

    public PulsarAdapterMetrics(PulsarSpace pulsarSpace, String defaultMetricsPrefix) {
        this.pulsarSpace = pulsarSpace;
        this.defaultAdapterMetricsPrefix = defaultMetricsPrefix;
    }

    public void initPulsarAdapterInstrumentation() {
        // Counter metrics
        this.bytesCounter =
            ActivityMetrics.counter(this.defaultAdapterMetricsPrefix + "bytes");
        this.msgErrOutOfSeqCounter =
            ActivityMetrics.counter(this.defaultAdapterMetricsPrefix + "err_msg_oos");
        this.msgErrLossCounter =
            ActivityMetrics.counter(this.defaultAdapterMetricsPrefix + "err_msg_loss");
        this.msgErrDuplicateCounter =
            ActivityMetrics.counter(this.defaultAdapterMetricsPrefix + "err_msg_dup");

        // Histogram metrics
        this.messageSizeHistogram =
            ActivityMetrics.histogram(this.defaultAdapterMetricsPrefix + "message_size");
        this.e2eMsgProcLatencyHistogram =
            ActivityMetrics.histogram(this.defaultAdapterMetricsPrefix + "e2e_msg_latency");
        this.payloadRttHistogram =
            ActivityMetrics.histogram(this.defaultAdapterMetricsPrefix + "payload_rtt");

        // Timer metrics
        this.bindTimer =
            ActivityMetrics.timer(this.defaultAdapterMetricsPrefix + "bind");
        this.executeTimer =
            ActivityMetrics.timer(this.defaultAdapterMetricsPrefix + "execute");
        this.createTransactionTimer =
            ActivityMetrics.timer(this.defaultAdapterMetricsPrefix + "create_transaction");
        this.commitTransactionTimer =
            ActivityMetrics.timer(this.defaultAdapterMetricsPrefix + "commit_transaction");
    }


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

        ActivityMetrics.gauge(metricsPrefix + "total_bytes_sent",
            producerSafeExtractMetric(producer, (s -> s.getTotalBytesSent() + s.getNumBytesSent())));
        ActivityMetrics.gauge(metricsPrefix + "total_msg_sent",
            producerSafeExtractMetric(producer, (s -> s.getTotalMsgsSent() + s.getNumMsgsSent())));
        ActivityMetrics.gauge(metricsPrefix + "total_send_failed",
            producerSafeExtractMetric(producer, (s -> s.getTotalSendFailed() + s.getNumSendFailed())));
        ActivityMetrics.gauge(metricsPrefix + "total_ack_received",
            producerSafeExtractMetric(producer,(s -> s.getTotalAcksReceived() + s.getNumAcksReceived())));
        ActivityMetrics.gauge(metricsPrefix + "send_bytes_rate",
            producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate));
        ActivityMetrics.gauge(metricsPrefix + "send_msg_rate",
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

        ActivityMetrics.gauge(metricsPrefix + "total_bytes_recv",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalBytesReceived() + s.getNumBytesReceived())));
        ActivityMetrics.gauge(metricsPrefix + "total_msg_recv",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalMsgsReceived() + s.getNumMsgsReceived())));
        ActivityMetrics.gauge(metricsPrefix + "total_recv_failed",
            consumerSafeExtractMetric(consumer, (s -> s.getTotalReceivedFailed() + s.getNumReceiveFailed())));
        ActivityMetrics.gauge(metricsPrefix + "total_acks_sent",
            consumerSafeExtractMetric(consumer,(s -> s.getTotalAcksSent() + s.getNumAcksSent())));
        ActivityMetrics.gauge(metricsPrefix + "recv_bytes_rate",
            consumerSafeExtractMetric(consumer, ConsumerStats::getRateBytesReceived));
        ActivityMetrics.gauge(metricsPrefix + "recv_msg_rate",
            consumerSafeExtractMetric(consumer, ConsumerStats::getRateMsgsReceived));
    }
}
