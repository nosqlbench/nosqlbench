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

package io.nosqlbench.adapter.venice.dispensers;

import io.nosqlbench.adapter.venice.VeniceSpace;
import io.nosqlbench.adapter.venice.ops.ReadSingleKeyOp;
import io.nosqlbench.adapter.venice.util.AvroUtils;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.avro.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.function.LongFunction;
public class ReadSingleKeyOpDispenser extends VeniceBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("ReadSingleKeyOpDispenser");
    private final LongFunction<String> keyStrFunc;

    private final Schema keySchema;

    private static final String KEY_SCHEMA_OP_PARAM = "keySchema";

    private static final String KEY_OP_PARAM = "key";

    public ReadSingleKeyOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      VeniceSpace s4jSpace) {
        super(adapter, op, s4jSpace);
        this.keyStrFunc = lookupMandtoryStrOpValueFunc(KEY_OP_PARAM);
        this.keySchema = lookupAvroSchema(KEY_SCHEMA_OP_PARAM);
    }

    @Override
    public ReadSingleKeyOp apply(long cycle) {
        String key = keyStrFunc.apply(cycle);
        Object encodedKey = AvroUtils.encodeToAvro(keySchema, key);
        return new ReadSingleKeyOp(
            veniceAdapterMetrics,
            veniceSpace,
            encodedKey);
    }
}
