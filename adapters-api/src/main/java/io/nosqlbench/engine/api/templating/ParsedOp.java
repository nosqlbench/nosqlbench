package io.nosqlbench.engine.api.templating;

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


import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.binders.ArrayBinder;
import io.nosqlbench.engine.api.templating.binders.ListBinder;
import io.nosqlbench.engine.api.templating.binders.OrderedMapBinder;
import io.nosqlbench.nb.api.config.fieldreaders.DynamicFieldReader;
import io.nosqlbench.nb.api.config.fieldreaders.StaticFieldReader;
import io.nosqlbench.nb.api.config.standard.NBConfigError;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.NBTypeConverter;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Parse an OpTemplate into a ParsedOp, which can dispense object maps
 */
public class ParsedOp implements LongFunction<Map<String, ?>>, StaticFieldReader, DynamicFieldReader {

    private final static Logger logger = LogManager.getLogger(ParsedOp.class);

    /**
     * The fields which are statically assigned
     **/
    private final Map<String, Object> statics = new LinkedHashMap<>();

    /**
     * The fields which are dynamic, and must be realized via functions.
     * This map contains keys which identify the field names, and values, which may be null or undefined.
     */
    private final Map<String, LongFunction<?>> dynamics = new LinkedHashMap<>();

    /**
     * The names of payload values in the result of the operation which should be saved.
     * The keys in this map represent the name of the value as it would be found in the native
     * representation of a result. If the values are defined, then each one represents the name
     * that the found value should be saved as instead of the original name.
     */
    private final List<List<CapturePoint>> captures = new ArrayList<>();
    private final int mapsize;

    /**
     * A prototype of the fully generated map, to be used as the starting point
     * when rendering the full map with dynamic values.
     */
    private final LinkedHashMap<String, Object> protomap = new LinkedHashMap<>();
    private final OpTemplate ot;
    private final NBConfiguration activityCfg;

    /**
     * Create a parsed command from an Op template.
     *
     * @param ot   An OpTemplate representing an operation to be performed in a native driver.
     * @param activityCfg The activity configuration, used for reading config parameters
     */
    public ParsedOp(OpTemplate ot, NBConfiguration activityCfg) {
        this(ot, activityCfg, List.of());
    }

    /**
     * Create a parsed command from an Op template. This version is exactly like
     * {@link ParsedOp (OpTemplate,NBConfiguration)} except that it allows
     * preprocessors. Preprocessors are all applied to the the op template before
     * it is applied to the parsed command fields, allowing you to combine or destructure
     * fields from more tha one representation into a single canonical representation
     * for processing.
     *
     * @param opTemplate            The OpTemplate as provided by a user via YAML, JSON, or API (data structure)
     * @param activityCfg          The activity configuration, used to resolve nested config parameters
     * @param preprocessors Map->Map transformers.
     */
    public ParsedOp(OpTemplate opTemplate, NBConfiguration activityCfg, List<Function<Map<String, Object>, Map<String, Object>>> preprocessors) {
        this.ot = opTemplate;
        this.activityCfg = activityCfg;

        Map<String, Object> map = opTemplate.getOp().orElseThrow();
        for (Function<Map<String, Object>, Map<String, Object>> preprocessor : preprocessors) {
            map = preprocessor.apply(map);
        }

        applyTemplateFields(map, opTemplate.getBindings());
        mapsize = statics.size() + dynamics.size();
    }

    // For now, we only allow bind points to reference bindings, not other op template
    // fields. This seems like the saner and less confusing approach, so implementing
    // op field references should be left until it is requested if at all
    private void applyTemplateFields(Map<String, Object> map, Map<String, String> bindings) {
        map.forEach((k, v) -> {
            if (v instanceof CharSequence) {
                ParsedTemplate pt = ParsedTemplate.of(((CharSequence) v).toString(), bindings);
                this.captures.add(pt.getCaptures());
                switch (pt.getType()) {
                    case literal:
                        statics.put(k, ((CharSequence) v).toString());
                        protomap.put(k, ((CharSequence) v).toString());
                        break;
                    case bindref:
                        String spec = pt.asBinding().orElseThrow().getBindspec();
                        Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(spec);
                        dynamics.put(k, mapper.orElseThrow());
                        protomap.put(k, null);
                        break;
                    case concat:
                        StringBindings sb = new StringBindings(pt);
                        dynamics.put(k, sb);
                        protomap.put(k, null);
                        break;
                }
            } else {
                // Eventually, nested and mixed static dynamic structure could be supported, but
                // it would be complex to implement and also not that efficient, so let's just copy
                // structure for now
                statics.put(k, v);
                protomap.put(k, v);
            }
        });

    }

