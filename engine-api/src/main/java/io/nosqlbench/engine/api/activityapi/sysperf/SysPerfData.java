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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SysPerfData {

    public final static long currentVersion=1L;

    private final static String METHOD_THREAD_SLEEP = "callThreadSleep";
//    private final static String METHOD_EMPTY_METHOD = "callEmptyMethod";
    private final static String METHOD_SYSTEM_NANOTIME = "callSystemNanoTime";
    private static final String METHOD_LOCKSUPPORT_PARKNANOS = "callLockSupportParkNanos";

    private Map<String, Double> values = new HashMap<>();
    //    private double avgNanos_Method_Call;
//    private double avgNanos_Thread_Sleep;
//    private double avgNanos_System_NanoTime;
    private long version;

    public SysPerfData() {
    }

    public SysPerfData(Collection<RunResult> results, long version) {
        this.version = version;

        System.out.println(results);
        values = results.stream().filter(r -> r.getPrimaryResult().getScoreUnit().equals("ns/op"))
                .collect(Collectors.toMap(r -> r.getPrimaryResult().getLabel(), k -> k.getPrimaryResult().getScore()));

    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("sleep=%.3fns parknanos=%.3fns nanotime=%.3fns",
                this.getAvgNanos_Thread_Sleep(),
                this.getAvgNanos_LockSupport_ParkNanos(),
                this.getAvgNanos_System_NanoTime());
    }

    public double getAvgNanos_LockSupport_ParkNanos() {
        return values.get(METHOD_LOCKSUPPORT_PARKNANOS);
    }

    public double getAvgNanos_System_NanoTime() {
        return values.get(METHOD_SYSTEM_NANOTIME);
    }

    public double getAvgNanos_Thread_Sleep() {
        return values.get(METHOD_THREAD_SLEEP);
    }
}
