/*
 * Copyright (c) nosqlbench
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
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.ReplaceOneOptions;

public class DataApiReplaceOneOp extends DataApiBaseOp {
    private final Collection collection;
    private final Filter filter;
    private final Object replacement;
    private final ReplaceOneOptions options;

    public DataApiReplaceOneOp(Database db, Collection collection, Filter filter, Object replacement, ReplaceOneOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.replacement = replacement;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.replaceOne(filter, replacement, options);
    }
}
