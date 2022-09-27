package io.nosqlbench.driver.jms;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.jms.conn.S4JConnInfo;
import io.nosqlbench.driver.jms.ops.ReadyS4JOp;
import io.nosqlbench.driver.jms.ops.S4JOp;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JConfFromFile;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiters;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

public class S4JActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(S4JActivity.class);

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
    private S4JSpaceCache s4JSpaceCache;
    private S4JConnInfo s4JConnInfo;

    private OpSequence<OpDispenser<S4JOp>> sequence;
    private volatile Throwable asyncOperationFailure;
    private NBErrorHandler errorhandler;

    private boolean cycleratePerThread;

    private Counter bytesCounter;
    private Histogram messageSizeHistogram;
    private Timer bindTimer;
    private Timer executeTimer;

    public S4JActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void shutdownActivity() {
        super.shutdownActivity();

        if (s4JSpaceCache == null) {
            return;
        }

        for (S4JSpace s4jSpace : s4JSpaceCache.getAssociatedSpaces()) {
            s4jSpace.shutdownSpace();
        }
    }

    @Override
    public void initActivity() {
        super.initActivity();
        s4JSpaceCache = new S4JSpaceCache(this);

        String curThreadName = Thread.currentThread().getName();

        String s4jConfFile =
            activityDef.getParams().getOptionalString("config").orElse("config.properties");
        S4JConfFromFile s4JConfFromFile = new S4JConfFromFile(s4jConfFile);

        String webSvcUrl =
            activityDef.getParams().getOptionalString("web_url").orElse("http://localhost:8080");
        String pulsarSvcUrl =
            activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");

        String maxS4JOpTimeInSecStr =
            activityDef.getParams().getOptionalString("max_s4jop_time").orElse("0");
        maxS4JOpTimeInSec = NumberUtils.toLong(maxS4JOpTimeInSecStr, 0);
        if (maxS4JOpTimeInSec < 0) maxS4JOpTimeInSec = 0;

        String trackingMsgRecvCntStr =
            activityDef.getParams().getOptionalString("track_msg_cnt").orElse("false");
        trackingMsgRecvCnt = BooleanUtils.toBoolean(trackingMsgRecvCntStr);

        String numConnectionStr =
            activityDef.getParams().getOptionalString("num_conn").orElse("");
        maxNumConn = NumberUtils.toInt(numConnectionStr, 1);

        String numSessionPerConnStr =
            activityDef.getParams().getOptionalString("num_session").orElse("");
        maxNumSessionPerConn = NumberUtils.toInt(numSessionPerConnStr, 1);

        String sessionModeStr =
            activityDef.getParams().getOptionalString("session_mode").orElse("");

        String strictMsgErrorHandlingStr =
            activityDef.getParams().getOptionalString("strict_msg_error_handling").orElse("false");
        strictMsgErrorHandling = BooleanUtils.toBoolean(strictMsgErrorHandlingStr);

        // Global level connection info that is read from the config.properties file
        s4JConnInfo = new S4JConnInfo(webSvcUrl, pulsarSvcUrl, sessionModeStr, s4JConfFromFile);

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messageSizeHistogram = ActivityMetrics.histogram(activityDef, "messageSize");

        this.sequence = createOpSequence((ot) -> new ReadyS4JOp(ot, s4JSpaceCache,this));

        setDefaultsFromOpSequence(sequence);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );

        cycleratePerThread = activityDef.getParams().takeBoolOrDefault("cyclerate_per_thread", false);
    }

    private final ThreadLocal<RateLimiter> cycleLimiterThreadLocal = ThreadLocal.withInitial(() -> {
        if (super.getCycleLimiter() != null) {
            return RateLimiters.createOrUpdate(this.getActivityDef(), "cycles", null,
                super.getCycleLimiter().getRateSpec());
        } else {
            return null;
        }
    });

    @Override
    public RateLimiter getCycleLimiter() {
        if (cycleratePerThread) {
            return cycleLimiterThreadLocal.get();
        } else {
            return super.getCycleLimiter();
        }
    }

    public long getS4JActivityStartTimeMills() { return this.s4JActivityStartTimeMills; }
    public void setS4JActivityStartTimeMills(long startTime) { this.s4JActivityStartTimeMills = startTime; }

    public long getMaxS4JOpTimeInSec() { return this.maxS4JOpTimeInSec; }

    public boolean isTrackingMsgRecvCnt() { return trackingMsgRecvCnt; }

    public int getMaxNumSessionPerConn() { return this.maxNumSessionPerConn; }
    public int getMaxNumConn() { return this.maxNumConn; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public void processMsgAck(JMSContext jmsContext, Message message, float msgAckRatio, int slowAckInSec) throws JMSException {
        int jmsSessionMode = jmsContext.getSessionMode();

        if ((jmsSessionMode != Session.AUTO_ACKNOWLEDGE) &&
            (jmsSessionMode != Session.SESSION_TRANSACTED)) {
            float rndVal = RandomUtils.nextFloat(0, 1);
            if (rndVal < msgAckRatio) {
                S4JActivityUtil.pauseCurThreadExec(slowAckInSec);
                message.acknowledge();
            }
        }
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) { super.onActivityDefUpdate(activityDef); }
    public OpSequence<OpDispenser<S4JOp>> getSequencer() { return sequence; }

    public S4JConnInfo getS4JConnInfo() { return s4JConnInfo; }

    public Timer getBindTimer() { return bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Counter getBytesCounter() { return bytesCounter; }
    public Histogram getMessagesizeHistogram() { return messageSizeHistogram; }

    public NBErrorHandler getErrorhandler() { return errorhandler; }

    public void failOnAsyncOperationFailure() {
        if (asyncOperationFailure != null) {
            throw new RuntimeException(asyncOperationFailure);
        }
    }
    public void asyncOperationFailed(Throwable ex) {
        this.asyncOperationFailure = ex;
    }
}
