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

package io.nosqlbench.engine.core.clientload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class StatReader extends LinuxSystemFileReader {
    /**
     * Note that all fields are cumulative within /proc/stat.
     *
     * Reference:
     * - https://docs.kernel.org/filesystems/proc.html#miscellaneous-kernel-statistics-in-proc-stat
     *
     * Example line:
     * cpu  6955150 945 1205506 139439365 115574 0 113356 0 0 0
     */
    private static final String regex = "cpu\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)";

    public StatReader() {
        super("/proc/stat");
    }

    public Double getUserTime() {
        return extract(regex, 1);
    }

    public Double getSystemTime() {
        return extract(regex, 3);
    }

    public Double getIdleTime() {
        return extract(regex, 4);
    }

    public Double getIoWaitTime() {
        return extract(regex, 5);
    }

    public Double getTotalTime() {
        MatchResult result = findFirstMatch(Pattern.compile(regex));
        if (result == null)
            return null;
        Double user = Double.valueOf(result.group(1));
        Double nice = Double.valueOf(result.group(2));
        Double system = Double.valueOf(result.group(3));
        Double idle = Double.valueOf(result.group(4));
        Double iowait = Double.valueOf(result.group(5));
        Double irq = Double.valueOf(result.group(6));
        Double softirq = Double.valueOf(result.group(7));
        return user + nice + system + idle + iowait + irq + softirq;
    }
}
