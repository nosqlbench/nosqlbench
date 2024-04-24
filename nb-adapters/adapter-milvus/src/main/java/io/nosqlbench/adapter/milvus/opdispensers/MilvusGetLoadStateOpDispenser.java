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

package io.nosqlbench.adapter.milvus.opdispensers;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.LoadState;
import io.milvus.param.collection.GetLoadStateParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusAdapterUtils;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusGetLoadStateOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class MilvusGetLoadStateOpDispenser extends MilvusBaseOpDispenser<GetLoadStateParam> {

    private Duration awaitTimeout = Duration.ZERO;
    private Duration awaitInterval = Duration.of(10, ChronoUnit.SECONDS);
    private LoadState awaitState = LoadState.UNRECOGNIZED;

    public MilvusGetLoadStateOpDispenser(MilvusDriverAdapter adapter,
                                         ParsedOp op,
                                         LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        op.getOptionalStaticValue("await_timeout", Number.class)
            .map(Number::doubleValue)
            .ifPresent(v->this.awaitTimeout=Duration.of((long)(v*1000),ChronoUnit.MILLIS));
        op.getOptionalStaticValue("await_interval", Number.class)
            .map(Number::doubleValue).ifPresent(v->this.awaitInterval=Duration.of((long)(v*1000),ChronoUnit.MILLIS));
        op.getOptionalStaticValue("await_state", String.class).ifPresent(s -> {
            var spec = s.toLowerCase();
            for (LoadState value : LoadState.values()) {
                if (value.name().toLowerCase().equals(spec) || value.name().toLowerCase().equals("loadstate" + spec)) {
                    this.awaitState = value;
                    break;
                }
            }
            if (this.awaitState == null) {
                throw new OpConfigError("Unrecognizable load state to await: " + spec);
            }
        });

    }

    @Override
    public LongFunction<GetLoadStateParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<GetLoadStateParam.Builder> ebF =
            l -> GetLoadStateParam.newBuilder().withCollectionName(targetF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF, List.of("database_name", "database"), String.class,
            GetLoadStateParam.Builder::withDatabaseName);

        Optional<LongFunction<String>> partitionsF = op.getAsOptionalFunction("partition_name", String.class);
        if (partitionsF.isPresent()) {
            LongFunction<String> pfunc = partitionsF.get();
            LongFunction<GetLoadStateParam.Builder> finalEbF = ebF;
            ebF = l -> finalEbF.apply(l).withPartitionNames(MilvusAdapterUtils.splitNames(pfunc.apply(l)));
        }



        final LongFunction<GetLoadStateParam.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<GetLoadStateParam>> createOpFunc(
        LongFunction<GetLoadStateParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusGetLoadStateOp(
            clientF.apply(l),
            paramF.apply(l),
            this.awaitState,
            this.awaitTimeout,
            this.awaitInterval
        );
    }
}
