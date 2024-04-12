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

package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * This implementation of a string compositor takes a logically coherent
 * string template and bindings set. It employs a few simplistic optimizations
 * to avoid re-generating duplicate values, as well as lower allocation
 * rate of buffer data.
 */
public class StringCompositor implements LongFunction<String> {

    private final String[] spans;
    private final DataMapper<?>[] mappers;
    private final int[] LUT;
    private final int bufsize;

    private final Function<Object, String> stringfunc;

    public StringCompositor(ParsedTemplateString template, Map<String, Object> fconfig, Function<Object, String> stringfunc) {
        Map<String, Integer> specs = new HashMap<>();
        List<BindPoint> bindpoints = template.getBindPoints();
        for (BindPoint bindPoint : bindpoints) {
            String spec = bindPoint.getBindspec();
            specs.compute(spec, (s, i) -> i == null ? specs.size() : i);
        }
        mappers = new DataMapper<?>[specs.size()];
        specs.forEach((k, v) -> {
            mappers[v] = VirtData.getOptionalMapper(k, fconfig).orElseThrow();
        });
        String[] even_odd_spans = template.getSpans();
        this.spans = new String[bindpoints.size() + 1];
        LUT = new int[bindpoints.size()];
        for (int i = 0; i < bindpoints.size(); i++) {
            spans[i] = even_odd_spans[i << 1];
            LUT[i] = specs.get(template.getBindPoints().get(i).getBindspec());
        }
        spans[spans.length - 1] = even_odd_spans[even_odd_spans.length - 1];
        this.stringfunc = stringfunc;

        int minsize = 0;
        for (int i = 0; i < 100; i++) {
            String result = apply(i);
            if (result!=null) {
                minsize = Math.max(minsize,result.length());
            }
        }
        bufsize = spans.length*1024;
    }

    public StringCompositor(ParsedTemplateString template, Map<String, Object> fconfig) {
        this(template, fconfig, s -> s != null ? s.toString() : "NULL");
    }

    @Override
    public String apply(long value) {
        StringBuilder sb = new StringBuilder(bufsize);
        String[] ary = new String[mappers.length];
        for (int i = 0; i < ary.length; i++) {
            DataMapper<?> mapperType = mappers[i];
            Object object = mapperType.apply(value);
            ary[i] = stringfunc.apply(object);
        }
        for (int i = 0; i < LUT.length; i++) {
            sb.append(spans[i]).append(ary[LUT[i]]);
        }
        sb.append(spans[spans.length - 1]);
        return sb.toString();
    }
}
