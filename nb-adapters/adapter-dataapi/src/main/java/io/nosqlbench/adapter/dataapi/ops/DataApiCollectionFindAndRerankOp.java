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
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;

public class DataApiCollectionFindAndRerankOp extends DataApiBaseOp {
    private final Collection<Document> collection;
    private final Filter filter;
    private final CollectionFindAndRerankOptions options;

    public DataApiCollectionFindAndRerankOp(Database db, Collection<Document> collection, Filter filter, CollectionFindAndRerankOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        CollectionFindAndRerankCursor<Document, Document> cursor = collection.findAndRerank(filter, options);
        return cursor.toList();
    }
}
