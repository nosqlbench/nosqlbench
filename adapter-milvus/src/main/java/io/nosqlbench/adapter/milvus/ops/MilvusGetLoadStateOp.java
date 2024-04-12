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
import io.milvus.grpc.GetLoadStateResponse;
import io.milvus.grpc.LoadState;
import io.milvus.param.R;
import io.milvus.param.collection.GetLoadStateParam;
import io.nosqlbench.adapter.milvus.exceptions.MilvusAwaitStateIncompleteError;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpGenerator;
import io.nosqlbench.adapters.api.scheduling.TimeoutPredicate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.LockSupport;

public class MilvusGetLoadStateOp extends MilvusBaseOp<GetLoadStateParam> implements OpGenerator {
    private final TimeoutPredicate<LoadState> timeoutPredicate;
    private int tried;
    private MilvusGetLoadStateOp nextOp;
    private long lastAttemptAt = 0L;

    public MilvusGetLoadStateOp(
        MilvusServiceClient client,
        GetLoadStateParam request,
        LoadState awaitState,
        Duration timeout,
        Duration interval
    ) {
        super(client, request);
        this.timeoutPredicate = TimeoutPredicate.of(s -> s==awaitState, timeout, interval, true);
    }

    @Override
    public Object applyOp(long value) {
        this.nextOp = null;
        timeoutPredicate.blockUntilNextInterval();
        R<GetLoadStateResponse> getLoadStateResponse = client.getLoadState(request);
        TimeoutPredicate.Result<LoadState> result = timeoutPredicate.test(getLoadStateResponse.getData().getState());

        String message = result.status().name() + " await state " + result.value() + " at time " + result.timeSummary();
        logger.info(message);

        if (result.status()== TimeoutPredicate.Status.pending) {
            nextOp=this;
        }

        return getLoadStateResponse;
    }

    @Override
    public Op getNextOp() {
        return this.nextOp;
    }
}
