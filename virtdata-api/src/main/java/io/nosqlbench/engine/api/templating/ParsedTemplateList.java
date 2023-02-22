/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.virtdata.core.templates.CapturePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class ParsedTemplateList implements LongFunction<List<?>> {
    private final List<Object> protolist = new ArrayList<>();
    private final int[] dynamic_idx;
    private final LongFunction<?>[] functions;
    private final List<CapturePoint> captures = new ArrayList<>();

    public ParsedTemplateList(List<Object> sublist, Map<String, String> bindings, List<Map<String, Object>> cfgsources) {

        List<LongFunction<?>> funcs = new ArrayList<>();
        List<Integer> dindexes = new ArrayList<>();

        for (int i = 0; i < sublist.size(); i++) {
            Object item = sublist.get(i);
            Templatizer.Result result = Templatizer.make(bindings, item, null, cfgsources);
            this.captures.addAll(result.getCaptures());
            switch (result.getType()) {
                case literal:
                    protolist.add(result.getValue());
                    break;
                case bindref:
                case concat:
                    protolist.add(null);
                    funcs.add(result.getFunction());
                    dindexes.add(i);
                    break;
            }
        }
        this.dynamic_idx = dindexes.stream().mapToInt(Integer::intValue).toArray();
        this.functions = funcs.toArray(new LongFunction<?>[0]);

    }

    @Override
    public List<?> apply(long value) {
        List<Object> list = new ArrayList<>(protolist);
        for (int i = 0; i < dynamic_idx.length; i++) {
            Object obj = functions[i].apply(value);
            list.set(dynamic_idx[i], obj);
        }
        return list;
    }

    public boolean isStatic() {
        return dynamic_idx.length==0;
    }

    public List<CapturePoint> getCaptures() {
        return this.captures;
    }
}
