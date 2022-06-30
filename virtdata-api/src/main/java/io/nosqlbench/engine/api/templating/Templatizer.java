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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedStringTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class Templatizer {

    public static Result make(Map<String, String> bindings, Object v, String name, List<Map<String, Object>> cfgsources) {
        Result result = new Result();
        result.setName(name);

        if (v instanceof CharSequence) {
            ParsedStringTemplate pt = ParsedStringTemplate.of(((CharSequence) v).toString(), bindings);
            result.addCaptures(pt.getCaptures());
            result.setType(pt.getType());
            switch (pt.getType()) {
                case literal:
                    result.setValue(((CharSequence)v).toString());
                    break;
                case bindref:
                    String spec = pt.asBinding().orElseThrow().getBindspec();
                    if (spec == null) {
                        throw new OpConfigError("Empty binding spec for '" + (name!=null?name:"anonymous binding") + "'");
                    }
                    Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(spec);
                    result.setFunction(mapper.orElseThrow());
                    break;
                case concat:
                    StringBindings sb = new StringBindings(pt);
                    result.setFunction(sb);
                    break;
            }
        } else if (v instanceof Map) {
            ((Map) v).keySet().forEach(smk -> {
                if (!CharSequence.class.isAssignableFrom(smk.getClass())) {
                    throw new OpConfigError("Only string keys are allowed in submaps.");
                }
            });
            Map<String, Object> submap = (Map<String, Object>) v;
            ParsedTemplateMap subtpl = new ParsedTemplateMap(name, submap, bindings, cfgsources);
            if (subtpl.isStatic()) {
                result.setValue(submap);
            } else {
                result.setFunction(subtpl);
            }
        } else if (v instanceof List) {
            List<Object> sublist = (List<Object>) v;
            ParsedTemplateList subtpl = new ParsedTemplateList(sublist, bindings, cfgsources);
            if (subtpl.isStatic()) {
                result.setValue(sublist);
            } else {
                result.setFunction(subtpl);
            }
        } else {
            result.setValue(v);
            // Eventually, nested and mixed static dynamic structure could be supported, but
            // it would be complex to implement and also not that efficient, so let's just copy
            // structure for now
        }
        return result;
    }

    public static class Result {
        private BindType type;
        private final List<CapturePoint> captures = new ArrayList<>();
        private String name;
        private Object value;
        private LongFunction<?> function;

        public Result() {
            this.type = null;
        }

        public BindType getType() {
            return this.type;
        }

        public List<CapturePoint> getCaptures() {
            return this.captures;
        }

        public void addCaptures(List<CapturePoint> captures) {
            this.captures.addAll(captures);
        }

        public void setType(BindType type) {
            this.type = type;
        }

        public void setName(String name) {
            this.name =name;
        }

        public String getName() {
            return name;
        }

        public void setValue(Object v) {
            this.value = v;
        }

        public void setFunction(LongFunction<?> mapper) {
            this.function = mapper;
        }

        public Object getValue() {
            return value;
        }

        public LongFunction<?> getFunction() {
            return this.function;
        }
    }


}
