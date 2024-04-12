/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.userlibs.apps.summarizer;

import io.nosqlbench.virtdata.core.bindings.DataMapper;

public class ValuesTask implements Runnable {

    private final long startIncl;
    private final long endExcl;
    private final DataMapper<Object> mapper;
    private final DataSetSummary<?> summary;

    public ValuesTask(long startIncl, long endExcl, DataMapper<Object> mapper, DataSetSummary<?> summary) {
        this.startIncl = startIncl;
        this.endExcl = endExcl;
        this.mapper = mapper;
        this.summary = summary;
    }

    @Override
    public void run() {
        summary.setSource(Thread.currentThread().getName()+"[" + getRange()+"): ");
        for (long cycle = startIncl; cycle < endExcl; cycle++) {
            Object apply = mapper.apply(cycle);
            summary.addObject(apply);
        }
    }

    public String getRange() {
        return startIncl + ".." + (endExcl);
    }
}
