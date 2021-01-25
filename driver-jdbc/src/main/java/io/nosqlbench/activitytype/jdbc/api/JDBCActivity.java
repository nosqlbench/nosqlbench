package io.nosqlbench.activitytype.jdbc.api;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.nosqlbench.activitytype.jdbc.impl.ReadyJDBCOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class JDBCActivity extends SimpleActivity {
    private final static Logger LOGGER = LogManager.getLogger(JDBCActivity.class);
    private Timer bindTimer;
    private Timer resultTimer;
    private Timer resultSuccessTimer;
    private Histogram triesHisto;
    private ExceptionCountMetrics exceptionCount;
    private SQLExceptionCountMetrics sqlExceptionCount;

    protected DataSource dataSource;
    protected OpSequence<ReadyJDBCOp> opSequence;

    public JDBCActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    /*
    Subclasses construct a DataSource object. Concrete type should *not* be a pooled DataSource,
    as this class implements wrapping with HikariDataSource if required.
     */
    protected abstract DataSource newDataSource();

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        LOGGER.debug("initializing data source");
        dataSource = newDataSource();

        String connectionPool = getParams().getOptionalString("connectionpool").orElse("");
        if (!connectionPool.isEmpty()) {
            LOGGER.debug("initializing connectionpool " + connectionPool);
            if (connectionPool.equals("hikari")) {
                HikariConfig config = new HikariConfig();
                config.setDataSource(dataSource);
                dataSource = new HikariDataSource(config);
            } else {
                throw new RuntimeException("unknown connectionpool parameter value " + connectionPool);
            }
        }
    }

    @Override
    public void initActivity() {
        LOGGER.debug("initializing activity: " + getActivityDef().getAlias());
        bindTimer = ActivityMetrics.timer(getActivityDef(), "bind");
        resultTimer = ActivityMetrics.timer(getActivityDef(), "result");
        resultSuccessTimer = ActivityMetrics.timer(getActivityDef(), "result-success");
        triesHisto = ActivityMetrics.histogram(getActivityDef(), "tries");
        exceptionCount = new ExceptionCountMetrics(getActivityDef());
        sqlExceptionCount = new SQLExceptionCountMetrics(getActivityDef());

        opSequence = createOpSequence(ReadyJDBCOp::new);
        setDefaultsFromOpSequence(opSequence);

        onActivityDefUpdate(getActivityDef());
    }

    public int getMaxTries() { return 3; }

    public boolean isRetryable(SQLException sqlException) {
        return true;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public OpSequence<ReadyJDBCOp> getOpSequence() { return opSequence; }

    public Timer getBindTimer() { return bindTimer; }

    public Timer getResultTimer() { return resultTimer; }

    public Timer getResultSuccessTimer() { return resultSuccessTimer; }

    public Histogram getTriesHisto() { return triesHisto; }

    public ExceptionCountMetrics getExceptionCount() { return exceptionCount; }

    public SQLExceptionCountMetrics getSQLExceptionCount() { return sqlExceptionCount; }

    public static class SQLExceptionCountMetrics {
        private final ConcurrentHashMap<Integer, Counter> counters = new ConcurrentHashMap<>();
        private final ActivityDef activityDef;

        private SQLExceptionCountMetrics(ActivityDef activityDef) {
            this.activityDef = activityDef;
        }

        public void inc(SQLException e) {
            Counter c = counters.computeIfAbsent(
                e.getErrorCode(),
                k -> ActivityMetrics.counter(activityDef, "errorcodecounts." + k)
            );
            c.inc();
        }
    }
}
