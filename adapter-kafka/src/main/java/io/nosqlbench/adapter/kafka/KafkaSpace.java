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

import io.nosqlbench.adapter.kafka.exception.KafkaAdapterUnexpectedException;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaClient;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.kafka.util.KafkaClientConf;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(KafkaSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    // TODO: currently this NB Kafka driver only supports String type for message key and value
    //       add schema support in the future
    private final ConcurrentHashMap<String, OpTimeTrackKafkaClient> opTimeTrackKafkaClients = new ConcurrentHashMap<>();

    private final String bootstrapSvr;
    private final String kafkaClientConfFileName;
    private final KafkaClientConf kafkaClientConf;

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the Pulsar server while doing message receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private final boolean strictMsgErrorHandling;

    // Maximum time length to execute S4J operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4JOp is always executed until NB execution cycle finishes
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

    private AtomicBoolean beingShutdown = new AtomicBoolean(false);

    public KafkaSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
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

    public OpTimeTrackKafkaClient getOpTimeTrackKafkaClient(String cacheKey) {
        return opTimeTrackKafkaClients.get(cacheKey);
    }
    public void addOpTimeTrackKafkaClient(String cacheKey, OpTimeTrackKafkaClient client) {
        opTimeTrackKafkaClients.put(cacheKey, client);
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

    public boolean isShuttigDown() {
        return beingShutdown.get();
    }
    public void shutdownSpace() {
        try {
            beingShutdown.set(true);

            for (OpTimeTrackKafkaClient client : opTimeTrackKafkaClients.values()) {
                client.close();
            }

            // Pause 5 seconds before closing producers/consumers
            KafkaAdapterUtil.pauseCurThreadExec(5);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new KafkaAdapterUnexpectedException("Unexpected error when shutting down NB S4J space.");
        }
    }
}
