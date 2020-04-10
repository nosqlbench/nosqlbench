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

package io.nosqlbench.nb.api.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Result {

    private final long start;
    private final long end;
    private final long ops;
    private String description;

    public Result(String description, long start, long end, long ops) {
        this.description = description;
        this.start = start;
        this.end = end;
        this.ops = ops;
    }

    public String getDescription() {
        return description;
    }

    public long getTotalOps() {
        return ops;
    }

    public double getTimeSeconds() {
        return (double) (end - start) / 1_000_000_000d;
    }

    public double getNsPerOp() {
        return (double) (end - start) / (double) ops;
    }

    public double getOpsPerSec() {
        return (double) getTotalOps() / getTimeSeconds();
    }

    @Override
    public String toString() {
        long time_ns = end - start;
        return String.format("'%s': %d_ops %f_S %.3f_ops_s, %.0f_ns_op", description, ops, getTimeSeconds(), getOpsPerSec(), getNsPerOp());
    }

    public static List<String> toString(List<Result> results) {
        List<String> ldesc = results.stream().map(Result::getDescription).collect(Collectors.toList());
        List<String> lops = results.stream().map(r -> String.format("%d_ops",r.getTotalOps())).collect(Collectors.toList());
        List<String> ltime_s = results.stream().map(r -> String.format("%f_S",r.getTimeSeconds())).collect(Collectors.toList());
        List<String> lops_s = results.stream().map(r -> String.format("%.3f_ops_s",r.getOpsPerSec())).collect(Collectors.toList());
        List<String> lns_op = results.stream().map(r -> String.format("%.0f_ns_op",r.getNsPerOp())).collect(Collectors.toList());

        int sizeof_ldesc = ldesc.stream().mapToInt(String::length).max().orElse(0);
        int sizeof_lops = lops.stream().mapToInt(String::length).max().orElse(0);
        int sizeof_ltime_s = ltime_s.stream().mapToInt(String::length).max().orElse(0);
        int sizeof_lops_s = lops_s.stream().mapToInt(String::length).max().orElse(0);
        int sizeof_lns_op = lns_op.stream().mapToInt(String::length).max().orElse(0);

        String fmt = "'%" + sizeof_ldesc + "s': %" + sizeof_lops + "s %" + sizeof_ltime_s + "s %" + sizeof_lops_s + "s %" + sizeof_lns_op + "s";
        List<String> rows = new ArrayList<>(results.size());
        for (int i = 0; i < ldesc.size(); i++) {
            String row = String.format(fmt, ldesc.get(i), lops.get(i), ltime_s.get(i), lops_s.get(i), lns_op.get(i));
            rows.add(row);
        }
        return rows;
    }


    public long getStartNanos() {
        return this.start;
    }

    public long getEndNanos() {
        return this.end;
    }
}
