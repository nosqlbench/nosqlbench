/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.core.script;

import io.nosqlbench.engine.core.ScenarioLogger;
import io.nosqlbench.engine.core.ScenarioResult;
import io.nosqlbench.engine.core.ScenariosResults;
import org.apache.commons.compress.utils.IOUtils;
import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptIntegrationTests {

    public static ScenarioResult runScenario(String scriptname, String... params) {
        if ((params.length % 2) != 0) {
            throw new RuntimeException("params must be pairwise key, value, ...");
        }
        Map<String, String> paramsMap = new HashMap<>();

        for (int i = 0; i < params.length; i += 2) {
            paramsMap.put(params[i], params[i + 1]);
        }
        String scenarioName = "scenario " + scriptname;
        System.out.println("=".repeat(29) + " Running SYNC integration test for: " + scenarioName);
        ScenariosExecutor e = new ScenariosExecutor(ScriptIntegrationTests.class.getSimpleName() + ":" + scriptname, 1);
        Scenario s = new Scenario(scenarioName, Scenario.Engine.Graalvm);
        s.addScenarioScriptParams(paramsMap);
        ClassLoader cl = AsyncScriptIntegrationTests.class.getClassLoader();
        String script;
        try {
            InputStream sstream = cl.getResourceAsStream("scripts/sync/" + scriptname + ".js");
            byte[] bytes = IOUtils.toByteArray(sstream);
            script = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        s.addScriptText(script);
//        s.addScriptText("load('classpath:scripts/sync/" + scriptname + ".js');");
        ScenarioLogger scenarioLogger = new ScenarioLogger(s).setMaxLogs(0).setLogDir("logs/test").start();
        e.execute(s, scenarioLogger);
        ScenariosResults scenariosResults = e.awaitAllResults();
        ScenarioResult scenarioResult = scenariosResults.getOne();
        scenarioResult.reportToLog();
        return scenarioResult;
    }


    @BeforeClass
    public static void logit() {
        System.out.println("Running SYNC version of Script Integration Tests.");
    }


    @Test
    public void testCycleRate() {

        ScenarioResult scenarioResult = runScenario("sync_cycle_rate");
        String iolog = scenarioResult.getIOLog();
        System.out.println("iolog\n" + iolog);
        Pattern p = Pattern.compile(".*mean cycle rate = (\\d[.\\d]+).*", Pattern.DOTALL);
        Matcher m = p.matcher(iolog);
        assertThat(m.matches()).isTrue();

        String digits = m.group(1);
        assertThat(digits).isNotEmpty();
        double rate = Double.valueOf(digits);
        assertThat(rate).isCloseTo(1000, Offset.offset(100.0));
    }

    // The tests below are being disabled and eventually removed.
    // The async versions are higher sensitivity and equivalent in
    // every other way.
    // This should reduce the build time for integrated testing.

//    @Test
//    public void testStrideRateOnly() {
//        ScenarioResult scenarioResult = runScenario("stride_rate");
//        String iolog = scenarioResult.getIOLog();
//        System.out.println("iolog\n" + iolog);
//        Pattern p = Pattern.compile(".*stride_rate.strides.servicetime.meanRate = (\\d[.\\d]+).*", Pattern.DOTALL);
//        Matcher m = p.matcher(iolog);
//        assertThat(m.matches()).isTrue();
//
//        String digits = m.group(1);
//        assertThat(digits).isNotEmpty();
//        double rate = Double.valueOf(digits);
//        assertThat(rate).isCloseTo(10000.0D, Offset.offset(1000D));
//    }
//
//    @Test
//    public void testPhaseRateOnly() {
//        ScenarioResult scenarioResult = runScenario("phase_rate");
//        String iolog = scenarioResult.getIOLog();
//        System.out.println("iolog\n" + iolog);
//        Pattern p = Pattern.compile(".*phase_rate.phases.servicetime.meanRate = (\\d[.\\d]+).*", Pattern.DOTALL);
//        Matcher m = p.matcher(iolog);
//        assertThat(m.matches()).isTrue();
//
//        String digits = m.group(1);
//        assertThat(digits).isNotEmpty();
//        double rate = Double.valueOf(digits);
//        assertThat(rate).isCloseTo(25000.0D, Offset.offset(5000D));
//    }
//
//
//    @Test
//    public void testExtensionPoint() {
//        ScenarioResult scenarioResult = runScenario("extensions");
//        assertThat(scenarioResult.getIOLog()).contains("sum is 46");
//    }
//
//    @Test
//    public void testLinkedInput() {
//        ScenarioResult scenarioResult = runScenario("linkedinput");
//        Pattern p = Pattern.compile(".*started leader.*started follower.*stopped leader.*stopped follower.*",
//                Pattern.DOTALL);
//        assertThat(p.matcher(scenarioResult.getIOLog()).matches()).isTrue();
//    }
//
//    @Test
//    public void testExtensionCsvLogger() {
//        ScenarioResult scenarioResult = runScenario("extension_csvmetrics");
//        assertThat(scenarioResult.getIOLog()).contains("started new " +
//                "csvlogger: logs/csvmetricstestdir");
//    }
//
//
//    @Test
//    public void testScriptParamsVariable() {
//        ScenarioResult scenarioResult = runScenario("params_variable", "one", "two", "three", "four");
//        assertThat(scenarioResult.getIOLog()).contains("params.get(\"one\")='two'");
//        assertThat(scenarioResult.getIOLog()).contains("params.get(\"three\")='four'");
//        assertThat(scenarioResult.getIOLog()).contains("params.size()=2");
//        assertThat(scenarioResult.getIOLog()).contains("params.get(\"three\") [overridden-three-five]='five'");
//        assertThat(scenarioResult.getIOLog()).contains("params.get(\"four\") [defaulted-four-niner]='niner'");
//    }
//
//    @Test
//    public void testExtensionHistoStatsLogger() throws IOException {
//        ScenarioResult scenarioResult = runScenario("extension_histostatslogger");
//        assertThat(scenarioResult.getIOLog()).contains("stdout started " +
//                "logging to logs/histostats.csv");
//        List<String> strings = Files.readAllLines(Paths.get(
//                "logs/histostats.csv"));
//        String logdata = strings.stream().collect(Collectors.joining("\n"));
//        assertThat(logdata).contains("min,p25,p50,p75,p90,p95,");
//        assertThat(logdata.split("Tag=testhistostatslogger.cycles.servicetime,").length).isGreaterThanOrEqualTo(3);
//    }
//
//    @Test
//    public void testExtensionHistogramLogger() throws IOException {
//        ScenarioResult scenarioResult = runScenario("extension_histologger");
//        assertThat(scenarioResult.getIOLog()).contains("stdout started logging to hdrhistodata.log");
//        List<String> strings = Files.readAllLines(Paths.get("hdrhistodata.log"));
//        String logdata = strings.stream().collect(Collectors.joining("\n"));
//        assertThat(logdata).contains(",HIST");
//        assertThat(logdata.split("Tag=testhistologger.cycles.servicetime,").length).isGreaterThanOrEqualTo(3);
//    }
//
//    @Test
//    public void testBlockingRun() {
//        ScenarioResult scenarioResult = runScenario("blockingrun");
//        int a1end = scenarioResult.getIOLog().indexOf("blockingactivity1 finished");
//        int a2start = scenarioResult.getIOLog().indexOf("running blockingactivity2");
//        assertThat(a1end).isLessThan(a2start);
//    }
//
//    @Test
//    public void testAwaitFinished() {
//        ScenarioResult scenarioResult = runScenario("awaitfinished");
//        scenarioResult.reportToLog();
//    }
//
//    @Test
//    public void testStartStop() {
//        ScenarioResult scenarioResult = runScenario("startstopdiag");
//        scenarioResult.reportToLog();
//        int startedAt = scenarioResult.getIOLog().indexOf("starting activity teststartstopdiag");
//        int stoppedAt = scenarioResult.getIOLog().indexOf("stopped activity teststartstopdiag");
//        assertThat(startedAt).isGreaterThan(0);
//        assertThat(stoppedAt).isGreaterThan(startedAt);
//    }
//
//    @Test
//    public void testThreadChange() {
//        ScenarioResult scenarioResult = runScenario("threadchange");
//        int changedTo1At = scenarioResult.getIOLog().indexOf("threads now 1");
//        int changedTo5At = scenarioResult.getIOLog().indexOf("threads now 5");
//        assertThat(changedTo1At).isGreaterThan(0);
//        assertThat(changedTo5At).isGreaterThan(changedTo1At);
//    }
//
//    @Test
//    public void testReadMetric() {
//        ScenarioResult scenarioResult = runScenario("readmetrics");
//        assertThat(scenarioResult.getIOLog()).contains("count: ");
//    }
//
//    @Test
//    public void testExceptionPropagationFromMotorThread() {
//        ScenarioResult scenarioResult = runScenario("activityerror");
//        assertThat(scenarioResult.getException()).isPresent();
//        assertThat(scenarioResult.getException().get().getMessage()).contains("For input string: \"unparsable\"");
//    }
//
//    @Test
//    public void testExceptionPropagationFromActivityInit() {
//        ScenarioResult scenarioResult = runScenario("activityiniterror");
//        assertThat(scenarioResult.getException()).isPresent();
//        assertThat(scenarioResult.getException().get().getMessage()).contains("For input string: \"unparsable\"");
//        assertThat(scenarioResult.getException()).isNotNull();
//    }
//
//    @Test
//    public void testReportedCoDelayBursty() {
//        ScenarioResult scenarioResult = runScenario("cocycledelay_bursty");
//        assertThat(scenarioResult.getIOLog()).contains("step1 metrics.waittime=");
//        assertThat(scenarioResult.getIOLog()).contains("step2 metrics.waittime=");
//        String iolog = scenarioResult.getIOLog();
//        System.out.println(iolog);
//        assertThat(iolog).contains("waittime trended back down as expected");
//    }
//
//    @Test
//    public void testReportedCoDelayStrict() {
//        ScenarioResult scenarioResult = runScenario("cocycledelay_strict");
//        assertThat(scenarioResult.getIOLog()).contains("step1 cycles.waittime=");
//        assertThat(scenarioResult.getIOLog()).contains("step2 cycles.waittime=");
//        String iolog = scenarioResult.getIOLog();
//        System.out.println(iolog);
//        // TODO: ensure that waittime is staying the same or increasing
//        // after investigating minor decreasing effect
//    }
//
//
//    @Test
//    public void testCycleRateChange() {
//        ScenarioResult scenarioResult = runScenario("cycle_rate_change");
//        String ioLog = scenarioResult.getIOLog();
//        assertThat(ioLog).contains("cycles adjusted, exiting on iteration");
//    }
//
//    @Test
//    public void testExitLogic() {
//        ScenarioResult scenarioResult = runScenario(
//                "basicdiag",
//                "type", "diag", "cyclerate", "5", "erroroncycle", "10", "cycles", "2000"
//        );
//    }

}
