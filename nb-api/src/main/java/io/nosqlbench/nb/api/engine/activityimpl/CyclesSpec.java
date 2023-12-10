/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.nb.api.engine.activityimpl;

import io.nosqlbench.nb.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;

public record CyclesSpec(long first_inclusive, long last_exclusive, String firstSpec, String lastSpec) {
    private final static Logger logger = LogManager.getLogger(CyclesSpec.class);
    public CyclesSpec {
        if (first_inclusive>last_exclusive) {
            throw new InvalidParameterException("cycles must start with a lower first cycle than last cycle");
        }
//        if (first_inclusive==last_exclusive) {
//            logger.warn("This cycles interval means zero total:" + this);
//        }
    }

    public static CyclesSpec parse(String spec) {
        int rangeAt = spec.indexOf("..");
        String beginningInclusive = "0";
        String endingExclusive = spec;
        if (0 < rangeAt) {
            beginningInclusive = spec.substring(0, rangeAt);
            endingExclusive = spec.substring(rangeAt+2);
        }
        long first = Unit.longCountFor(beginningInclusive).orElseThrow(() -> new RuntimeException("Unable to parse start cycles from " + spec));
        long last = Unit.longCountFor(endingExclusive).orElseThrow(() -> new RuntimeException("Unable to parse start cycles from " + spec));

        return new CyclesSpec(first, last, beginningInclusive, endingExclusive);
    }

    public String summary() {
        return "[" + firstSpec + ".." + lastSpec + ")=" + (last_exclusive - first_inclusive);
    }

    @Override
    public String toString() {
        return firstSpec + ".." + lastSpec;
    }

    public CyclesSpec withFirst(long first) {
        return new CyclesSpec(first, last_exclusive, String.valueOf(first), lastSpec);
    }

    public CyclesSpec withFirst(String firstSpec) {
        long start = Unit.longCountFor(firstSpec).orElseThrow(() -> new RuntimeException("Unable to parse start cycle from " + firstSpec));
        return new CyclesSpec(start, last_exclusive, firstSpec, lastSpec);
    }

    public CyclesSpec withLast(long last) {
        return new CyclesSpec(first_inclusive, last, firstSpec, String.valueOf(last));
    }

    public CyclesSpec withLast(String lastSpec) {
        long last = Unit.longCountFor(lastSpec).orElseThrow(() -> new RuntimeException("Unable to parse end cycle from " + lastSpec));
        return new CyclesSpec(first_inclusive, last, firstSpec, lastSpec);
    }

    public long cycle_count() {
        return last_exclusive -first_inclusive;
    }
}
