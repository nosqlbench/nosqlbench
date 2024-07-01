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

import java.util.Collections;
import java.util.HashMap;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.api.ObjectCreator;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;

public class WeaviateCreateObjectsOp extends WeaviateBaseOp<ObjectCreator> {

	public WeaviateCreateObjectsOp(WeaviateClient client, ObjectCreator request) {
		super(client, request);
	}

	@SuppressWarnings("serial")
	@Override
	public Object applyOp(long value) {
		Result<WeaviateObject> response = null;
		try {
			response = client.data().creator().withClassName("Glove25").withProperties(new HashMap<String, Object>() {
				{
					put("key", "This is a key");
					put("value", "This is the value for a given key");
				}
			}).withVector(Collections.nCopies(25, 0.12345f).toArray(new Float[0]))
					.withConsistencyLevel(ConsistencyLevel.QUORUM).run();
			logger.debug("Successfully inserted objects in the collection: {}", response.getResult());
		} catch (RuntimeException rte) {
			throw rte;
		}
		return response;
	}

}
