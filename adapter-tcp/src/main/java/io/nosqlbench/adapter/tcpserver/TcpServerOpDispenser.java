/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.tcpserver;

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class TcpServerOpDispenser extends BaseOpDispenser<TcpServerOp,TcpServerAdapterSpace> {

    private final LongFunction<TcpServerAdapterSpace> ctxFunction;
    private final LongFunction<String> outFunction;

    public TcpServerOpDispenser(TcpServerDriverAdapter adapter, ParsedOp cmd, LongFunction<TcpServerAdapterSpace> ctxfunc) {
        super(adapter,cmd);
        this.ctxFunction = ctxfunc;
        LongFunction<Object> objectFunction = cmd.getAsRequiredFunction("stmt", Object.class);
        LongFunction<String> stringFunction = l -> objectFunction.apply(l).toString();
        this.outFunction = stringFunction;
    }

    @Override
    public TcpServerOp apply(long value) {
        TcpServerAdapterSpace ctx = ctxFunction.apply(value);
        String output = outFunction.apply(value);
        return new TcpServerOp(ctx,output);
    }
}
