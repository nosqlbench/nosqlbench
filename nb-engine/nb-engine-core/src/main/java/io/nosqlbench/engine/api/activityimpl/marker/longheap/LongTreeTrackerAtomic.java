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

package io.nosqlbench.engine.api.activityimpl.marker.longheap;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a version of longTreeTracker that is safe for concurrent access.
 */
public class LongTreeTrackerAtomic extends LongTreeTracker {
    private final AtomicLong timage = new AtomicLong(0L);

    @Override
    public long setCompleted(long index) {
        long before = timage.get();
        long after = super.setCompleted(index, before);
        while (!timage.compareAndSet(before,after)) {
            before = timage.get();
            after = super.setCompleted(index, before);
        }
        return after;
    }
}
