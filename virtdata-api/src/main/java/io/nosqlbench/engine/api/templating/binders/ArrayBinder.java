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

package io.nosqlbench.engine.api.templating.binders;

import io.nosqlbench.engine.api.templating.ParsedTemplateMap;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.BindPoint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class ArrayBinder implements LongFunction<Object[]> {

    private final Object[] protoary;
    private final LongFunction<?>[] mapperary;
    private final int[] dindexes;

    public ArrayBinder(ParsedTemplateMap cmd, String[] fields) {
        this.protoary = new Object[fields.length];
        this.mapperary = new LongFunction<?>[fields.length];
        int[] indexes = new int[fields.length];
        int nextIndex = 0;

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (cmd.isStatic(field)) {
                protoary[i] = cmd.getStaticValue(field);
            } else if (cmd.isDynamic(field)) {
                mapperary[i] = cmd.getMapper(field);
                indexes[nextIndex++] = i;
            } else {
                throw new OpConfigError("There was no field named '" + field + "' while building an ArrayBinder.");
            }
        }
        this.dindexes = Arrays.copyOf(indexes, nextIndex);
    }

    public ArrayBinder(ParsedTemplateMap cmd, List<String> fields) {
        this(cmd, fields.toArray(new String[0]));
    }

    public ArrayBinder(List<BindPoint> bindPoints) {
        this.protoary = new Object[bindPoints.size()];
        this.mapperary = new LongFunction<?>[bindPoints.size()];
        int[] indexes = new int[bindPoints.size()];
        int nextIndex = 0;

        for (int i = 0; i < bindPoints.size(); i++) {
            BindPoint bindPoint = bindPoints.get(i);
            Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(bindPoint.getBindspec());
            mapperary[i] = mapper.orElseThrow();
            indexes[nextIndex++] = i;
        }
        this.dindexes = Arrays.copyOf(indexes, nextIndex);

    }

    @Override
    public Object[] apply(long value) {
        Object[] ary = Arrays.copyOf(protoary, protoary.length);
        for (int dindex : this.dindexes) {
            ary[dindex] = this.mapperary[dindex].apply(value);
        }
        return ary;
    }
}
