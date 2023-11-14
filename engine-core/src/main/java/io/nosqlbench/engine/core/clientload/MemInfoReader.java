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

public class MemInfoReader extends LinuxSystemFileReader {
    /**
     * References:
     * - https://docs.kernel.org/filesystems/proc.html#meminfo
     * - https://stackoverflow.com/questions/41224738/how-to-calculate-system-memory-usage-from-proc-meminfo-like-htop
     */
    public MemInfoReader() {
        super("/proc/meminfo");
    }

    public Double getMemTotalkB() {
        String regex = "MemTotal:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getMemFreekB() {
        String regex = "MemFree:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getMemAvailablekB() {
        String regex = "MemAvailable:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getMemUsedkB() {
        Double memTotal = getMemTotalkB();
        Double memFree = getMemFreekB();
        if (memTotal != null && memFree != null)
            return memTotal - memFree;
        return null;
    }

    public Double getMemCachedkB() {
        String regex = "Cached:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getMemBufferskB() {
        String regex = "Buffers:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getSwapTotalkB() {
        String regex = "SwapTotal:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getSwapFreekB() {
        String regex = "SwapFree:\\s+(\\d+) kB";
        return extract(regex, 1);
    }

    public Double getSwapUsedkB() {
        Double swapTotal = getSwapTotalkB();
        Double swapFree = getSwapFreekB();
        if (swapTotal != null && swapFree != null)
            return swapTotal - swapFree;
        return null;
    }
}
