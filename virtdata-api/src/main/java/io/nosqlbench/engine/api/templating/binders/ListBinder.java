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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class ListBinder implements LongFunction<List<Object>> {

    private final ArrayList<Object> protolist;
    private final ArrayList<LongFunction<?>> mapperlist;
    private final int[] dindexes;

    public ListBinder(ParsedTemplateMap cmd, String... fields) {
        this.protolist = new ArrayList<>(fields.length);
        this.mapperlist = new ArrayList<>(fields.length);
        int[] indexes = new int[fields.length];
        int lastIndex = 0;

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (cmd.isStatic(field)) {
                protolist.add(cmd.getStaticValue(field));
                mapperlist.add(null);
            } else if (cmd.isDynamic(field)) {
                protolist.add(null);
                mapperlist.add(cmd.getMapper(field));
                indexes[lastIndex++]=i;
            } else {
                throw new OpConfigError("No defined field '" + field + "' when creating list binder");
            }
        }
        this.dindexes = Arrays.copyOf(indexes,lastIndex);
    }

    public ListBinder(ParsedTemplateMap cmd, List<String> fields) {
        this(cmd,fields.toArray(new String[0]));
    }

    @Override
    public List<Object> apply(long value) {
        ArrayList<Object> list = new ArrayList<>(protolist);
        for (int index : this.dindexes) {
            list.set(index,mapperlist.get(index).apply(value));
        }
        return list;
    }
}
