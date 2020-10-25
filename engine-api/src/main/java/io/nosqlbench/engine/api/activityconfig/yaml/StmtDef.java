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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StmtDef implements OpTemplate {

    private final RawStmtDef rawStmtDef;
    private final StmtsBlock block;
    private final LinkedHashMap<String, Object> params;
    private final LinkedHashMap<String, String> bindings;
    private final LinkedHashMap<String, String> tags;

    public StmtDef(StmtsBlock block, RawStmtDef rawStmtDef) {
        this.block = block;
        this.rawStmtDef = rawStmtDef;
        this.params = composeParams();
        this.bindings = composeBindings();
        this.tags = composeTags();
    }

    @Override
    public String getName() {
        return block.getName() + "--" + rawStmtDef.getName();
    }

    @Override
    public String getStmt() {
        return rawStmtDef.getStmt();
    }

    @Override
    public LinkedHashMap<String, String> getBindings() {
        return bindings;
//        return new MultiMapLookup<>(rawStmtDef.getBindings(), block.getBindings());
    }

    private LinkedHashMap<String, String> composeBindings() {
        MultiMapLookup<String> lookup = new MultiMapLookup<>(rawStmtDef.getBindings(), block.getBindings());
        return new LinkedHashMap<>(lookup);
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    private LinkedHashMap<String, Object> composeParams() {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawStmtDef.getParams(), block.getParams());
        LinkedHashMap<String, Object> params = new LinkedHashMap<>(lookup);
        return params;
    }

    @Override
    public <T> Map<String, T> getParamsAsValueType(Class<? extends T> type) {
        Map<String, T> map = new LinkedHashMap<>();
        for (String pname : getParams().keySet()) {
            Object object = getParams().get(pname);
            if (type.isAssignableFrom(object.getClass())) {
                map.put(pname, type.cast(object));
            } else {
                throw new RuntimeException("With param named '" + pname + "" +
                        "' You can't assign an object of type '" + object.getClass().getSimpleName() + "" +
                        "' to '" + type.getSimpleName() + "'. Maybe the YAML format is suggesting the wrong type.");
            }
        }
        return map;
    }

    @Override
    public <V> V removeParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);

        if (!getParams().containsKey(name)) {
            return defaultValue;
        }

        Object value = getParams().remove(name);

        try {
            return (V) defaultValue.getClass().cast(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to cast type " + value.getClass().getCanonicalName() + " to " + defaultValue.getClass().getCanonicalName(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);

        if (!getParams().containsKey(name)) {
            return defaultValue;
        }
        Object value = getParams().get(name);
        try {
            return (V) defaultValue.getClass().cast(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to cast type " + value.getClass().getCanonicalName() + " to " + defaultValue.getClass().getCanonicalName(), e);
        }
    }

    @Override
    public <V> V getParam(String name, Class<? extends V> type) {
        Object object = getParams().get(name);
        if (object == null) {
            return null;
        }
        if (type.isAssignableFrom(object.getClass())) {
            V value = type.cast(object);
            return value;
        }
        throw new RuntimeException("Unable to cast type " + object.getClass().getSimpleName() + " to" +
                " " + type.getSimpleName() + ". Perhaps the yaml format is suggesting the wrong type.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Optional<V> getOptionalStringParam(String name, Class<? extends V> type) {
        if (type.isPrimitive()) {
            throw new RuntimeException("Do not use primitive types for the target class here. For example, Boolean.class is accepted, but boolean.class is not.");
        }
        if (getParams().containsKey(name)) {
            Object object = getParams().get(name);
            if (object == null) {
                return Optional.empty();
            }
            try {
                V reified = type.cast(object);
                return Optional.of(reified);
            } catch (Exception e) {
                throw new RuntimeException("Unable to cast type " + object.getClass().getCanonicalName() + " to " + type.getCanonicalName());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getOptionalStringParam(String name) {
        return getOptionalStringParam(name, String.class);
    }


    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    private LinkedHashMap<String, String> composeTags() {
        return new LinkedHashMap<>(new MultiMapLookup<>(rawStmtDef.getTags(), block.getTags()));
    }

    @Override
    public String toString() {
        return "stmt(name:" + getName() + ", stmt:" + getStmt() + ", tags:(" + getTags() + "), params:(" + getParams() + "), bindings:(" + getBindings() + "))";
    }

    @Override
    public ParsedStmt getParsed() {
        return new ParsedStmt(this);
    }

    @Override
    public String getDesc() {
        return rawStmtDef.getDesc();
    }
}
