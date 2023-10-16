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

package io.nosqlbench.engine.extensions.vectormath;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PgvecUtils {

    public static long[] sqlResultSetFieldsToLongArray(String fieldName, List<ResultSet> resultSets) {
        return resultSets.stream().filter(r -> {
            try {
                return ((r!=null) && !r.isClosed());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).mapToLong(r -> {
            try {
                return r.getLong(fieldName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).toArray();
    }

    public static String[] sqlResultSetFieldsToStringArray(String fieldName, List<ResultSet> resultSets) {
        return resultSets.stream().filter(r -> {
            try {
                return ((r!=null) && !r.isClosed());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).map(r -> {
            try {
                return r.getString(fieldName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(String[]::new);
    }

    public static int[] sqlResultSetListToIntArray(String fieldName, List<ResultSet> resultSets) {
        return resultSets.stream().filter(r -> {
            try {
                return ((r!=null) && !r.isClosed());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).mapToInt(r -> {
            try {
                return r.getInt(fieldName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).toArray();
    }

    public static int[] sqlStringColumnToIntArray(String fieldName, List<ResultSet> resultSets) {
        return resultSets.stream().filter(r -> {
            try {
                return ((r!=null) && !r.isClosed());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).mapToInt(r -> {
            try {
                return Integer.parseInt(Objects.requireNonNull(r.getString(fieldName)));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).toArray();
    }
}
