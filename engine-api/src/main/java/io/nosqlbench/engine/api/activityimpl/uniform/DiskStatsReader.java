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

public class DiskStatsReader {
    private static final String filePath = "/proc/diskstats";
    private static final Map<String,Map<String,Double>> metricsMap = new HashMap<>();

    public static List<String> getDevices() {
        parseFile();
        return new ArrayList<>(metricsMap.keySet());
    }

    public static Double getTransactionsForDevice(String device) {
        return getValue(device, "transactions");
    }

    public static Double getKbReadForDevice(String device) {
        return getValue(device, "kB_read");
    }

    public static Double getKbWrittenForDevice(String device) {
        return getValue(device, "kB_written");
    }

    private static Double getValue(String device, String metric) {
        parseFile();
        if (metricsMap.get(device) == null)
            return null;
        return metricsMap.get(device).get(metric);
    }

    private static void parseFile() {
        /*
        Note that all fields are cumulative within /proc/diskstats.

        Reference:
        - https://serverfault.com/questions/619097/interpreting-proc-diskstats-for-a-webserver-more-writes-than-reads
         */
        metricsMap.clear();
        try {
            FileReader file = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(file);
            int sectorSizeBytes = 512;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 14)
                    continue;
                Map<String,Double> innerMap = new HashMap<>();
                String device = parts[2];
                Double readsCompleted = Double.parseDouble(parts[3]);
                Double sectorsRead = Double.parseDouble(parts[5]);
                Double writesCompleted = Double.parseDouble(parts[7]);
                Double sectorsWritten = Double.parseDouble(parts[9]);
                Double transactions = readsCompleted + writesCompleted;
                Double kbRead = (sectorsRead * sectorSizeBytes) / 1024;
                Double kbWritten = (sectorsWritten * sectorSizeBytes) / 1024;
                innerMap.put("transactions", transactions);
                innerMap.put("kB_read", kbRead);
                innerMap.put("kB_written", kbWritten);
                metricsMap.put(device, innerMap);
            }
        } catch (FileNotFoundException e) {
            return;
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
    }
}
