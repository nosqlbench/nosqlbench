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
import io.milvus.param.index.DescribeIndexParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusDescribeIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.LongFunction;

public class MilvusDescribeIndexOpDispenser extends MilvusBaseOpDispenser<DescribeIndexParam> {

    private Duration awaitTimeout = Duration.ZERO;
    private Duration awaitInterval = Duration.of(10, ChronoUnit.SECONDS);

    public MilvusDescribeIndexOpDispenser(MilvusDriverAdapter adapter,
                                          ParsedOp op,
                                          LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);

        op.getOptionalStaticValue("await_timeout", Number.class)
            .map(Number::doubleValue)
            .ifPresent(v->this.awaitTimeout = Duration.of((long)(v*1000), ChronoUnit.MILLIS));
        op.getOptionalStaticValue("await_interval", Number.class)
            .map(Number::doubleValue).ifPresent(v->this.awaitInterval =Duration.of((long)(v*1000),ChronoUnit.MILLIS));
    }

    @Override
    public LongFunction<DescribeIndexParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<DescribeIndexParam.Builder> ebF =
            l -> DescribeIndexParam.newBuilder().withIndexName(targetF.apply(l));
        ebF = op.enhanceFunc(ebF, List.of("collection","collection_name"), String.class,
            DescribeIndexParam.Builder::withCollectionName);
        ebF = op.enhanceFuncOptionally(ebF, List.of("database_name","database"), String.class,
            DescribeIndexParam.Builder::withDatabaseName);


        final LongFunction<DescribeIndexParam.Builder> lastF = ebF;
        final LongFunction<DescribeIndexParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<DescribeIndexParam>> createOpFunc(
        LongFunction<DescribeIndexParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusDescribeIndexOp(
            clientF.apply(l),
            paramF.apply(l),
            awaitTimeout,
            awaitInterval
        );
    }
}
