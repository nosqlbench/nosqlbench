/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.mongodb.ops;

import com.mongodb.client.MongoClient;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDirectCommandOp implements CycleOp<Document> {

    private final MongoClient client;
    private final Bson rqBson;
    private final String database;
    private int resultSize;

    // https://docs.mongodb.com/manual/reference/method/db.runCommand/#command-response
    public MongoDirectCommandOp(MongoClient client, String database, Bson rqBson) {
        this.client = client;
        this.database = database;
        this.rqBson = rqBson;
    }

    @Override
    public Document apply(long value) {
        Document document = client.getDatabase(database).runCommand(rqBson);
        int okcode =0;

        Object ok = document.get("ok");
        if (ok instanceof Number n) {
            okcode = n.intValue();
        }
        if (okcode!=1) {
            throw new MongoOpFailedException(rqBson, okcode, document);
        }
        Object nObj=document.get("n");
        if (nObj instanceof Number n) {
            this.resultSize = n.intValue();
        }
        return document;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
