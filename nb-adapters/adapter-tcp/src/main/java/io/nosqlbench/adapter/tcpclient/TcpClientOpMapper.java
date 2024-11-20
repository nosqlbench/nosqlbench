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

package io.nosqlbench.adapter.tcpclient;

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

public class TcpClientOpMapper implements OpMapper<TcpClientOp,TcpClientAdapterSpace> {

    private final TcpClientDriverAdapter adapter;


    public TcpClientOpMapper(TcpClientDriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<TcpClientOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<TcpClientAdapterSpace> spaceInitF) {
        LongFunction<String> spacefunc = op.getAsFunctionOr("space", "default");
        LongFunction<TcpClientAdapterSpace> ctxfunc = adapter.getSpaceFunc(op);
        return new TcpClientOpDispenser(adapter,op,ctxfunc);
    }


}