    public String getName() {
        return ot.getName();
    }

    public Map<String, Object> getStaticPrototype() {
        return statics;
    }

    public Map<String, LongFunction<?>> getDynamicPrototype() {
        return dynamics;
    }

    @Override
    public Map<String, Object> apply(long value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(protomap);
        dynamics.forEach((k, v) -> {
            map.put(k, v.apply(value));
        });
        return map;
    }

    @Override
    public boolean isDefinedDynamic(String field) {
        return dynamics.containsKey(field);
    }


    /**
     * @param field The field name to look for in the static field map.
     * @return true if and only if the named field is present in the static field map.
     */
    public boolean isStatic(String field) {
        return statics.containsKey(field);
    }

    public boolean isStatic(String field, Class<?> type) {
        return statics.containsKey(field) && type.isAssignableFrom(field.getClass());
    }

    /**
     * @param fields Names of fields to look for in the static field map.
     * @return true if and only if all provided field names are present in the static field map.
     */
    @Override
    public boolean isDefined(String... fields) {
        for (String field : fields) {
            if (!statics.containsKey(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the static value for the provided name, cast to the required type.
     *
     * @param field    Name of the field to get
     * @param classOfT The type of the field to return. If actual type is not compatible to a cast to this type, then a
     *                 casting error will be thrown.
     * @param <T>      The parameter type of the return type, used at compile time only to qualify asserted return type
     * @return A value of type T, or null
     */
    @Override
    public <T> T getStaticValue(String field, Class<T> classOfT) {
        return (T) statics.get(field);
    }

    /**
     * Get the static value for the provided name, cast to the required type, where the type is inferred
     * from the calling context.
     *
     * @param field Name of the field to get
     * @param <T>   The parameter type of the return type. used at compile time only to quality return type.
     * @return A value of type T, or null
     */
    @Override
    public <T> T getStaticValue(String field) {
        return (T) statics.get(field);
    }

    public Optional<ParsedTemplate> getStmtAsTemplate() {
        return ot.getParsed();
    }

    /**
     * Get the named static field value, or return the provided default, but throw an exception if
     * the named field is dynamic.
     *
     * @param name         The name of the field value to return.
     * @param defaultValue A value to return if the named value is not present in static nor dynamic fields.
     * @param <T>          The type of the field to return.
     * @return The value
     * @throws RuntimeException if the field name is only present in the dynamic fields.
     */
    @Override
    public <T> T getStaticValueOr(String name, T defaultValue) {
        if (statics.containsKey(name)) {
            return (T) statics.get(name);
        } else if (dynamics.containsKey(name)) {
            throw new BasicError("static field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        } else {
            return defaultValue;
        }
    }

    /**
     * Get the specified parameter by the user using the defined field which is closest to the op
     * template. This is the standard way of getting parameter values which can be specified at the
     * op template, op param, or activity level.
     *
     * @param name         The name of the configuration param
     * @param defaultValue the default value to return if the value is not defined anywhere in
     *                     (op fields, op params, activity params)
     * @param <T>          The type of the value to return
     * @return A configuration value
     * @throws io.nosqlbench.nb.api.config.standard.NBConfigError if the named field is defined dynamically,
     *                                                            as in this case, it is presumed that the parameter is not supported unless it is defined statically.
     */
    public <T> T getStaticConfigOr(String name, T defaultValue) {
        if (statics.containsKey(name)) {
            return NBTypeConverter.convertOr(statics.get(name), defaultValue);
        } else if (ot.getParams().containsKey(name)) {
            return NBTypeConverter.convertOr(ot.getParams().get(name), defaultValue);
        } else if (activityCfg.getMap().containsKey(name)) {
            return NBTypeConverter.convertOr(activityCfg.get("name"), defaultValue);
        } else if (dynamics.containsKey(name)) {
            throw new NBConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        } else {
            return defaultValue;
        }
    }

    public <T> Optional<T> getOptionalStaticConfig(String name, Class<T> type) {
        if (statics.containsKey(name)) {
            return Optional.of(NBTypeConverter.convert(statics.get(name), type));
        } else if (ot.getParams().containsKey(name)) {
            return Optional.of(NBTypeConverter.convert(ot.getParams().get(name), type));
        } else if (activityCfg.getMap().containsKey(name)) {
            return Optional.of(NBTypeConverter.convert(activityCfg.get("name"), type));
        } else if (dynamics.containsKey("name")) {
            throw new NBConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        } else {
            return Optional.empty();
        }
    }


    /**
     * Works exactly like {@link #getStaticConfigOr(String, Object)}, except that dynamic values
     * at the op field level will be generated on a per-input basis. This is a shortcut method for
     * allowing configuration values to be accessed dynamically where it makes sense.
     */
    public <T> T getConfigOr(String name, T defaultValue, long input) {
        if (statics.containsKey(name)) {
            return NBTypeConverter.convertOr(statics.get(name), defaultValue);
        } else if (dynamics.containsKey(name)) {
            return NBTypeConverter.convertOr(dynamics.get(name).apply(input), defaultValue);
        } else if (ot.getParams().containsKey(name)) {
            return NBTypeConverter.convertOr(ot.getParams().get(name), defaultValue);
        } else if (activityCfg.getMap().containsKey(name)) {
            return NBTypeConverter.convertOr(activityCfg.get("name"), defaultValue);
        } else return defaultValue;

    }


    /**
     * Return an optional value for the named field. This is an {@link Optional} form of {@link #getStaticValue}.
     *
     * @param field    Name of the field to get
     * @param classOfT The type of field to return. If the actual type is not compatible to a cast to this type,
     *                 then a casting error will be thrown.
     * @param <T>      The parameter type of the return
     * @return An optional value, empty unless the named value is defined in the static field map.
     */
    @Override
    public <T> Optional<T> getOptionalValue(String field, Class<T> classOfT) {
        return Optional.ofNullable(getStaticValue(field, classOfT));
    }

    public <T> Optional<T> getStaticValueOptionally(String field) {
        return Optional.ofNullable(getStaticValue(field));
    }

    /**
     * Get the named field value for a given long input. This uses parameter type inference -- The casting
     * to the return type will be based on the type of any assignment or casting on the caller's side.
     * Thus, if the actual type is not compatable to a cast to the needed return type, a casting error will
     * be thrown.
     *
     * @param field The name of the field to get.
     * @param input The seed value, or cycle value for which to generate the value.
     * @param <T>   The parameter type of the returned value. Inferred from usage context.
     * @return The value.
     */
    @Override
    public <T> T get(String field, long input) {
        if (statics.containsKey(field)) {
            return (T) statics.get(field);
        }
        if (dynamics.containsKey(field)) {
            return (T) dynamics.get(field).apply(input);
        }
        return null;
    }

    public Set<String> getDefinedNames() {
        HashSet<String> nameSet = new HashSet<>(statics.keySet());
        nameSet.addAll(dynamics.keySet());
        return nameSet;
    }

    public <V> LongFunction<V> getAsFunction(String name, Class<? extends V> type) {
        if (isStatic(name)) {
            V value = getStaticValue(name);
            return (cycle) -> value;
        } else if (isDefinedDynamic(name)) {
            Object testValue = dynamics.get(name).apply(0L);
            if (type.isAssignableFrom(testValue.getClass())) {
                return (LongFunction<V>) dynamics.get(name);
            } else {
                throw new OpConfigError(
                    "function for '" + name + "' yielded a " + testValue.getClass().getCanonicalName()
                        + " type, which is not assignable to " + type.getClass().getCanonicalName() + "'");
            }
        } else {
            throw new OpConfigError("No op field named '" + name + "' was found. If this field has a reasonable" +
                " default value, consider using getAsFunctionOr(...) and documenting the default.");
        }
    }

    @Override
    public <V> LongFunction<V> getAsFunctionOr(String name, V defaultValue) {
        if (isStatic(name)) {
            V value = getStaticValue(name);
            return l -> value;
        } else if (isDefinedDynamic(name)) {
            return l -> get(name, l);
        } else {
            return l -> defaultValue;
        }
    }

    public <V> LongFunction<V> getAsCachedFunctionOr(String fieldname, String defaultValue, Function<String, V> init) {
        if (isStatic(fieldname)) {
            V value = getStaticValue(fieldname);
            if (value instanceof String) {
                V defaultObject = init.apply((String) value);
                return l -> defaultObject;
            } else {
                throw new OpConfigError("Unable to compose string to object cache with non-String value of type " + defaultValue.getClass().getCanonicalName());
            }
        } else if (isDefinedDynamic(fieldname)) {
            LongFunction<V> f = l -> get(fieldname, l);
            V testValue = f.apply(0);
            if (testValue instanceof String) {
                LongFunction<String> fs = l -> (String) get(fieldname, l);
                ObjectCache<V> oc = new ObjectCache<>(init);
                return l -> oc.apply(fs.apply(l));
            } else {
                throw new OpConfigError(
                    "Unable to compose string func to obj cache with non-String function of type " + f.getClass().getCanonicalName()
                );
            }
        } else {
            throw new OpConfigError(
                "Unable to compose string func to obj cache with no defined static nor dynamic field named " + fieldname
            );
        }
    }

    public boolean isDefined(String field) {
        return statics.containsKey(field) || dynamics.containsKey(field);
    }

    @Override
    public boolean isDefined(String field, Class<?> type) {
        if (statics.containsKey(field)) {
            if (type.isAssignableFrom(statics.get(field).getClass())) {
                return true;
            } else {
                throw new OpConfigError("field " + field + " was defined, but not as the requested type " + type.getCanonicalName());
            }
        } else if (dynamics.containsKey(field)) {
            Object testObject = dynamics.get(field).apply(0L);
            if (type.isAssignableFrom(testObject.getClass())) {
                return true;
            } else {
                throw new OpConfigError("field " + field + " was defined as a function, but not one that returns the" +
                    " requested type " + testObject.getClass().getCanonicalName());
            }
        }
        return false;
    }

    public boolean isDefinedAll(String... fields) {
        for (String field : fields) {
            if (!statics.containsKey(field) && !dynamics.containsKey(field)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void assertDefinedStatic(String... fields) {
        for (String field : fields) {
            if (!statics.containsKey(field)) {
                Set<String> missing = new HashSet<>();
                for (String readoutfield : fields) {
                    if (!statics.containsKey(readoutfield)) {
                        missing.add(readoutfield);
                    }
                }
                throw new OpConfigError("Fields " + missing + " are required to be defined with static values for this type of operation.");
            }
        }
    }

    public LongFunction<List<Object>> newListBinder(String... fields) {
        return new ListBinder(this, fields);
    }

    public LongFunction<List<Object>> newListBinder(List<String> fields) {
        return new ListBinder(this, fields);
    }

    public LongFunction<Map<String, Object>> newOrderedMapBinder(String... fields) {
        return new OrderedMapBinder(this, fields);

    }

    public LongFunction<Object[]> newArrayBinder(String... fields) {
        return new ArrayBinder(this, fields);
    }

    public LongFunction<Object[]> newArrayBinder(List<String> fields) {
        return new ArrayBinder(this, fields);
    }

    public LongFunction<Object[]> newArrayBinderFromBindPoints(List<BindPoint> bindPoints) {
        return new ArrayBinder(bindPoints);
    }

    public LongFunction<?> getMapper(String field) {
        LongFunction<?> mapper = dynamics.get(field);
        return mapper;
    }

    public int getSize() {
        return this.mapsize;
    }

    public boolean isUndefined(String field) {
        return !(statics.containsKey(field) || dynamics.containsKey(field));
    }


}
