/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.clientload.*;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentMetrics;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.labels.NBLabels;

public class NBSessionSafetyMetrics extends NBBaseComponent {

    public NBSessionSafetyMetrics(NBSession parent) {
        super(parent, NBLabels.forKV());
        ClientSystemMetricChecker metricsChecker = registerSessionSafetyMetrics(parent);
        metricsChecker.start();
    }

    private ClientSystemMetricChecker registerSessionSafetyMetrics(NBSession nbSession) {
        ClientSystemMetricChecker metricsChecker = new ClientSystemMetricChecker(
            this,
            NBLabels.forKV(),
            10
        );
        registerLoadAvgMetrics(metricsChecker);
        registerMemInfoMetrics(metricsChecker);
//        registerDiskStatsMetrics();
        registerNetworkInterfaceMetrics(metricsChecker);
        registerCpuStatMetrics(metricsChecker);
        return metricsChecker;
    }

    private void registerCpuStatMetrics(ClientSystemMetricChecker metricsChecker) {
        StatReader reader = new StatReader();
        if (!reader.fileExists())
            return;
        NBMetricGauge cpuUserGauge = create().gauge(
            "cpu_user",
            reader::getUserTime,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge cpuSystemGauge = create().gauge(
            "cpu_system",
            reader::getSystemTime,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge cpuIdleGauge = create().gauge(
            "cpu_idle",
            reader::getIdleTime,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge cpuIoWaitGauge = create().gauge(
            "cpu_iowait",
            reader::getIoWaitTime,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge cpuTotalGauge = create().gauge(
            "cpu_total",
            reader::getTotalTime,
            MetricCategory.Internals,
            ""
        );
        // add checking for percent of time spent in user space; TODO: Modify percent threshold
        metricsChecker.addRatioMetricToCheck(
            cpuUserGauge,
            cpuTotalGauge,
            50.0,
            true
        );
    }

    private void registerDiskStatsMetrics(ClientSystemMetricChecker metricsChecker) {
        DiskStatsReader reader = new DiskStatsReader();
        if (!reader.fileExists())
            return;

        for (String device : reader.getDevices()) {
            create().gauge(
                device + "_transactions",
                () -> reader.getTransactionsForDevice(device),
                MetricCategory.Internals,
                ""
            );
            create().gauge(device + "_kB_read",
                () -> reader.getKbReadForDevice(device),
                MetricCategory.Internals,
                ""
            );
            create().gauge(device + "_kB_written",
                () -> reader.getKbWrittenForDevice(device),
                MetricCategory.Internals,
                ""
            );
        }
    }

    private void registerNetworkInterfaceMetrics(ClientSystemMetricChecker metricsChecker) {
        NetDevReader reader = new NetDevReader();
        if (!reader.fileExists())
            return;
        for (String iface : reader.getInterfaces()) {
            create().gauge(
                iface + "_rx_bytes",
                () -> reader.getBytesReceived(iface),
                MetricCategory.Internals,
                ""
            );
            create().gauge(
                iface + "_rx_packets",
                () -> reader.getPacketsReceived(iface),
                MetricCategory.Internals,
                ""
            );
            create().gauge(
                iface + "_tx_bytes",
                () -> reader.getBytesTransmitted(iface),
                MetricCategory.Internals,
                ""
            );
            create().gauge(
                iface + "_tx_packets",
                () -> reader.getPacketsTransmitted(iface),
                MetricCategory.Internals,
                ""
            );
        }
    }

    private void registerLoadAvgMetrics(ClientSystemMetricChecker metricsChecker) {
        LoadAvgReader reader = new LoadAvgReader();
        if (!reader.fileExists())
            return;

        NBFunctionGauge load1m = create().gauge(
            "loadavg_1min",
            reader::getOneMinLoadAverage,
            MetricCategory.Internals,
            "the 1 minute load average of the test client client"
        );
        metricsChecker.addMetricToCheck(load1m, 50.0);

        NBFunctionGauge load5m = create().gauge(
            "loadavg_5min",
            reader::getFiveMinLoadAverage,
            MetricCategory.Internals,
            "the 5 minute load average of the test client client"
        );
        metricsChecker.addMetricToCheck(load5m, 50.0);

        NBFunctionGauge load15m = create().gauge(
            "loadavg_15min",
            reader::getFifteenMinLoadAverage,
            MetricCategory.Internals,
            "the 15 minute load average of the test client"
        );
        metricsChecker.addMetricToCheck(load15m, 50.0);
        // add checking for CPU load averages; TODO: Modify thresholds

    }

    private void registerMemInfoMetrics(ClientSystemMetricChecker metricsChecker) {
        MemInfoReader reader = new MemInfoReader();
        if (!reader.fileExists())
            return;

        NBMetricGauge memTotalGauge = create().gauge(
            "mem_total",
            reader::getMemTotalkB,
            MetricCategory.Internals,
            ""

        );
        NBMetricGauge memUsedGauge = create().gauge(
            "mem_used",
            reader::getMemUsedkB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge memFreeGauge = create().gauge(
            "mem_free",
            reader::getMemFreekB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge memAvailableGauge = create().gauge(
            "mem_available",
            reader::getMemAvailablekB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge memCachedGauge = create().gauge(
            "mem_cache",
            reader::getMemCachedkB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge memBufferedGauge = create().gauge(
            "mem_buffered",
            reader::getMemBufferskB,
            MetricCategory.Internals,
            ""
        );
        // add checking for percent memory used at some given time; TODO: Modify percent threshold
        metricsChecker.addRatioMetricToCheck(
            memUsedGauge,
            memTotalGauge,
            90.0,
            false
        );

        NBMetricGauge swapTotalGauge = create().gauge(
            "swap_total",
            reader::getSwapTotalkB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge swapFreeGauge = create().gauge(
            "swap_free",
            reader::getSwapFreekB,
            MetricCategory.Internals,
            ""
        );
        NBMetricGauge swapUsedGauge = create().gauge(
            "swap_used",
            reader::getSwapUsedkB,
            MetricCategory.Internals,
            ""
        );
    }


}
