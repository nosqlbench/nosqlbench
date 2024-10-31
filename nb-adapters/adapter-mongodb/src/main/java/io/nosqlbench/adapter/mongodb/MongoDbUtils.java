package io.nosqlbench.adapter.mongodb;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbUtils {
    public static int[] getFieldFromResults(String field, Document document) {
        Document _cursor = document.get("cursor", Document.class);
        List<Document> _firstBatch = _cursor.getList("firstBatch", Document.class);
        List<String> keyStrings = new ArrayList<>();
        for (Document matchingVector : _firstBatch) {
            keyStrings.add(matchingVector.get("key",String.class));
        }
        return keyStrings.stream().mapToInt(Integer::parseInt).toArray();
    }
}
