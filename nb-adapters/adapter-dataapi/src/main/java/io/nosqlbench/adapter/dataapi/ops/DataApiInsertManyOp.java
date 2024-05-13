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

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertManyOptions;

import java.util.List;

public class DataApiInsertManyOp extends DataApiBaseOp {
    private final List<? extends Document> documents;
    private final String collectionName;
    private final InsertManyOptions options;


    public DataApiInsertManyOp(Database db, String collectionName, List<? extends Document> documents, InsertManyOptions options) {
        super(db);
        this.collectionName = collectionName;
        this.documents = documents;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return db.getCollection(collectionName).insertMany(documents, options);
    }
}
