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

import io.milvus.grpc.GetLoadStateResponse;
import io.milvus.param.R;

import java.time.Duration;

public class MilvusAwaitStateIncompleteError extends RuntimeException {
    private final R<GetLoadStateResponse> loadState;
    private final Duration timeout;
    private final String timeSummary;

    public MilvusAwaitStateIncompleteError(R<GetLoadStateResponse> loadState, Duration timeout, String timeSummary) {
        this.loadState = loadState;
        this.timeout = timeout;
        this.timeSummary = timeSummary;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": at time " +timeSummary;
    }
}
