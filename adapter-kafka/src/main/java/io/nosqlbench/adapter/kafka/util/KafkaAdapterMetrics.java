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

package io.nosqlbench.adapter.kafka.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.kafka.dispensers.KafkaBaseOpDispenser;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class KafkaAdapterMetrics implements NBLabeledElement {

    private static final Logger logger = LogManager.getLogger("S4JAdapterMetrics");

    private final String defaultAdapterMetricsPrefix;

    private Histogram messageSizeHistogram;
    private Timer bindTimer;
    private Timer executeTimer;
    // - message out of sequence error counter
    private Counter msgErrOutOfSeqCounter;
    // - message loss counter
    private Counter msgErrLossCounter;
    // - message duplicate error counter
    private Counter msgErrDuplicateCounter;

    public Histogram getE2eMsgProcLatencyHistogram() {
        return e2eMsgProcLatencyHistogram;
    }

    // end-to-end latency
    private Histogram e2eMsgProcLatencyHistogram;
    private final KafkaBaseOpDispenser kafkaBaseOpDispenser;

    public KafkaAdapterMetrics(KafkaBaseOpDispenser kafkaBaseOpDispenser, String defaultMetricsPrefix) {
        this.kafkaBaseOpDispenser = kafkaBaseOpDispenser;
        this.defaultAdapterMetricsPrefix = defaultMetricsPrefix;
    }

    public String getName() {
        return "KafkaAdapterMetrics";
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of("name", getName());
    }

    public void initS4JAdapterInstrumentation() {
        // Histogram metrics
        this.messageSizeHistogram =
            ActivityMetrics.histogram(
                this,
                    defaultAdapterMetricsPrefix + "message_size",
                ActivityMetrics.DEFAULT_HDRDIGITS);

        // Timer metrics
        this.bindTimer =
            ActivityMetrics.timer(
                this,
                    defaultAdapterMetricsPrefix + "bind",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        this.executeTimer =
            ActivityMetrics.timer(
                this,
                    defaultAdapterMetricsPrefix + "execute",
                ActivityMetrics.DEFAULT_HDRDIGITS);

        // End-to-end metrics
        // Latency
        this.e2eMsgProcLatencyHistogram =
            ActivityMetrics.histogram(
                    kafkaBaseOpDispenser,
                    defaultAdapterMetricsPrefix + "e2e_msg_latency",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        // Error metrics
        this.msgErrOutOfSeqCounter =
            ActivityMetrics.counter(
                    kafkaBaseOpDispenser,
                    defaultAdapterMetricsPrefix + "err_msg_oos");
        this.msgErrLossCounter =
            ActivityMetrics.counter(
                    kafkaBaseOpDispenser,
                    defaultAdapterMetricsPrefix + "err_msg_loss");
        this.msgErrDuplicateCounter =
            ActivityMetrics.counter(
                    kafkaBaseOpDispenser,
                    defaultAdapterMetricsPrefix + "err_msg_dup");
    }

    public Timer getBindTimer() { return this.bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Histogram getMessagesizeHistogram() { return this.messageSizeHistogram; }

    public Counter getMsgErrOutOfSeqCounter() {
        return this.msgErrOutOfSeqCounter;
    }

    public void setMsgErrOutOfSeqCounter(final Counter msgErrOutOfSeqCounter) {
        this.msgErrOutOfSeqCounter = msgErrOutOfSeqCounter;
    }

    public Counter getMsgErrLossCounter() {
        return this.msgErrLossCounter;
    }

    public void setMsgErrLossCounter(final Counter msgErrLossCounter) {
        this.msgErrLossCounter = msgErrLossCounter;
    }

    public Counter getMsgErrDuplicateCounter() {
        return this.msgErrDuplicateCounter;
    }

    public void setMsgErrDuplicateCounter(final Counter msgErrDuplicateCounter) {
        this.msgErrDuplicateCounter = msgErrDuplicateCounter;
    }
}
