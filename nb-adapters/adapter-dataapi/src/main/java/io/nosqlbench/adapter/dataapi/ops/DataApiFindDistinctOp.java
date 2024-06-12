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

package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Filter;

public class DataApiFindDistinctOp extends DataApiBaseOp {
    private final Collection collection;
    private final String fieldName;
    private final Filter filter;
    private final Class<?> resultClass;

    public DataApiFindDistinctOp(Database db, Collection collection, String fieldName, Filter filter, Class<?> resultClass) {
        super(db);
        this.collection = collection;
        this.fieldName = fieldName;
        this.filter = filter;
        this.resultClass = resultClass;
    }

    @Override
    public Object apply(long value) {
        return collection.distinct(fieldName, filter, resultClass);
    }
}
