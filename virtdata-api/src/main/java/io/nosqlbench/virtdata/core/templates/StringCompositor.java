package io.nosqlbench.virtdata.core.templates;

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


import io.nosqlbench.virtdata.core.bindings.ValuesBinder;
import io.nosqlbench.virtdata.core.bindings.Bindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * StringCompositor provides a way to build strings from a string template and provided values.
 *
 * <p>
 * The template is simply an array of string values, where odd indices represent token positions, and even indices represent
 * literals. This version of the StringCompositor fetches data from the bindings only for the named fields in the template.
 * </p>
 */
public class StringCompositor implements ValuesBinder<StringCompositor, String> {

    private final String[] templateSegments;
    private Function<Object, String> stringfunc = String::valueOf;

    /**
     * Create a string template which has positional tokens, in "{}" form.
     *
     * @param template The string template
     */
    public StringCompositor(String template) {
        templateSegments = parseTemplate(template);
    }

    public StringCompositor(String template, Function<Object, String> stringfunc) {
        this(template);
        this.stringfunc = stringfunc;
    }

    public StringCompositor(ParsedTemplate pt) {
        templateSegments = pt.getSpans();
    }

    // for testing
    protected String[] parseTemplate(String template) {
        ParsedTemplate parsed = new ParsedTemplate(template, Collections.emptyMap());
        return parsed.getSpans();
    }


    @Override
    public String bindValues(StringCompositor context, Bindings bindings, long cycle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < templateSegments.length; i++) {
            if (i % 2 == 0) {
                sb.append(templateSegments[i]);
            } else {
                String key = templateSegments[i];
                Object value = bindings.get(key, cycle);
                String valueString = stringfunc.apply(value);
                sb.append(valueString);
            }
        }
        return sb.toString();
    }

    public List<String> getBindPointNames() {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < templateSegments.length; i++) {
            if (i % 2 == 1) {
                tokens.add(templateSegments[i]);
            }
        }
        return tokens;
    }
}
