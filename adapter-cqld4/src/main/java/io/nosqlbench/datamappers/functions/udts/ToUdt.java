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

package io.nosqlbench.datamappers.functions.udts;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.internal.core.type.UserDefinedTypeBuilder;
import io.nosqlbench.converters.cql.cqlast.CqlType;
import io.nosqlbench.converters.cql.parser.CqlModelParser;

import java.util.function.LongFunction;

/**
 * map<A,B>
 * where:
 * A := (f1 text, f2 text, f3 int)
 * AND
 * B := (f1 text, f2 text)
 */
public class ToUdt implements LongFunction<UdtValue> {

    private final String spec;
    private final CqlType typeinfo;
    private final UserDefinedType udt;
    public ToUdt(String spec) {
        this.spec=spec;
        typeinfo = CqlModelParser.parseCqlType(spec);
        UserDefinedTypeBuilder builder = new UserDefinedTypeBuilder(typeinfo.getKeyspace(), typeinfo.getName());
        typeinfo.getFields().forEach((name,typedef) -> {
            DataType dataType = resolveDataType(typedef);
            builder.withField(name,dataType);
        });
        this.udt = builder.build();
    }

    private DataType resolveDataType(String typedef) {

        return null;
    }

    @Override
    public UdtValue apply(long value) {
        return null;
    }
}
