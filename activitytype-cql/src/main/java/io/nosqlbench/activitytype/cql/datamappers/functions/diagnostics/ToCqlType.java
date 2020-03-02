package io.nosqlbench.activitytype.cql.datamappers.functions.diagnostics;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.UDTValue;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Shows the compatible CQL type most associated with the incoming Java type.
 */
@ThreadSafeMapper
public class ToCqlType implements Function<Object, String> {

    private final static Map<String, String> typemap = new HashMap<String, String>() {{
        put("a", "b");
        put(String.class.getCanonicalName(), DataType.text().getName().toString() +
                " or " + DataType.ascii().getName().toString() +
                " or " + DataType.varchar().getName().toString());
        put(Long.class.getCanonicalName(), DataType.bigint().getName().toString() +
                " or " + DataType.time().getName().toString() +
                " or " + DataType.counter().getName().toString());
        put(long.class.getCanonicalName(), DataType.bigint().getName().toString() +
                " or " + DataType.counter().getName().toString());
        put(ByteBuffer.class.getCanonicalName(), DataType.blob().getName().toString() +
                ",CUSTOM");
        put(Boolean.class.getCanonicalName(), DataType.cboolean().getName().toString());
        put(boolean.class.getCanonicalName(), DataType.cboolean().getName().toString());
        put(BigDecimal.class.getCanonicalName(), DataType.decimal().getName().toString());
        put(Double.class.getCanonicalName(),DataType.cdouble().getName().toString());
        put(double.class.getCanonicalName(),DataType.cdouble().getName().toString());
        put(Float.class.getCanonicalName(), DataType.cfloat().getName().toString());
        put(float.class.getCanonicalName(), DataType.cfloat().getName().toString());
        put(InetAddress.class.getCanonicalName(), DataType.inet().getName().toString());
        put(Integer.class.getCanonicalName(),DataType.cint().getName().toString());
        put(int.class.getCanonicalName(),DataType.cint().getName().toString());
        put(java.util.Date.class.getCanonicalName(),DataType.timestamp().getName().toString());
        put(java.util.UUID.class.getCanonicalName(),DataType.timeuuid().getName().toString()+" or "+DataType.uuid().getName().toString());
        put(BigInteger.class.getCanonicalName(),DataType.varint().getName().toString());
        put(Short.class.getCanonicalName(), DataType.smallint().getName().toString());
        put(short.class.getCanonicalName(), DataType.smallint().getName().toString());
        put(Byte.class.getCanonicalName(), DataType.tinyint().getName().toString());
        put(byte.class.getCanonicalName(), DataType.tinyint().getName().toString());
        put(LocalDate.class.getCanonicalName(), DataType.date().getName().toString());
        put(UDTValue.class.getCanonicalName(), "<udt>");
        put(TupleValue.class.getCanonicalName(),"<tuple>");
    }};

    private final ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(StringBuilder::new);

    @Override
    public String apply(Object o) {
        String canonicalName = o.getClass().getCanonicalName();
        String cqlTypeName = typemap.get(canonicalName);
        StringBuilder sb = tlsb.get();
        sb.setLength(0);
        if (cqlTypeName!=null) {
            return sb.append(canonicalName).append(" -> ").append(cqlTypeName).toString();
        }
        return findAlternates(o,canonicalName);
    }

    private String findAlternates(Object o, String canonicalName) {
        StringBuilder sb = tlsb.get();

        if (List.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");

            if (((List)o).size()>0) {
                Object o1 = ((List) o).get(0);
                String elementType = o1.getClass().getCanonicalName();
                sb.append(elementType).append("> -> List<");
                sb.append(typemap.getOrDefault(elementType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?> -> List<?>").toString();
        }
        if (Map.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");
            if (((Map)o).size()>0) {
                Map.Entry next = (Map.Entry) ((Map) o).entrySet().iterator().next();
                String keyType = next.getKey().getClass().getCanonicalName();
                String valType = next.getValue().getClass().getCanonicalName();
                sb.append(keyType).append(",").append(valType).append("> -> Map<");
                sb.append(typemap.getOrDefault(keyType,"UNKNOWN")).append(",");
                sb.append(typemap.getOrDefault(valType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?,?> -> Map<?,?>").toString();
        }
        if (Set.class.isAssignableFrom(o.getClass())) {
            sb.append(canonicalName).append("<");
            if (((Set)o).size()>0) {
                Object o1=((Set)o).iterator().next();
                String elementType = o1.getClass().getCanonicalName();
                sb.append(elementType).append("> -> Set<");
                sb.append(typemap.getOrDefault(elementType,"UNKNOWN")).append(">");
                return sb.toString();
            }
            return sb.append("?> -> Set<?>").toString();
        }
        return typemap.getOrDefault(o.getClass().getSuperclass().getCanonicalName(), "UNKNOWN");
    }
}
