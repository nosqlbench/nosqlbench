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

import io.nosqlbench.engine.api.templating.binders.ArrayBinder;
import io.nosqlbench.engine.api.templating.binders.ListBinder;
import io.nosqlbench.engine.api.templating.binders.OrderedMapBinder;
import io.nosqlbench.api.config.fieldreaders.DynamicFieldReader;
import io.nosqlbench.api.config.fieldreaders.StaticFieldReader;
import io.nosqlbench.api.config.params.ParamsParser;
import io.nosqlbench.api.config.standard.NBConfigError;
import io.nosqlbench.api.config.standard.NBTypeConverter;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * A parsed map template, which allows construction of extracted or projected functions related
 * to dynamic value templates. This is the backing implementation for ParsedOp which is map-based
 * at the root level, but which can have recursive structure and thus needed an inner value type.
 * This implementation supports parsing/restructuring around string, map, and list templates.
 *
 * The provided map is interpreted as a map of string to object templates using these rules:
 * <OL>
 * <LI>If the value is a String and contains no binding points, it is interpreted as a literal</LI>
 * <LI>If the value is a String and contains only a binding point with no leading nor trailing text, it is interpreted as an object binding</LI>
 * <LI>If the value is a String and contains a binding point with any leading or trailing text, it is interpreted as a String template binding</LI>
 * <LI>If the value is a map, list, or set, then each element is interpreted as above</LI>
 * </OL>
 *
 * TODO: Proactively check casting on functional methods, fallback to {@link NBTypeConverter} only if needed and can
 */
public class ParsedTemplateMap implements LongFunction<Map<String, ?>>, StaticFieldReader, DynamicFieldReader {

    private final static Logger logger = LogManager.getLogger(ParsedTemplateMap.class);

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
    private final List<CapturePoint> captures = new ArrayList<>();
    private final int mapsize;

    /**
     * A prototype of the fully generated map, to be used as the starting point
     * when rendering the full map with dynamic values.
     */
    private final LinkedHashMap<String, Object> protomap = new LinkedHashMap<>();

    /**
     * Any auxiliary source of values to be applied beyond what is specified directly in the op fields.
     * This includes, for example, the activity parameters which are allowed by the config model on
     * an adapter. This means that you can specify defaults for an op field outside of the workload/op
     * templates simply by providing them on the command line or activity parameters otherwise.
     * This is exactly how the required op field `driver` works.
     */
    private final List<Map<String, Object>> cfgsources;

    /**
     * This remembers the original template object so that diagnostic and debugging views
     * may see the original specifiers, whether they are literals of any type, or a string
     * value which is recognized as being or containing some dynamic span, i.e. bind points.
     */
    private Map<String, Object> originalTemplateObject;

    /**
     * The bindings definitions from the raw op template data structure.
     */
    private Map<String, String> bindings;
    private final String name;

    public ParsedTemplateMap(String name, Map<String, Object> map, Map<String, String> bindings, List<Map<String, Object>> cfgsources) {
        this.name = name;
        this.cfgsources = cfgsources;
        applyTemplateFields(map, bindings);
        mapsize = statics.size() + dynamics.size();
    }

