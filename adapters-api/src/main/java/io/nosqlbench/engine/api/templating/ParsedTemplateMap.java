package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.templating.binders.ArrayBinder;
import io.nosqlbench.engine.api.templating.binders.ListBinder;
import io.nosqlbench.engine.api.templating.binders.OrderedMapBinder;
import io.nosqlbench.nb.api.config.fieldreaders.DynamicFieldReader;
import io.nosqlbench.nb.api.config.fieldreaders.StaticFieldReader;
import io.nosqlbench.nb.api.config.standard.NBConfigError;
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
 * A parsed map template, which allows construction of extracted or projected functions related
 * to dynamic value templates.
 *
 * The provided map is taken as a map of string to object templates using these rules:
 * <OL>
 * <LI>If the value is a String and contains no binding points, it is taken as a literal</LI>
 * <LI>If the value is a String and contains only a binding point with no leading nor trailing text, it is taken as an object binding</LI>
 * <LI>If the value is a String and contains a binding point with any leading or trailing text, it is taken as a String template binding</LI>
 * <LI>If the value is a map, list, or set, then each element is interpreted as above</LI>
 * </OL>
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
    private final List<List<CapturePoint>> captures = new ArrayList<>();
    private final int mapsize;

    /**
     * A prototype of the fully generated map, to be used as the starting point
     * when rendering the full map with dynamic values.
     */
    private final LinkedHashMap<String, Object> protomap = new LinkedHashMap<>();
    private final List<Map<String, Object>> cfgsources;
    private Map<String, Object> specmap;
    private Map<String, String> bindings;

    public ParsedTemplateMap(Map<String, Object> map, Map<String, String> bindings, List<Map<String, Object>> cfgsources) {
        this.cfgsources = cfgsources;
        applyTemplateFields(map, bindings);
        mapsize = statics.size() + dynamics.size();
    }

    // For now, we only allow bind points to reference bindings, not other op template
    // fields. This seems like the saner and less confusing approach, so implementing
    // op field references should be left until it is requested if at all
    private void applyTemplateFields(Map<String, Object> map, Map<String, String> bindings) {
        this.specmap = map;
        this.bindings = bindings;
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
            } else if (v instanceof Map) {
                ((Map) v).keySet().forEach(smk -> {
                    if (!CharSequence.class.isAssignableFrom(smk.getClass())) {
                        throw new OpConfigError("Only string keys are allowed in submaps.");
                    }
                });
                Map<String, Object> submap = (Map<String, Object>) v;
                ParsedTemplateMap subtpl = new ParsedTemplateMap(submap, bindings, cfgsources);
                if (subtpl.isStatic()) {
                    statics.put(k, submap);
                    protomap.put(k, submap);
                } else {
                    dynamics.put(k, subtpl);
                    protomap.put(k, null);
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

    /**
     * @return true if any field of this template map is dynamic
     */
    public boolean isDynamic() {
        return (dynamics.size() > 0);
    }

    public boolean isStatic() {
        return (dynamics.size() == 0);
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
        dynamics.forEach((k, v) -> map.put(k, v.apply(value)));
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

    public <T> Optional<T> getOptionalStaticConfig(String name, Class<T> type) {
        if (statics.containsKey(name)) {
            return Optional.of(NBTypeConverter.convert(statics.get(name), type));
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
        return null;
    }

    /**
     * @return a set of names which are defined, whether in static fields or dynamic fields
     */
    public Set<String> getDefinedNames() {
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

    /**
     * Get the op field as a {@link LongFunction}
     *
     * @param name The field name which must be defined as static or dynamic
     * @param type The value type which the field must be assignable to
     * @return A function which can provide a value for the given name and type
     */
    @SuppressWarnings("unchecked")
    public <V> Optional<LongFunction<V>> getAsOptionalFunction(String name, Class<? extends V> type) {
        if (isStatic(name)) {
            V value = getStaticValue(name);
            return Optional.of((cycle) -> value);
        } else if (isDefinedDynamic(name)) {
            Object testValue = dynamics.get(name).apply(0L);
            if (type.isAssignableFrom(testValue.getClass())) {
                return Optional.of((LongFunction<V>) dynamics.get(name));
            } else {
                throw new OpConfigError(
                    "function for '" + name + "' yielded a " + testValue.getClass().getCanonicalName()
                        + " type, which is not assignable to " + type.getCanonicalName() + "'");
            }
        } else {
            return Optional.empty();
        }
    }

    public <V> LongFunction<? extends V> getAsRequiredFunction(String name, Class<? extends V> type) {
        Optional<? extends LongFunction<? extends V>> sf = getAsOptionalFunction(name, type);
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
        if (isStatic(name)) {
            V value = getStaticValue(name);
            return l -> value;
        } else if (isDefinedDynamic(name)) {
            return l -> get(name, l);
        } else {
            return l -> defaultValue;
        }
    }

    /**
     * Get a LongFunction that first creates a LongFunction of String as in {@link #getAsFunctionOr(String, Object)} )}, but then
     * applies the result and cached it for subsequent access. This relies on {@link ObjectCache} internally.
     *
     * @param fieldname    The name of the field which could contain a static or dynamic value
     * @param defaultValue The default value to use in the init function if the fieldname is not defined as static nor dynamic
     * @param init         A function to apply to the value to produce the product type
     * @param <V>          The type of object to return
     * @return A caching function which chains to the init function, with caching
     */
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

    /**
     * @param field The requested field name
     * @return true if the named field is defined as static or dynamic
     */
    public boolean isDefined(String field) {
        return statics.containsKey(field) || dynamics.containsKey(field);
    }

    /**
     * Inverse of {@link #isDefined(String)}, provided for clarify in some situations
     *
     * @param field The field name
     * @return true if the named field is defined neither as static nor as dynamic
     */
    public boolean isUndefined(String field) {
        return !(statics.containsKey(field) || dynamics.containsKey(field));
    }

    /**
     * @param field The requested field name
     * @param type  The required type of the field value
     * @return true if the named field is defined as static or dynamic and the value produced can be assigned to the specified type
     */
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

    public Optional<ParsedTemplate> getAsTemplate(String fieldname) {
        if (specmap.containsKey(fieldname)) {
            Object fval = specmap.get(fieldname);
            if (fval instanceof CharSequence) {
                return Optional.of(new ParsedTemplate(fval.toString(),this.bindings));
            } else {
                throw new RuntimeException("Can not make a parsed text template from op template field '" + fieldname +"' of type '" + fval.getClass().getSimpleName() + "'");
            }
        }
        return Optional.empty();
    }

    /**
     * convenience method for conjugating {@link #isDefined(String)} with AND
     *
     * @param fields The fields which should be defined as either static or dynamic
     * @return true if all specified fields are defined as static or dynamic
     */
    public boolean isDefinedAll(String... fields) {
        for (String field : fields) {
            if (!statics.containsKey(field) && !dynamics.containsKey(field)) {
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
        if (isDefined(fieldname)) {
            if (isStatic(fieldname)) {
                return statics.get(fieldname).getClass();
            } else {
                return dynamics.get(fieldname).apply(1L).getClass();
            }
        } else {
            throw new OpConfigError("Unable to determine value type for undefined op field '" + fieldname + "'");
        }

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
    public <E extends Enum<E>> Optional<NamedTarget<E>> getOptionalTypeFromEnum(Class<E> enumclass) {
        List<NamedTarget<E>> matched = new ArrayList<>();
        for (E e : EnumSet.allOf(enumclass)) {
            String lowerenum = e.name().toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
            for (String s : statics.keySet()) {
                String lowerkey = s.toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
                if (lowerkey.equals(lowerenum)) {
                    matched.add(new NamedTarget<>(e, s, null));
                }
            }
            for (String s : dynamics.keySet()) {
                String lowerkey = s.toLowerCase(Locale.ROOT).replaceAll("[^\\w]", "");
                if (lowerkey.equals(lowerenum)) {
                    matched.add(new NamedTarget<>(e, s, null));
                }
            }
        }
        if (matched.size() == 1) {
            NamedTarget<E> prototype = matched.get(0);
            LongFunction<? extends String> asFunction = getAsRequiredFunction(prototype.field);
            return Optional.of(new NamedTarget<>(prototype.enumId, prototype.field, asFunction));
        }
        if (matched.size() > 1) {
            throw new OpConfigError("Multiple matches were found from op template fieldnames ["
                + getDefinedNames() + "] to possible enums: [" + EnumSet.allOf(enumclass) + "]");
        }
        return Optional.empty();
    }

    public <E extends Enum<E>> NamedTarget<E> getRequiredTypeFromEnum(Class<E> enumclass) {
        Optional<NamedTarget<E>> typeFromEnum = getOptionalTypeFromEnum(enumclass);

        return typeFromEnum.orElseThrow(
            () -> {
                String values = EnumSet.allOf(enumclass).toString();
                Set<String> definedNames = getDefinedNames();
                return new OpConfigError("Unable to match op template fields [" + definedNames + "] with " +
                    "possible op types [" + values + "]. " +
                    "If you are specifying an op type which should be implemented, please file an issue.");
            }
        );
    }


    /**
     * Map a named op field to an enum
     *
     * @param enumclass   The type of enum to look within
     * @param fieldname   The field name to look for
     * @param <E>         The generic type of the enum
     * @return An optional enum value
     */
    public <E extends Enum<E>> Optional<E> getOptionalEnumFromField(Class<E> enumclass,String fieldname) {

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
                + getDefinedNames() + "] to possible enums: [" + EnumSet.allOf(enumclass) + "]");
        }
        return Optional.empty();
    }

}
