/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.mongodb.core;

import com.mongodb.ReadPreference;
import io.nosqlbench.adapter.mongodb.ops.MongoOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Map;
import java.util.function.LongFunction;

public class MongoOpDispenser extends BaseOpDispenser<Op> {
    private final LongFunction<MongoOp> opFunc;
    private final LongFunction<MongoOp> mongoOpF;

    public MongoOpDispenser(LongFunction<MongoSpace> ctxFunc, ParsedOp op) {
        super(op);
        opFunc = createOpFunc(ctxFunc, op);
        this.mongoOpF = createOpFunc(ctxFunc,op);
    }

    private LongFunction<MongoOp> createOpFunc(LongFunction<MongoSpace> ctxFunc, ParsedOp op) {

        LongFunction<String> rpstring = op.getAsOptionalFunction("readPreference")
            .orElseGet(() -> op.getAsOptionalFunction("read-preference")
                .orElse(l -> "primary"));
        LongFunction<ReadPreference> readPreferenceF = l -> ReadPreference.valueOf(rpstring.apply(l));

        LongFunction<?> payload = op.getAsRequiredFunction("stmt", Object.class);
        Object exampleValue = payload.apply(0);

        LongFunction<Bson> bsonFunc;
        if (exampleValue instanceof CharSequence cs) {
            bsonFunc = l -> Document.parse(payload.apply(l).toString());
        } else if ( exampleValue instanceof Map map) {
            bsonFunc = l -> new Document((Map<String,Object>)payload.apply(l));
        } else {
            throw new RuntimeException("You must provide a String or Map for your BSON payload.");
        }

        LongFunction<String> databaseNamerF = op.getAsRequiredFunction("database", String.class);

        return l-> new MongoOp(
            ctxFunc.apply(l).getClient(),
            databaseNamerF.apply(l),
            bsonFunc.apply(l)
        );
    }

    @Override
    public Op apply(long cycle) {
        return mongoOpF.apply(cycle);
    }
}