    // For now, we only allow bind points to reference bindings, not other op template
    // fields. This seems like the saner and less confusing approach, so implementing
    // op field references should be left until it is requested if at all
    private void applyTemplateFields(Map<String, Object> map, Map<String, String> bindings) {
        this.originalTemplateObject = map;
        this.bindings = bindings;
        map.forEach((k, v) -> {
            if (v instanceof CharSequence charvalue) {
                ParsedTemplateString pt = ParsedTemplateString.of(charvalue.toString(), bindings);
                this.captures.addAll(pt.getCaptures());
                switch (pt.getType()) {
                    case literal:
                        statics.put(k, charvalue.toString());
                        protomap.put(k, charvalue.toString());
                        break;
                    case bindref:
                        String spec = pt.asBinding().orElseThrow().getBindspec();
                        if (spec == null) {
                            throw new OpConfigError("Empty binding spec for '" + k + "'");
                        }
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
            } else if (v instanceof Map mapvalue) {
                mapvalue.keySet().forEach(smk -> {
                    if (!CharSequence.class.isAssignableFrom(smk.getClass())) {
                        throw new OpConfigError("Only string keys are allowed in submaps.");
                    }
                });
                Map<String, Object> submap = (Map<String, Object>) v;
                ParsedTemplateMap subtpl = new ParsedTemplateMap(getName(),submap, bindings, cfgsources);
                this.captures.addAll(subtpl.getCaptures());
                if (subtpl.isStatic()) {
                    statics.put(k, submap);
                    protomap.put(k, submap);
                } else {
                    dynamics.put(k, subtpl);
                    protomap.put(k, null);
                }
            } else if (v instanceof List listvalue) {
                List<Object> sublist = listvalue;
                ParsedTemplateList subtpl = new ParsedTemplateList(sublist, bindings, cfgsources);
                this.captures.addAll(subtpl.getCaptures());
                if (subtpl.isStatic()) {
                    statics.put(k, sublist);
                    protomap.put(k, sublist);
                } else {
                    dynamics.put(k,subtpl);
                    protomap.put(k,null);
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

    public List<CapturePoint> getCaptures() {
        return this.captures;
    }

    /**
     * @return true if any field of this template map is dynamic
     */
    public boolean isDynamic() {
        return (dynamics.size() > 0);
    }

    public boolean isStatic() {
        return (dynamics.size() == 0);
    }

    public boolean isConfig(String fieldname) {
        for (Map<String, Object> cfgsource : this.cfgsources) {
            if (cfgsource.containsKey(fieldname)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> getStaticPrototype() {
        return statics;
    }

    public Map<String, LongFunction<?>> getDynamicPrototype() {
        return dynamics;
    }

    public Map<String, Object> getConfigPrototype() {
        Map<String,Object> cfgs = new LinkedHashMap<>();
        for (Map<String, Object> cfgsource : cfgsources) {
            for (String key : cfgsource.keySet()) {
                if (!cfgs.containsKey(key)) {
                    cfgs.put(key,cfgsource.get(key));
                } else {
                    logger.warn("config sources contain overlapping keys for '" + key + "', precedence is undefined");
                }
            }
        }
        return cfgs;
    }

    /**
     * create a map of op field names and values, containing all
     * statically and dynamically defined fields, but not including
     * auxilliary config like params or activity params.
     * @param value The input value to the binding functions
     * @return A {@link Map} of {@link String} to {@link Object}
     */
    @Override
    public Map<String, Object> apply(long value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(protomap);
        dynamics.forEach((k, v) -> map.put(k, v.apply(value)));
        return map;
    }

    public Map<String, Object> applyFull(long value) {
        Map<String, Object> newmap = apply(value);
        for (int i = cfgsources.size()-1; i>0 ; i--) {
            newmap.putAll(cfgsources.get(i));
        }
        return newmap;
    }

    /**
     * @return true if the specified field is found in the dynamic op fields
     */
    @Override
    public boolean isDynamic(String field) {
        return dynamics.containsKey(field);
    }

    /**
     * @return true if and only if the named field is present in the static field map.
     */
    public boolean isStatic(String field) {
        return statics.containsKey(field);
    }

    /**
     * @return true if and only if the named field is present in the static field map and the type is assignable to the specified class
     */
    public boolean isStatic(String field, Class<?> type) {
        return statics.containsKey(field) && type.isAssignableFrom(field.getClass());
    }

    /**
     * @param fields Names of fields to look for in the static, dynamic, or config field maps.
     * @return true if and only if all provided field names are present in the static or dynamic or config field maps.
     */
    @Override
    public boolean isDefined(String... fields) {
        for (String field : fields) {
            if (!isStatic(field)&&!isDynamic(field)&&!isConfig(field)) {
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
        if (isStatic(field)) {
            return (T) statics.get(field);
        } else if (isConfig(field)) {
            return getConfig(field);
        }
        return null;
    }

    private <T> T getConfig(String field) {
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(field)) {
                return (T) cfgsource.get(field);
            }
        }
        throw new OpConfigError("config value for '" +field +"' was not found in " + cfgsources);
    }

    public <T> T takeStaticValue(String field, Class<T> classOfT) {
        if (statics.containsKey(field)) {
            protomap.remove(field);
            T value = (T) statics.remove(field);
            return value;
        } else if (isConfig(field)) {
            return getConfig(field);
        }
        return null;
    }

    /**
     * Get the static value for the provided name, cast to the required type, where the type is inferred
     * from the calling context.
     *
     * @param field Name of the field to get
     * @param <T>   The parameter type of the return type. used at compile time only to quality return type.
     * @return A value of type T, or null
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getStaticValue(String field) {
        return (T) statics.get(field);
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
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getStaticValueOr(String name, T defaultValue) {
        if (isStatic(name)) {
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
     * @throws NBConfigError if the named field is defined dynamically,
     *                       as in this case, it is presumed that the parameter is not supported unless it is defined statically.
     */
    public <T> T getStaticConfigOr(String name, T defaultValue) {
        if (statics.containsKey(name)) {
            return NBTypeConverter.convertOr(statics.get(name), defaultValue);
        }
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(name)) {
                return NBTypeConverter.convertOr(cfgsource.get(name), defaultValue);
            }
        }
        if (dynamics.containsKey(name)) {
            throw new OpConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        } else {
            return defaultValue;
        }
    }

    public <T> T getStaticConfig(String name, Class<T> clazz) {
        if (statics.containsKey(name)) {
            return NBTypeConverter.convert(statics.get(name),clazz);
        }
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(name)) {
                return NBTypeConverter.convert(cfgsource.get(name),clazz);
            }
        }
        if (dynamics.containsKey(name)) {
            throw new OpConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        }
        throw new OpConfigError("static config field '" + name + "' was requested, but it does not exist");
    }

    public <T> T takeStaticConfigOr(String name, T defaultValue) {
        if (statics.containsKey(name)) {
            Object value = statics.remove(name);
            protomap.remove(name);
            return NBTypeConverter.convertOr(value, defaultValue);
        }
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(name)) {
                return NBTypeConverter.convertOr(cfgsource.get(name), defaultValue);
            }
        }
        if (dynamics.containsKey(name)) {
            throw new OpConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
                "updates the op mapper to support this field as a dynamic field, but it is not yet supported.");
        } else {
            return defaultValue;
        }
    }


    public <T> Optional<T> getOptionalStaticConfig(String name, Class<T> type) {
        if (isStatic(name)) {
            return Optional.of(NBTypeConverter.convert(getStaticConfig(name, type), type));
        }
        if (isConfig(name)) {
            return Optional.of(NBTypeConverter.convert(getConfig(name), type));
        }
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(name)) {
                return Optional.of(NBTypeConverter.convert(cfgsource.get(name), type));
            }
        }
        if (dynamics.containsKey("name")) {
            throw new OpConfigError("static config field '" + name + "' was defined dynamically. This may be supportable if the driver developer" +
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
        }
        for (Map<String, Object> cfgsource : cfgsources) {
            if (cfgsource.containsKey(name)) {
                return NBTypeConverter.convertOr(cfgsource.get(name), defaultValue);
            }
        }
        return defaultValue;

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
    public <T> Optional<T> getOptionalStaticValue(String field, Class<T> classOfT) {
        return Optional.ofNullable(getStaticValue(field, classOfT));
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
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String field, long input) {
        if (statics.containsKey(field)) {
            return (T) statics.get(field);
        }
        if (dynamics.containsKey(field)) {
            return (T) dynamics.get(field).apply(input);
        }
        if (isConfig(field)) {
            return getConfig(field);
        }
        return null;
    }

    /**
     * @return a set of names which are defined, whether in static fields or dynamic fields,
     * but NOT including params nor other config
     */
    public Set<String> getOpFieldNames() {
        HashSet<String> nameSet = new HashSet<>(statics.keySet());
        nameSet.addAll(dynamics.keySet());
        return nameSet;
    }

    /**
     * Get the op field as a {@link LongFunction} of String. This is a convenience form for
     * {@link #getAsRequiredFunction(String, Class)}
     *
     * @param name The field name which must be defined as static or dynamic
     * @return A function which can provide the named field value
     */
    public LongFunction<? extends String> getAsRequiredFunction(String name) {
        return getAsRequiredFunction(name, String.class);
    }

    public <V extends Enum<V>> Optional<LongFunction<V>> getAsOptionalEnumFunction(String name, Class<V> type) {
        Optional<LongFunction<String>> nameFunc = this.getAsOptionalFunction(name, String.class);
        return nameFunc.map((f) -> (l) -> Enum.valueOf(type, f.apply(l)));
    }

    /**
     * Get the op field as a {@link LongFunction}, using statics, then dynamics, then config sources.
     *
     * @param name The field name which must be defined as static or dynamic
     * @param type The value type which the field must be assignable to
     * @return A function which can provide a value for the given name and type
     */
    @SuppressWarnings("unchecked")
    public <V> Optional<LongFunction<V>> getAsOptionalFunction(String name, Class<? extends V> type) {
        if (isStatic(name)) {
            V value = getStaticValue(name);
            if (type.isAssignableFrom(value.getClass())) {
                return Optional.of((cycle) -> value);
            } else if (NBTypeConverter.canConvert(value, type)) {
                V converted = NBTypeConverter.convert(value, type);
                return Optional.of((cycle) -> converted);
            } else {
                throw new OpConfigError(
                    "function for '" + name + "' yielded a " + value.getClass().getCanonicalName()
                        + " type, which is not assignable to a '" + type.getCanonicalName() + "'"
                );
            }
        } else if (isDynamic(name)) {
            Object testValue = dynamics.get(name).apply(0L);
            if (type.isAssignableFrom(testValue.getClass())) {
                return Optional.of((LongFunction<V>) dynamics.get(name));
            } else if (NBTypeConverter.canConvert(testValue, type)) {
                return Optional.of(l -> NBTypeConverter.convert(dynamics.get(name).apply(l), type));
            } else {
                throw new OpConfigError(
                    "function for '" + name + "' yielded a " + testValue.getClass().getCanonicalName()
                        + " type, which is not assignable to '" + type.getCanonicalName() + "'");
            }
        } else {
            if (isConfig(name)) {
                Object cfgval = getConfig(name);
                if (type.isAssignableFrom(cfgval.getClass())) {
                    return Optional.of(l -> type.cast(cfgval));
                } else if (NBTypeConverter.canConvert(cfgval,type)) {
                    return Optional.of(l -> NBTypeConverter.convert(cfgval, type));
                } else {
                    throw new OpConfigError(
                        "function for '" + name + "' found a " + cfgval.getClass().getCanonicalName()
                            + " type in cfg source, which is not assignable to '" + type.getCanonicalName() + "'");

                }
            }
            return Optional.empty();
        }
    }

    public <V> LongFunction<V> getAsRequiredFunction(String name, Class<? extends V> type) {
        Optional<? extends LongFunction<V>> sf = getAsOptionalFunction(name, type);
        return sf.orElseThrow(() -> new OpConfigError("The op field '" + name + "' is required, but it wasn't found in the op template."));
    }


    /**
     * Get a LongFunction which returns either the static value, the dynamic value, or the default value,
     * in that order, depending on where it is found first.
     *
     * @param name         The param name for the value
     * @param defaultValue The default value to provide the value is not defined for static nor dynamic
     * @param <V>          The type of value to return
     * @return A {@link LongFunction} of type V
     */
    @Override
    public <V> LongFunction<V> getAsFunctionOr(String name, V defaultValue) {
        if (isDynamic(name)) {
            return l -> get(name, l);
        } else if (isStatic(name)) {
            V value = getStaticValue(name);
            return l -> value;
        } else if (isConfig(name)) {
            return l -> getConfig(name);
        } else {
            return l -> defaultValue;
        }
    }

    /**
     * Get a LongFunction that first creates a LongFunction of String as in {@link #getAsFunctionOr(String, Object)} )}, but then
     * applies the result and cached it for subsequent access. This relies on {@link ObjectCache} internally.
     * The purpose of this is to avoid costly re-computation for mapped values over pure functions where the computation
     * cost is significantly high. For trivial functions, the cost is generally lower than the hash lookup within the
     * object cache..
     *
     * @param fieldname    The name of the field which could contain a static or dynamic value
     * @param defaultValue The default value to use in the init function if the fieldname is not defined as static nor dynamic
     * @param init         A function to apply to the value to produce the product type
     * @param <V>          The type of object to return
     * @return A caching function which chains to the init function, with caching
     */
    public <V> LongFunction<V> getAsCachedFunctionOr(String fieldname, String defaultValue, Function<String, V> init) {
        // caching is only valid for the dynamic case, everything else can elide it
        if (isDynamic(fieldname)) {
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
        }
        if (isStatic(fieldname)) {
            V value = getStaticValue(fieldname);
            if (value instanceof String s) {
                V defaultObject = init.apply(s);
                return l -> defaultObject;
            } else {
                throw new OpConfigError("Unable to compose string to object cache with non-String value of type " + defaultValue.getClass().getCanonicalName());
            }
        }
        if (isConfig(fieldname)) {
            V value = getConfig(fieldname);
            if (value instanceof String s) {
                V defaultObject = init.apply(s);
                return l -> defaultObject;
            } else {
                throw new OpConfigError("Unable to compose string to object cache with non-String value of type " + defaultValue.getClass().getCanonicalName());
            }
        } else {
            V defaultObject = init.apply(defaultValue);
            return l -> defaultObject;
        }
    }


    /**
     * @param field The requested field name
     * @return true if the named field is defined as static or dynamic or config (params and activity params)
     */
    public boolean isDefined(String field) {
        return statics.containsKey(field) || dynamics.containsKey(field) || isConfig(field);
    }

    /**
     * Inverse of {@link #isDefined(String)}, provided for clarify in some situations
     *
     * @param field The field name
     * @return true if the named field is defined neither as static nor as dynamic
     */
    public boolean isUndefined(String field) {
        return !(statics.containsKey(field) || dynamics.containsKey(field) || isConfig(field));
    }

    /**
     * @param field The requested field name
     * @param type  The required type of the field value
     * @return true if the named field is defined as static or dynamic and the value produced can be assigned to the specified type
     */
    @Override
    public boolean isDefined(String field, Class<?> type) {
        if (isDynamic(field)) {
            Object testObject = dynamics.get(field).apply(0L);
            if (type.isAssignableFrom(testObject.getClass())) {
                return true;
            } else {
                throw new OpConfigError("field " + field + " was defined as a function, but not one that returns the" +
                    " requested type " + testObject.getClass().getCanonicalName());
            }
        }
        if (isStatic(field)) {
            if (type.isAssignableFrom(statics.get(field).getClass())) {
                return true;
            } else {
                throw new OpConfigError("field " + field + " was defined (static), but not as the requested type " + type.getCanonicalName());
            }
        }
        if (isConfig(field)) {
            if (type.isAssignableFrom(getConfig(field).getClass())) {
                return true;
            } else {
                throw new OpConfigError("field " + field + " was defined (config), but not as the requested type " + type.getCanonicalName());
            }
        }
        return false;
    }

    public Optional<ParsedTemplateString> takeAsOptionalStringTemplate(String field) {
        Optional<ParsedTemplateString> asStringTemplate = this.getAsStringTemplate(field);
        if (asStringTemplate.isPresent()) {
            originalTemplateObject.remove(field);
            return asStringTemplate;
        }
        return Optional.empty();
    }

    public <V> Optional<V> takeAsOptionalRawSpecifier(String field) {
        if (dynamics.containsKey(name)) {
            Object value = statics.remove(name);
            protomap.remove(name);
            return (Optional<V>) Optional.of(value);
        }
        if (statics.containsKey(name)) {
            Object value = statics.remove(name);
            protomap.remove(name);
            return (Optional<V>) Optional.of(value);
        }
        return Optional.empty();
    }

    /**
     * Take the value of the specified field from the dynamic or static layers, or reference it
     * from the config layer without removal. Then, flatten any string, list, or map structures
     * into a map of strings with names injected as needed. Then, convert the values to string
     * templates and return that.
     * @param fieldname the field to take the templates from
     * @return A map of templates, or an empty map if the field is not defined or is empty.
     */
    public Map<String,ParsedTemplateString> takeAsNamedTemplates(String fieldname) {
        Object entry = originalTemplateObject.get(fieldname);
        if (entry !=null) {
            dynamics.remove(fieldname);
            statics.remove(fieldname);
            protomap.remove(fieldname);
        }

        if (entry==null) {
            for (Map<String, Object> cfgsource : cfgsources) {
                if (cfgsource.containsKey(fieldname)) {
                    entry = cfgsource.get(fieldname);
                    break;
                }
            }
        }

        if (entry==null) {
            return Map.of();
        }

        Map<String,Object> elements = new LinkedHashMap<>();
        if (entry instanceof CharSequence chars) {
            elements.put(this.getName()+"-verifier-0",chars.toString());
        } else if (entry instanceof List list) {
            for (int i = 0; i < list.size(); i++) {
                elements.put(this.getName()+"-verifier-"+i,list.get(0));
            }
        } else if (entry instanceof Map map) {
            map.forEach((k,v) -> {
                elements.put(this.getName()+"-verifier-"+k,v);
            });
        }
        Map<String,ParsedTemplateString> parsedStringTemplates
            = new LinkedHashMap<>();
        elements.forEach((k,v) -> {
            if (v instanceof CharSequence chars) {
                parsedStringTemplates.put(k,new ParsedTemplateString(chars.toString(), this.bindings));
            }
        });
        return parsedStringTemplates;
    }


    public Optional<ParsedTemplateString> getAsStringTemplate(String fieldname) {
        if (originalTemplateObject.containsKey(fieldname)) {
            Object fval = originalTemplateObject.get(fieldname);
            if (fval instanceof CharSequence) {
                return Optional.of(new ParsedTemplateString(fval.toString(), this.bindings));
            } else {
                throw new RuntimeException("Can not make a parsed text template from op template field '" + fieldname + "' of type '" + fval.getClass().getSimpleName() + "'");
            }
        }
        return Optional.empty();
    }

    /**
     * convenience method for conjugating {@link #isDefined(String)} with AND
     * @return true if all specified fields are defined as static or dynamic or config
     */
    public boolean isDefinedAll(String... fields) {
        for (String field : fields) {
            if (!isDefined(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param fields The ordered field names for which the {@link ListBinder} will be created
     * @return a new {@link ListBinder} which can produce a {@link List} of Objects from a long input.
     */
    public LongFunction<List<Object>> newListBinder(String... fields) {
        return new ListBinder(this, fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ListBinder} will be created
     * @return a new {@link ListBinder} which can produce a {@link List} of Objects from a long input.
     */
    public LongFunction<List<Object>> newListBinder(List<String> fields) {
        return new ListBinder(this, fields);
    }

    /**
     * @param fields The ordered field names for which the {@link OrderedMapBinder} will be created
     * @return a new {@link OrderedMapBinder} which can produce a {@link Map} of String to Objects from a long input.
     */
    public LongFunction<Map<String, Object>> newOrderedMapBinder(String... fields) {
        return new OrderedMapBinder(this, fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinder(String... fields) {
        return new ArrayBinder(this, fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinder(List<String> fields) {
        return new ArrayBinder(this, fields);
    }

    /**
     * @param bindPoints The {@link BindPoint}s for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinderFromBindPoints(List<BindPoint> bindPoints) {
        return new ArrayBinder(bindPoints);
    }

    /**
     * Get the {@link LongFunction} which is used to resolve a dynamic field value.
     *
     * @param field The field name for a dynamic parameter
     * @return The mapping function
     */
    public LongFunction<?> getMapper(String field) {
        LongFunction<?> mapper = dynamics.get(field);
        return mapper;
    }

    /**
     * @return the logical map size, including all static and dynamic fields
     */
    public int getSize() {
        return this.mapsize;
    }


    public Class<?> getValueType(String fieldname) {
        if (isDynamic(fieldname)) {
            return get(fieldname,1).getClass();
        }
        if (isStatic(fieldname)) {
            return getStaticValue(fieldname).getClass();
        }
        if (isConfig(fieldname)) {
            return getConfig(fieldname).getClass();
        }
        throw new OpConfigError("Unable to determine value type for undefined op field '" + fieldname + "'");
    }

    public <E extends Enum<E>, V> Optional<TypeAndTarget<E, V>> getOptionalTargetEnum(
        Class<E> enumclass,
        String typeFieldName,
        String valueFieldName,
        Class<V> valueClass
    ) {
        if (isStatic(typeFieldName)) {
            String enumValue = statics.get(typeFieldName).toString();
            E verifiedEnumValue;

            try {
                verifiedEnumValue = Enum.valueOf(enumclass, enumValue);
            } catch (IllegalArgumentException iae) {
                throw new OpConfigError("type designator field '" + typeFieldName + "' had value of '" + enumValue + ", but this failed to match " +
                    "any of known types in " + EnumSet.allOf(enumclass));
            }

            if (isDefined(valueFieldName)) {
                if (isStatic(typeFieldName)) {
                    return Optional.of(
                        new TypeAndTarget<E, V>(verifiedEnumValue, typeFieldName, l -> NBTypeConverter.convert(statics.get(valueFieldName), valueClass))
                    );
                } else if (isDynamic(valueFieldName)) {
                    return Optional.of(
                        new TypeAndTarget<E, V>(verifiedEnumValue, typeFieldName, getAsRequiredFunction(valueFieldName, valueClass))
                    );
                }
            }
        } else if (isDynamic(typeFieldName)) {
            throw new OpConfigError("The op template field '" + typeFieldName + "' must be a static value. You can not vary it by cycle.");
        }

        return Optional.empty();
    }

    public <E extends Enum<E>, V> Optional<TypeAndTarget<E, V>> getOptionalTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass,
        String alternateTypeField,
        String alternateValueField
    ) {
        Optional<TypeAndTarget<E, V>> result = getOptionalTargetEnum(enumclass, valueClass);
        if (result.isPresent()) {
            return result;
        }
        return getOptionalTargetEnum(enumclass, alternateTypeField, alternateValueField, valueClass);
    }

    /**
     * Given an enum of any type, return the enum value which is found in any of the field names of the op template,
     * ignoring case and any non-word characters. This is useful for matching op templates to op types where the presence
     * of a field determines the type. Further, if there are multiple matching names, an {@link OpConfigError} is thrown to
     * avoid possible ambiguity.
     *
     * @param enumclass The enum class for matching values
     * @param <E>       Generic type for the enum class
     * @return Optionally, an enum value which matches, or {@link Optional#empty()}
     * @throws OpConfigError if more than one field matches
     */
    public <E extends Enum<E>, V> Optional<TypeAndTarget<E, V>> getOptionalTargetEnum(
        Class<E> enumclass,
        Class<? extends V> valueClass
    ) {
        List<TypeAndTarget<E, V>> matched = new ArrayList<>();
        for (E e : EnumSet.allOf(enumclass)) {
            String lowerenum = e.name().toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
            for (String s : statics.keySet()) {
                String lowerkey = s.toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
                if (lowerkey.equals(lowerenum)) {
                    matched.add(new TypeAndTarget<>(e, s, null));
                }
            }
            for (String s : dynamics.keySet()) {
                String lowerkey = s.toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
                if (lowerkey.equals(lowerenum)) {
                    matched.add(new TypeAndTarget<>(e, s, null));
                }
            }
        }
        if (matched.size() == 1) {
            TypeAndTarget<E, V> prototype = matched.get(0);
            LongFunction<V> asFunction = getAsRequiredFunction(prototype.field, valueClass);
            return Optional.of(new TypeAndTarget<E, V>(prototype.enumId, prototype.field, asFunction));
        } else if (matched.size() > 1) {
            throw new OpConfigError("Multiple matches were found from op template fieldnames ["
                + getOpFieldNames() + "] to possible enums: [" + EnumSet.allOf(enumclass) + "]");
        }

        return Optional.empty();
    }

    public <E extends Enum<E>, V> TypeAndTarget<E, V> getTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass,
        String tname,
        String vname
    ) {
        Optional<TypeAndTarget<E, V>> optionalMappedEnum = getOptionalTargetEnum(enumclass, valueClass);
        if (optionalMappedEnum.isPresent()) {
            return optionalMappedEnum.get();
        }
        Optional<TypeAndTarget<E, V>> optionalSpecifiedEnum = getOptionalTargetEnum(enumclass, tname, vname, valueClass);
        if (optionalSpecifiedEnum.isPresent()) {
            return optionalSpecifiedEnum.get();
        }
        throw new OpConfigError("While mapping op template named '" + this.getName() + "', Unable to map the type and target for possible values " + EnumSet.allOf(enumclass) + " either by key or by fields " + tname + " and " + vname + ". " +
            "Fields considered: static:" + statics.keySet() + " dynamic:" + dynamics.keySet());
    }

    private String getName() {
        return name;
    }

    public <E extends Enum<E>, V> TypeAndTarget<E, V> getTargetEnum(Class<E> enumclass, Class<V> valueClass) {
        Optional<TypeAndTarget<E, V>> typeFromEnum = getOptionalTargetEnum(enumclass, valueClass);

        return typeFromEnum.orElseThrow(
            () -> {
                String values = EnumSet.allOf(enumclass).toString();
                Set<String> definedNames = getOpFieldNames();
                return new OpConfigError("Unable to match op template fields [" + definedNames + "] with " +
                    "possible op types [" + values + "]. " +
                    "If you are specifying an op type which should be implemented, please file an issue.");
            }
        );
    }


    /**
     * Map a named op field to an enum
     *
     * @param enumclass The type of enum to look within
     * @param fieldname The field name to look for
     * @param <E>       The generic type of the enum
     * @return An optional enum value
     */
    public <E extends Enum<E>> Optional<E> getOptionalEnumFromField(Class<E> enumclass, String fieldname) {

        Optional<String> enumField = getOptionalStaticConfig(fieldname, String.class);
        if (enumField.isEmpty()) {
            return Optional.empty();
        }
        String lowerv = enumField.get().toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");

        List<E> matched = new ArrayList<>();
        for (E e : EnumSet.allOf(enumclass)) {
            String lowerenum = e.name().toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
            if (lowerv.equals(lowerenum)) {
                matched.add(e);
            }
        }
        if (matched.size() == 1) {
            return Optional.of(matched.get(0));
        }
        if (matched.size() > 1) {
            throw new OpConfigError("Multiple matches were found from op template fieldnames ["
                + getOpFieldNames() + "] to possible enums: [" + EnumSet.allOf(enumclass) + "]");
        }
        return Optional.empty();
    }


    public Map<String, Object> parseStaticCmdMap(String taskname, String mainField) {
        Object mapsrc = getStaticValue(taskname);
        return new LinkedHashMap<String,Object>(ParamsParser.parseToMap(mapsrc,mainField));
    }

    public List<Map<String, Object>> parseStaticCmdMaps(String key, String mainField) {
        Object mapsSrc = getStaticValue(key);
        List<Map<String,Object>> maps = new ArrayList<>();
        for (String spec : mapsSrc.toString().split("; +")) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>(ParamsParser.parseToMap(spec, mainField));
            maps.add(map);
        }
        return maps;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("protomap:\n");
        for (String k : this.protomap.keySet()) {
            Object v = this.protomap.get(k);
            sb.append(" ")
                .append(k)
                .append("->")
                .append(
                    v ==null? originalTemplateObject.get(k) : v.toString()
                ).append("\n");

        }
        return sb.toString();
    }


}
