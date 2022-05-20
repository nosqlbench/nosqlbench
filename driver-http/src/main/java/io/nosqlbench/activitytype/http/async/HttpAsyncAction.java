package io.nosqlbench.activitytype.http.async;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.activitytype.cmds.HttpAsyncOp;
import io.nosqlbench.activitytype.cmds.HttpOp;
import io.nosqlbench.activitytype.http.HttpActivity;
import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongFunction;

public class HttpAsyncAction extends BaseAsyncAction<HttpAsyncOp, HttpActivity> {

    private final static Logger logger = LogManager.getLogger(HttpAsyncAction.class);

    private OpSequence<OpDispenser<HttpOp>> sequencer;
    private HttpClient client;

    private CompletableFuture<HttpResponse<Void>> future;


    public HttpAsyncAction(HttpActivity httpActivity, int slot) {
        super(httpActivity,slot);
    }

    @Override
    public void startOpCycle(TrackedOp<HttpAsyncOp> opc) {
        HttpAsyncOp opdata = opc.getOpData();
        HttpOp op = opdata.getOp();

        opc.start();
        future = opdata.getAction().client.sendAsync(op.request, HttpResponse.BodyHandlers.discarding());
    }

    public void init() {
        this.sequencer = activity.getSequencer();
        this.client = activity.getClient().apply(Thread.currentThread());
    }

    @Override
    public LongFunction<HttpAsyncOp> getOpInitFunction() {
        return l -> {
            LongFunction<HttpOp> readyHttpOp = sequencer.apply(l);
            return new HttpAsyncOp(this,readyHttpOp,l,client);
        };
    }


//    @Override
//    public boolean enqueue(TrackedOp<HttpAsyncOp> opc) {
//        HttpAsyncOp opData = opc.getOpData();
//        opData.op.
//        return false;
//    }
}
