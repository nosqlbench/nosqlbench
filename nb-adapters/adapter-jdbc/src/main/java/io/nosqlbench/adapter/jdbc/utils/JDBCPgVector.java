/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.jdbc.utils;

import io.nosqlbench.adapter.jdbc.exceptions.JDBCPgVectorException;
import org.postgresql.PGConnection;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class JDBCPgVector extends PGobject implements PGBinaryObject, Serializable, Cloneable {
    private float[] vec;

    /**
     * Constructor
     */
    public JDBCPgVector() {
        type = "vector";
    }

    /**
     * Constructor
     */
    public JDBCPgVector(float[] v) {
        this();
        vec = v;
    }

    /**
     * Constructor
     */
    public JDBCPgVector(String s) {
        this();
        setValue(s);
    }

    /**
     * Sets the value from a text representation of a vector
     */
    public void setValue(String s) throws JDBCPgVectorException {
        if (s == null) {
            vec = null;
        } else {
            String[] sp = s.substring(1, s.length() - 1).split(",");
            vec = new float[sp.length];
            try {
                for (int i = 0; i < sp.length; i++) {
                    vec[i] = Float.parseFloat(sp[i]);
                }
            }
            catch (NumberFormatException nfe) {
                throw new JDBCPgVectorException("the embedding value can't be converted to float");
            }
        }
    }

    /**
     * Returns the text representation of a vector
     */
    public String getValue() {
        if (vec == null) {
            return null;
        } else {
            return Arrays.toString(vec).replace(" ", "");
        }
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    public int lengthInBytes() {
        return vec == null ? 0 : 4 + vec.length * 4;
    }

    /**
     * Sets the value from a binary representation of a vector
     */
    public void setByteValue(byte[] value, int offset) throws JDBCPgVectorException {
        int dim = ByteConverter.int2(value, offset);

        int unused = ByteConverter.int2(value, offset + 2);
        if (unused != 0) {
            throw new JDBCPgVectorException("expected unused to be 0");
        }

        vec = new float[dim];
        for (int i = 0; i < dim; i++) {
            vec[i] = ByteConverter.float4(value, offset + 4 + i * 4);
        }
    }

    /**
     * Writes the binary representation of a vector
     */
    public void toBytes(byte[] bytes, int offset) {
        if (vec == null) {
            return;
        }

        // server will error on overflow due to unconsumed buffer
        // could set to Short.MAX_VALUE for friendlier error message
        ByteConverter.int2(bytes, offset, vec.length);
        ByteConverter.int2(bytes, offset + 2, 0);
        for (int i = 0; i < vec.length; i++) {
            ByteConverter.float4(bytes, offset + 4 + i * 4, vec[i]);
        }
    }

    /**
     * Returns an array
     */
    public float[] toArray() {
        return vec;
    }

    /**
     * Registers the vector type
     */
    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("vector", JDBCPgVector.class);
    }
}
