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

package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.activitytype.http.async.HttpAsyncAction;

import java.net.http.HttpClient;
import java.util.function.LongFunction;

public class HttpAsyncOp {
    public final HttpAsyncAction action;
    public final LongFunction<? extends HttpOp> op;
    public final long cycle;

    private final HttpOp httpOp;
    private final HttpClient client;

    public HttpAsyncOp(HttpAsyncAction action, LongFunction<? extends HttpOp> op, long cycle, HttpClient client) {
        this.action = action;
        this.op = op;
        this.cycle = cycle;
        this.client = client;
        this.httpOp = op.apply(cycle);

    }

    public HttpOp getOp() {
        return httpOp;
    }

    public HttpClient getClient() {
        return client;
    }

    public HttpAsyncAction getAction() {
        return action;
    }

}
