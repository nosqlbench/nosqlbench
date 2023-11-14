/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.sysperf;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SysPerfBaseliner {

    private final static Logger logger = LogManager.getLogger(SysPerfBaseliner.class);

    public static void main(String[] args) {
        try {
            SysPerfBaseliner sysPerfBaseliner = new SysPerfBaseliner();
            Collection<RunResult> jmhResults = sysPerfBaseliner.runBenchmarks();
//            logger.info("Results of JMH benchmarks:\n" + result.toString());
//            result.forEach(System.out::println);
            logger.info(() -> "SysPerfData (selected details for EB):\n" + new SysPerfData(jmhResults, SysPerfData.currentVersion));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<RunResult> runBenchmarks() {
//            File jmhOut = File.createTempFile("jmh", "out");

        // These are broken out simply to provide more friendly feedback to users.
        Map<String, Class<?>> namedTests = new LinkedHashMap<>();
        namedTests.put("nanotime", SysBenchMethodNanoTime.class);
        namedTests.put("parknanos", SysBenchMethodParkNanos.class);
        namedTests.put("sleep", SysBenchMethodThreadSleep.class);

        Collection<RunResult> results = new ArrayList<>();
        namedTests.forEach((n, c) -> {
            try {
                String logfile = Files.createTempFile("jmh_" + n, ".log").toString();
                Options options = new OptionsBuilder().forks(1).include(c.getSimpleName()).output(logfile).build();
                logger.info(() -> "running microbench for " + n + ", for about 20 seconds; details in " + logfile);
                RunResult runResult = new Runner(options).runSingle();
                results.add(runResult);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return results;
    }

    public SysPerfData getSysPerfData() {
        logger.info("Running system calibration tests for about a minute. This is used to calibrate delay loops, and is done only once.");
        Collection<RunResult> runResult = runBenchmarks();
        SysPerfData sysPerfData = new SysPerfData(runResult, SysPerfData.currentVersion);
        logger.info(() -> "System timing test complete: " + sysPerfData);
        return sysPerfData;
    }

}
