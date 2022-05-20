package io.nosqlbench.adapter.cqld4;

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


import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import io.nosqlbench.nb.api.errors.BasicError;

/**
 * @see <a href="https://github.com/apache/cassandra/blob/bf96367f4d55692017e144980cf17963e31df127/doc/native_protocol_v5.spec">native protocol v5</a>
 *
 */
public class LWTRebinder {

    public static BoundStatement rebindUnappliedStatement(Statement<?> statement, Row row) {

        BoundStatement bs;
        if (statement instanceof BoundStatement) {
            bs = (BoundStatement)statement;
        } else {
            throw new BasicError("An LWT operation was being rebound to new values, but this is not possible yet " +
                "unless it is a bound statement as in prepared statements.");
        }

        for (ColumnDefinition def : row.getColumnDefinitions()) {
            String name = def.getName().toString();
            if (!name.equals("[applied]")) {
                DataType typeName = def.getType();

                int code = typeName.getProtocolCode();
                switch (code) {
                    case ProtocolConstants.DataType.ASCII:
                    case ProtocolConstants.DataType.VARCHAR:
                        bs=bs.setString(name,row.getString(name));
                        break;
                    case ProtocolConstants.DataType.CUSTOM:
                        throw new BasicError("Rebinding custom datatypes is not supported. (Custom DataType)");
                    case ProtocolConstants.DataType.BIGINT:
                    case ProtocolConstants.DataType.COUNTER:
                        bs = bs.setLong(name, row.getLong(name));
                        break;
                    case ProtocolConstants.DataType.BLOB:
                        bs = bs.setByteBuffer(name, row.getByteBuffer(name));
                        break;
                    case ProtocolConstants.DataType.BOOLEAN:
                        bs = bs.setBoolean(name,row.getBoolean(name));
                        break;
                    case ProtocolConstants.DataType.DECIMAL:
                        bs = bs.setBigDecimal(name,row.getBigDecimal(name));
                        break;
                    case ProtocolConstants.DataType.DOUBLE:
                        bs = bs.setDouble(name,row.getDouble(name));
                        break;
                    case ProtocolConstants.DataType.FLOAT:
                        bs=bs.setFloat(name,row.getFloat(name));
                        break;
                    case ProtocolConstants.DataType.INT:
                    case ProtocolConstants.DataType.VARINT:
                    case ProtocolConstants.DataType.SMALLINT:
                    case ProtocolConstants.DataType.TINYINT:
                        bs=bs.setInt(name,row.getInt(name));
                        break;
                    case ProtocolConstants.DataType.TIMESTAMP:
                        break;
                    case ProtocolConstants.DataType.UUID:
                    case ProtocolConstants.DataType.TIMEUUID:
                        bs=bs.setUuid(name,row.getUuid(name));
                        break;
                    case ProtocolConstants.DataType.INET:
                        bs=bs.setInetAddress(name,row.getInetAddress(name));
                        break;

                    case ProtocolConstants.DataType.DATE:
                        bs=bs.setLocalDate(name,row.getLocalDate(name));
                        break;
                    case ProtocolConstants.DataType.TIME:
                        bs = bs.setLocalTime(name,bs.getLocalTime(name));
                        break;
                    case ProtocolConstants.DataType.DURATION:
                        bs = bs.setCqlDuration(name,bs.getCqlDuration(name));
                        break;
                    case ProtocolConstants.DataType.LIST:
                        bs = bs.setList(name,bs.getList(name,Object.class),Object.class);
                        break;
                    case ProtocolConstants.DataType.MAP:
                        bs = bs.setMap(name,bs.getMap(name,Object.class,Object.class),Object.class,Object.class);
                        break;
                    case ProtocolConstants.DataType.SET:
                        bs = bs.setSet(name,bs.getSet(name,Object.class),Object.class);
                        break;
                    case ProtocolConstants.DataType.UDT:
                        bs = bs.setUdtValue(name,bs.getUdtValue(name));
                        break;
                    case ProtocolConstants.DataType.TUPLE:
                        bs = bs.setTupleValue(name,bs.getTupleValue(name));
                        break;
                    default:
                        throw new RuntimeException("Unrecognized type:" + typeName);
                }
            }
        }
        return bs;
    }

}
