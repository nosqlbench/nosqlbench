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

package io.nosqlbench.adapter.milvus.exceptions;

import io.milvus.param.index.DescribeIndexParam;
import io.nosqlbench.adapter.milvus.ops.MilvusDescribeIndexOp;

import java.util.List;

public class MilvusIndexingIncompleteError extends RuntimeException {
    private final DescribeIndexParam milvusDescribeIndexOp;
    private final int tried;
    private final List<MilvusDescribeIndexOp.IndexStat> stats;

    public MilvusIndexingIncompleteError(DescribeIndexParam milvusDescribeIndexOp, int tried, List<MilvusDescribeIndexOp.IndexStat> stats) {
        this.milvusDescribeIndexOp = milvusDescribeIndexOp;
        this.tried = tried;
        this.stats = stats;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": "
            + "tries:" + tried + "/" + tried
            + ", index:" + milvusDescribeIndexOp.getIndexName()
            + ", database:" + milvusDescribeIndexOp.getDatabaseName()
            + ", collection:" + milvusDescribeIndexOp.getCollectionName();
    }
}
