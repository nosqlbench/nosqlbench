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

package io.nosqlbench.adapter.venice.dispensers;

import io.nosqlbench.adapter.venice.VeniceSpace;
import io.nosqlbench.adapter.venice.ops.WriteOp;
import io.nosqlbench.adapter.venice.util.AvroUtils;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.avro.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class WriteOpDispenser extends VeniceBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("ReadSingleKeyOpDispenser");
    private final LongFunction<String> keyStrFunc;
    private final LongFunction<String> valueStrFunc;
    private final Schema keySchema;

    private final Schema valueSchema;

    private static final String KEY_OP_PARAM = "key";
    private static final String VALUE_OP_PARAM = "value";

    private static final String VALUE_SCHEMA_OP_PARAM = "valueSchema";
    private static final String KEY_SCHEMA_OP_PARAM = "keySchema";


    public WriteOpDispenser(DriverAdapter adapter,
                            ParsedOp op,
                            VeniceSpace s4jSpace) {
        super(adapter, op, s4jSpace);
        this.keyStrFunc = lookupMandtoryStrOpValueFunc(KEY_OP_PARAM);
        this.keySchema = lookupAvroSchema(KEY_SCHEMA_OP_PARAM);
        this.valueStrFunc = lookupMandtoryStrOpValueFunc(VALUE_OP_PARAM);
        this.valueSchema = lookupAvroSchema(VALUE_SCHEMA_OP_PARAM);
    }

    @Override
    public WriteOp apply(long cycle) {
        String key = keyStrFunc.apply(cycle);
        String value = valueStrFunc.apply(cycle);
        Object encodedKey = AvroUtils.encodeToAvro(keySchema, key);
        Object encodedValue = AvroUtils.encodeToAvro(valueSchema, value);
        return new WriteOp(
            veniceAdapterMetrics,
            veniceSpace,
            encodedKey,
            encodedValue);
    }
}
