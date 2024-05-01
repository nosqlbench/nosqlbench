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

public record CyclesSpec(
    long first_inclusive,
    long last_exclusive,
    String firstSpec,
    String lastSpec
) {
    private final static Logger logger = LogManager.getLogger(CyclesSpec.class);
    public CyclesSpec {
        if (first_inclusive>last_exclusive) {
            throw new InvalidParameterException("cycles must start with a lower first cycle than last cycle");
        }
    }

    public static CyclesSpec parse(String spec) {
        int rangeAt = spec.indexOf("..");
        String beginningSpec = "0";
        String endingSpec = spec;
        if (0 < rangeAt) {
            beginningSpec = spec.substring(0, rangeAt);
            endingSpec = spec.substring(rangeAt+2);
        }
        long first = Unit.longCountFor(beginningSpec).orElseThrow(() -> new RuntimeException("Unable to parse start cycles from " + spec));
        long last=first;
        if (endingSpec.startsWith("+")) {
            long added=Unit.longCountFor(endingSpec.substring(1)).orElseThrow(() -> new RuntimeException(
                "Unable to parse incremental cycle interval. Use one of these forms: 100 or 0..100 or 0..+100"
            ));
            last = first+added;
        } else {
            last = Unit.longCountFor(endingSpec).orElseThrow(() -> new RuntimeException("Unable to parse start cycles from " + spec));
        }

        return new CyclesSpec(first, last, beginningSpec, endingSpec);
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CyclesSpec that))
            return false;
        if (last_exclusive!=that.last_exclusive)
            return false;
        if (first_inclusive!=that.first_inclusive)
            return false;
        if (!firstSpec.equals(that.firstSpec))
            return false;
        if (!lastSpec.equals(that.lastSpec))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(first_inclusive);
        result = 31 * result + Long.hashCode(last_exclusive);
        result = 31 * result + firstSpec.hashCode();
        result = 31 * result + lastSpec.hashCode();
        return result;
    }
}
