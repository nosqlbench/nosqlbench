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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiskStatsReader extends LinuxSystemFileReader {
    /**
     * Note that all fields are cumulative within /proc/diskstats.
     *
     * Reference:
     * - https://serverfault.com/questions/619097/interpreting-proc-diskstats-for-a-webserver-more-writes-than-reads
     *
     * Example line:
     *  259       0 nvme0n1 669494 21 65326120 388760 3204963 2891102 734524354 42209620 0 446420 41361212
     */
    private static final Double sectorSizeBytes = 512.0;

    public DiskStatsReader() {
        super("/proc/diskstats");
    }

    public Double getTransactionsForDevice(String deviceName) {
        MatchResult result = findFirstMatch(Pattern.compile(buildRegex(deviceName)));
        if (result == null)
            return null;
        Double readsCompleted = Double.valueOf(result.group(1));
        Double writesCompleted = Double.valueOf(result.group(5));
        return readsCompleted + writesCompleted;
    }

    public Double getKbReadForDevice(String deviceName) {
        MatchResult result = findFirstMatch(Pattern.compile(buildRegex(deviceName)));
        if (result == null)
            return null;
        Double sectorsRead = Double.valueOf(result.group(3));
        return sectorsRead * sectorSizeBytes / 1024;
    }

    public Double getKbWrittenForDevice(String deviceName) {
        MatchResult result = findFirstMatch(Pattern.compile(buildRegex(deviceName)));
        if (result == null)
            return null;
        Double sectorsWritten = Double.valueOf(result.group(7));
        return sectorsWritten * sectorSizeBytes / 1024;
    }

    private String buildRegex(String deviceName) {
        return "\\b" + Pattern.quote(deviceName) + "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)";
    }

    public List<String> getDevices() {
        String regex = "^\\s*\\d+\\s+\\d+\\s+([a-zA-Z0-9]+)\\s+.*$";
        Pattern pattern = Pattern.compile(regex);
        List<MatchResult> results = findAllLinesMatching(pattern);
        return results.stream().map(m -> m.group(1)).collect(Collectors.toList());
    }
}
