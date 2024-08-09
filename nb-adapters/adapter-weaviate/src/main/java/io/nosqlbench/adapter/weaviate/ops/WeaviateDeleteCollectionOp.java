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

public class WeaviateDeleteCollectionOp extends WeaviateBaseOp<String> {

	public WeaviateDeleteCollectionOp(WeaviateClient client, String request) {
		super(client, request);
	}

	@Override
	public Object applyOp(long value) {
		Result<Boolean> delColResponse = null;
		try {
			delColResponse = client.schema().classDeleter().withClassName(request).run();
			if (delColResponse.hasErrors()) {
				logger.error("Delete collection operation has errors {}",
						delColResponse.getError().toString());
			}
			logger.debug("Successfully deleted the collection: {}", delColResponse.getResult().toString());
		} catch (RuntimeException rte) {
			throw rte;
		}
		return delColResponse;
	}

}
