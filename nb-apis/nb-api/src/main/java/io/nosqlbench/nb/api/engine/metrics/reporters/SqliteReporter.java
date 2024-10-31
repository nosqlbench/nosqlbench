/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.Snapshot;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.PeriodicTaskComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SqliteReporter extends PeriodicTaskComponent {
    private static final Logger logger = LogManager.getLogger(SqliteReporter.class);
    private final String url;
    private final NBComponent parent;
    private final MetricInstanceFilter filter;
    private final Connection connection;
    private final PreparedStatement insert_statement;

    //TODO: These need to be dynamically passed in, just hard-coding POC for now
    private final String create_table = """
        CREATE TABLE IF NOT EXISTS metrics (
            REPORT_ID TEXT NOT NULL,
            REPORT_TIME DATETIME NOT NULL,
            METRIC_NAME TEXT NOT NULL,
            LABELS TEXT,
            TYPE_NAME TEXT,
            METRIC_VALUE TEXT,
            DESCRIPTION TEXT,
            METRIC_CATEGORIES TEXT,
            PRIMARY KEY(REPORT_ID,METRIC_NAME)
        );
        """;

    private final String insert_record = """
        INSERT INTO metrics(REPORT_ID,REPORT_TIME,METRIC_NAME,LABELS,TYPE_NAME,METRIC_VALUE,DESCRIPTION,METRIC_CATEGORIES)
            VALUES(?,?,?,?,?,?,?,?)
        """;

    public SqliteReporter(NBComponent parent, String url, long intervalMs, MetricInstanceFilter filter, NBLabels extraLabels) {
        super(parent, extraLabels, intervalMs, "REPORT-SQLITE", FirstReport.OnInterval, LastReport.OnInterrupt);
        this.parent = parent;
        this.url = url;
        this.filter = filter;
        try {
            connection = DriverManager.getConnection(url);
            logger.info(() -> "SQLite connection to " + url + " has been established.");
            // Method to compare existing schema with desired schema and update if necessary
            validateSchema();
            insert_statement = connection.prepareStatement(insert_record);
        } catch (SQLException e) {
            logger.error(() -> "Exception constructing SQLite reporter: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateSchema() throws SQLException {
        connection.createStatement().execute(create_table);
    }

    public SqliteReporter(NBComponent parent, String url, long intervalMs, MetricInstanceFilter filter) {
        this(parent, url, intervalMs, filter, null);
    }

    public SqliteReporter(NBComponent parent, String url, long intervalMs) {
        this(parent, url, intervalMs, null, null);
    }

    @Override
    public void task() {
        List<NBMetric> metrics = parent.find().metrics();
        final long report_time = System.currentTimeMillis();
        String report_id = String.valueOf(UUID.randomUUID());
        for (NBMetric metric : metrics) {
            String metric_name = metric.getLabels().valueOf("name");
            String labels = metric.getHandle();
            String type_name = metric.typeName();
            String description = metric.getDescription();
            String metric_categories = Arrays.stream(metric.getCategories()).map(Enum::name)
                .collect(Collectors.joining(","));
            String metric_value = switch (metric) {
                case NBMetricGauge gauge -> extractValue(gauge);
                case NBMetricCounter counter -> extractValue(counter);
                case NBMetricHistogram histogram -> extractValue(histogram);
                case NBMetricTimer timer -> extractValue(timer);
                case NBMetricMeter meter -> extractValue(meter);
                default -> throw new RuntimeException("Unrecognized metric type to report '" + metric.getClass().getSimpleName() + "'");
            };
            try {
                insert_statement.setString(1, report_id);
                insert_statement.setLong(2, report_time);
                insert_statement.setString(3, metric_name);
                insert_statement.setString(4, labels);
                insert_statement.setString(5, type_name);
                insert_statement.setString(6, metric_value);
                insert_statement.setString(7, description);
                insert_statement.setString(8, metric_categories);
                insert_statement.executeUpdate();
            } catch (SQLException e) {
                logger.error(() -> "Exception inserting record: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private String extractValue(NBMetricGauge gauge) {
        return String.valueOf(gauge.getValue());
    }

    private String extractValue(NBMetricCounter counter) {
        return String.valueOf(counter.getCount());
    }

    private String extractValue(NBMetricHistogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        return String.format("count=%d,max=%d,mean=%f,min=%d,stddev=%f,p50=%f,p75=%f,p95=%f,p98=%f,p99=%f,p999=%f",
            histogram.getCount(),
            snapshot.getMax(),
            snapshot.getMean(),
            snapshot.getMin(),
            snapshot.getStdDev(),
            snapshot.getMedian(),
            snapshot.get75thPercentile(),
            snapshot.get95thPercentile(),
            snapshot.get98thPercentile(),
            snapshot.get99thPercentile(),
            snapshot.get999thPercentile());
    }

    private String extractValue(NBMetricTimer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        return "count=" + timer.getCount() + "," +
            "max=" + snapshot.getMax() + "," +
            "mean=" + snapshot.getMean() + "," +
            "min=" + snapshot.getMin() + "," +
            "stddev=" + snapshot.getStdDev() + "," +
            "p50=" + snapshot.getMedian() + "," +
            "p75=" + snapshot.get75thPercentile() + "," +
            "p95=" + snapshot.get95thPercentile() + "," +
            "p98=" + snapshot.get98thPercentile() + "," +
            "p99=" + snapshot.get99thPercentile() + "," +
            "p999=" + snapshot.get999thPercentile() + "," +
            "mean_rate=" + timer.getMeanRate() + "," +
            "m1_rate=" + timer.getOneMinuteRate() + "," +
            "m5_rate=" + timer.getFiveMinuteRate() + "," +
            "m15_rate=" + timer.getFifteenMinuteRate();
    }

    private String extractValue(NBMetricMeter meter) {
        return String.format("count=%d,mean_rate=%f,m1_rate=%f,m5_rate=%f,m15_rate=%f",
            meter.getCount(),
            meter.getMeanRate(),
            meter.getOneMinuteRate(),
            meter.getFiveMinuteRate(),
            meter.getFifteenMinuteRate());
    }

}
