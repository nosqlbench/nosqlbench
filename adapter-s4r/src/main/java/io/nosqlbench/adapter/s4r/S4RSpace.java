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

package io.nosqlbench.adapter.s4r;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterInvalidParamException;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.adapter.s4r.util.S4RClientConf;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class S4RSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(S4RSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    private final S4RClientConf s4rClientConf;

    ///////////////////////////////////////////////////////////////////
    // NOTE: in this driver, we assume:
    //       - possible multiple connections
    //       - possible multiple channels per connection
    //       - TBD: only one exchange per channel
    //       - for senders, possible multiple senders per exchange
    //       - for receivers,
    //         * possible multiple queues per exchange
    //         * possible multiple receivers per queue
    //
    // Each NB thread is a single sender or receiver
    //
    // All senders/receivers share the same set of connections/channels/exchanges/queues
    ///////////////////////////////////////////////////////////////////


    // Maximum number of AMQP connections
    private final int amqpConnNum;

    // Maximum number of AMQP channels per connection
    private final int amqpConnChannelNum;

    // Max number of queues (per exchange)
    // - only relevant with message receivers
    private final int amqpExchangeQueueNum;

    // Max number of message clients (senders or receivers)
    // - for senders, this is the number of message senders per exchange
    // - for recievers, this is the number of message receivers per queue
    //   (there could be multiple queues per exchange)
    private final int amqpMsgClntNum;


    private final AtomicBoolean beingShutdown = new AtomicBoolean(false);

    private ConnectionFactory s4rConnFactory;

    // Default to "direct" type
    private String amqpExchangeType = S4RAdapterUtil.AMQP_EXCHANGE_TYPES.DIRECT.label;

    private final ConcurrentHashMap<Long, Connection> amqpConnections = new ConcurrentHashMap<>();

    ///////////////////////////////////
    // NOTE: Do NOT mix sender and receiver workload in one NB workload
    ///////////////////////////////////

    // Amqp Channels for senders
    public record AmqpSenderChannelKey(Long connId, Long channelId, Long senderId) { }
    private final ConcurrentHashMap<AmqpSenderChannelKey, Channel> amqpSenderChannels = new ConcurrentHashMap<>();

    // Amqp Channels for receivers
    public record AmqpReceiverChannelKey(Long connId, Long channelId, Long queueId, Long consumerId) { }
    private final ConcurrentHashMap<AmqpReceiverChannelKey, Channel> amqpReceiverChannels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<AmqpReceiverChannelKey, Set<String>> amqpRecvChannelQueueSetMap = new ConcurrentHashMap<>();

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the AMQP server (or AMQP compatible sever like Pulsar) while doing
    //        message receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private final boolean strictMsgErrorHandling;

    // Maximum time length to execute S4R operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4RTimeTrackOp is always executed until NB execution cycle finishes
    private final long maxOpTimeInSec;
    private final long activityStartTimeMills;

    private long totalCycleNum;
    private long totalThreadNum;

    public S4RSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        String s4rClientConfFileName = cfg.get("config");
        this.s4rClientConf = new S4RClientConf(s4rClientConfFileName);
        this.amqpConnNum =
            NumberUtils.toInt(cfg.getOptional("num_conn").orElse("1"));
        this.amqpConnChannelNum =
            NumberUtils.toInt(cfg.getOptional("num_channel").orElse("1"));
        this.amqpExchangeQueueNum =
            NumberUtils.toInt(cfg.getOptional("num_queue").orElse("1"));
        this.amqpMsgClntNum =
            NumberUtils.toInt(cfg.getOptional("num_msg_clnt").orElse("1"));
        this.maxOpTimeInSec =
            NumberUtils.toLong(cfg.getOptional("max_op_time").orElse("0L"));
        this.strictMsgErrorHandling =
            BooleanUtils.toBoolean(cfg.getOptional("strict_msg_error_handling").orElse("false"));
        this.activityStartTimeMills = System.currentTimeMillis();

        this.initializeSpace(s4rClientConf);
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(S4RSpace.class)
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("S4R client connection configuration property file."))
            .add(Param.defaultTo("num_conn", 1)
                .setDescription("Maximum number of AMQP connections."))
            .add(Param.defaultTo("num_channel", 1)
                .setDescription("Maximum number of AMQP channels per connection"))
            .add(Param.defaultTo("max_op_time", 0)
                .setDescription("Maximum time (in seconds) to run NB Kafka testing scenario."))
            .add(Param.defaultTo("strict_msg_error_handling", false)
                .setDescription("Whether to do strict error handling which is to stop NB Kafka execution."))
            .asReadOnly();
    }

    public Connection getAmqpConnection(Long id) { return amqpConnections.get(id); }

    public Channel getAmqpSenderChannel(
        AmqpSenderChannelKey key,
        Supplier<Channel> channelSupplier) {
            return amqpSenderChannels.computeIfAbsent(key, __ -> channelSupplier.get());
    }

    public Channel getAmqpReceiverChannel(
        AmqpReceiverChannelKey key,
        Supplier<Channel> channelSupplier) {
        return amqpReceiverChannels.computeIfAbsent(key, __ -> channelSupplier.get());
    }

    public long getActivityStartTimeMills() { return this.activityStartTimeMills; }
    public long getMaxOpTimeInSec() { return this.maxOpTimeInSec; }
    public S4RClientConf getS4rClientConf() { return s4rClientConf; }

    public String getAmqpExchangeType() { return amqpExchangeType; }
    public int getAmqpConnNum() { return this.amqpConnNum; }
    public int getAmqpConnChannelNum() { return this.amqpConnChannelNum; }
    public int getAmqpExchangeQueueNum() { return this.amqpConnNum; }
    public int getAmqpMsgClntNum() { return this.amqpMsgClntNum; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public long getTotalCycleNum() { return totalCycleNum; }
    public void setTotalCycleNum(long cycleNum) { totalCycleNum = cycleNum; }

    public long getTotalThreadNum() { return totalThreadNum; }
    public void setTotalThreadNum(long threadNum) { totalThreadNum = threadNum; }

    public void initializeSpace(S4RClientConf s4rClientConnInfo) {
        Map<String, String> cfgMap = s4rClientConnInfo.getS4rConfMap();

        if  (amqpConnNum < 1) {
            String errMsg = "AMQP connection number (\"num_conn\") must be a positive number!";
            throw new S4RAdapterInvalidParamException(errMsg);
        }

        if  (amqpConnChannelNum < 1) {
            String errMsg = "AMQP channel number per connection (\"num_channel\") must be a positive number!";
            throw new S4RAdapterInvalidParamException(errMsg);
        }

        amqpExchangeType = cfgMap.get("exchangeType");
        if (!S4RAdapterUtil.AMQP_EXCHANGE_TYPES.isValidLabel(amqpExchangeType)) {
            String errMsg = "Invalid AMQP exchange type: \"" + amqpExchangeType + "\". " +
                "Valid values are: \"" + S4RAdapterUtil.getValidAmqpExchangeTypeList() + "\"";
            throw new S4RAdapterInvalidParamException(errMsg);
        }

        if (s4rConnFactory == null) {
            try {
                s4rConnFactory = new ConnectionFactory();

                String passWord = cfg.get("jwtToken");
                s4rConnFactory.setPassword(cfgMap.get(""));
                s4rConnFactory.setPassword(passWord);

                String amqpServerHost = cfg.get("amqpSrvHost");
                s4rConnFactory.setHost(amqpServerHost);

                int amqpServerPort = Integer.parseInt(cfg.get("amqpSrvPort"));
                s4rConnFactory.setPort(amqpServerPort);

                String amqpVirtualHost = cfg.get("virtualHost");
                s4rConnFactory.setVirtualHost(amqpVirtualHost);


                for (int i = 0; i < getAmqpConnNum(); i++) {
                    Connection connection = s4rConnFactory.newConnection();
                    amqpConnections.put((long) i, connection);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[AMQP Connection created] {} -- [{}] {}",
                            Thread.currentThread().getName(),
                            i,
                            connection);
                    }
                }
            } catch (IOException|TimeoutException  ex) {
                logger.error("Unable to establish AMQP connections with the following configuration parameters: {}",
                    s4rClientConnInfo.toString());
                throw new S4RAdapterUnexpectedException(ex);
            }
        }
    }

    public void shutdownSpace() {
        try {
            beingShutdown.set(true);

            for (Channel channel : amqpSenderChannels.values()) {
                channel.close();
            }

            for (Channel channel : amqpReceiverChannels.values()) {
                channel.close();
            }

            for (Connection connection : amqpConnections.values()) {
                connection.close();
            }

            // Pause 5 seconds before closing producers/consumers
            S4RAdapterUtil.pauseCurThreadExec(5);
        }
        catch (Exception ex) {
            String exp = "Unexpected error when shutting down the S4R adaptor space";
            logger.error(exp, ex);
        }
    }
}
