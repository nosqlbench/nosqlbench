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

public class LoadAvgReader {
    private static final String filePath = "/proc/loadavg";
    private static Map<String,Double> metricsMap = new HashMap<>();

    public static Double getOneMinLoadAverage() {
        return getValue("loadAvg1min");
    }

    public static Double getFiveMinLoadAverage() {
        return getValue("loadAvg5min");
    }

    public static Double getFifteenMinLoadAverage() {
        return getValue("loadAvg15min");
    }

    private static Double getValue(String key) {
        parseFile();
        return metricsMap.get(key);
    }

    private static void parseFile()
    {
        metricsMap.clear();
        try {
            FileReader file = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(file);
            String line = reader.readLine();
            if (line == null)
                return;
            String[] parts = line.split("\\s+");
            Double loadAvgOneMin = Double.parseDouble(parts[0]);
            Double loadAvgFiveMin = Double.parseDouble(parts[1]);
            Double loadAvgFifteenMinute = Double.parseDouble(parts[2]);
            metricsMap.put("loadAvg1min", loadAvgOneMin);
            metricsMap.put("loadAvg5min", loadAvgFiveMin);
            metricsMap.put("loadAvg15min", loadAvgFifteenMinute);

        } catch (FileNotFoundException e) {
            return;
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
    }
}
