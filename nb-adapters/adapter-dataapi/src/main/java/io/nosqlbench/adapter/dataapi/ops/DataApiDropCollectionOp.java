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

import com.datastax.astra.client.Database;

public class DataApiDropCollectionOp extends DataApiBaseOp {
    private final String collectionName;
    public DataApiDropCollectionOp(Database db, String collectionName) {
        super(db);
        this.collectionName = collectionName;
    }

    @Override
    public Object apply(long value) {
        Boolean exists = db.collectionExists(collectionName);
        // TODO: we need to remove these from the ops when we can, because this hides additional ops which
        // should be surfaced in the test definition. Condition operations should be provided with clear views
        // at the workload template level
        if (exists) {
            db.dropCollection(collectionName);
        }
        return exists;
    }
}
