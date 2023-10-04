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
import java.util.HashMap;
import java.util.Map;

public class StatReader {
    private static final String filePath = "/proc/stat";
    private static final Map<String,Double> metricsMap = new HashMap<>();

    public static Double getUserTime() {
        return getValue("user");
    }

    public static Double getSystemTime() {
        return getValue("system");
    }

    public static Double getIdleTime() {
        return getValue("idle");
    }

    public static Double getIoWaitTime() {
        return getValue("iowait");
    }

    public static Double getTotalTime() {
        return getValue("total");
    }

    private static Double getValue(String key) {
        parseFile();
        return metricsMap.get(key);
    }

    private static void parseFile() {
        /*
        Note that all fields are cumulative within /proc/stat.

        Reference:
        - https://docs.kernel.org/filesystems/proc.html#miscellaneous-kernel-statistics-in-proc-stat
         */
        metricsMap.clear();
        try {
            FileReader file = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(file);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("cpu ")) {
                    String[] parts = line.split("\\s+");
                    Double user = Double.parseDouble(parts[1]);
                    Double nice = Double.parseDouble(parts[2]);
                    Double system = Double.parseDouble(parts[3]);
                    Double idle = Double.parseDouble(parts[4]);
                    Double iowait = Double.parseDouble(parts[5]);
                    Double irq = Double.parseDouble(parts[6]);
                    Double softirq = Double.parseDouble(parts[7]);
                    Double steal = Double.parseDouble(parts[8]);

                    Double total = user + nice + system + idle + iowait + irq + softirq + steal;
                    metricsMap.put("user", user);
                    metricsMap.put("system", system);
                    metricsMap.put("idle", idle);
                    metricsMap.put("iowait", iowait);
                    metricsMap.put("total", total);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            return;
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
    }
}
