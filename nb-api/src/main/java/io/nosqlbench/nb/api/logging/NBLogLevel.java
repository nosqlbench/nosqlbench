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

package io.nosqlbench.nb.api.logging;

public enum NBLogLevel {
    NONE(0L),
    FATAL(1L << 0),
    ERROR(1L << 1),
    WARN(1L << 2),
    INFO(1L << 3),
    DEBUG(1L << 4),
    TRACE(1L << 5),
    ALL(1L << 30),
    ;

    private final long level;

    NBLogLevel(long level) {
        this.level = level;
    }

    public static NBLogLevel valueOfName(String name) {
        for (NBLogLevel possible : NBLogLevel.values()) {
            if (name.toUpperCase().equals(possible.toString())) {
                return possible;
            }
        }
        throw new RuntimeException("Unable to find NBLogLevel for " + name);
    }

    public static NBLogLevel max(NBLogLevel... levels) {
        NBLogLevel max = NBLogLevel.NONE;
        for (NBLogLevel level : levels) {
            if (level.level > max.level) {
                max = level;
            }
        }
        return max;
    }

    public boolean isGreaterOrEqualTo(NBLogLevel other) {
        return level >= other.level;
    }
}
