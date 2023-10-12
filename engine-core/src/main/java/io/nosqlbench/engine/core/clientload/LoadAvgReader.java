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

public class LoadAvgReader extends LinuxSystemFileReader {
    /**
     * Example line:
     *  0.78 1.39 2.03 1/2153 2818
     */
    private static final String regex = "(\\d+\\.\\d+)\\s(\\d+\\.\\d+)\\s(\\d+\\.\\d+)";

    public LoadAvgReader(){
        super("/proc/loadavg");
    }

    public Double getOneMinLoadAverage() {
        return extract(regex, 1);
    }

    public Double getFiveMinLoadAverage() {
        return extract(regex, 2);
    }

    public Double getFifteenMinLoadAverage() {
        return extract(regex, 3);
    }
}
