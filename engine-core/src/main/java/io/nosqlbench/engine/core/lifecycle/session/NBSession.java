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

import io.nosqlbench.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponentSubScope;
import io.nosqlbench.components.decorators.NBTokenWords;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.clientload.*;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
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

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName
    ) {
        super(null, labelContext.getLabels().and("session", sessionName));
        this.sessionName = sessionName;

        this.clientMetricChecker = new ClientSystemMetricChecker(this, NBLabels.forKV(),10);
        registerLoadAvgMetrics();
        registerMemInfoMetrics();
//        registerDiskStatsMetrics();
        registerNetworkInterfaceMetrics();
        registerCpuStatMetrics();
        clientMetricChecker.start();

    }

    public ExecutionResult apply(List<Cmd> cmds) {

        if (cmds.isEmpty()) {
            logger.info("No commands provided.");
        }

        Map<String, String> params = new CmdParamsBuffer(cmds).getGlobalParams();

        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector)) {
            final ScenariosExecutor scenariosExecutor = new ScenariosExecutor(this, "executor-" + sessionName, 1);

            NBScenario scenario;
            if (cmds.get(0).getCmdType().equals(Cmd.CmdType.java)) {
                scenario = buildJavaScenario(cmds);
            } else {
                scenario = buildJavacriptScenario(cmds);
            }
            try (NBComponentSubScope scope = new NBComponentSubScope(scenario)) {
                scenariosExecutor.execute(scenario, params);
                //             this.doReportSummaries(this.reportSummaryTo, this.result);
            }
            final ScenariosResults scenariosResults = scenariosExecutor.awaitAllResults();
            logger.debug(() -> "Total of " + scenariosResults.getSize() + " result object returned from ScenariosExecutor");

            //            logger.info(scenariosResults.getExecutionSummary());

            if (scenariosResults.hasError()) {
                results.error(scenariosResults.getAnyError().orElseThrow());
                final Exception exception = scenariosResults.getOne().getException();
                logger.warn(scenariosResults.getExecutionSummary());
                NBCLIErrorHandler.handle(exception, true);
                System.err.println(exception.getMessage()); // TODO: make this consistent with ConsoleLogging sequencing
            }

            results.output(scenariosResults.getExecutionSummary());
            results.ok();

        }
        return collector.toExecutionResult();
    }


    private NBScenario buildJavacriptScenario(List<Cmd> cmds) {
//        boolean dryrun;
//        NBScriptedScenario.Invocation invocation = dryrun ?
//            NBScriptedScenario.Invocation.RENDER_SCRIPT :
//            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final ScenarioParams scenarioParams = new ScenarioParams();
        scenarioParams.putAll(buffer.getCombinedParams());

        final NBScriptedScenario scenario = new NBScriptedScenario(sessionName, this);

        scenario.addScriptText(scriptData);
        return scenario;
    }

    private NBScenario buildJavaScenario(List<Cmd> cmds) {
        if (cmds.size() != 1) {
            throw new RuntimeException("java scenarios require exactly 1 java command");
        }
        Cmd javacmd = cmds.get(0);
        String mainClass = javacmd.getArg("main_class");

//        This doesn't work as expected; The newest service loader docs are vague about Provider and no-args ctor requirements
//        and the code suggests that you still have to have one unless you are in a named module
//        SimpleServiceLoader<NBScenario> loader = new SimpleServiceLoader<>(NBScenario.class, Maturity.Any);
//        List<SimpleServiceLoader.Component<? extends NBScenario>> namedProviders = loader.getNamedProviders(mainClass);
//        SimpleServiceLoader.Component<? extends NBScenario> provider = namedProviders.get(0);
//        Class<? extends NBScenario> type = provider.provider.type();

        Class<NBScenario> type;
        try {
            type = (Class<NBScenario>) Class.forName(mainClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Constructor<? extends NBScenario> constructor = type.getConstructor(NBComponent.class, String.class);
            NBScenario scenario = constructor.newInstance(this, sessionName);
            return scenario;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


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

        NBMetricGauge memTotalGauge = create().gauge("mem_total",reader::getMemTotalkB);
        NBMetricGauge memUsedGauge = create().gauge("mem_used",reader::getMemUsedkB);
        NBMetricGauge memFreeGauge = create().gauge("mem_free",reader::getMemFreekB);
        NBMetricGauge memAvailableGauge = create().gauge("mem_avaialble",reader::getMemAvailablekB);
        NBMetricGauge memCachedGauge = create().gauge("mem_cache",reader::getMemCachedkB);
        NBMetricGauge memBufferedGauge = create().gauge("mem_buffered", reader::getMemBufferskB);
        // add checking for percent memory used at some given time; TODO: Modify percent threshold
        clientMetricChecker.addRatioMetricToCheck(memUsedGauge, memTotalGauge, 90.0, false);

        NBMetricGauge swapTotalGauge = create().gauge("swap_total", reader::getSwapTotalkB);
        NBMetricGauge swapFreeGauge = create().gauge("swap_free",reader::getSwapFreekB);
        NBMetricGauge swapUsedGauge = create().gauge("swap_used",reader::getSwapUsedkB);
    }

    private void registerDiskStatsMetrics() {
        DiskStatsReader reader = new DiskStatsReader();
        if (!reader.fileExists())
            return;

        for (String device : reader.getDevices()) {
            create().gauge(device +"_transactions", () ->reader.getTransactionsForDevice(device));
            create().gauge(device +"_kB_read", () -> reader.getKbReadForDevice(device));
            create().gauge(device+"_kB_written", () -> reader.getKbWrittenForDevice(device));
        }
    }

    private void registerNetworkInterfaceMetrics() {
        NetDevReader reader = new NetDevReader();
        if (!reader.fileExists())
            return;
        for (String iface : reader.getInterfaces()) {
            create().gauge(iface+"_rx_bytes",() -> reader.getBytesReceived(iface));
            create().gauge(iface+"_rx_packets",() -> reader.getPacketsReceived(iface));
            create().gauge(iface+"_tx_bytes",() -> reader.getBytesTransmitted(iface));
            create().gauge(iface+"_tx_packets",() -> reader.getPacketsTransmitted(iface));
        }
    }

    private void registerCpuStatMetrics() {
        StatReader reader = new StatReader();
        if (!reader.fileExists())
            return;
        NBMetricGauge cpuUserGauge = create().gauge("cpu_user", reader::getUserTime);
        NBMetricGauge cpuSystemGauge = create().gauge("cpu_system",reader::getSystemTime);
        NBMetricGauge cpuIdleGauge = create().gauge("cpu_idle", reader::getIdleTime);
        NBMetricGauge cpuIoWaitGauge = create().gauge("cpu_iowait", reader::getIoWaitTime);
        NBMetricGauge cpuTotalGauge = create().gauge("cpu_total", reader::getTotalTime);
        // add checking for percent of time spent in user space; TODO: Modify percent threshold
        clientMetricChecker.addRatioMetricToCheck(cpuUserGauge, cpuTotalGauge, 50.0, true);
    }

}

