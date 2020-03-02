package io.nosqlbench.activitytype.cqlverify;

import com.datastax.driver.core.*;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.RowCycleOperator;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.errorhandling.exceptions.RowVerificationException;
import io.nosqlbench.virtdata.api.Bindings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>RowDifferencer uses the metadata associated with a row to access and compare
 * {@link Row} values in a type-specific way.
 * </p>
 */
public class RowDifferencer implements RowCycleOperator {

    private final StringBuilder logbuffer = new StringBuilder();
    private final Map<String, Object> refMap = new HashMap<>();
    private final DiffType difftype;
    private final Bindings bindings;
    private final VerificationMetrics metrics;


    private RowDifferencer(VerificationMetrics metrics, Bindings bindings, DiffType diffType) {
        this.metrics = metrics;
        this.bindings = bindings;
        this.difftype = diffType;
    }

    /**
     * see {@link DataType}
     *
     * @param typeName  The DataType.Name of the field in question
     * @param row       The row to read the field value from
     * @param fieldName The field name to read
     * @param genValue  the generated value to compare against
     * @return true, if the value is equal
     */
    private static boolean isEqual(DataType.Name typeName, Row row, String fieldName, Object genValue) {
        switch (typeName) {
            case ASCII: // ASCII(1, String.class)
            case VARCHAR: // VARCHAR(13, String.class)
            case TEXT: //  TEXT(10, String.class)
                String textValue = row.getString(fieldName);
                return textValue.equals(genValue);
            case BIGINT: // BIGINT(2, Long.class)
            case COUNTER: // COUNTER(5, Long.class)
                long longValue = row.getLong(fieldName);
                return longValue == (long) genValue;
            case BLOB: // BLOB(3, ByteBuffer.class)
                // TODO: How do we test this one?
            case CUSTOM: // CUSTOM(0, ByteBuffer.class)
                ByteBuffer blobValue = row.getBytes(fieldName);
                return blobValue.equals(genValue);
            case BOOLEAN: // BOOLEAN(4, Boolean.class)
                boolean boolValue = row.getBool(fieldName);
                return boolValue == (boolean) genValue;
            case DECIMAL: // DECIMAL(6, BigDecimal.class)
                BigDecimal bigDecimalValue = row.getDecimal(fieldName);
                return bigDecimalValue.equals(genValue);
            case DOUBLE: // DOUBLE(7, Double.class)
                double doubleValue = row.getDouble(fieldName);
                return doubleValue == (double) genValue;
            case FLOAT: // FLOAT(8, Float.class)
                float floatValue = row.getFloat(fieldName);
                return floatValue == (float) genValue;
            case INET: // INET(16, InetAddress.class)
                InetAddress inetAddressValue = row.getInet(fieldName);
                return inetAddressValue.equals(genValue);
            case INT: // INT(9, Integer.class)
                int intValue = row.getInt(fieldName);
                return intValue == (int) genValue;
            case TIMESTAMP: // TIMESTAMP(11, Date.class)
                Date timestamp = row.getTimestamp(fieldName);
                return timestamp.equals(genValue);
            case UUID: // UUID(12, UUID.class)
            case TIMEUUID: // TIMEUUID(15, UUID.class)
                UUID uuidValue = row.getUUID(fieldName);
                return uuidValue.equals(genValue);
            case VARINT: // VARINT(14, BigInteger.class)
                BigInteger bigIntValue = row.getVarint(fieldName);
                return bigIntValue.equals(genValue);
            case LIST: // LIST(32, List.class)
                // TODO: How do we make getCollection methods work with non-String CQL types?
                List<?> list = row.getList(fieldName, String.class);
                return list.equals(genValue);
            case SET: // SET(34, Set.class)
                Set<?> set = row.getSet(fieldName, String.class);
                return set.equals(genValue);
            case MAP: // MAP(33, Map.class)
                Map<?, ?> map = row.getMap(fieldName, String.class, String.class);
                return map.equals(genValue);
            case UDT: // UDT(48, UDTValue.class)
                UDTValue udtValue = row.getUDTValue(fieldName);
                return udtValue.equals(genValue);
            case TUPLE: // TUPLE(49, TupleValue.class)
                TupleValue tupleValue = row.getTupleValue(fieldName);
                return tupleValue.equals(genValue);
            case SMALLINT:
                short shortVal = row.getShort(fieldName);
                return shortVal == (Short) genValue;
            case TINYINT:
                byte byteValue = row.getByte(fieldName);
                return byteValue == (byte) genValue;
            case DATE:
                LocalDate dateValue = row.getDate(fieldName);
                return dateValue.equals(genValue);
            case TIME:
                long timeValue = row.getTime(fieldName);
                return timeValue == (long) genValue;
            default:
                throw new RuntimeException("Unrecognized type:" + typeName);
        }
    }

