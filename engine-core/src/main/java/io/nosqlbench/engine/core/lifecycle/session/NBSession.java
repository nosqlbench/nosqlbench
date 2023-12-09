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

import io.nosqlbench.nb.api.components.core.NBComponentProps;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.decorators.NBTokenWords;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.clientload.*;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A session represents a single execution of NoSQLBench, whether episodic or persistent under some service layer.
 * An NBSession takes care of system level concerns like logging, annotations, error reporting, metrics flows, and so
 * on.
 * All NBScenarios are run within an NBSession.
 */
public class NBSession extends NBBaseComponent implements Function<List<Cmd>, ExecutionResult>, NBTokenWords {
    private final static Logger logger = LogManager.getLogger(NBSession.class);
    private final String sessionName;
    private final ClientSystemMetricChecker clientMetricChecker;

    private final Map<String, NBBufferedContainer> containers = new ConcurrentHashMap<>();

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    private NBBufferedContainer getContext(String name) {
        return containers.computeIfAbsent(
            name,
            n -> NBContainer.builder().name(n).build(this)
        );
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName
    ) {
        super(
            null,
            labelContext.getLabels()
                .and("session", sessionName)
        );
        this.sessionName = sessionName;

        this.clientMetricChecker = new ClientSystemMetricChecker(this, NBLabels.forKV(), 10);
        registerLoadAvgMetrics();
        registerMemInfoMetrics();
//        registerDiskStatsMetrics();
        registerNetworkInterfaceMetrics();
        registerCpuStatMetrics();
        clientMetricChecker.start();
        bufferOrphanedMetrics = true;
    }


    /**
     * Notes on scenario names:
     * <UL>
     * <LI>If none are provided, then all cmds are implicitly allocated to the "default" scenario.</LI>
     * <LI>If the name "default" is provided directly, then this is considered an error.</LI>
     * <LI>Otherwise, the most recently set scenario name is the one in which all following commands are run.</LI>
     * <LI></LI>
     * </UL>
     *
     * @param cmds
     *     the function argument
     * @return
     */
    public ExecutionResult apply(List<Cmd> cmds) {

        // TODO: add container closing command
        // TODO: inject container closing commands after the last command referencing each container
        List<NBCommandAssembly.CommandInvocation> invocationCalls = NBCommandAssembly.assemble(cmds, this::getContext);
        ResultCollector collector = new ResultCollector();
        try (ResultContext results = new ResultContext(collector).ok()) {
            for (NBCommandAssembly.CommandInvocation invocation : invocationCalls) {
                try {
                    String targetContext = invocation.containerName();
                    NBBufferedContainer container = getContext(targetContext);
                    NBCommandResult cmdResult = container.apply(invocation.command(), invocation.params());
                    results.apply(cmdResult);
                } catch (Exception e) {
                    String msg = "While running command '" + invocation.command() + "' in container '" + invocation.containerName() + "', an error occurred: " + e.toString();
                    logger.error(msg);
                    results.error(e);
                    break;
                }
            }
        }

        for (String containerName : containers.keySet()) {
            NBBufferedContainer ctx = containers.get(containerName);
            logger.debug("awaiting end of activities in container '" + containerName + "':" +
                ctx.controller().getActivityDefs().stream().map(ActivityDef::getAlias).toList());
            ctx.controller().shutdown();
            ctx.controller().awaitCompletion(Long.MAX_VALUE);
            logger.debug("completed");
        }
        metricsBuffer.printMetricSummary(this);
        return collector.toExecutionResult();
    }


    private void registerLoadAvgMetrics() {
        LoadAvgReader reader = new LoadAvgReader();
        if (!reader.fileExists())
            return;

        NBFunctionGauge load1m = create().gauge("loadavg_1min", reader::getOneMinLoadAverage);
        clientMetricChecker.addMetricToCheck(load1m, 50.0);

        NBFunctionGauge load5m = create().gauge("loadavg_5min", reader::getFiveMinLoadAverage);
        clientMetricChecker.addMetricToCheck(load5m, 50.0);

        NBFunctionGauge load15m = create().gauge("loadavg_15min", reader::getFifteenMinLoadAverage);
        clientMetricChecker.addMetricToCheck(load15m, 50.0);
        // add checking for CPU load averages; TODO: Modify thresholds

    }

    private void registerMemInfoMetrics() {
        MemInfoReader reader = new MemInfoReader();
        if (!reader.fileExists())
            return;

        NBMetricGauge memTotalGauge = create().gauge("mem_total", reader::getMemTotalkB);
        NBMetricGauge memUsedGauge = create().gauge("mem_used", reader::getMemUsedkB);
        NBMetricGauge memFreeGauge = create().gauge("mem_free", reader::getMemFreekB);
        NBMetricGauge memAvailableGauge = create().gauge("mem_avaialble", reader::getMemAvailablekB);
        NBMetricGauge memCachedGauge = create().gauge("mem_cache", reader::getMemCachedkB);
        NBMetricGauge memBufferedGauge = create().gauge("mem_buffered", reader::getMemBufferskB);
        // add checking for percent memory used at some given time; TODO: Modify percent threshold
        clientMetricChecker.addRatioMetricToCheck(memUsedGauge, memTotalGauge, 90.0, false);

        NBMetricGauge swapTotalGauge = create().gauge("swap_total", reader::getSwapTotalkB);
        NBMetricGauge swapFreeGauge = create().gauge("swap_free", reader::getSwapFreekB);
        NBMetricGauge swapUsedGauge = create().gauge("swap_used", reader::getSwapUsedkB);
    }

    private void registerDiskStatsMetrics() {
        DiskStatsReader reader = new DiskStatsReader();
        if (!reader.fileExists())
            return;

        for (String device : reader.getDevices()) {
            create().gauge(device + "_transactions", () -> reader.getTransactionsForDevice(device));
            create().gauge(device + "_kB_read", () -> reader.getKbReadForDevice(device));
            create().gauge(device + "_kB_written", () -> reader.getKbWrittenForDevice(device));
        }
    }

    private void registerNetworkInterfaceMetrics() {
        NetDevReader reader = new NetDevReader();
        if (!reader.fileExists())
            return;
        for (String iface : reader.getInterfaces()) {
            create().gauge(iface + "_rx_bytes", () -> reader.getBytesReceived(iface));
            create().gauge(iface + "_rx_packets", () -> reader.getPacketsReceived(iface));
            create().gauge(iface + "_tx_bytes", () -> reader.getBytesTransmitted(iface));
            create().gauge(iface + "_tx_packets", () -> reader.getPacketsTransmitted(iface));
        }
    }

    private void registerCpuStatMetrics() {
        StatReader reader = new StatReader();
        if (!reader.fileExists())
            return;
        NBMetricGauge cpuUserGauge = create().gauge("cpu_user", reader::getUserTime);
        NBMetricGauge cpuSystemGauge = create().gauge("cpu_system", reader::getSystemTime);
        NBMetricGauge cpuIdleGauge = create().gauge("cpu_idle", reader::getIdleTime);
        NBMetricGauge cpuIoWaitGauge = create().gauge("cpu_iowait", reader::getIoWaitTime);
        NBMetricGauge cpuTotalGauge = create().gauge("cpu_total", reader::getTotalTime);
        // add checking for percent of time spent in user space; TODO: Modify percent threshold
        clientMetricChecker.addRatioMetricToCheck(cpuUserGauge, cpuTotalGauge, 50.0, true);
    }

}

