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
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.nosqlbench.adapter.milvus.MilvusAdapterUtils;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusGetOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

/**
 * Because we are using type-and-target logic to identify the op variant,
 * we are limited to a string target value. In this particular op type, the
 * IDs are key target elements. Unfortunately, this creates a type incompatibility
 * where the target values should be determined at runtime based on the binding
 * in the most dynamic case. This is because the data model in use should determine
 * the type needed, and users would generally use bindings for this.
 *
 * <P>The target is expected to be nominal (naming the operation's noun), and thus is
 * historically limited to string forms. This op will need to be refactored around
 * a slightly more flexible API which can allow the target value to be type-variant,
 * and maybe even structure variant such that all nouns can be passed as named parameters
 * underneath the type designator.
 */
public class MilvusGetOpDispenser extends MilvusBaseOpDispenser<GetIdsParam> {

    public MilvusGetOpDispenser(MilvusDriverAdapter adapter,
                                ParsedOp op,
                                LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<GetIdsParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<GetIdsParam.Builder> ebF =
            l -> GetIdsParam.newBuilder();

//        ebF = l -> ebF.apply(l).withPrimaryIds(MilvusAdapterUtils.splitNames(targetF.apply(l)));
        LongFunction<Object> valueFunc = op.getAsRequiredFunction("primary_ids", Object.class);
        Object testValue = valueFunc.apply(0L);
        LongFunction<List<Object>> pidsF;
        if (testValue instanceof String string) {
            pidsF = l -> MilvusAdapterUtils.splitNames((String) valueFunc.apply(l))
                .stream().map(s -> (Object) s).toList();
        } else if (testValue instanceof List) {
            pidsF = l -> (List<Object>) valueFunc.apply(l);
        } else {
            throw new RuntimeException("Invalid type for primary_ids: " + testValue.getClass().getCanonicalName());
        }

        LongFunction<GetIdsParam.Builder> finalEbF2 = ebF;
        ebF = l -> finalEbF2.apply(l).withPrimaryIds(pidsF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, List.of("collection_name", "collection"), String.class,
            GetIdsParam.Builder::withCollectionName);
        ebF = op.enhanceEnumOptionally(ebF, "consistency_level", ConsistencyLevelEnum.class,
            GetIdsParam.Builder::withConsistencyLevel);
        ebF = op.enhanceEnumOptionally(ebF, "cl", ConsistencyLevelEnum.class,
            GetIdsParam.Builder::withConsistencyLevel);

        if (op.isDefined("output_fields")) {
            LongFunction<Object> outputFieldsF = op.getAsRequiredFunction("output_fields", Object.class);
            Object oftv = outputFieldsF.apply(0L);
            LongFunction<List> ofF;
            if (oftv instanceof List) {
                ofF = op.getAsRequiredFunction("output_fields", List.class);
                LongFunction<GetIdsParam.Builder> finalEbF = ebF;
                ebF = l -> finalEbF.apply(l).withOutputFields((List<String>) ofF.apply(l));
            } else if (oftv instanceof String) {
                var sF = op.getAsRequiredFunction("output_fields", String.class);
                LongFunction<GetIdsParam.Builder> finalEbF1 = ebF;
                ebF = l -> finalEbF1.apply(l).withOutputFields(MilvusAdapterUtils.splitNames(sF.apply(l)));
            } else throw new RuntimeException("Invalid type for output fields:" + oftv.getClass().getCanonicalName());
        }

        final LongFunction<GetIdsParam.Builder> lastF = ebF;
        final LongFunction<GetIdsParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<GetIdsParam>> createOpFunc(
        LongFunction<GetIdsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusGetOp(clientF.apply(l), paramF.apply(l));
    }
}
