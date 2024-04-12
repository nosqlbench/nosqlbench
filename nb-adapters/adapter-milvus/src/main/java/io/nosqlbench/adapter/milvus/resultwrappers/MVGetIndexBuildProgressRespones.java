/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.milvus.resultwrappers;

import io.milvus.grpc.GetIndexBuildProgressResponse;

public class MVGetIndexBuildProgressRespones {
    private final GetIndexBuildProgressResponse r;

    public MVGetIndexBuildProgressRespones(GetIndexBuildProgressResponse r) {
        this.r = r;
    }

    public int getPercent() {
        if (getTotalRows()==getIndexedRows()) {
            return 100;
        }

        double ratio = (double) getIndexedRows() / (double) getTotalRows();
        return (int) (ratio*100.0d);
    }

    public long getTotalRows() {
        return r.getTotalRows();
    }
    public long getIndexedRows() {
        return r.getIndexedRows();
    }
}
