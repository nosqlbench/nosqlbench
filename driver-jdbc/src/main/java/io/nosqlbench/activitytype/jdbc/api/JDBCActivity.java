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

package io.nosqlbench.activitytype.jdbc.api;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.nosqlbench.activitytype.jdbc.impl.ReadyJDBCOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Function;

// This should not be exposed as as service directly unless it can
// be used with a modular JDBC configuration.
public abstract class JDBCActivity extends SimpleActivity {
    private final static Logger LOGGER = LogManager.getLogger(JDBCActivity.class);
    private Timer bindTimer;
    private Timer resultTimer;
    private Timer resultSuccessTimer;
    private Histogram triesHisto;
    private int maxTries;
    private int minRetryDelayMs;

    protected DataSource dataSource;
    protected OpSequence<OpDispenser<? extends String>> opSequence;

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

        this.maxTries = getParams().getOptionalInteger("maxtries").orElse(3);
        this.minRetryDelayMs = getParams().getOptionalInteger("minretrydelayms").orElse(200);

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

        opSequence = createOpSequence(ReadyJDBCOp::new,false);
        setDefaultsFromOpSequence(opSequence);

        onActivityDefUpdate(getActivityDef());
    }

    public String errorNameMapper(Throwable e) {
        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName());
        if (e instanceof SQLException) {
            String sqlState = ((SQLException) e).getSQLState();
            if (sqlState != null && !sqlState.isEmpty()) {
                sb.append('_');
                sb.append(sqlState);
            }
        }
        return sb.toString();
    }

    @Override
    public Function<Throwable, String> getErrorNameMapper() {
        return this::errorNameMapper;
    }

    public int getMaxTries() {
        return this.maxTries;
    }

    public int getMinRetryDelayMs() {
        return this.minRetryDelayMs;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public OpSequence<OpDispenser<? extends String>> getOpSequence() {
        return opSequence;
    }

    public Timer getBindTimer() {
        return bindTimer;
    }

    public Timer getResultTimer() {
        return resultTimer;
    }

    public Timer getResultSuccessTimer() {
        return resultSuccessTimer;
    }

    public Histogram getTriesHisto() {
        return triesHisto;
    }
}
