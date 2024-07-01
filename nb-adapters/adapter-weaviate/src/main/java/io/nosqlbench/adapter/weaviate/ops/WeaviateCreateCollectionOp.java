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
package io.nosqlbench.adapter.weaviate.ops;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.WeaviateClass;

public class WeaviateCreateCollectionOp extends WeaviateBaseOp<WeaviateClass> {

	public WeaviateCreateCollectionOp(WeaviateClient client, WeaviateClass request) {
		super(client, request);
	}

	@Override
	public Object applyOp(long value) {
		Result<Boolean> createResponse = null;
		try {
			createResponse = client.schema().classCreator().withClass(request).run();
			logger.debug("Successfully created the collection with return code of {}", createResponse.getResult());
		} catch (RuntimeException rte) {
			throw rte;
		}
		return createResponse;
	}

}
