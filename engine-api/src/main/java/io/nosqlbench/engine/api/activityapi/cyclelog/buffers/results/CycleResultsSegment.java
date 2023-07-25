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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results;



import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public interface CycleResultsSegment extends Comparable<CycleResultsSegment>, Iterable<CycleResult> {
    long getCount();
    long getMinCycle();

    // TODO: Specialize this for push-down performance
    default CycleResultsSegment filter(Predicate<ResultReadable> filter) {
        CycleResult[] filteredResults = StreamSupport.stream(spliterator(), false).filter(filter).toArray(CycleResult[]::new);
        return new CycleResultArray(filteredResults);
    }

    default int compareTo( CycleResultsSegment other) {
        return Long.compare(getMinCycle(),other.getMinCycle());
    }

    CycleResultsSegment EMPTY = new EmptySegment();

    class EmptySegment implements CycleResultsSegment {
        @Override
        public long getCount() {
            return 0;
        }

        @Override
        public long getMinCycle() {
            return Long.MAX_VALUE;
        }


        @Override
        public Iterator<CycleResult> iterator() {
            return Collections.emptyIterator();
        }
    }
}
