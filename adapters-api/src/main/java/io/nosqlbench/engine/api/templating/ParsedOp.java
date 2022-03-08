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

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.binders.ArrayBinder;
import io.nosqlbench.engine.api.templating.binders.ListBinder;
import io.nosqlbench.engine.api.templating.binders.OrderedMapBinder;
import io.nosqlbench.nb.api.config.fieldreaders.DynamicFieldReader;
import io.nosqlbench.nb.api.config.fieldreaders.StaticFieldReader;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Parse an OpTemplate into a ParsedOp, which can dispense object maps
 */
public class ParsedOp implements LongFunction<Map<String, ?>>, StaticFieldReader, DynamicFieldReader {

    private final static Logger logger = LogManager.getLogger(ParsedOp.class);

    /**
     * The names of payload values in the result of the operation which should be saved.
     * The keys in this map represent the name of the value as it would be found in the native
     * representation of a result. If the values are defined, then each one represents the name
     * that the found value should be saved as instead of the original name.
     */
    private final List<List<CapturePoint>> captures = new ArrayList<>();

    private final OpTemplate _opTemplate;
    private final NBConfiguration activityCfg;
    private final ParsedTemplateMap tmap;

    /**
     * Create a parsed command from an Op template.
     *
     * @param ot          An OpTemplate representing an operation to be performed in a native driver.
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
     * @param opTemplate    The OpTemplate as provided by a user via YAML, JSON, or API (data structure)
     * @param activityCfg   The activity configuration, used to resolve nested config parameters
     * @param preprocessors Map->Map transformers.
     */
    public ParsedOp(
        OpTemplate opTemplate,
        NBConfiguration activityCfg,
        List<Function<Map<String, Object>, Map<String, Object>>> preprocessors
    ) {
        this._opTemplate = opTemplate;
        this.activityCfg = activityCfg;

        Map<String, Object> map = opTemplate.getOp().orElseThrow(() ->
            new OpConfigError("ParsedOp constructor requires a non-null value for the op field, but it was missing."));
        for (Function<Map<String, Object>, Map<String, Object>> preprocessor : preprocessors) {
            map = preprocessor.apply(map);
        }

        this.tmap = new ParsedTemplateMap(
            map,
            opTemplate.getBindings(),
            List.of(opTemplate.getParams(),
            activityCfg.getMap())
        );

    }


    public String getName() {
        return _opTemplate.getName();
    }

    public ParsedTemplateMap getTemplateMap() {
        return tmap;
    }

    @Override
    public Map<String, Object> apply(long value) {
        return tmap.apply(value);
    }

    @Override
    public boolean isDynamic(String field) {
        return tmap.isDynamic(field);
    }


    /**
     * @param field The field name to look for in the static field map.
     * @return true if and only if the named field is present in the static field map.
     */
    public boolean isStatic(String field) {
        return tmap.isStatic(field);
    }

    public boolean isStatic(String field, Class<?> type) {
        return tmap.isStatic(field, type);
    }

    /**
     * @param fields Names of fields to look for in the static field map.
     * @return true if and only if all provided field names are present in the static field map.
     */
    @Override
    public boolean isDefined(String... fields) {
        return tmap.isDefined(fields);
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
        return tmap.getStaticValue(field, classOfT);
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
        return tmap.getStaticValue(field);
    }

//    public Optional<ParsedTemplate> getStmtAsTemplate() {
//        return _opTemplate.getParsed();
//    }


    public Optional<ParsedTemplate> getAsTemplate(String fieldname) {
        return this.tmap.getAsTemplate(fieldname);
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
        return tmap.getStaticValueOr(name, defaultValue);
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
        return tmap.getStaticConfigOr(name, defaultValue);
    }

    public <T> Optional<T> getOptionalStaticConfig(String name, Class<T> type) {
        return tmap.getOptionalStaticConfig(name, type);
    }


    /**
     * Works exactly like {@link #getStaticConfigOr(String, Object)}, except that dynamic values
     * at the op field level will be generated on a per-input basis. This is a shortcut method for
     * allowing configuration values to be accessed dynamically where it makes sense.
     */
    public <T> T getConfigOr(String name, T defaultValue, long input) {
        return tmap.getConfigOr(name, defaultValue, input);
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
        return Optional.ofNullable(tmap.getStaticValue(field, classOfT));
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
        return tmap.get(field, input);
    }

    /**
     * @return a set of names which are defined, whether in static fields or dynamic fields
     */
    public Set<String> getDefinedNames() {
        return tmap.getDefinedNames();
    }