    private static String prettyPrint(DataType.Name typeName, Row row, String fieldName) {
        switch (typeName) {
            case ASCII: // ASCII(1, String.class)
            case VARCHAR: // VARCHAR(13, String.class)
            case TEXT: //  TEXT(10, String.class)
                return row.getString(fieldName);
            case BIGINT: // BIGINT(2, Long.class)
            case COUNTER: // COUNTER(5, Long.class)
                long counterValue = row.getLong(fieldName);
                return String.valueOf(counterValue);
            case BLOB: // BLOB(3, ByteBuffer.class)
            case CUSTOM: // CUSTOM(0, ByteBuffer.class)
                ByteBuffer blobValue = row.getBytes(fieldName);
                return String.valueOf(blobValue);
            case BOOLEAN: // BOOLEAN(4, Boolean.class)
                boolean boolValue = row.getBool(fieldName);
                return String.valueOf(boolValue);
            case DECIMAL: // DECIMAL(6, BigDecimal.class)
                BigDecimal bigDecimalValue = row.getDecimal(fieldName);
                return String.valueOf(bigDecimalValue);
            case DOUBLE: // DOUBLE(7, Double.class)
                double doubleValue = row.getDouble(fieldName);
                return String.valueOf(doubleValue);
            case FLOAT: // FLOAT(8, Float.class)
                float floatValue = row.getFloat(fieldName);
                return String.valueOf(floatValue);
            case INET: // INET(16, InetAddress.class)
                InetAddress inetAddressValue = row.getInet(fieldName);
                return String.valueOf(inetAddressValue);
            case INT: // INT(9, Integer.class)
                int intValue = row.getInt(fieldName);
                return String.valueOf(intValue);
            case TIMESTAMP: // TIMESTAMP(11, Date.class)
                Date timestamp = row.getTimestamp(fieldName);
                return String.valueOf(timestamp);
            case UUID: // UUID(12, UUID.class)
            case TIMEUUID: // TIMEUUID(15, UUID.class)
                UUID uuidValue = row.getUUID(fieldName);
                return String.valueOf(uuidValue);
            case VARINT: // VARINT(14, BigInteger.class)
                BigInteger bigIntValue = row.getVarint(fieldName);
                return String.valueOf(bigIntValue);
            case LIST: // LIST(32, List.class)
                List<?> list = row.getList(fieldName, String.class);
                return String.valueOf(list);
            case SET: // SET(34, Set.class)
                Set<?> set = row.getSet(fieldName, String.class);
                return String.valueOf(set);
            case MAP: // MAP(33, Map.class)
                Map<?, ?> map = row.getMap(fieldName, String.class, String.class);
                return String.valueOf(map);
            case UDT: // UDT(48, UDTValue.class)
                UDTValue udtValue = row.getUDTValue(fieldName);
                return String.valueOf(udtValue);
            case TUPLE: // TUPLE(49, TupleValue.class)
                TupleValue tupleValue = row.getTupleValue(fieldName);
                return String.valueOf(tupleValue);
            case SMALLINT:
                short val = row.getShort(fieldName);
                return String.valueOf(val);
            case TINYINT:
                byte byteValue = row.getByte(fieldName);
                return String.valueOf(byteValue);
            case DATE:
                LocalDate dateValue = row.getDate(fieldName);
                return String.valueOf(dateValue);
            case TIME:
                long timeValue = row.getTime(fieldName);
                return String.valueOf(timeValue);
            default:
                throw new RuntimeException("Type not recognized:" + typeName);
        }
    }

