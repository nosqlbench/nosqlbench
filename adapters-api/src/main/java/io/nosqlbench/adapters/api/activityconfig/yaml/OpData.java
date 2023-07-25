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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class OpData extends OpTemplate {

    private String desc = "";
    private String name = "";
    private Map<String, Object> op;
    private Map<String, Object> params = Map.of();
    private Map<String, String> bindings = Map.of();
    private Map<String, String> tags = new LinkedHashMap<>();

    public OpData(String desc, String name, Map<String, String> tags, Map<String, String> bindings, Map<String, Object> params, Map<String, Object> op) {
        this.desc = desc;
        this.name = name;
        this.tags = tags;
        this.bindings = bindings;
        this.params = params;
        this.op = op;
    }

    public OpData() {}


    public OpData(Map<String,Object> opdata) {
        applyFields(opdata);
        if (opdata.size()>0) {
            throw new RuntimeException("Unconsumed fields in construction of op data from map: " + opdata);
        }
    }

    public OpData applyFields(Map<String,Object> opdata) {
        LinkedHashMap<String,Object> toapply = new LinkedHashMap<>(opdata);
        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_DESC)).ifPresent(v -> this.setDesc(v.toString()));
        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_NAME)).ifPresent(v -> this.setName(v.toString()));

        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_BINDINGS)).ifPresent(this::setBindings);
        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_OP)).ifPresent(this::setOp);
        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_PARAMS)).ifPresent(this::setParams);
        Optional.ofNullable(toapply.remove(OpTemplate.FIELD_TAGS)).ifPresent(this::setTags);

        if (toapply.size()>0) {
            throw new InvalidParameterException("Fields were not applied to OpData:" + toapply);
        }
        return this;
    }

    private void setTags(Object o) {
        if (o instanceof Map) {
            ((Map<?, ?>) o).forEach((k,v) -> {
                this.tags.put(k.toString(),v.toString());
            });
        } else {
            throw new RuntimeException("Invalid type for tags: " + o.getClass().getSimpleName());
        }
    }

    private void setParams(Object o) {
        if (o instanceof Map) {
            this.params = new LinkedHashMap<>();
            ((Map<?, ?>) o).forEach((k,v) -> {
                this.params.put(k.toString(),v);
            });
        } else {
            throw new RuntimeException("Invalid type for params: " + op.getClass().getSimpleName());
        }
    }

    private void setOp(Object o) {
        if (o instanceof CharSequence) {
            this.op = new LinkedHashMap<>(Map.of("stmt",o.toString()));
        } else if (o instanceof Map) {
            this.op = new LinkedHashMap<>((Map)o);
        } else {
            throw new RuntimeException("Invalid type for op:" + op.getClass().getSimpleName());
        }
    }

    private void setBindings(Object bindings) {
        if (bindings instanceof Map) {
            this.bindings = new LinkedHashMap<>();
            ((Map<?, ?>) bindings).forEach((k,v) -> {
                this.bindings.put(k.toString(),v.toString());
            });
        } else if (bindings!=null) {
            throw new RuntimeException("Invalid type for bindings: " + bindings.getClass().getSimpleName());
        }
    }

    private void setName(String name) {
        this.name = name;
        this.tags.put("name",name);
    }

    private void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, String> getTags() {
        return this.tags;
    }

    @Override
    public Map<String, String> getBindings() {
        return this.bindings;
    }

    @Override
    public Map<String, Object> getParams() {
        return this.params;
    }

    @Override
    public Optional<Map<String, Object>> getOp() {
        return Optional.of(this.op);
    }

    @Override
    public Optional<String> getStmt() {
        return Optional.empty();
    }
}
