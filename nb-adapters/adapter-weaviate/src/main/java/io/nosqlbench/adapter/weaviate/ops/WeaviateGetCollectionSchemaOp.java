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

import com.google.gson.GsonBuilder;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.Schema;

public class WeaviateGetCollectionSchemaOp extends WeaviateBaseOp<String,Result<?>> {

	public WeaviateGetCollectionSchemaOp(WeaviateClient client, String request) {
		super(client, request);
	}

	@Override
	public Result<?> applyOp(long value) {
		Result<Schema> getColSchemaResponse = null;
		try {
			getColSchemaResponse = client.schema().getter().run();
			if (getColSchemaResponse.hasErrors()) {
				logger.error("Get all collection schema operation has errors {}",
						getColSchemaResponse.getError().toString());
			}
			logger.info("Successfully fetched entire schema for all the collections: \n{}",
					new GsonBuilder().setPrettyPrinting().create()
							.toJson(getColSchemaResponse.getResult()));
		} catch (RuntimeException rte) {
			throw rte;
		}
		return getColSchemaResponse;
	}

}
