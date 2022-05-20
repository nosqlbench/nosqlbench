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


import com.datastax.driver.core.*;
import io.nosqlbench.engine.api.activityconfig.ParsedStmtOp;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CQLBindHelper {

    private final static Pattern stmtToken = Pattern.compile("\\?(\\w+[-_\\d\\w]*)|\\{(\\w+[-_\\d\\w.]*)}");
    public static Statement rebindUnappliedStatement(Statement statement, ColumnDefinitions defs, Row row) {

        for (ColumnDefinitions.Definition def : defs) {
            String name = def.getName();
            def.getType();
            if (!name.equals("[applied]")) {
                DataType.Name typeName = def.getType().getName();
                switch (typeName) {
                    case ASCII: // ASCII(1, String.class)
                        ((BoundStatement) statement).bind().setString(name, row.getString(name));
                    case VARCHAR: // VARCHAR(13, String.class)
                        ((BoundStatement) statement).bind().setString(name, row.getString(name));
                    case TEXT: //  TEXT(10, String.class)
                        ((BoundStatement) statement).bind().setString(name, row.getString(name));
                    case BIGINT: // BIGINT(2, Long.class)
                        ((BoundStatement) statement).bind().setLong(name, row.getLong(name));
                    case COUNTER: // COUNTER(5, Long.class)
                        ((BoundStatement) statement).bind().setLong(name, row.getLong(name));
                    case BLOB: // BLOB(3, ByteBuffer.class)
                        ((BoundStatement) statement).bind().setBytes(name, row.getBytes(name));
                    case CUSTOM: // CUSTOM(0, ByteBuffer.class)
                        throw new RuntimeException("The diagnostic binder does not understand custom types yet.");
                    case BOOLEAN: // BOOLEAN(4, Boolean.class)
                        ((BoundStatement) statement).bind().setBool(name, row.getBool(name));
                    case DECIMAL: // DECIMAL(6, BigDecimal.class)
                        ((BoundStatement) statement).bind().setDecimal(name, row.getDecimal(name));
                    case DOUBLE: // DOUBLE(7, Double.class)
                        ((BoundStatement) statement).bind().setDouble(name, row.getDouble(name));
                    case FLOAT: // FLOAT(8, Float.class)
                        ((BoundStatement) statement).bind().setFloat(name, row.getFloat(name));
                    case INET: // INET(16, InetAddress.class)
                        ((BoundStatement) statement).bind().setInet(name, row.getInet(name));
                    case INT: // INT(9, Integer.class)
                        ((BoundStatement) statement).bind().setInt(name, row.getInt(name));
                    case TIMESTAMP: // TIMESTAMP(11, Date.class)
                        ((BoundStatement) statement).bind().setTimestamp(name, row.getTimestamp(name));
                    case UUID: // UUID(12, UUID.class)
                        ((BoundStatement) statement).bind().setUUID(name, row.getUUID(name));
                    case TIMEUUID: // TIMEUUID(15, UUID.class)
                        ((BoundStatement) statement).bind().setUUID(name, row.getUUID(name));
                    case VARINT: // VARINT(14, BigInteger.class)
                        ((BoundStatement) statement).bind().setInt(name, row.getInt(name));
                    case UDT: // UDT(48, UDTValue.class)
                        ((BoundStatement) statement).bind().setUDTValue(name, row.getUDTValue(name));
                    case TUPLE: // TUPLE(49, TupleValue.class)
                        ((BoundStatement) statement).bind().setTupleValue(name, row.getTupleValue(name));
                    case SMALLINT:
                        ((BoundStatement) statement).bind().setInt(name, row.getInt(name));
                    case TINYINT:
                        ((BoundStatement) statement).bind().setInt(name, row.getInt(name));
                    case DATE:
                        ((BoundStatement) statement).bind().setDate(name, row.getDate(name));
                    case TIME:
                        ((BoundStatement) statement).bind().setTime(name, row.getTime(name));
                    default:
                        throw new RuntimeException("Unrecognized type:" + typeName);
                }
            }
        }
        return statement;
    }

    public static BoundStatement bindStatement(Statement statement, String name, Object value, DataType.Name typeName) {
        switch (typeName) {
            case TEXT: //  TEXT(10, String.class)
            case ASCII: // ASCII(1, String.class)
                return ((BoundStatement) statement).bind().setString(name, (String) value);
            case VARCHAR: // VARCHAR(13, String.class)
            case BIGINT: // BIGINT(2, Long.class)
            case COUNTER: // COUNTER(5, Long.class)
                return ((BoundStatement) statement).bind().setLong(name, (long) value);
            case BLOB: // BLOB(3, ByteBuffer.class)
                return ((BoundStatement) statement).bind().setBytes(name, (ByteBuffer) value);
            case CUSTOM: // CUSTOM(0, ByteBuffer.class)
                throw new RuntimeException("The diagnostic binder does not understand custom types yet.");
            case BOOLEAN: // BOOLEAN(4, Boolean.class)
                return ((BoundStatement) statement).bind().setBool(name, (boolean) value);
            case DECIMAL: // DECIMAL(6, BigDecimal.class)
                return ((BoundStatement) statement).bind().setDecimal(name, (BigDecimal) value);
            case DOUBLE: // DOUBLE(7, Double.class)
                return ((BoundStatement) statement).bind().setDouble(name, (double) value);
            case FLOAT: // FLOAT(8, Float.class)
                return ((BoundStatement) statement).bind().setFloat(name, (float) value);
            case INET: // INET(16, InetAddress.class)
                return ((BoundStatement) statement).bind().setInet(name, (InetAddress) value);
            case TIMESTAMP: // TIMESTAMP(11, Date.class)
                return ((BoundStatement) statement).bind().setTimestamp(name, (Date) value);
            case UUID: // UUID(12, UUID.class)
            case TIMEUUID: // TIMEUUID(15, UUID.class)
                return ((BoundStatement) statement).bind().setUUID(name, (UUID) value);
            case UDT: // UDT(48, UDTValue.class)
                 return ((BoundStatement) statement).bind().setUDTValue(name, (UDTValue) value);
            case TUPLE: // TUPLE(49, TupleValue.class
                return ((BoundStatement) statement).bind().setTupleValue(name, (TupleValue) value);
            case VARINT: // VARINT(14, BigInteger.class)
            case SMALLINT:
            case TINYINT:
            case INT: // INT(9, Integer.class)
                return ((BoundStatement) statement).bind().setInt(name, (int) value);
            case DATE:
                return ((BoundStatement) statement).bind().setDate(name, (LocalDate) value);
            case TIME:
                return ((BoundStatement) statement).bind().setTime(name, (long) value);
            default:
                throw new RuntimeException("Unrecognized type:" + typeName);
        }
    }

    public static Map<String, String> parseAndGetSpecificBindings(OpTemplate opDef, ParsedStmtOp parsed) {
        List<String> spans = new ArrayList<>();

        String statement = opDef.getStmt().orElseThrow();

        Set<String> extraBindings = new HashSet<>();
        extraBindings.addAll(opDef.getBindings().keySet());
        Map<String, String> specificBindings = new LinkedHashMap<>();

        Matcher m = stmtToken.matcher(statement);
        int lastMatch = 0;
        String remainder = "";
        while (m.find(lastMatch)) {
            String pre = statement.substring(lastMatch, m.start());

            String form1 = m.group(1);
            String form2 = m.group(2);
            String tokenName = (form1 != null && !form1.isEmpty()) ? form1 : form2;
            lastMatch = m.end();
            spans.add(pre);

            if (extraBindings.contains(tokenName)) {
                if (specificBindings.get(tokenName) != null){
                    String postfix = UUID.randomUUID().toString();
                    specificBindings.put(tokenName + postfix, opDef.getBindings().get(tokenName));
                }else {
                    specificBindings.put(tokenName, opDef.getBindings().get(tokenName));
                }
            }
        }
        return specificBindings;
    }
}
