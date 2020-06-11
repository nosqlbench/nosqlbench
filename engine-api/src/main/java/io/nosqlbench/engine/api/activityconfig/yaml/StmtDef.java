/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.MultiMapLookup;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtDef;
import io.nosqlbench.engine.api.util.Tagged;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StmtDef implements Tagged {

    private final RawStmtDef rawStmtDef;
    private StmtsBlock block;

    public StmtDef(StmtsBlock block, RawStmtDef rawStmtDef) {
        this.block = block;
        this.rawStmtDef = rawStmtDef;
    }

    public String getName() {
        return block.getName() + "--" + rawStmtDef.getName();
    }

    public String getStmt() {
        return rawStmtDef.getStmt();
    }

    public Map<String,String> getBindings() {
        return new MultiMapLookup<>(rawStmtDef.getBindings(), block.getBindings());
    }

    public Map<String, Object> getParams() {
        return new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());
    }
    public <T> Map<String,T> getParamsAsValueType(Class<? extends T> type) {
        MultiMapLookup<Object> lookup =  new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());
        Map<String,T> map = new LinkedHashMap<>();
        lookup.forEach((k,v)->map.put(k,type.cast(v)));
        return map;
    }

    @SuppressWarnings("unchecked")
    public <V> V getParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);
        MultiMapLookup<Object> lookup =  new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());

        if (!lookup.containsKey(name)) {
            return defaultValue;
        }
        Object value = lookup.get(name);
        return (V) defaultValue.getClass().cast(value);
    }

    public <V> V getParam(String name, Class<? extends V> type) {
        MultiMapLookup<Object> lookup =  new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());
        Object object = lookup.get(name);
        V value = type.cast(object);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> getOptionalParam(String name, Class<? extends V> type) {
        MultiMapLookup<Object> lookup =  new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());
        if (lookup.containsKey(name)) {
            Object object = lookup.get(name);
            if (object==null) {
                return Optional.empty();
            }
            return Optional.of((V) type.cast(object));
        }
        return Optional.empty();
    }

    public Optional<String> getOptionalParam(String name) {
        return getOptionalParam(name,String.class);
    }


    public Map<String,String> getTags() {
        return new MultiMapLookup<>(rawStmtDef.getTags(), block.getTags());
    }

    @Override
    public String toString() {
        return "stmt(name:" + getName() + ", stmt:" + getStmt() + ", tags:(" + getTags() + "), params:(" + getParams() +"), bindings:(" + getBindings()+"))";
    }

    /**
     * Parse the statement for anchors and return a richer view of the StmtDef which
     * is simpler to use for most statement configuration needs.
     * @return a new {@link ParsedStmt}
     */
    public ParsedStmt getParsed() {
        return new ParsedStmt(this);
    }

    public String getDesc() {
        return rawStmtDef.getDesc();
    }
}
