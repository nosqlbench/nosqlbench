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
import com.datastax.astra.client.collections.exceptions.TooManyDocumentsToCountException;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;

import java.util.Optional;

public class DataApiCollectionCountDocumentsOp extends DataApiBaseOp {
    private final Collection<Document> collection;
    private final Filter filter;
    private final Optional<Integer> upperBound;

    private final int MAX_UPPER_BOUND = 1000;
    private final int SILENT_FAILED_COUNT_RESULT = -1;

    public DataApiCollectionCountDocumentsOp(Database db, Collection<Document> collection, Filter filter, Optional<Integer> upperBound) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.upperBound = upperBound;
    }

    @Override
    public Object apply(long value) {
        // upper-bound being optional enables a pattern that ignores the "moreData"
        // (getting rid of the 'upper bound' client-only feature). Still honored if passed (incl. throwing the error).
        int upperBoundToUse = MAX_UPPER_BOUND;
        boolean shouldThrowBoundExceeded = false;
        if (upperBound.isPresent()) {
            upperBoundToUse = upperBound.get();
            shouldThrowBoundExceeded = true;
        }
        try {
            return collection.countDocuments(filter, upperBoundToUse);
        } catch (TooManyDocumentsToCountException e) {
            if (shouldThrowBoundExceeded) {
                throw new RuntimeException(e);
            }
            return SILENT_FAILED_COUNT_RESULT;
        }
    }
}
