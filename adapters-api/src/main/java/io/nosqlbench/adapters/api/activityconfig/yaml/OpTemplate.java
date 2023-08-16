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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.api.engine.util.Tagged;
import io.nosqlbench.api.config.params.Element;
import io.nosqlbench.api.config.params.NBParams;
import io.nosqlbench.api.config.standard.NBTypeConverter;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * The OpTemplate is a structurally normalized type which presents the user-provided op template to the NoSQLBench
 * loading and templating mechanisms. This type is not generally used directly for new driver development. It is the
 * backing data which is used by {@link ParsedOp}, which is used in drivers to map
 * op templates to function to be used for a given cycle value.
 * </p>
 *
 * <p>
 * This is part of the implementation of the NoSQLBench <em>Uniform Workload Specification</em>. Check the tests
 * for UniformWorkloadSpecification directly to see how this specification is tested and documented.
 * </p>
 */
public abstract class OpTemplate implements Tagged {

    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // TODO: coalesce Gson instances to a few statics on a central NB API class

    public final static String FIELD_DESC = "description";
    public final static String FIELD_NAME = "name";
    public final static String FIELD_OP = "op";
    public final static String FIELD_BINDINGS = "bindings";
    public final static String FIELD_PARAMS = "params";
    public final static String FIELD_TAGS = "tags";

    /**
     * @return a description for the op template, or an empty string
     */
    public abstract String getDesc();

    /**
     * @return a name for the op template, user-specified or auto-generated
     */
    public abstract String getName();

    /**
     * Return a map of tags for this statement. Implementations are required to
     * add a tag for "name" automatically when this value is set during construction.
     *
     * @return A map of assigned tags for the op, with the name added as an auto-tag.
     */
    public abstract Map<String, String> getTags();

    public abstract Map<String, String> getBindings();

    public abstract Map<String, Object> getParams();

    public <T> Map<String, T> getParamsAsValueType(Class<? extends T> type) {
        Map<String, T> map = new LinkedHashMap<>();
        for (String pname : getParams().keySet()) {
            Object object = getParams().get(pname);
            if (object != null) {
                if (type.isAssignableFrom(object.getClass())) {
                    map.put(pname, type.cast(object));
                } else {
                    throw new RuntimeException("With param named '" + pname +
                        "' You can't assign an object of type '" + object.getClass().getSimpleName() +
                        "' to '" + type.getSimpleName() + "'. Maybe the YAML format is suggesting the wrong type.");
                }
            }
        }
        return map;
    }


    public <V> V removeParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);

        if (!getParams().containsKey(name)) {
            return defaultValue;
        }

        Object value = getParams().remove(name);

        if (defaultValue.getClass().isAssignableFrom(value.getClass())) {
            return (V) value;
        } else {
            return NBTypeConverter.convertOr(value, defaultValue);
        }

    }

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

    public Optional<String> getOptionalStringParam(String name) {
        return getOptionalStringParam(name, String.class);
    }

    /**
     * Parse the statement for anchors and return a richer view of the StmtDef which
     * is simpler to use for most statement configuration needs.
     *
     * @return an optional {@link ParsedTemplateString}
     */
    public Optional<ParsedTemplateString> getParsed(Function<String, String>... rewriters) {
        Optional<String> os = getStmt();
        return os.map(s -> {
            String result = s;
            for (Function<String, String> rewriter : rewriters) {
                result = rewriter.apply(result);
            }
            return result;
        }).map(s -> new ParsedTemplateString(s, getBindings()));
    }

    public Optional<ParsedTemplateString> getParsed() {
        return getStmt().map(s -> new ParsedTemplateString(s, getBindings()));
    }

    public abstract Optional<Map<String, Object>> getOp();

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

        this.getOp().ifPresent(o -> fields.put(FIELD_OP, o));

        fields.put(FIELD_NAME, this.getName());

        return fields;
    }

    /**
     * Legacy support for String form statements. This is left here as a convenience method,
     * however it is changed to an Optional to force caller refactorings.
     *
     * @return An optional string version of the op, empty if there is no 'stmt' property in the op fields, or no op
     *     fields at all.
     */
    public Optional<String> getStmt() {
        return getOp().map(m -> m.get("stmt")).map(s -> {
            if (s instanceof CharSequence) {
                return s.toString();
            } else {
                return gson.toJson(s);
            }
        });
    }

    public Element getParamReader() {
        return NBParams.one(getName(), getParams());
    }

    /**
     * @return the size of remaining fields from the op template and the params map.
     */
    public int size() {
        return getOp().map(Map::size).orElse(0) + getParams().size();
    }

    /**
     * @return the map of all remaining fields from the op template and the params map.
     */
    public Map<String, Object> remainingFields() {
        Map<String, Object> remaining = new LinkedHashMap<>(getOp().orElse(Map.of()));
        remaining.putAll(getParams());
        return remaining;
    }

    public void assertConsumed() {
        if (size() > 0) {
            throw new OpConfigError("The op template named '" + getName() + "' was not fully consumed. These fields are not being applied:" + remainingFields());
        }
    }
}
