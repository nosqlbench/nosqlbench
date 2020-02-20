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

package io.nosqlbench.activityimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CpuInfo {
    private final static Logger logger = LoggerFactory.getLogger(CpuInfo.class);

    public static Optional<ProcDetails> getProcDetails() {
        List<Map<String, String>> cpuinfo = new ArrayList<>();
        try {
            String data = Files.readString(Path.of("/proc/cpuinfo"), StandardCharsets.UTF_8);
            String[] sections = data.split("\n\n");
            for (String section : sections) {
                Map<String, String> cpuMap = new HashMap<>();
                cpuinfo.add(cpuMap);

                String[] props = section.split("\n");
                for (String prop : props) {
                    String[] assignment = prop.split("\\s*:\\s*");
                    if (assignment.length == 2) {
                        String property = assignment[0].trim();
                        String value = assignment[1].trim();
                        cpuMap.put(property, value);
                    } else {
                        cpuMap.put(assignment[0], "");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to learn about CPU architecture: " + e.getMessage());
            return Optional.empty();
        }
        return Optional.of(new ProcDetails(cpuinfo));
    }


    public static class ProcDetails {
        private final List<Map<String, String>> details;

        public ProcDetails(List<Map<String, String>> details) {
            this.details = details;
        }

        public int getCoreCount() {
            return (int) details.stream()
                    .map(m -> m.get("core id"))
                    .distinct()
                    .count();
        }

        public int getCpuCount() {
            return (int) details.stream()
                    .map(m -> m.get("processor"))
                    .distinct()
                    .count();
        }

        public String getModelName() {
            return details.stream()
                    .map(m -> m.get("model name")).findFirst().orElseThrow();
        }

        public String getMhz() {
            return details.stream()
                    .map(m -> m.get("cpu MHz")).findFirst().orElseThrow();
        }

        public String getCacheInfo() {
            return details.stream()
                    .map(m -> m.get("cache size")).findFirst().orElseThrow();
        }

        public String toString() {
            return "cores=" + getCoreCount() +
                    " cpus=" + getCpuCount() + " mhz=" + getMhz() +
                    " speedavg=" + getCurrentSpeed().getAverage() +
                    " cache=" + getCacheInfo() + " model='" + getModelName() + "'";

        }


        public double getMaxFreq(int cpu) {
            return readFile("/sys/devices/system/cpu/cpu" + cpu + "/cpufreq/cpuinfo_max_freq",Double.NaN);
        }
        public double getCurFreq(int cpu) {
            return readFile("/sys/devices/system/cpu/cpu" + cpu + "/cpufreq/cpuinfo_max_freq",Double.NaN);
        }
        public double getMinFreq(int cpu) {
            return readFile("/sys/devices/system/cpu/cpu" + cpu + "/cpufreq/cpuinfo_min_freq",Double.NaN);
        }

        public double getCurrentSpeed(int cpu) {
            double curFreq = getCurFreq(cpu);
            double maxFreq = getMaxFreq(cpu);
            if (Double.isNaN(curFreq) || Double.isNaN(maxFreq)) {
                return Double.NaN;
            }
            return curFreq / maxFreq;
        }

        public DoubleSummaryStatistics getCurrentSpeed() {
            DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
            for (int i = 0; i < getCpuCount(); i++) {
                double currentSpeed = getCurrentSpeed(i);
                if (!Double.isNaN(currentSpeed)) {
                    dss.accept(currentSpeed);
                }
            }
            return dss;
        }

        private double readFile(String path, double defaultValue) {
            try {
                Path readPath = Path.of(path);
                String content = Files.readString(readPath);
                return Double.parseDouble(content);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
}
