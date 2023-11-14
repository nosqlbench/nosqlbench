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

public class NetDevReader extends LinuxSystemFileReader {
    /**
     * Note that all fields are cumulative in /proc/net/dev
     *
     * Reference:
     * - https://www.linuxquestions.org/questions/linux-networking-3/need-explanation-of-proc-net-dev-bytes-counters-4175458860/
     *
     * Example line:
     * wlp59s0: 2941956695 4935327    0    0    0     0          0         0 1213470966 3450551    0    0    0     0       0          0
     */
    public NetDevReader() {
        super("/proc/net/dev");
    }

    public Double getBytesReceived(String interfaceName) {
        return extract(buildRegex(interfaceName), 1);
    }

    public Double getPacketsReceived(String interfaceName) {
        return extract(buildRegex(interfaceName), 2);
    }

    public Double getBytesTransmitted(String interfaceName) {
        return extract(buildRegex(interfaceName), 3);
    }

    public Double getPacketsTransmitted(String interfaceName) {
        return extract(buildRegex(interfaceName), 4);
    }

    private String buildRegex(String interfaceName) {
        return "\\b" + Pattern.quote(interfaceName) + "\\s*:(\\s*\\d+)\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+(\\d+)\\s+(\\d+)";
    }

    public List<String> getInterfaces() {
        String regex = "^\\s*([^\\s:]+):\\s*\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+";
        Pattern pattern = Pattern.compile(regex);
        List<MatchResult> results = findAllLinesMatching(pattern);
        return results.stream().map(m -> m.group(1)).collect(Collectors.toList());
    }
}
