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

package io.nosqlbench.engine.api.activityimpl;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.DoubleSummaryStatistics;
import java.util.Optional;

public class CpuInfo {
    private final static Logger logger = LogManager.getLogger(CpuInfo.class);

    final private static SystemInfo SYSTEM_INFO = new SystemInfo();

    public static Optional<ProcDetails> getProcDetails() {
        return Optional.of(new ProcDetails(SYSTEM_INFO));
    }

    public static class ProcDetails {
        SystemInfo si;
        HardwareAbstractionLayer hal;
        CentralProcessor processor;

        public ProcDetails(SystemInfo si) {
            this.si = si;
            this.hal = si.getHardware();
            this.processor = hal.getProcessor();
        }

        public int getCoreCount() {
            return processor.getLogicalProcessorCount();
        }

        public int getCpuCount() {
            return processor.getPhysicalProcessorCount();
        }

        public String getModelName() {
            return processor.getProcessorIdentifier().toString();
        }

        public String getMhz() {
            double vendorFreq = processor.getProcessorIdentifier().getVendorFreq();
            return String.valueOf((long) (vendorFreq/1E6));
        }

        public String toString() {
            return "cores=" + getCoreCount() +
                    " cpus=" + getCpuCount() + " mhz=" + getMhz() +
                    " speedavg=" + getCurrentSpeed().getAverage() +
                    " model='" + getModelName() + "'";

        }


        public double getMaxFreq(int cpu) {
            return (double)processor.getMaxFreq();
        }
        public double getCurFreq(int cpu) {
            return (double)processor.getCurrentFreq()[cpu];
        }

        public double getCurrentSpeed(int cpu) {
            double curFreq = getCurFreq(cpu);
            double maxFreq = getMaxFreq(cpu);
            if (curFreq < 0 || maxFreq < 0) {
                return -1;
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

    }
}
