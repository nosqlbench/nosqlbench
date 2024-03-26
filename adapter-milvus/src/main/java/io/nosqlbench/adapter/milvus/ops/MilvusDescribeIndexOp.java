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

package io.nosqlbench.adapter.milvus.ops;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.IndexDescription;
import io.milvus.param.R;
import io.milvus.param.index.DescribeIndexParam;
import io.nosqlbench.adapter.milvus.exceptions.MilvusIndexingIncompleteError;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpGenerator;

import java.util.ArrayList;
import java.util.List;

public class MilvusDescribeIndexOp extends MilvusBaseOp<DescribeIndexParam> implements OpGenerator {
    private final boolean doPollTillIndexed;
    private final int awaitIndexTries;
    private int tried = 0;
    private MilvusDescribeIndexOp nextOp;
    private long lastAttemptAt=0L;

    public MilvusDescribeIndexOp(
        MilvusServiceClient client,
        DescribeIndexParam request,
        boolean doPollTillIndexed,
        int awaitIndexTries
    ) {
        super(client, request);
        this.doPollTillIndexed = doPollTillIndexed;
        this.awaitIndexTries = awaitIndexTries;
    }

    @Override
    public Object applyOp(long value) {
        long attemptAt = System.currentTimeMillis();
        long gap = attemptAt - lastAttemptAt;
        if (gap<500) {
            logger.warn("You are polling the state of indexes with an interval of only " + gap + "ms.");
        }
        lastAttemptAt=attemptAt;

        R<DescribeIndexResponse> describeIndexResponseR = client.describeIndex(request);
        tried++;
        DescribeIndexResponse data = describeIndexResponseR.getData();

        if (doPollTillIndexed) {
            List<IndexStats> stats = getIndexStats(data);
            int maxpct = stats.stream().mapToInt(IndexStats::percent).max().orElse(100);
            if (maxpct == 100) {
                logger.info("indexing percent at " + maxpct + " on try " + tried);
                this.nextOp=null;
            } else if (awaitIndexTries<tried) {
                logger.info("indexing percent at " + maxpct + " on try " + tried + ", retrying");
                this.nextOp = this;
            } else {
                logger.info("indexing percent at " + maxpct + " on try " + tried + ", throwing error");
                throw new MilvusIndexingIncompleteError(request, tried, stats);
            }
        }
        return describeIndexResponseR;
    }

    private List<IndexStats> getIndexStats(DescribeIndexResponse data) {
        var stats = new ArrayList<IndexStats>();
        for (IndexDescription desc : data.getIndexDescriptionsList()) {
            stats.add(new IndexStats(desc.getIndexName(),desc.getIndexedRows(),desc.getPendingIndexRows()));
        }
        return stats;
    }

    public static final record IndexStats(
        String index_name,
        long indexed_rows,
        long pending_rows
    ) {
        public int percent() {
            if (pending_rows==0) {
                return 100;
            }
            return (int) (100.0d*((double)indexed_rows/(double)(indexed_rows+pending_rows)));
        }
    }

    @Override
    public Op getNextOp() {
        return nextOp;
    }
}
