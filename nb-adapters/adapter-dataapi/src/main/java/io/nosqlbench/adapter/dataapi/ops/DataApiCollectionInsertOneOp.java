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

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.definition.documents.Document;

public class DataApiCollectionInsertOneOp extends DataApiBaseOp {
    private final Document doc;
    private final String collectionName;

    public DataApiCollectionInsertOneOp(Database db, String collectionName, Document doc) {
        super(db);
        this.collectionName = collectionName;
        this.doc = doc;
    }

    @Override
    public Object apply(long value) {
        return db.getCollection(collectionName).insertOne(doc);
    }
}
