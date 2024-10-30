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

package io.nosqlbench.adapter.kafka;

import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaConsumer;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaProducer;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.kafka.util.KafkaClientConf;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class KafkaSpace extends BaseSpace<KafkaSpace> {

    private final static Logger logger = LogManager.getLogger(KafkaSpace.class);

    private final NBConfiguration cfg;

    // TODO: currently this NB Kafka driver only supports String type for message key and value
    //       add schema support in the future

    private final String bootstrapSvr;
    private final String kafkaClientConfFileName;
    private final KafkaClientConf kafkaClientConf;

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the Kafka server (or Kafka compatible server like Pulsar) while doing message
    //        receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private final boolean strictMsgErrorHandling;

    // Maximum time length to execute Kafka operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. KafkaOp is always executed until NB execution cycle finishes
    private final long maxOpTimeInSec;
    private final long activityStartTimeMills;

    // Maximum number of Kafka clients
    // - For Producer workload, this represents how many total producers to publish messages
    //   it must be the same value as the NB "threads" parameter
    // - For Consumer workload, this represents how many total consumers per consumer group to subscribe messages
    private final int kafkaClntNum;

    // Maximum number of Kafka consumer groups
    // - Only relevant for Consumer workload
    // - (topicPartNum * consumerGrpNum) is the total consumer thread number and must be the same
    //   as the NB "threads" parameter
    // - For multi-topic testing, this means one consumer thread may read from multiple topics.
    private final int consumerGrpNum;

    private long totalCycleNum;

    private final AtomicBoolean beingShutdown = new AtomicBoolean(false);


    public record ProducerCacheKey(String producerName, String topicName, String clientId) {
    }
    private final ConcurrentHashMap<ProducerCacheKey, OpTimeTrackKafkaProducer> producers =
        new ConcurrentHashMap<>();

    public record ConsumerCacheKey(String consumerName, List<String> topicList, String clientId) {
    }
    private final ConcurrentHashMap<ConsumerCacheKey, OpTimeTrackKafkaConsumer> consumers =
        new ConcurrentHashMap<>();


    public KafkaSpace(KafkaDriverAdapter adapter, int idx, NBConfiguration cfg) {
        super(adapter, idx);
        this.cfg = cfg;

        this.bootstrapSvr = cfg.get("bootstrap_server");
        this.kafkaClntNum =
            NumberUtils.toInt(cfg.getOptional("num_clnt").orElse("1"));
        this.consumerGrpNum =
            NumberUtils.toInt(cfg.getOptional("num_cons_grp").orElse("1"));
        this.maxOpTimeInSec =
            NumberUtils.toLong(cfg.getOptional("max_op_time").orElse("0L"));
        this.strictMsgErrorHandling =
            BooleanUtils.toBoolean(cfg.getOptional("strict_msg_error_handling").orElse("false"));
        this.kafkaClientConfFileName = cfg.get("config");
        this.kafkaClientConf = new KafkaClientConf(kafkaClientConfFileName);
        this.activityStartTimeMills = System.currentTimeMillis();
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(KafkaSpace.class)
            .add(Param.defaultTo("bootstrap_server", "pulsar://localhost:9020")
                .setDescription("Kafka bootstrap server URL."))
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("Kafka client connection configuration property file."))
            .add(Param.defaultTo("num_clnt", 1)
                .setDescription("Number of Kafka clients. For consumer, this is the number of consumers per consumer group"))
            .add(Param.defaultTo("num_cons_grp", 1)
                .setDescription("Number of consumer groups (only relevant for Kafka consumer workload). "))
            .add(Param.defaultTo("max_op_time", 0)
                .setDescription("Maximum time (in seconds) to run NB Kafka testing scenario."))
            .add(Param.defaultTo("strict_msg_error_handling", false)
                .setDescription("Whether to do strict error handling which is to stop NB Kafka execution."))
            .asReadOnly();
    }

    public OpTimeTrackKafkaProducer getOpTimeTrackKafkaProducer(
        ProducerCacheKey key,
        Supplier<OpTimeTrackKafkaProducer> producerSupplier) {
            return producers.computeIfAbsent(key, __ -> producerSupplier.get());
    }

    public OpTimeTrackKafkaConsumer getOpTimeTrackKafkaConsumer(
        ConsumerCacheKey key,
        Supplier<OpTimeTrackKafkaConsumer> consumerSupplier) {
            return consumers.computeIfAbsent(key, __ -> consumerSupplier.get());
    }

    public long getActivityStartTimeMills() { return this.activityStartTimeMills; }
    public long getMaxOpTimeInSec() { return this.maxOpTimeInSec; }
    public String getBootstrapSvr() { return this.bootstrapSvr; }
    public KafkaClientConf getKafkaClientConf() { return kafkaClientConf; }

    public int getKafkaClntNum() { return this.kafkaClntNum; }
    public int getConsumerGrpNum() { return this.consumerGrpNum; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public long getTotalCycleNum() { return totalCycleNum; }
    public void setTotalCycleNum(long cycleNum) { totalCycleNum = cycleNum; }

    public boolean isShuttingDown() {
        return beingShutdown.get();
    }
    public void shutdownSpace() {
        try {
            beingShutdown.set(true);

            for (OpTimeTrackKafkaProducer producer : producers.values()) {
                producer.close();
            }

            for (OpTimeTrackKafkaConsumer consumer : consumers.values()) {
                consumer.close();
            }

            // Pause 5 seconds before closing producers/consumers
            KafkaAdapterUtil.pauseCurThreadExec(5);
        }
        catch (Exception ex) {
            String exp = "Unexpected error when shutting down the Kafka adaptor space";
            logger.error(exp, ex);
        }
    }
}
