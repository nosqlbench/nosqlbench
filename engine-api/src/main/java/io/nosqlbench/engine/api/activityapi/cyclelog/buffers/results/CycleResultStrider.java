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

import java.util.Iterator;

public class CycleResultStrider {

    private final Iterator<CycleResult> iterator;

    public CycleResultStrider(Iterator<CycleResult> iterator) {
        this.iterator = iterator;
    }

    public CycleResultsSegment getCycleResultsSegment(int stride) {
        if (!iterator.hasNext()) {
            return null;
        }

        CycleResultSegmentBuffer buffer = new CycleResultSegmentBuffer(stride);
        for (int i = 0; i < stride; i++) {
            if (iterator.hasNext()) {
                buffer.append(iterator.next());
            }
        }
        return buffer.toReader();
    }

}
