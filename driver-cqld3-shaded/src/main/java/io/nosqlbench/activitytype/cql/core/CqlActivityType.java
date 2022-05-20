package io.nosqlbench.activitytype.cql.core;

/*
 * Copyright (c) 2022 nosqlbench
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



import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.UDTValue;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

@Service(value = ActivityType.class, selector = "cqld3")
public class CqlActivityType implements ActivityType<CqlActivity> {

    @Override
    public CqlActivity getActivity(ActivityDef activityDef) {

        Optional<String> yaml = activityDef.getParams().getOptionalString("yaml", "workload");

        // sanity check that we have a yaml parameter, which contains our statements and bindings
        if (yaml.isEmpty()) {
            throw new RuntimeException("Currently, the cql activity type requires yaml/workload activity parameter.");
        }

        return new CqlActivity(activityDef);
    }

    /**
     * Returns the per-activity level dispenser. The ActionDispenser can then dispense
     * per-thread actions within the activity instance.
     * @param activity The activity instance which will parameterize this action
     */
    @Override
    public ActionDispenser getActionDispenser(CqlActivity activity) {
        return new CqlActionDispenser(activity);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        Map<String,Class<?>> typemap = new LinkedHashMap<>();
        typemap.put("ascii",String.class);
        typemap.put("bigint",long.class);
        typemap.put("blob", ByteBuffer.class);
        typemap.put("boolean",boolean.class);
        typemap.put("counter",long.class);
        typemap.put("date", LocalDate.class);
        typemap.put("decimal", BigDecimal.class);
        typemap.put("double",double.class);
//        typemap.put("duration",CqlDuration.class);
        typemap.put("float",float.class);
        typemap.put("inet", InetAddress.class);
        typemap.put("int",int.class);
        typemap.put("list", List.class);
        typemap.put("map",Map.class);
        typemap.put("set", Set.class);
        typemap.put("smallint",short.class);
        typemap.put("text",String.class);
        typemap.put("time", LocalTime.class);
        typemap.put("timestamp", Instant.class);
        typemap.put("tinyint",byte.class);
        typemap.put("tuple", TupleValue.class);
        typemap.put("<udt>", UDTValue.class);
        typemap.put("uuid",UUID.class);
        typemap.put("timeuuid",UUID.class);
        typemap.put("varchar",String.class);
        typemap.put("varint", BigInteger.class);

        return typemap;
    }
}
