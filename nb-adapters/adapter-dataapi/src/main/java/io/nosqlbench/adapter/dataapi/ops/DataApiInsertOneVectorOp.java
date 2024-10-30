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
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertOneResult;

public class DataApiInsertOneVectorOp extends DataApiBaseOp {
    private final Document doc;
    private final String collectionName;
    private float[] vector;

    public DataApiInsertOneVectorOp(Database db, String collectionName, Document doc, float[] vector) {
        super(db);
        this.collectionName = collectionName;
        this.doc = doc;
        this.vector = vector;
    }

    @Override
    public Object apply(long value) {
        Collection<Document> collection = db.getCollection(collectionName);
        InsertOneResult result = collection.insertOne(doc, vector);
        return result;
    }
}
