package io.nosqlbench.activitytype.cqld4.core;


import com.datastax.oss.driver.api.core.data.TupleValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import io.nosqlbench.activitytype.cqld4.codecsupport.UDTJavaType;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service(ActivityType.class)
public class CqlActivityType implements ActivityType<CqlActivity> {

    public String getName() {
        return "cqld4";
    }

    @Override
    public CqlActivity getActivity(ActivityDef activityDef) {

        Optional<String> yaml = activityDef.getParams().getOptionalString("yaml", "workload");

        // sanity check that we have a yaml parameter, which contains our statements and bindings
        if (yaml.isEmpty()) {
            throw new RuntimeException("Currently, the cql activity type requires yaml/workload activity parameter.");
        }

        // allow shortcut: yaml parameter provide the default alias name
        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            activityDef.getParams().set("alias",yaml.get());
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
        typemap.put("<udt>", UserDefinedType.class);
        typemap.put("uuid",UUID.class);
        typemap.put("timeuuid",UUID.class);
        typemap.put("varchar",String.class);
        typemap.put("varint", BigInteger.class);

        return typemap;
    }
}