    /**
     * Compare the values of the row with the values generated.
     * <p>
     * Specifically,
     * <ol>
     * <li>Ensure the same number of fields.</li>
     * <li>Ensure the same pair-wise field names.</li>
     * <li>Ensure that each pair of same-named fields has the same data type.</li>
     * <li>Ensure that the value of each pair of fields is equal according to the equals
     * operator for the respective type.</li>
     * </ol>
     * *
     *
     * @param row          A row of data
     * @param referenceMap a map of values
     * @return a count of differences between the row and the reference values
     */
    private int compare(Row row, Map<String, Object> referenceMap) {
        int diff = 0;
        ColumnDefinitions cdefs = row.getColumnDefinitions();

        logbuffer.setLength(0);

        if (difftype.is(DiffType.reffields)) {
            List<String> missingRowFields = referenceMap.keySet().stream()
                    .filter(gk -> !cdefs.contains(gk))
                    .collect(Collectors.toList());
            if (missingRowFields.size() > 0) {
                diff += missingRowFields.size();

                logbuffer.append("\nexpected fields '");
                logbuffer.append(String.join("','", missingRowFields));
                logbuffer.append("' not in row.");
            }
        }

        if (difftype.is(DiffType.rowfields)) {
            List<String> missingRefFields = cdefs.asList().stream()
                    .map(ColumnDefinitions.Definition::getName)
                    .filter(k -> !referenceMap.containsKey(k))
                    .collect(Collectors.toList());
            if (missingRefFields.size() > 0) {
                diff += missingRefFields.size();

                logbuffer.append("\nexpected fields '");
                logbuffer.append(String.join("','", missingRefFields));
                logbuffer.append("' not in reference data: " + referenceMap);
            }
        }

        if (difftype.is(DiffType.values)) {
            for (ColumnDefinitions.Definition definition : row.getColumnDefinitions()) {
                String name = definition.getName();
                if (referenceMap.containsKey(name)) {
                    DataType type = definition.getType();
                    if (!isEqual(type.getName(), row, name, referenceMap.get(name))) {
                        logbuffer.append("\nvalue differs for '").append(name).append("' ");
                        logbuffer.append("expected:'").append(referenceMap.get(name).toString()).append("'");
                        logbuffer.append(" actual:'").append(prettyPrint(type.getName(), row, name)).append("'");
                        diff++;
                        metrics.unverifiedValuesCounter.inc();
                    } else {
                        metrics.verifiedValuesCounter.inc();
                    }
                }
            }
        }
        if (diff == 0) {
            metrics.verifiedRowsCounter.inc();
        } else {
            metrics.unverifiedRowsCounter.inc();
        }
        return diff;
    }

    /**
     * Get the most recent detail log recorded by this thread.
     *
     * @return a logbuffer string, with one entry per line
     */
    public String getDetail() {
        return this.logbuffer.toString();
    }

    @Override
    public int apply(Row row, long cycle) {
        refMap.clear();
        bindings.setMap(refMap, cycle);
        int diffs = compare(row, refMap);
        if (diffs > 0) {
            HashMap<String, Object> mapcopy = new HashMap<>();
            mapcopy.putAll(refMap);
            throw new RowVerificationException(cycle, row, mapcopy, getDetail());
        } else {
            return 0;
        }
    }

    public static class ThreadLocalWrapper implements RowCycleOperator {

        private final VerificationMetrics metrics;
        private final Bindings bindings;
        private final DiffType diffType;
        private ThreadLocal<RowDifferencer> tl;

        public ThreadLocalWrapper(VerificationMetrics metrics, Bindings bindings, DiffType diffType) {
            this.metrics = metrics;
            this.bindings = bindings;
            this.diffType = diffType;
            tl = ThreadLocal.withInitial(() -> new RowDifferencer(metrics,bindings,diffType));
        }

        @Override
        public int apply(Row row, long cycle) {
            return tl.get().apply(row,cycle);
        }
    }
}