    /**
     * Get the op field as a {@link LongFunction} of String. This is a convenience form for
     * {@link #getAsRequiredFunction(String, Class)}
     *
     * @param name The field name which must be defined as static or dynamic
     * @return A function which can provide the named field value
     */
    public LongFunction<? extends String> getAsRequiredFunction(String name) {
        return tmap.getAsRequiredFunction(name, String.class);
    }

    /**
     * Get the op field as a {@link LongFunction}
     *
     * @param name The field name which must be defined as static or dynamic
     * @param type The value type which the field must be assignable to
     * @return A function which can provide a value for the given name and type
     */
    public <V> Optional<LongFunction<V>> getAsOptionalFunction(String name, Class<V> type) {
        return tmap.getAsOptionalFunction(name, type);
    }
    public <V extends Enum<V>> Optional<LongFunction<V>> getAsOptionalEnumFunction(String name, Class<V> type) {
        return tmap.getAsOptionalEnumFunction(name, type);
    }

    public <V> Optional<LongFunction<String>> getAsOptionalFunction(String name) {
        return this.getAsOptionalFunction(name, String.class);
    }

    public <V> LongFunction<V> getAsRequiredFunction(String name, Class<? extends V> type) {
        return tmap.getAsRequiredFunction(name, type);
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
        return tmap.getAsFunctionOr(name, defaultValue);
    }

    /**
     * Get a LongFunction that first creates a LongFunction of String as in {@link #getAsRequiredFunction(String, Class)}, but then
     * applies the result and caches it for subsequent access. This relies on {@link ObjectCache} internally.
     *
     * @param fieldname    The name of the field which could contain a static or dynamic value
     * @param defaultValue The default value to use in the init function if the fieldname is not defined as static nor dynamic
     * @param init         A function to apply to the value to produce the product type
     * @param <V>          The type of object to return
     * @return A caching function which chains to the init function, with caching
     */
    public <V> LongFunction<V> getAsCachedFunctionOr(String fieldname, String defaultValue, Function<String, V> init) {
        return tmap.getAsCachedFunctionOr(fieldname, defaultValue, init);
    }

    /**
     * @param field The requested field name
     * @return true if the named field is defined as static or dynamic
     */
    public boolean isDefined(String field) {
        return tmap.isDefined(field);
    }

    /**
     * Inverse of {@link #isDefined(String)}, provided for clarify in some situations
     *
     * @param field The field name
     * @return true if the named field is defined neither as static nor as dynamic
     */
    public boolean isUndefined(String field) {
        return tmap.isUndefined(field);
    }

    /**
     * @param field The requested field name
     * @param type  The required type of the field value
     * @return true if the named field is defined as static or dynamic and the value produced can be assigned to the specified type
     */
    @Override
    public boolean isDefined(String field, Class<?> type) {
        return tmap.isDefined(field, type);
    }

