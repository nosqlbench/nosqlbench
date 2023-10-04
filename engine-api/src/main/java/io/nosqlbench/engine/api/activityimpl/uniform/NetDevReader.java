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

package io.nosqlbench.engine.api.activityimpl.uniform;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetDevReader {
    private static final String filePath = "/proc/net/dev";
    private static final Map<String,Map<String,Double>> metricsMap = new HashMap<>();

    public static List<String> getInterfaces() {
        parseFile();
        return new ArrayList<>(metricsMap.keySet());
    }

    public static Double getBytesReceived(String interfaceName) {
        return getValue(interfaceName, "rx_bytes");
    }

    public static Double getPacketsReceived(String interfaceName) {
        return getValue(interfaceName, "rx_packets");
    }

    public static Double getBytesTransmitted(String interfaceName) {
        return getValue(interfaceName, "tx_bytes");
    }

    public static Double getPacketsTransmitted(String interfaceName) {
        return getValue(interfaceName, "tx_packets");
    }

    private static Double getValue(String interfaceName, String metric) {
        parseFile();
        if (metricsMap.get(interfaceName) == null)
            return null;
        return metricsMap.get(interfaceName).get(metric);
    }

    private static void parseFile() {
        /*
        Note that all fields are cumulative in /proc/net/dev

        Reference:
        - https://www.linuxquestions.org/questions/linux-networking-3/need-explanation-of-proc-net-dev-bytes-counters-4175458860/
         */
        metricsMap.clear();
        try {
            FileReader file = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(file);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Inter-|"))
                    continue;
                String[] parts = line.split(":");
                if (parts.length != 2)
                    continue;
                String interfaceName = parts[0].trim();
                String[] stats = parts[1].trim().split("\\s+");
                if (stats.length < 16)
                    continue;
                Map<String,Double> innerMap = new HashMap<>();
                Double receivedBytes = Double.parseDouble(stats[0]);
                Double receivedPackets = Double.parseDouble(stats[1]);
                Double transmittedBytes = Double.parseDouble(stats[8]);
                Double transmittedPackets = Double.parseDouble(stats[9]);
                innerMap.put("rx_bytes", receivedBytes);
                innerMap.put("rx_packets", receivedPackets);
                innerMap.put("tx_bytes", transmittedBytes);
                innerMap.put("tx_packets", transmittedPackets);
                metricsMap.put(interfaceName, innerMap);
            }
        } catch (FileNotFoundException e) {
            return;
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
    }
}
