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

import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.CanSortCycles;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CycleResultArray implements CycleResultsSegment, CanSortCycles {

    private final CycleResult[] cycleResults;

    public CycleResultArray(CycleResult[] cycleResults) {
        this.cycleResults = cycleResults;
    }

    public CycleResultArray(CycleResultsSegment segment) {
        cycleResults = new CycleResult[(int) segment.getCount()];
        Iterator<CycleResult> iterator = segment.iterator();
        for (int i = 0; i < cycleResults.length; i++) {
            cycleResults[i]=iterator.next();
        }
    }

    @Override
    public long getCount() {
        return cycleResults.length;
    }

    @Override
    public long getMinCycle() {
        return cycleResults[0].getCycle();
    }


    @Override
    public Iterator<CycleResult> iterator() {
        return new CycleResultArrayIterator(cycleResults);
    }

    @Override
    public void sort() {
        Arrays.sort(cycleResults);
    }

    private static class CycleResultArrayIterator implements Iterator<CycleResult> {
        private final CycleResult[] results;
        private int idx;

        public CycleResultArrayIterator(CycleResult[] results) {
            this.results = results;
            this.idx=0;
        }

        @Override
        public boolean hasNext() {
            return (idx<results.length);
        }

        @Override
        public CycleResult next() {
            if (idx>=results.length) {
                throw new NoSuchElementException("Unable to read array past last value");
            }
            CycleResult result = results[idx];
            idx++;
            return result;
        }
    }
}
