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
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
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
            pulsarBaseOpDispenser.create().counter("pulsar_err_msg_oos",
                MetricCategory.Driver,
                "pulsar out-of-sequence error counter"
            );
        msgErrLossCounter =
            pulsarBaseOpDispenser.create().counter("pulsar_err_msg_loss",
                MetricCategory.Driver,
                "pulsar lost message error counter"
            );
        msgErrDuplicateCounter =
            pulsarBaseOpDispenser.create().counter("pulsar_err_msg_dup",
                MetricCategory.Driver,
                "pulsar duplicate message error counter"
            );

        // Histogram metrics
        messageSizeHistogram = pulsarBaseOpDispenser.create().histogram("pulsar_message_size",
            MetricCategory.Driver,
            "pulsar message size"
        );
        e2eMsgProcLatencyHistogram = pulsarBaseOpDispenser.create().histogram("pulsar_e2e_msg_latency",
            MetricCategory.Driver,
            "pulsar end-to-end message latency"
        );
        payloadRttHistogram = pulsarBaseOpDispenser.create().histogram("pulsar_payload_rtt",
            MetricCategory.Driver,
            "pulsar payload round-trip-time"
        );

        // Timer metrics
        bindTimer = pulsarBaseOpDispenser.create().timer("pulsar_bind",
            MetricCategory.Driver,
            "pulsar bind timer"
        );
        executeTimer = pulsarBaseOpDispenser.create().timer("pulsar_execute",
            MetricCategory.Driver,
            "pulsar execution timer"
        );
        createTransactionTimer = pulsarBaseOpDispenser.create().timer("pulsar_create_transaction",
            MetricCategory.Driver,
            "pulsar create transaction timer"
        );
        commitTransactionTimer = pulsarBaseOpDispenser.create().timer("pulsar_commit_transaction",
            MetricCategory.Driver,
            "pulsar commit transaction timer"
        );
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

        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_bytes_sent",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalBytesSent() + s.getNumBytesSent()),
            MetricCategory.Driver,
            "pulsar total bytes sent"
        );
        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_msg_sent",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalMsgsSent() + s.getNumMsgsSent()),
            MetricCategory.Driver,
            "pulsar total message sent"
        );
        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_send_failed",
            PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalSendFailed() + s.getNumSendFailed()),
            MetricCategory.Driver,
            "pulsar message send failures"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_total_ack_received", PulsarAdapterMetrics.producerSafeExtractMetric(producer, s -> (double) s.getTotalAcksReceived() + s.getNumAcksReceived()),
            MetricCategory.Driver,
            "pulsar total acknowledgements received"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_send_bytes_rate", PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate),
            MetricCategory.Driver,
            "pulsar rate of bytes sent"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_send_msg_rate", PulsarAdapterMetrics.producerSafeExtractMetric(producer, ProducerStats::getSendMsgsRate),
            MetricCategory.Driver,
            "pulsar rate of messages sent"
        );
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

        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_bytes_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalBytesReceived() + s.getNumBytesReceived()),
            MetricCategory.Driver,
            "pulsar total bytes received"
        );
        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_msg_recv",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalMsgsReceived() + s.getNumMsgsReceived()),
            MetricCategory.Driver,
            "pulsar total messages received"
        );
        pulsarBaseOpDispenser.create().gauge(
            "pulsar_total_recv_failed",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalReceivedFailed() + s.getNumReceiveFailed()),
            MetricCategory.Driver,
            "pulsar total receive failures"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_total_acks_sent",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getTotalAcksSent() + s.getNumAcksSent()),
            MetricCategory.Driver,
            "pulsar total acknowledgements sent"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_recv_bytes_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getRateBytesReceived()),
            MetricCategory.Driver,
            "pulsar rate of bytes received"
        );
        pulsarBaseOpDispenser.create().gauge("pulsar_recv_msg_rate",
            PulsarAdapterMetrics.consumerSafeExtractMetric(consumer, s -> (double) s.getRateMsgsReceived()),
            MetricCategory.Driver,
            "pulsar rate of message received"
        );
    }
}
