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

import com.google.gson.annotations.SerializedName;
import io.nosqlbench.engine.api.activityconfig.MultiMapLookup;
import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtDef;
import io.nosqlbench.nb.api.errors.BasicError;

import java.util.*;
import java.util.function.Function;

public class OpDef implements OpTemplate {

    private static final String FIELD_DESC = "description";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_OP = "op";
    private static final String FIELD_BINDINGS = "bindings";
    private static final String FIELD_PARAMS = "params";
    private static final String FIELD_TAGS = "tags";

    private final RawStmtDef rawStmtDef;
    private final StmtsBlock block;
    private final LinkedHashMap<String, Object> params;
    private final LinkedHashMap<String, String> bindings;
    private final LinkedHashMap<String, String> tags;

    public OpDef(StmtsBlock block, RawStmtDef rawStmtDef) {
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
    public Map<String,?> getOp() {
        Object op = rawStmtDef.getOp();
        HashMap<String, Object> newmap = new LinkedHashMap<>();
        if (op instanceof Map) {
            ((Map<?,?>)op).forEach((k,v) -> {
                newmap.put(k.toString(),v);
            });
        } else if (op instanceof  CharSequence) {
            newmap.put("stmt",op.toString());
        } else {
            throw new BasicError("Unable to coerce a '" + op.getClass().getCanonicalName() + "' into an op template");
        }

        return newmap;
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
    @SerializedName("params")
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
            if (object != null) {
                if (type.isAssignableFrom(object.getClass())) {
                    map.put(pname, type.cast(object));
                } else {
                    throw new RuntimeException("With param named '" + pname + "" +
                        "' You can't assign an object of type '" + object.getClass().getSimpleName() + "" +
                        "' to '" + type.getSimpleName() + "'. Maybe the YAML format is suggesting the wrong type.");
                }
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
        return "stmt(name:" + getName() + ", stmt:" + getOp() + ", tags:(" + getTags() + "), params:(" + getParams() + "), bindings:(" + getBindings() + "))";
    }

    @Override
    public ParsedStmt getParsed(Function<String, String>... transforms) {
        return new ParsedStmt(this, transforms);
    }


    @Override
    public String getDesc() {
        return rawStmtDef.getDesc();
    }


    @Override
    public Map<String, Object> asData() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

        if (this.getDesc() != null && !this.getDesc().isBlank()) {
            fields.put(FIELD_DESC, this.getDesc());
        }

        if (this.getBindings().size() > 0) {
            fields.put(FIELD_BINDINGS, this.getBindings());
        }

        if (this.getParams().size() > 0) {
            fields.put(FIELD_PARAMS, this.getParams());
        }

        if (this.getTags().size() > 0) {
            fields.put(FIELD_TAGS, this.getTags());
        }

        fields.put(FIELD_OP, this.getOp());

        fields.put(FIELD_NAME, this.getName());

        return fields;
    }

    @Override
    public String getStmt() {
        if (getOp() instanceof CharSequence) {
            return getOp().toString();
        } else {
            throw new BasicError("tried to access op type '" + getOp().getClass().getSimpleName() + "' as a string statement.");
        }
    }
}