    /**
     * convenience method for conjugating {@link #isDefined(String)} with AND
     *
     * @param fields The fields which should be defined as either static or dynamic
     * @return true if all specified fields are defined as static or dynamic
     */
    public boolean isDefinedAll(String... fields) {
        return tmap.isDefinedAll(fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ListBinder} will be created
     * @return a new {@link ListBinder} which can produce a {@link List} of Objects from a long input.
     */
    public LongFunction<List<Object>> newListBinder(String... fields) {
        return tmap.newListBinder(fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ListBinder} will be created
     * @return a new {@link ListBinder} which can produce a {@link List} of Objects from a long input.
     */
    public LongFunction<List<Object>> newListBinder(List<String> fields) {
        return tmap.newListBinder(fields);
    }

    /**
     * @param fields The ordered field names for which the {@link OrderedMapBinder} will be created
     * @return a new {@link OrderedMapBinder} which can produce a {@link Map} of String to Objects from a long input.
     */
    public LongFunction<Map<String, Object>> newOrderedMapBinder(String... fields) {
        return tmap.newOrderedMapBinder(fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinder(String... fields) {
        return tmap.newArrayBinder(fields);
    }

    /**
     * @param fields The ordered field names for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinder(List<String> fields) {
        return tmap.newArrayBinder(fields);
    }

    /**
     * @param bindPoints The {@link BindPoint}s for which the {@link ArrayBinder} will be created
     * @return a new {@link ArrayBinder} which can produce a {@link Object} array from a long input.
     */
    public LongFunction<Object[]> newArrayBinderFromBindPoints(List<BindPoint> bindPoints) {
        return tmap.newArrayBinderFromBindPoints(bindPoints);
    }

    /**
     * Get the {@link LongFunction} which is used to resolve a dynamic field value.
     *
     * @param field The field name for a dynamic parameter
     * @return The mapping function
     */
    public LongFunction<?> getMapper(String field) {
        return tmap.getMapper(field);
    }

    /**
     * @return the logical map size, including all static and dynamic fields
     */
    public int getSize() {
        return tmap.getSize();
    }

    public Class<?> getValueType(String fieldname) {
        return tmap.getValueType(fieldname);
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
    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getTypeFromEnum(Class<E> enumclass, Class<V> valueClass) {
        return tmap.getOptionalTargetEnum(enumclass,valueClass);
    }

    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getOptionalTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass
    ){
        return tmap.getOptionalTargetEnum(enumclass,valueClass);
    }

    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getOptionalTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass,
        String alternateTypeField,
        String alternateValueField
    ) {
        return tmap.getOptionalTargetEnum(enumclass, valueClass, alternateTypeField, alternateValueField);
    }

    public <E extends Enum<E>,V> TypeAndTarget<E,V> getTargetEnum(Class<E> enumclass, Class<V> valueClass) {
        return tmap.getTargetEnum(enumclass, valueClass);
    }

    public <E extends Enum<E>,V> TypeAndTarget<E,V> getTargetEnum(Class<E> enumclass, Class<V> valueclass, String tname, String vname) {
        return tmap.getTargetEnum(enumclass, valueclass,tname,vname);
    }

    public <E extends Enum<E>> Optional<E> getOptionalEnumFromField(Class<E> enumclass, String fieldName) {
        return tmap.getOptionalEnumFromField(enumclass,fieldName);
    }

    public <E extends Enum<E>> E getEnumFromFieldOr(Class<E> enumClass, E defaultEnum, String fieldName) {
        return getOptionalEnumFromField(enumClass,fieldName).orElse(defaultEnum);
    }

    public <FA,FE> LongFunction<FA> enhance(
        LongFunction<FA> func,
        String field,
        Class<FE> type,
        FE defaultFe,
        BiFunction<FA,FE,FA> combiner
    ) {
        LongFunction<FE> fieldEnhancerFunc = getAsFunctionOr(field, defaultFe);
        LongFunction<FA> faLongFunction = func;
        LongFunction<FA> lfa = l -> combiner.apply(faLongFunction.apply(l),fieldEnhancerFunc.apply(l));
        return lfa;

    }

    public <FA,FE> Optional<LongFunction<FA>> enhance(
        Optional<LongFunction<FA>> func,
        String field,
        Class<FE> type,
        FE defaultFe,
        BiFunction<FA,FE,FA> combiner
    ) {
        if (func.isEmpty()) {
            return func;
        }
        LongFunction<FE> fieldEnhancerFunc = getAsFunctionOr(field, defaultFe);
        LongFunction<FA> faLongFunction = func.get();
        LongFunction<FA> lfa = l -> combiner.apply(faLongFunction.apply(l),fieldEnhancerFunc.apply(l));
        return Optional.of(lfa);
    }

    public <FA,FE> Optional<LongFunction<FA>> enhance(
        Optional<LongFunction<FA>> func,
        String field,
        Class<FE> type,
        BiFunction<FA,FE,FA> combiner
    ) {
        Optional<LongFunction<FE>> fieldEnhancerFunc = getAsOptionalFunction(field, type);
        if (func.isEmpty()||fieldEnhancerFunc.isEmpty()) {
            return func;
        }
        LongFunction<FA> faLongFunction = func.get();
        LongFunction<FE> feLongFunction = fieldEnhancerFunc.get();
        LongFunction<FA> lfa = l -> combiner.apply(faLongFunction.apply(l),feLongFunction.apply(l));
        return Optional.of(lfa);
    }

    public <FA,FE> LongFunction<FA> enhance(
        LongFunction<FA> func,
        String field,
        Class<FE> type,
        BiFunction<FA,FE,FA> combiner
    ) {
        Optional<LongFunction<FE>> fieldEnhancerFunc = getAsOptionalFunction(field, type);
        if (fieldEnhancerFunc.isEmpty()) {
            return func;
        }
        LongFunction<FE> feLongFunction = fieldEnhancerFunc.get();
        LongFunction<FA> lfa = l -> combiner.apply(func.apply(l),feLongFunction.apply(l));
        return lfa;
    }

    public <FA,FE extends Enum<FE>> LongFunction<FA> enhanceEnum(
        LongFunction<FA> func,
        String field,
        Class<FE> type,
        BiFunction<FA,FE,FA> combiner
    ) {
        Optional<LongFunction<FE>> fieldEnhancerFunc = getAsOptionalEnumFunction(field, type);
        if (fieldEnhancerFunc.isEmpty()) {
            return func;
        }
        LongFunction<FE> feLongFunction = fieldEnhancerFunc.get();
        LongFunction<FA> lfa = l -> combiner.apply(func.apply(l),feLongFunction.apply(l));
        return lfa;
    }

}
