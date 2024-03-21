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
import io.milvus.param.credential.UpdateCredentialParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusUpdateCredentialOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusUpdateCredentialOpDispenser extends MilvusBaseOpDispenser<UpdateCredentialParam> {

    public MilvusUpdateCredentialOpDispenser(MilvusDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<UpdateCredentialParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<UpdateCredentialParam.Builder> ebF =
            l -> UpdateCredentialParam.newBuilder().withUsername(targetF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF,"old_password",String.class,UpdateCredentialParam.Builder::withOldPassword);
        ebF = op.enhanceFuncOptionally(ebF,"new_password",String.class,UpdateCredentialParam.Builder::withNewPassword);

        final LongFunction<UpdateCredentialParam.Builder> lastF = ebF;
        final LongFunction<UpdateCredentialParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<UpdateCredentialParam>> createOpFunc(
        LongFunction<UpdateCredentialParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusUpdateCredentialOp(clientF.apply(l),paramF.apply(l));
    }
}
