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
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpGenerator;
import io.nosqlbench.adapters.api.scheduling.TimeoutPredicate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MilvusDescribeIndexOp extends MilvusBaseOp<DescribeIndexParam> implements OpGenerator {
    private final Duration timeout;
    private final Duration interval;
    private final TimeoutPredicate<Integer> timeoutPredicate;
    private MilvusDescribeIndexOp nextOp;
    private long lastAttemptAt = 0L;

    public MilvusDescribeIndexOp(
        MilvusServiceClient client,
        DescribeIndexParam request,
        Duration timeout,
        Duration interval
    ) {
        super(client, request);
        this.timeout = timeout;
        this.interval = interval;
        this.timeoutPredicate = TimeoutPredicate.of(p -> p>=100, timeout, interval, true);
    }

    @Override
    public Object applyOp(long value) {
        nextOp = null;
        timeoutPredicate.blockUntilNextInterval();

        R<DescribeIndexResponse> describeIndexResponseR = client.describeIndex(request);
        DescribeIndexResponse data = describeIndexResponseR.getData();

        TimeoutPredicate.Result<Integer> result = timeoutPredicate.test(getIndexStats(data).percent());
        String message = result.status().name() + " await state " + result.value() + " at time " + result.timeSummary();
        logger.info(message);

        if (result.isPending()) {
            this.nextOp=this;
        }

        return describeIndexResponseR;
    }

    private IndexStats getIndexStats(DescribeIndexResponse data) {
        var stats = new ArrayList<IndexStat>();
        for (IndexDescription desc : data.getIndexDescriptionsList()) {
            stats.add(new IndexStat(desc.getIndexName(), desc.getIndexedRows(), desc.getPendingIndexRows()));
        }
        return new IndexStats(stats);
    }

    public static class IndexStats extends ArrayList<IndexStat> {
        public IndexStats(List<IndexStat> stats) {
            super(stats);
        }

        public int percent() {
            return stream().mapToInt(IndexStat::percent).min().orElse(0);
        }
    }
    public static final record IndexStat(
        String index_name,
        long indexed_rows,
        long pending_rows
    ) {
        public int percent() {
            if (pending_rows == 0) {
                return 100;
            }
            return (int) (100.0d * ((double) indexed_rows / (double) (indexed_rows + pending_rows)));
        }
    }

    @Override
    public Op getNextOp() {
        return nextOp;
    }
}
