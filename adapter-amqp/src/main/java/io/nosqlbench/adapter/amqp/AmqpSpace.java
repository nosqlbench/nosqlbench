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

package io.nosqlbench.adapter.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterInvalidParamException;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterUtil;
import io.nosqlbench.adapter.amqp.util.AmqpClientConf;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AmqpSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(AmqpSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    private final AmqpClientConf amqpClientConf;

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

    // Maximum number of AMQP exchanges per channel
    private final int amqpChannelExchangeNum;

    // Max number of queues (per exchange)
    // - only relevant with message receivers
    private final int amqpExchangeQueueNum;

    // Max number of message clients (senders or receivers)
    // - for senders, this is the number of message senders per exchange
    // - for receivers, this is the number of message receivers per queue
    //   (there could be multiple queues per exchange)
    private final int amqpMsgClntNum;


    private final AtomicBoolean beingShutdown = new AtomicBoolean(false);

    private ConnectionFactory amqpConnFactory;

    // Default to "direct" type
    private String amqpExchangeType = AmqpAdapterUtil.AMQP_EXCHANGE_TYPES.DIRECT.label;

    private final ConcurrentHashMap<Long, Connection> amqpConnections = new ConcurrentHashMap<>();

    // Amqp connection/chanel/exchange combination for a sender
    public record AmqpChannelKey(Long connId, Long channelId) { }
    private final ConcurrentHashMap<AmqpChannelKey, Channel> amqpChannels = new ConcurrentHashMap<>();

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the AMQP server (or AMQP compatible sever like Pulsar) while doing
    //        message receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private final boolean strictMsgErrorHandling;

    // Maximum time length to execute AMQP operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. AmqpTimeTrackOp is always executed until NB execution cycle finishes
    private final long maxOpTimeInSec;
    private final long activityStartTimeMills;

    private long totalCycleNum;
    private long totalThreadNum;

    public AmqpSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        String amqpClientConfFileName = cfg.get("config");
        this.amqpClientConf = new AmqpClientConf(amqpClientConfFileName);
        this.amqpConnNum =
            NumberUtils.toInt(cfg.getOptional("num_conn").orElse("1"));
        this.amqpConnChannelNum =
            NumberUtils.toInt(cfg.getOptional("num_channel").orElse("1"));
        this.amqpChannelExchangeNum =
            NumberUtils.toInt(cfg.getOptional("num_exchange").orElse("1"));
        this.amqpExchangeQueueNum =
            NumberUtils.toInt(cfg.getOptional("num_queue").orElse("1"));
        this.amqpMsgClntNum =
            NumberUtils.toInt(cfg.getOptional("num_msg_clnt").orElse("1"));
        this.maxOpTimeInSec =
            NumberUtils.toLong(cfg.getOptional("max_op_time").orElse("0L"));
        this.strictMsgErrorHandling =
            BooleanUtils.toBoolean(cfg.getOptional("strict_msg_error_handling").orElse("false"));
        this.activityStartTimeMills = System.currentTimeMillis();

        this.initializeSpace(amqpClientConf);
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(AmqpSpace.class)
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("AMQP client connection configuration property file."))
            .add(Param.defaultTo("num_conn", 1)
                .setDescription("Maximum number of AMQP connections."))
            .add(Param.defaultTo("num_channel", 1)
                .setDescription("Maximum number of AMQP channels per connection"))
            .add(Param.defaultTo("num_exchange", 1)
                .setDescription("Maximum number of AMQP exchanges per channel."))
            .add(Param.defaultTo("num_queue", 1)
                .setDescription("Max number of queues per exchange (only relevant for receivers)."))
            .add(Param.defaultTo("num_msg_clnt", 1)
                .setDescription("Max number of message clients per exchange (sender) or per queue (receiver)."))
            .add(Param.defaultTo("max_op_time", 0)
                .setDescription("Maximum time (in seconds) to run NB Kafka testing scenario."))
            .add(Param.defaultTo("strict_msg_error_handling", false)
                .setDescription("Whether to do strict error handling which is to stop NB Kafka execution."))
            .asReadOnly();
    }

    public Connection getAmqpConnection(Long id) { return amqpConnections.get(id); }

    public Channel getAmqpChannels(
        AmqpChannelKey key,
        Supplier<Channel> channelSupplier) {
            return amqpChannels.computeIfAbsent(key, __ -> channelSupplier.get());
    }

    public long getActivityStartTimeMills() { return this.activityStartTimeMills; }
    public long getMaxOpTimeInSec() { return this.maxOpTimeInSec; }
    public AmqpClientConf getAmqpClientConf() { return amqpClientConf; }

    public String getAmqpExchangeType() { return amqpExchangeType; }
    public int getAmqpConnNum() { return this.amqpConnNum; }
    public int getAmqpConnChannelNum() { return this.amqpConnChannelNum; }
    public int getAmqpChannelExchangeNum() { return this.amqpChannelExchangeNum; }
    public int getAmqpExchangeQueueNum() { return this.amqpExchangeQueueNum; }
    public int getAmqpMsgClntNum() { return this.amqpMsgClntNum; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public long getTotalCycleNum() { return totalCycleNum; }
    public void setTotalCycleNum(long cycleNum) { totalCycleNum = cycleNum; }

    public long getTotalThreadNum() { return totalThreadNum; }
    public void setTotalThreadNum(long threadNum) { totalThreadNum = threadNum; }

    public void initializeSpace(AmqpClientConf amqpClientConf) {
        Map<String, String> cfgMap = amqpClientConf.getConfigMap();

        if  (amqpConnNum < 1) {
            String errMsg = "AMQP connection number (\"num_conn\") must be a positive number!";
            throw new AmqpAdapterInvalidParamException(errMsg);
        }

        if  (amqpConnChannelNum < 1) {
            String errMsg = "AMQP channel number per connection (\"num_channel\") must be a positive number!";
            throw new AmqpAdapterInvalidParamException(errMsg);
        }

        amqpExchangeType = cfgMap.get("exchangeType");
        if (!AmqpAdapterUtil.AMQP_EXCHANGE_TYPES.isValidLabel(amqpExchangeType)) {
            String errMsg = "Invalid AMQP exchange type: \"" + amqpExchangeType + "\". " +
                "Valid values are: \"" + AmqpAdapterUtil.getValidAmqpExchangeTypeList() + "\"";
            throw new AmqpAdapterInvalidParamException(errMsg);
        }

        if (amqpConnFactory == null) {
            try {
                amqpConnFactory = new ConnectionFactory();

                String amqpServerHost = cfgMap.get("amqpSrvHost");
                if (StringUtils.isBlank(amqpServerHost)) {
                    String errMsg = "AMQP server host (\"amqpSrvHost\") must be specified!";
                    throw new AmqpAdapterInvalidParamException(errMsg);
                }
                amqpConnFactory.setHost(amqpServerHost);

                String amqpSrvPortCfg = cfgMap.get("amqpSrvPort");
                if (StringUtils.isBlank(amqpSrvPortCfg)) {
                    String errMsg = "AMQP server port (\"amqpSrvPort\") must be specified!";
                    throw new AmqpAdapterInvalidParamException(errMsg);
                }
                amqpConnFactory.setPort(Integer.parseInt(amqpSrvPortCfg));

                String amqpVirtualHost = cfgMap.get("virtualHost");
                if (StringUtils.isBlank(amqpVirtualHost)) {
                    String errMsg = "AMQP virtual host (\"virtualHost\") must be specified!";
                    throw new AmqpAdapterInvalidParamException(errMsg);
                }
                amqpConnFactory.setVirtualHost(amqpVirtualHost);

                String userNameCfg = cfgMap.get("amqpUser");

                String passWordCfg = cfgMap.get("amqpPassword");
                if (StringUtils.isNotBlank(passWordCfg)) {
                    String passWord = passWordCfg;
                    if (StringUtils.startsWith(passWordCfg, "file://")
                        && StringUtils.length(passWordCfg) > 7) {
                        String jwtTokenFile = StringUtils.substring(passWordCfg, 7);
                        passWord = FileUtils.readFileToString(new File(jwtTokenFile), "UTF-8");
                    }

                    if (StringUtils.isNotBlank(passWord)) {
                        if (StringUtils.isBlank(userNameCfg)) {
                            amqpConnFactory.setUsername("");
                        }
                        amqpConnFactory.setPassword(passWord);
                    }
                }

                String useTlsCfg = cfgMap.get("useTls");
                if (StringUtils.isNotBlank(useTlsCfg) && Boolean.parseBoolean(useTlsCfg)) {
                    amqpConnFactory.useSslProtocol();
                }

                for (int i = 0; i < getAmqpConnNum(); i++) {
                    Connection connection = amqpConnFactory.newConnection();
                    amqpConnections.put((long) i, connection);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[AMQP Connection created] {} -- [{}] {}",
                            Thread.currentThread().getName(),
                            i,
                            connection);
                    }
                }
            } catch (IOException|TimeoutException|NoSuchAlgorithmException|KeyManagementException  ex) {
                logger.error("Unable to establish AMQP connections with the following configuration parameters: {}",
                    amqpClientConf.toString());
                throw new AmqpAdapterUnexpectedException(ex);
            }
        }
    }

    public void shutdownSpace() {
        try {
            beingShutdown.set(true);

            for (Channel channel : amqpChannels.values()) {
                channel.close();
            }

            for (Connection connection : amqpConnections.values()) {
                connection.close();
            }

            // Pause 5 seconds before closing producers/consumers
            AmqpAdapterUtil.pauseCurThreadExec(5);
        }
        catch (Exception ex) {
            String exp = "Unexpected error when shutting down the AMQP adaptor space";
            logger.error(exp, ex);
        }
    }
}
