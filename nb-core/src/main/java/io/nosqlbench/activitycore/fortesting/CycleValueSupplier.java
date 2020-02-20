/*
*   Copyright 2015 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.nosqlbench.activitycore.fortesting;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public class CycleValueSupplier implements LongSupplier {

    private final AtomicLong fixedCycle = new AtomicLong(0L);

    public CycleValueSupplier setValue(long newFixedCycle) {
        fixedCycle.set(newFixedCycle);
        return this;
    }

    @Override
    public long getAsLong() {
        return fixedCycle.get();
    }
}
