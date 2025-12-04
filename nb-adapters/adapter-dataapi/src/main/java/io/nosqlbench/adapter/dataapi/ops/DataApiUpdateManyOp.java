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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateManyOptions;

public class DataApiUpdateManyOp extends DataApiBaseOp {
    private final Collection collection;
    private final Filter filter;
    private final Update update;
    private final CollectionUpdateManyOptions options;

    public DataApiUpdateManyOp(Database db, Collection collection, Filter filter, Update update, CollectionUpdateManyOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.update = update;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.updateMany(filter, update, options);
    }
}
