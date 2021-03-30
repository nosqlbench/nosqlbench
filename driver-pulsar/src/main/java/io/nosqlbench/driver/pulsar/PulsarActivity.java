package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.PulsarOp;
import io.nosqlbench.driver.pulsar.ops.ReadyPulsarOp;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(PulsarActivity.class);

    public Timer bindTimer;
    public Timer executeTimer;
    public Counter bytesCounter;
    public Histogram messagesizeHistogram;

    private PulsarSpaceCache pulsarCache;
    private PulsarAdmin pulsarAdmin;

    private PulsarNBClientConf clientConf;
    // e.g. pulsar://localhost:6650
    private String pulsarSvcUrl;
    // e.g. http://localhost:8080
    private String webSvcUrl;

    private NBErrorHandler errorhandler;
    private OpSequence<OpDispenser<PulsarOp>> sequencer;
    private volatile Throwable asyncOperationFailure;

    // private Supplier<PulsarSpace> clientSupplier;
    // private ThreadLocal<Supplier<PulsarClient>> tlClientSupplier;

    public PulsarActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    private void initPulsarAdmin() {
        PulsarAdminBuilder adminBuilder =
            PulsarAdmin.builder()
            .serviceHttpUrl(webSvcUrl);

        try {
            String authPluginClassName =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.authPulginClassName.label);
            String authParams =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.authParams.label);

            String useTlsStr =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.useTls.label);
            boolean useTls = BooleanUtils.toBoolean(useTlsStr);

            String tlsTrustCertsFilePath =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.tlsTrustCertsFilePath.label);

            String tlsAllowInsecureConnectionStr =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.tlsAllowInsecureConnection.label);
            boolean tlsAllowInsecureConnection = BooleanUtils.toBoolean(tlsAllowInsecureConnectionStr);

            String tlsHostnameVerificationEnableStr =
                (String) clientConf.getClientConfValue(PulsarActivityUtil.CLNT_CONF_KEY.tlsHostnameVerificationEnable.label);
            boolean tlsHostnameVerificationEnable = BooleanUtils.toBoolean(tlsHostnameVerificationEnableStr);

            if ( !StringUtils.isAnyBlank(authPluginClassName, authParams) ) {
                adminBuilder = adminBuilder.authentication(authPluginClassName, authParams);
            }

            if ( useTls ) {
                adminBuilder = adminBuilder
                    .useKeyStoreTls(useTls)
                    .allowTlsInsecureConnection(tlsAllowInsecureConnection)
                    .enableTlsHostnameVerification(tlsHostnameVerificationEnable);

                if (!StringUtils.isBlank(tlsTrustCertsFilePath))
                    adminBuilder = adminBuilder.tlsTrustCertsFilePath(tlsTrustCertsFilePath);

            }

            pulsarAdmin = adminBuilder.build();

        } catch (PulsarClientException e) {
            logger.error("Fail to create PulsarAdmin from global configuration!");
            throw new RuntimeException("Fail to create PulsarAdmin from global configuration!");
        }
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messagesizeHistogram = ActivityMetrics.histogram(activityDef, "messagesize");

        String pulsarClntConfFile =
            activityDef.getParams().getOptionalString("config").orElse("config.properties");
        clientConf = new PulsarNBClientConf(pulsarClntConfFile);

        pulsarSvcUrl =
            activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");
        webSvcUrl =
            activityDef.getParams().getOptionalString("web_url").orElse("pulsar://localhost:8080");

        initPulsarAdmin();

        pulsarCache = new PulsarSpaceCache(this);

        this.sequencer = createOpSequence((ot) -> new ReadyPulsarOp(ot, pulsarCache, this));
        setDefaultsFromOpSequence(sequencer);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );
    }

    public NBErrorHandler getErrorhandler() {
        return errorhandler;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    public OpSequence<OpDispenser<PulsarOp>> getSequencer() {
        return sequencer;
    }

    public PulsarNBClientConf getPulsarConf() {
        return clientConf;
    }

    public String getPulsarSvcUrl() {
        return pulsarSvcUrl;
    }

    public String getWebSvcUrl() { return webSvcUrl; }

    public PulsarAdmin getPulsarAdmin() { return pulsarAdmin; }

    public Timer getBindTimer() {
        return bindTimer;
    }

    public Timer getExecuteTimer() {
        return this.executeTimer;
    }

    public Counter getBytesCounter() {
        return bytesCounter;
    }

    public Histogram getMessagesizeHistogram() {
        return messagesizeHistogram;
    }

    public void failOnAsyncOperationFailure() {
        if (asyncOperationFailure != null) {
            throw new RuntimeException(asyncOperationFailure);
        }
    }

    public void asyncOperationFailed(Throwable ex) {
        this.asyncOperationFailure = asyncOperationFailure;
    }
}
