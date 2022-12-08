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

package io.nosqlbench.adapter.s4j;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.datastax.oss.pulsar.jms.PulsarJMSContext;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterInvalidParamException;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterUnexpectedException;
import io.nosqlbench.adapter.s4j.util.*;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class S4JSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(S4JSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    // - Each S4J space currently represents a number of JMS connections (\"num_conn\" NB CLI parameter);
    // - JMS connection can have a number of JMS sessions (\"num_session\" NB CLI parameter).
    // - Each JMS session has its own sets of JMS destinations, producers, consumers, etc.
    private final ConcurrentHashMap<String, JMSContext> connLvlJmsContexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, S4JJMSContextWrapper> sessionLvlJmsContexts = new ConcurrentHashMap<>();

    private final String pulsarSvcUrl;
    private final String webSvcUrl;
    private final String s4jClientConfFileName;
    private S4JClientConf s4JClientConf;
    private final int sessionMode;

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the Pulsar server while doing message receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private boolean strictMsgErrorHandling;

    // Maximum time length to execute S4J operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4JOp is always executed until NB execution cycle finishes
    private long maxS4JOpTimeInSec;
    private long s4JActivityStartTimeMills;

    // Whether to keep track of the received message count, which includes
    // - total received message count
    // - received null message count (only relevant when non-blocking message receiving is used)
    // By default, this setting is disabled
    private boolean trackingMsgRecvCnt;

    // How many JMS connections per NB S4J execution
    private int maxNumConn;
    // How many sessions per JMS connection
    private int maxNumSessionPerConn;

    // Total number of acknowledgement received
    // - this can apply to both message production and consumption
    // - for message consumption, this only applies to non-null messages received (which is for async API)
    private final AtomicLong totalOpResponseCnt = new AtomicLong(0);
    // Total number of null messages received
    // - only applicable to message consumption
    private final AtomicLong nullMsgRecvCnt = new AtomicLong(0);

    // Keep track the transaction count per thread
    private final ThreadLocal<Integer> txnBatchTrackingCnt = ThreadLocal.withInitial(() -> 0);

    // Represents the JMS connection
    private PulsarConnectionFactory s4jConnFactory;

    private long totalCycleNum;

    public S4JSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        this.pulsarSvcUrl = cfg.get("service_url");
        this.webSvcUrl = cfg.get("web_url");
        this.maxNumConn=
            NumberUtils.toInt(cfg.getOptional("num_conn").orElse("1"));
        this.maxNumSessionPerConn =
            NumberUtils.toInt(cfg.getOptional("num_session").orElse("1"));
        this.maxS4JOpTimeInSec =
            NumberUtils.toLong(cfg.getOptional("max_s4jop_time").orElse("0L"));
        this.trackingMsgRecvCnt =
            BooleanUtils.toBoolean(cfg.getOptional("track_msg_cnt").orElse("false"));
        this.strictMsgErrorHandling =
            BooleanUtils.toBoolean(cfg.getOptional("strict_msg_error_handling").orElse("false"));
        this.s4jClientConfFileName = cfg.get("config");
        this.sessionMode = S4JAdapterUtil.getSessionModeFromStr(
            cfg.getOptional("session_mode").orElse(""));
        this.s4JClientConf = new S4JClientConf(pulsarSvcUrl, webSvcUrl, s4jClientConfFileName);

        this.initializeSpace(s4JClientConf);
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(S4JSpace.class)
            .add(Param.defaultTo("service_url", "pulsar://localhost:6650")
                .setDescription("Pulsar broker service URL."))
            .add(Param.defaultTo("web_url", "http://localhost:8080")
                .setDescription("Pulsar web service URL."))
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("Pulsar client connection configuration property file."))
            .add(Param.defaultTo("num_conn", 1)
                .setDescription("Number of JMS connections"))
            .add(Param.defaultTo("num_session", 1)
                .setDescription("Number of JMS sessions per JMS connection"))
            .add(Param.defaultTo("max_s4jop_time", 0)
                .setDescription("Maximum time (in seconds) to run NB S4J testing scenario."))
            .add(Param.defaultTo("track_msg_cnt", false)
                .setDescription("Whether to keep track of message count(s)"))
            .add(Param.defaultTo("session_mode", "")
                .setDescription("JMS session mode"))
            .add(Param.defaultTo("strict_msg_error_handling", false)
                .setDescription("Whether to do strict error handling which is to stop NB S4J execution."))
            .asReadOnly();
    }

    public ConcurrentHashMap<String, JMSContext> getConnLvlJmsContexts() {
        return connLvlJmsContexts;
    }

    public ConcurrentHashMap<String, S4JJMSContextWrapper> getSessionLvlJmsContexts() {
        return sessionLvlJmsContexts;
    }

    public long getS4JActivityStartTimeMills() { return this.s4JActivityStartTimeMills; }
    public void setS4JActivityStartTimeMills(long startTime) { this.s4JActivityStartTimeMills = startTime; }

    public long getMaxS4JOpTimeInSec() { return this.maxS4JOpTimeInSec; }

    public int getSessionMode() { return sessionMode; }

    public String getS4jClientConfFileName() { return s4jClientConfFileName; }
    public S4JClientConf getS4JClientConf() { return s4JClientConf; }

    public boolean isTrackingMsgRecvCnt() { return trackingMsgRecvCnt; }

    public int getMaxNumSessionPerConn() { return this.maxNumSessionPerConn; }
    public int getMaxNumConn() { return this.maxNumConn; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public int getTxnBatchTrackingCnt() { return txnBatchTrackingCnt.get(); }
    public void incTxnBatchTrackingCnt() {
        int curVal = getTxnBatchTrackingCnt();
        txnBatchTrackingCnt.set(curVal + 1);
    }

    public long getTotalOpResponseCnt() { return totalOpResponseCnt.get();}
    public long incTotalOpResponseCnt() { return totalOpResponseCnt.incrementAndGet();}
    public void resetTotalOpResponseCnt() { totalOpResponseCnt.set(0); }

    public long getTotalNullMsgRecvdCnt() { return nullMsgRecvCnt.get();}
    public void resetTotalNullMsgRecvdCnt() { nullMsgRecvCnt.set(0); }

    public long incTotalNullMsgRecvdCnt() { return nullMsgRecvCnt.incrementAndGet(); }

    public PulsarConnectionFactory getS4jConnFactory() { return s4jConnFactory; }

    public long getTotalCycleNum() { return totalCycleNum; }
    public void setTotalCycleNum(long cycleNum) { totalCycleNum = cycleNum; }

    public void initializeSpace(S4JClientConf s4JClientConnInfo) {
        if (s4jConnFactory == null) {
            Map<String, Object> cfgMap;
            try {
                cfgMap = s4JClientConnInfo.getS4jConfObjMap();
                s4jConnFactory = new PulsarConnectionFactory(cfgMap);

                for (int i=0; i<getMaxNumConn(); i++) {
                    // Establish a JMS connection
                    String connLvlJmsConnContextIdStr = getConnLvlJmsContextIdentifier(i);
                    String clientIdStr = Base64.getEncoder().encodeToString(connLvlJmsConnContextIdStr.getBytes());

                    JMSContext jmsConnContext = getOrCreateConnLvlJMSContext(s4jConnFactory, s4JClientConnInfo, sessionMode);
                    jmsConnContext.setClientID(clientIdStr);
                    jmsConnContext.setExceptionListener(e -> {
                        if (logger.isDebugEnabled()) {
                            logger.error("onException::Unexpected JMS error happened:" + e);
                        }
                    });

                    connLvlJmsContexts.put(connLvlJmsConnContextIdStr, jmsConnContext);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[Connection level JMSContext] {} -- {}",
                            Thread.currentThread().getName(),
                            jmsConnContext );
                    }
                }
            }
            catch (JMSRuntimeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[ERROR] Unable to initialize JMS connection factory with the following configuration parameters: {}", s4JClientConnInfo.toString());
                }
                throw new S4JAdapterUnexpectedException("Unable to initialize JMS connection factory with the following error message: " + e.getCause());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdownSpace() {
        long shutdownStartTimeMills = System.currentTimeMillis();

        try {
            waitUntilAllOpFinished(shutdownStartTimeMills);

            this.txnBatchTrackingCnt.remove();

            for (S4JJMSContextWrapper s4JJMSContextWrapper : sessionLvlJmsContexts.values()) {
                if (s4JJMSContextWrapper != null) {
                    if (s4JJMSContextWrapper.isTransactedMode()) {
                        s4JJMSContextWrapper.getJmsContext().rollback();
                    }
                    s4JJMSContextWrapper.close();
                }
            }

            for (JMSContext jmsContext : connLvlJmsContexts.values()) {
                if (jmsContext != null) jmsContext.close();
            }

            s4jConnFactory.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new S4JAdapterUnexpectedException("Unexpected error when shutting down NB S4J space.");
        }
    }

    // When completing NB execution, don't shut down right away because otherwise, async operation processing may fail.
    // Instead, shut down when either one of the following condition is satisfied
    // 1) the total number of the received operation response is the same as the total number of operations being executed;
    // 2) time has passed for 10 seconds
    private void waitUntilAllOpFinished(long shutdownStartTimeMills) {
        long totalCycleNum = getTotalCycleNum();
        long totalResponseCnt = 0;
        long totalNullMsgCnt = 0;
        long timeElapsedMills;

        boolean trackingMsgCnt = isTrackingMsgRecvCnt();
        boolean continueChk;

        do {
            S4JAdapterUtil.pauseCurThreadExec(1);

            long curTimeMills = System.currentTimeMillis();
            timeElapsedMills = curTimeMills - shutdownStartTimeMills;
            continueChk = (timeElapsedMills <= 10000);

            if (trackingMsgCnt) {
                totalResponseCnt = this.getTotalOpResponseCnt();
                totalNullMsgCnt = this.getTotalNullMsgRecvdCnt();
                continueChk = continueChk && (totalResponseCnt < totalCycleNum);
            }

            if (logger.isTraceEnabled()) {
                logger.trace(
                    buildExecSummaryString(trackingMsgCnt, timeElapsedMills, totalResponseCnt, totalNullMsgCnt));
            }
        } while (continueChk);

        logger.info(
            buildExecSummaryString(trackingMsgCnt, timeElapsedMills, totalResponseCnt, totalNullMsgCnt));
    }

    private String buildExecSummaryString(
        boolean trackingMsgCnt,
        long timeElapsedMills,
        long totalResponseCnt,
        long totalNullMsgCnt)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("shutdownSpace::waitUntilAllOpFinished -- ")
            .append("shutdown time elapsed: ").append(timeElapsedMills).append("ms; ");

        if (trackingMsgCnt) {
            stringBuilder.append("response received: ").append(totalResponseCnt).append("; ");
            stringBuilder.append("null msg received: ").append(totalNullMsgCnt).append("; ");
        }

        return stringBuilder.toString();
    }

    public void processMsgAck(JMSContext jmsContext, Message message, float msgAckRatio, int slowAckInSec) throws JMSException {
        int jmsSessionMode = jmsContext.getSessionMode();

        if ((jmsSessionMode != Session.AUTO_ACKNOWLEDGE) &&
            (jmsSessionMode != Session.SESSION_TRANSACTED)) {
            float rndVal = RandomUtils.nextFloat(0, 1);
            if (rndVal < msgAckRatio) {
                S4JAdapterUtil.pauseCurThreadExec(slowAckInSec);
                message.acknowledge();
            }
        }
    }

    public String getConnLvlJmsContextIdentifier(int jmsConnSeqNum) {
        return S4JAdapterUtil.buildCacheKey(
            this.spaceName,
            StringUtils.join("conn-", jmsConnSeqNum));
    }

    public String getSessionLvlJmsContextIdentifier(int jmsConnSeqNum, int jmsSessionSeqNum) {
        return S4JAdapterUtil.buildCacheKey(
            this.spaceName,
            StringUtils.join("conn-", jmsConnSeqNum),
            StringUtils.join("session-", jmsSessionSeqNum));
    }

    // Create JMSContext that represents a new JMS connection
    public JMSContext getOrCreateConnLvlJMSContext(
        PulsarConnectionFactory s4jConnFactory,
        S4JClientConf s4JClientConf,
        int sessionMode)
    {
        if ( !S4JAdapterUtil.isAuthNRequired(s4JClientConf) &&
              S4JAdapterUtil.isUseCredentialsEnabled(s4JClientConf) ) {
            throw new S4JAdapterInvalidParamException(
                "'jms.useCredentialsFromCreateConnection' can't set be true " +
                    "when Pulsar client authN parameters are not set. "
            );
        }

        boolean useCredentialsEnable =
            S4JAdapterUtil.isAuthNRequired(s4JClientConf) &&
            S4JAdapterUtil.isUseCredentialsEnabled(s4JClientConf);
        JMSContext jmsConnContext;

        if (!useCredentialsEnable)
            jmsConnContext = s4jConnFactory.createContext(sessionMode);
        else {
            String userName = S4JAdapterUtil.getCredentialUserName(s4JClientConf);
            String passWord = S4JAdapterUtil.getCredentialPassword(s4JClientConf);

            // Password must be in "token:<token vale>" format
            if (! StringUtils.startsWith(passWord, "token:")) {
                throw new S4JAdapterInvalidParamException(
                    "When 'jms.useCredentialsFromCreateConnection' is enabled, " +
                        "the provided password must be in format 'token:<token_value_...> ");
            }

            jmsConnContext = s4jConnFactory.createContext(userName, passWord, sessionMode);
        }

        return jmsConnContext;
    }

    public S4JJMSContextWrapper getOrCreateS4jJmsContextWrapper(long curCycle) {
        return getOrCreateS4jJmsContextWrapper(curCycle, null);
    }

    // Get the next JMSContext Wrapper in the following approach
    // - The JMSContext wrapper pool has the following sequence (assuming 3 [c]onnections and 2 [s]essions per connection):
    //   c0s0, c0s1, c1s0, c1s1, c2s0, c2s1
    // - When getting the next JMSContext wrapper, always get from the next connection, starting from the first session
    //   When reaching the end of connection, move back to the first connection, but get the next session.
    //   e.g. first: c0s0   (0)
    //        next:  c1s0   (1)
    //        next:  c2s0   (2)
    //        next:  c0s1   (3)
    //        next:  c1s1   (4)
    //        next:  c2s1   (5)
    //        next:  c0s0   (6)  <-- repeat the pattern
    //        next:  c1s0   (7)
    //        next:  c2s0   (8)
    //        next:  c0s1   (9)
    //        ... ...
    public S4JJMSContextWrapper getOrCreateS4jJmsContextWrapper(
        long curCycle,
        Map<String, Object> overrideS4jConfMap)
    {
        int totalConnNum = getMaxNumConn();
        int totalSessionPerConnNum = getMaxNumSessionPerConn();

        int connSeqNum =  (int) curCycle % totalConnNum;
        int sessionSeqNum = ( (int)(curCycle / totalConnNum) ) % totalSessionPerConnNum;

        String jmsConnContextIdStr = getConnLvlJmsContextIdentifier(connSeqNum);
        JMSContext connLvlJmsContext = connLvlJmsContexts.get(jmsConnContextIdStr);
        // Connection level JMSContext objects should be already created during the initialization phase
        assert (connLvlJmsContext != null);

        String jmsSessionContextIdStr = getSessionLvlJmsContextIdentifier(connSeqNum, sessionSeqNum);
        S4JJMSContextWrapper jmsContextWrapper = sessionLvlJmsContexts.get(jmsSessionContextIdStr);

        if (jmsContextWrapper == null) {
            JMSContext jmsContext = null;

            if (overrideS4jConfMap == null || overrideS4jConfMap.isEmpty()) {
                jmsContext = connLvlJmsContext.createContext(connLvlJmsContext.getSessionMode());
            } else {
                jmsContext = ((PulsarJMSContext) connLvlJmsContext).createContext(
                    connLvlJmsContext.getSessionMode(), overrideS4jConfMap);
            }

            jmsContextWrapper = new S4JJMSContextWrapper(jmsSessionContextIdStr, jmsContext);
            sessionLvlJmsContexts.put(jmsSessionContextIdStr, jmsContextWrapper);

            if (logger.isDebugEnabled()) {
                logger.debug("[Session level JMSContext] {} -- {}",
                    Thread.currentThread().getName(),
                    jmsContextWrapper);
            }

        }
        return jmsContextWrapper;
    }
}
