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

package io.nosqlbench.cqlgen.core;

import io.nosqlbench.cqlgen.model.CqlColumnBase;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public enum CGLiteralFormat {
    TEXT(v -> "\""+v+"\""),
    ASCII(v -> "\""+v+"\""),
    VARCHAR(v -> "\""+v+"\""),
    TINYINT,
    SMALLINT,
    INT,
    BIGINT,
    COUNTER,
    BLOB,
    BOOLEAN,
    DECIMAL,
    DOUBLE,
    FLOAT,
    UNKNOWN,
    TIMESTAMP,
    TIMEUUID,
    VARINT;

    private final Function<String, String> literalFormat;
    CGLiteralFormat() {
        this.literalFormat=v->v;
    }
    CGLiteralFormat(Function<String,String> modifier) {
        this.literalFormat = modifier;
    }

    public static String formatBindType(CqlColumnBase cd, String valueref) {
        return CGLiteralFormat.valueOfCqlType(cd.getTrimmedTypedef()).orElse(CGLiteralFormat.UNKNOWN).format(valueref);
    }

    public String format(String value) {
        return this.literalFormat.apply(value);
    }

    public static Optional<CGLiteralFormat> valueOfCqlType(String typename) {
        for (CGLiteralFormat value : CGLiteralFormat.values()) {
            if (typename.toUpperCase(Locale.ROOT).equals(value.toString().toUpperCase(Locale.ROOT))) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
