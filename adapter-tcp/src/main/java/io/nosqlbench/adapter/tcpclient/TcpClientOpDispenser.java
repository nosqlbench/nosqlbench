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

package io.nosqlbench.adapter.tcpclient;

import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class TcpClientOpDispenser extends BaseOpDispenser<TcpClientOp, TcpClientAdapterSpace> {

<<<<<<< HEAD
    private final LongFunction<TcpClientAdapterSpace> ctxFunction;
=======
    private final LongFunction<TcpClientAdapterSpace> ctxfunc;
>>>>>>> ef397c742 (nosqlbench-440 convert tcpclient driver from ActivityType to DriverAdapter e:1d)
    private final LongFunction<String> outFunction;

    public TcpClientOpDispenser(TcpClientDriverAdapter adapter, ParsedOp cmd, LongFunction<TcpClientAdapterSpace> ctxfunc) {
        super(adapter,cmd);
<<<<<<< HEAD
        this.ctxFunction = ctxfunc;
        LongFunction<Object> objectFunction = cmd.getAsRequiredFunction("stmt", Object.class);
        LongFunction<String> stringFunction = l -> objectFunction.apply(l).toString();
        cmd.enhanceFuncOptionally(stringFunction,"suffix",String.class,(a, b) -> a+b);
        this.outFunction = stringFunction;
=======
        this.ctxfunc = ctxfunc;
        LongFunction<Object> objectFunction = cmd.getAsRequiredFunction("stmt", Object.class);
        LongFunction<String> stringfunc = l -> objectFunction.apply(l).toString();
        cmd.enhanceFuncOptionally(stringfunc,"suffix",String.class,(a, b) -> a+b);
        this.outFunction = stringfunc;
>>>>>>> ef397c742 (nosqlbench-440 convert tcpclient driver from ActivityType to DriverAdapter e:1d)
    }

    @Override
    public TcpClientOp apply(long value) {
<<<<<<< HEAD
        TcpClientAdapterSpace ctx = ctxFunction.apply(value);
=======
        TcpClientAdapterSpace ctx = ctxfunc.apply(value);
>>>>>>> ef397c742 (nosqlbench-440 convert tcpclient driver from ActivityType to DriverAdapter e:1d)
        String output = outFunction.apply(value);
        return new TcpClientOp(ctx,output);
    }
}
