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
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.FindOneOptions;

public class DataApiFindOneOp extends DataApiBaseOp {
    private final Collection<Document> collection;
    private final Filter filter;
    private final FindOneOptions options;

    public DataApiFindOneOp(Database db, Collection<Document> collection, Filter filter, FindOneOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.findOne(filter, options);
    }
}
