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

package io.nosqlbench.adapter.mongodb.dispensers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.adapter.mongodb.core.MongoSpace;
import io.nosqlbench.adapter.mongodb.core.MongodbDriverAdapter;
import io.nosqlbench.adapter.mongodb.ops.MongoDirectCommandOp;
import io.nosqlbench.adapter.mongodb.ops.MongoOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.bson.BsonDocument;

import java.util.function.LongFunction;

/**
 * https://www.mongodb.com/docs/manual/reference/command/update/#mongodb-dbcommand-dbcmd.update
 * https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/updateOne/
 */
public class MongoDbUpdateOpDispenser extends BaseOpDispenser<MongoOp<?>, MongoSpace> {
    private final LongFunction<MongoSpace> spaceF;
    private final LongFunction<MongoOp<?>> opF;
    private final LongFunction<String> collectionF;

    public MongoDbUpdateOpDispenser(MongodbDriverAdapter adapter, ParsedOp pop, LongFunction<String> collectionF) {
        super(adapter, pop, adapter.getSpaceFunc(pop));
        this.collectionF = collectionF;
        this.spaceF = adapter.getSpaceFunc(pop);
        this.opF = createOpF(pop);
    }

    private LongFunction<MongoOp<?>> createOpF(ParsedOp pop) {
        LongFunction<MongoClient> clientF = cycle -> spaceF.apply(cycle).getClient();
        LongFunction<MongoDatabase> docF = l -> clientF.apply(l).getDatabase(collectionF.apply(l));
        // TODO This needs to be completed for at least one working example of a specialized op form.
        return l -> new MongoDirectCommandOp(clientF.apply(l),docF.apply(l).getName(),new BsonDocument());
    }

    @Override
    public MongoOp<?> getOp(long cycle) {
        MongoOp<?> op = opF.apply(cycle);
        return op;
    }

}
