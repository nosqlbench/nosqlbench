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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;


import java.util.Iterator;

public interface CycleResultSegmentsReadable extends Iterable<CycleResultsSegment> {

    default Iterable<CycleResult> getCycleResultIterable() {
        return new Iterable<CycleResult>() {

            @Override
            public Iterator<CycleResult> iterator() {
                return new Iterator<CycleResult>() {
                    final Iterator<CycleResultsSegment> iterSegment = CycleResultSegmentsReadable.this.iterator();
                    Iterator<CycleResult> innerIter=iterSegment.next().iterator();

                    @Override
                    public boolean hasNext() {
                        while(!innerIter.hasNext()&&iterSegment.hasNext()) {
                            innerIter=iterSegment.next().iterator();
                        }
                        return innerIter.hasNext();
                    }

                    @Override
                    public CycleResult next() {
                        return innerIter.next();
                    }

                };
            }
        };
    }
}
