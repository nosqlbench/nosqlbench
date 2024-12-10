/*
 * Copyright (c) 2020-2024 nosqlbench
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
package io.nosqlbench.adapter.weaviate.opsdispensers;

import java.util.function.LongFunction;

import io.nosqlbench.adapter.weaviate.WeaviateDriverAdapter;
import io.nosqlbench.adapter.weaviate.WeaviateSpace;
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.ops.WeaviateCreateObjectsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.api.ObjectCreator;

/**
 * Create objects.
 *
 * @see <a href=
 *      "https://weaviate.io/developers/weaviate/manage-data/create">Create
 *      Objects</a>.
 * @see <a href=
 *      "https://weaviate.io/developers/weaviate/api/rest#tag/objects/post/objects">Create
 *      Objects - REST API</a>.
 */
public class WeaviateCreateObjectsOpDispenser extends WeaviateBaseOpDispenser<ObjectCreator, Result<?>> {

	public WeaviateCreateObjectsOpDispenser(WeaviateDriverAdapter adapter, ParsedOp op,
                                            LongFunction<WeaviateSpace> spaceF, LongFunction<String> targetF) {
		super(adapter, op, spaceF, targetF);
	}

	public LongFunction<ObjectCreator> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {
//		LongFunction<ObjectCreatorBuilder> ebF = l -> ObjectCreator.builder().className(targetF.apply(l));
//
//		final LongFunction<ObjectCreatorBuilder> lastF = ebF;
//		return l -> lastF.apply(l).build();

		@SuppressWarnings("deprecation")
		LongFunction<ObjectCreator> ebF = l -> {
			try {
				return ObjectCreator.class.newInstance().withClassName(targetF.apply(l));
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		};
		return l -> ebF.apply(l);
	}

	@Override
	public LongFunction<WeaviateBaseOp<ObjectCreator,Result<?>>> createOpFunc(LongFunction<ObjectCreator> paramF,
			LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new WeaviateCreateObjectsOp(clientF.apply(l), paramF.apply(l));
	}

}
