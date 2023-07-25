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

package io.nosqlbench.adapters.api.templating;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.config.fieldreaders.DynamicFieldReader;
import io.nosqlbench.api.config.fieldreaders.StaticFieldReader;
import io.nosqlbench.api.config.standard.NBConfigError;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.engine.api.templating.ObjectCache;
import io.nosqlbench.engine.api.templating.ParsedTemplateMap;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.engine.api.templating.binders.ArrayBinder;
import io.nosqlbench.engine.api.templating.binders.ListBinder;
import io.nosqlbench.engine.api.templating.binders.OrderedMapBinder;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.CapturePoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * <H1>ParsedOp API</H1>
 *
 * <P>This is the primary developer-focused API for driver developers to use when up-converting op templates
 * to operations. This {@link ParsedOp} is a wrapper around the op template structure. It provides many
 * ways of constructing higher-order objects from a variety of sources.</P>
 *
 * <H2>Supporting Variety</H2>
 * <P>
 * For some drivers or protocols, the primary user interface is a statement format or grammar. For CQL or SQL,
 * the most natural way of describing templates for operations is in that native format. For others, an operation
 * template may look like a block of JSON or a HTTP request. All these forms are supported. In order to deal with
 * the variety, there is a set of detailed rules for how the workload definitions are transformed into driver
 * operations for a native driver. The high-level flow is:
 * <OL>
 *     <LI>Op Template Form</LI>
 *     <LI>Normalized Data Structure</LI>
 *     <LI>ParsedOp Form</LI>
 * </OL>
 *
 * The specifications govern how the raw op template form is transformed into the normalized data structure.
 * This documentation focuses on the API provided by the last form, this class, although peripheral awareness
 * of the first two forms will certainly help for any advanced scenarios. You can find detailed examples and
 * specification tests under the workload_definition folder of the adapters-api module.
 * </P>
 *
 *
 * <H2>Op Template Parsing</H2>
 * <OL>
 *     <LI>Rule #1: All op templates are parsed into an internal normalized structure which contains:
 *     <OL>
 *         <LI>A map of op fields, which can consist of:
 *         <OL>
 *             <LI>static field values</LI>
 *             <LI>dynamic field values, the actual value of which can only be known for a given cycle</LI>
 *         </OL>
 *         </LI>
*          <LI>Access to auxiliary configuration values like activity parameters. These can back-fill
 *          when values aren't present in the direct static or dynamic op fields.</LI>
 *     </LI>
 *
 *     <LI>Rule #2: When asking for a dynamic parameter, static parameters may be automatically promoted to functional form as a back-fill.</LI>
 *     <LI>Rule #3: When asking for static parameters, config parameters may automatically be promoted as a back-fill.</LI>
 * </OL>
 * The net effect of these rules is that the NoSQLBench driver developer may safely use functional forms to access data
 * in the op template, or may decide that certain op fields must only be provided in a static way per operation.
 * </P>
 *
 * <H2>Distinguishing Op Payload from Op Config</H2>
 * <P>When a user specifies an op template, they may choose to provide only a single set of op fields without
 * distinguishing between config or payload, or they may choose to directly configure each.
 * <H4>Example:</H4>
 * <PRE>{@code
 * ops:
 * # both op and params explicitly named
 *   op1:
 *     op:
 *       opfield1: value1
 *     params:
 *       param2: value2
 * # neither op field nor params named, so all assumed to be op fields
 *   op2:
 *     opfield1: value1
 *     param2: value2
 *     # in this case, if param2 is meant to be config level,
 *     # it is a likely config error that should be thrown to the user
 * # only op field explicitly named, so remainder automatically pushed into params
 *   op3:
 *     op:
 *       opfield1: value1
 *     param2: value2
 * # only params explicitly named, so remainder pushed into op payload
 *   op4:
 *     params:
 *       param2: value2
 *     opfield1: value1
 * }</PRE>
 *
 * All of these are considered valid constructions, and all of them may actually achieve the same result.
 * This looks like an undesirable problem, but it serves to simplify things for users in one specific way: It allows
 * them to be a vague, within guidelines, and still have a valid workload definition.
 * The NoSQLBench runtime does a non-trivial amount of processing on the op template to
 * ensure that it can conform to an unambiguous normalized internal structure on your behalf.
 * This is needed because of how awful YAML is as a user configuration language in spite of its
 * ubiquity in practice. The basic design guideline for these forms is that the op template must
 * mean what a reasonable user would assume without looking at any documentation.
 *
 * <HR></HR>
 * <H2>Design Invariants</H2>
 *
 * <P>The above rules imply invariants, which are made explicit here. {@link ParsedOp}.</P>
 *
 * <P><UL>
 *
 *     <LI><EM>You may not use an op field name or parameter name for more than one purpose.</EM>
 *     <UL>
 *         <LI>Treat all parameters supported by a driver adapter and it's op fields as a globally shared namespace, even if it is not.
 *         This avoids creating any confusion about what a parameter can be used for and how to use it for the right thing in the right place.
 *         For example, you may not use the parameter name `socket` in an op template to mean one thing and then use it
 *         at the driver adapter level to mean something different. However, if the meaning is congruent, a driver developer
 *         may choose to support some cross-cutting parameters at the activity level. These allowances are explicit,
 *         however, as each driver dictates what it will allow as activity parameters.
 *         </LI>
 *     </UL>
 *     </LI>
 *
 *     <LI><EM>Users may specify op payload fields within op params or activity params as fallback config sources in that order.</EM>
 *     <UL>
 *         <LI>IF a name is valid as an op field, it must also be valid as such when specified in op params.</LI>
 *         <LI>If a name is valid as an op field, it must also be valid as such when specified in activity params, within the scope of {@link ParsedOp}</LI>
 *         <LI>When an op field is found via op params or activity params, it may NOT be dynamic. If dynamic values are intended to be provided
 *         at a common layer in the workload, then bindings support this already.</LI>
 *     </UL>
 *     </LI>
 *
 *     <LI><EM>You must access non-payload params via Config-oriented methods.</EM>
 *         <UL>
 *             <LI>Op Templates contain op payload data and op configs (params, activity params).</LI>
 *             <LI>You must use only {@link ParsedOp} getters with "...Config..." names, such as {@link #getConfigOr(String, Object, long)}
 *             when accessing non-payload fields.</LI>
 *             <LI>When a dynamic value is found via one of these calls, an error should be thrown,
 *              as configuration level data is not expected to be variant per-operation or cycle.</LI>
 *         </UL>
 *     </LI>
 *
 *     <LI><EM>The user must be warned when a required or optional config value is missing from op params (or activity params), but a value
 *     of the same name is found in op payload fields.</EM>
 *       <UL>
 *           <LI>If rule #1 is followed, and names are unambiguous across the driver, then it is almost certainly a configuration error.</LI>
 *       </UL>
 *     </LI>
 *
 *     <LI><EM>When both an op payload field and a param field of the same name are defined through cascading configuration of param fields,
 *     the local op payload field takes precedence.</EM>
 *       <UL>
 *           <LI>This is an extension of the param override rules which say that the closest (most local) value to an operation is the one that takes precedence.</LI>
 *           <LI>In practice, there will be no conflicts between direct static and dynamic fields, but there will be possibly between
 *           static or dynamic fields and parameters and activity params. If a user wants to promote an activity param as an override to existing op fields,
 *           template variables allow for this to happen gracefully. Otherwise, the order of precedence is 1) op fields 2) op params 3) activity params.</LI>
 *       </UL>
 *     </LI>
 * </UL>
 * </P>
 *
 * <HR></HR>
 *
 * <H2>Op Payload Forms</H2>
 * Field values can come from multiple sources. These forms and any of their combinations are supported.
 *
 * <H3>Static Op Fields</H3>
 * <H4>Example:</H4>
 * <PRE>{@code
 * op:
 *   field1: value1
 *   field2:
 *     map3:
 *       key4: value4
 *     map5:
 *       key6: value6
 *   field7: false
 *   field8: 8.8
 * }</PRE>
 *
 * As shown, any literal value of any valid YAML type, including structured values like lists or maps are accepted as
 * static op template values. A static value is any value which contains zero bind points at any level.
 *
 * <H3>Dynamic Op Fields with Binding References</H3>
 * <H4>Example:</H4>
 * <PRE>{@code
 * op:
 *   field1: "{binding1}"
 *   field2: "value is: {binding1}"
 * }
 * </PRE>
 *
 * <P>In this form, {@code {binding1}} is known as a <EM>binding reference</EM>, since the binding function is defined
 * elsewhere under the given name. The first field "field1" is specified with no leading nor trailing literals, and
 * is thus taken as a <EM>raw binding reference</EM>, meaning that it will not be converted to a String. The second,
 * named "field2", is what is known as a <EM>string template</EM>, and is syntactical sugar for a more complex binding
 * which concatenates static and dynamic parts together. In this form, object types produced by binding functions are
 * converted to string form before concatenation.
 * </P>
 *
 * <P>Note that a raw {@code {binding1}} value (without quotes) would be NOT be a binding reference, since YAML
 * is a superset of JSON. this means that {@code {binding1}} would be converted to a map or JSON object type
 * with invalid contents. This is warned about when detected as a null valued map key, although it also makes
 * null values invalid for ANY op template value.</P>
 *
 * <H3>Dynamic Op Fields with Binding Definitions</H3>
 * <H4>Example:</H4>
 *
 * <PRE>{@code
 * op:
 *   field1: "{{NumberNameToString()}}"
 *   field2: "value is: {{NumberNameToString()}}"
 * }
 * </PRE>
 *
 * This form has exactly the same effect as the previous example as long as your bindings definitions included:
 * <PRE>{@code
 * bindings:
 *   binding1: NumberNameToString();
 * }</PRE>
 *
 * <H3>Dynamic Op Fields with Structure</H3>
 * <H4>Example:</H4>
 *
 * <PRE>{@code
 *   field1:
 *     k1: "{binding1}
 *     k2: "literal value"
 *   field2:
 *     - "value3"
 *     - "{binding4}"
 *     - "a value: {binding5}"
 *     - "{{NumberNameToString}}"
 *     - "a value: {{NumberNameToString()}}"
 * }</PRE>
 *
 * <P>This example combines the previous ones with structure and dynamic values. Both field1 and field2 are dynamic,
 * since each contains some dynamic value or template within. When field1 is accessed within a cycle, that cycle's value
 * will be used as the seed to generate equivalent structures with all the literal and dynamic elements inserted as
 * the template implies. As before, direct binding references like {@code {binding4}} will be inserted into the
 * structure with whatever type the binding definition produces, so if you want strings in them, ensure that you
 * configure your binding definitions thusly.</P>
 *
 * <H3>Op Template Params</H3>
 * <H4>Example:</H4>
 * <PRE>{@code
 * params:
 *  prepared: true
 * ops:
 *  op1:
 *   field1: value1
 *   params:
 *    prepared: false
 * }</PRE>
 *
 * The params section are the first layer of external configuration values that an op template can use to distinguish
 * op configuration parameters from op payload content.
 * Op Template Params are referenced when any of the {@link #getConfigOr(String, Object, long)} or other <EM>...Config...</EM>
 * getters are used (bypassing non-param fields). They are also accessed as a fallback when no static nor dynamic value is found
 * for a reference op template field. Unlike op fields, op params cascade from within the workload YAML from the document level,
 * down to each block and then down to each statement.
 *
 * <H3>Activity Params</H3>
 * <H4>Example:</H4>
 * <PRE>{@code
 * ./nb run driver=... workload=... cl=LOCAL_QUORUM
 * }</PRE>
 *
 * <P>When explicitly allowed by a driver adapter's configuration model, values like {@code cl} above can be seen as
 * another fallback source for configuration parameters. The {@link ParsedOp} implementation will automatically look
 * in the activity parameters if needed to find a missing configuration parameter, but this will only work if
 * the specific named parameter is allowed at the activity level.</P>
 */
public class ParsedOp implements LongFunction<Map<String, ?>>, NBLabeledElement, StaticFieldReader, DynamicFieldReader {

    private static final Logger logger = LogManager.getLogger(ParsedOp.class);

    /**
     * The names of payload values in the result of the operation which should be saved.
     * The keys in this map represent the name of the value as it would be found in the native
     * representation of a result. If the values are defined, then each one represents the name
     * that the found value should be saved as instead of the original name.
     */
    private final List<CapturePoint> captures = new ArrayList<>();

    private final OpTemplate _opTemplate;
    private final NBConfiguration activityCfg;
    private final ParsedTemplateMap tmap;
    private final NBLabels labels;

    /**
     * Create a parsed command from an Op template. This version is exactly like
     *  except that it allows
     * preprocessors. Preprocessors are all applied to the the op template before
     * it is applied to the parsed command fields, allowing you to combine or destructure
     * fields from more tha one representation into a single canonical representation
     * for processing.
     *
     * @param opTemplate
     *     The OpTemplate as provided by a user via YAML, JSON, or API (data structure)
     * @param activityCfg
     *     The activity configuration, used to resolve nested config parameters
     * @param preprocessors
     *     Map->Map transformers.
     * @param labels
     */
    public ParsedOp(
        OpTemplate opTemplate,
        NBConfiguration activityCfg,
        List<Function<Map<String, Object>, Map<String, Object>>> preprocessors,
        NBLabeledElement parent) {
        this._opTemplate = opTemplate;
        this.activityCfg = activityCfg;
        labels=parent.getLabels().and("op", this.getName());

        Map<String, Object> map = opTemplate.getOp().orElseThrow(() ->
            new OpConfigError("ParsedOp constructor requires a non-null value for the op field, but it was missing."));
        for (Function<Map<String, Object>, Map<String, Object>> preprocessor : preprocessors) {
            map = preprocessor.apply(map);
        }

        this.tmap = new ParsedTemplateMap(
            getName(),
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

    private boolean isConfig(String fieldname) {
        return tmap.isConfig(fieldname);
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

    public Optional<ParsedTemplateString> getAsTemplate(String fieldname) {
        return this.tmap.getAsStringTemplate(fieldname);
    }

    public Optional<ParsedTemplateString> takeAsOptionalStringTemplate(String fieldname) {
        return this.tmap.takeAsOptionalStringTemplate(fieldname);
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
     * @throws NBConfigError if the named field is defined dynamically,
     *                                                            as in this case, it is presumed that the parameter is not supported unless it is defined statically.
     */
    public <T> T getStaticConfigOr(String name, T defaultValue) {
        return tmap.getStaticConfigOr(name, defaultValue);
    }

    /**
     * Get the parameter value from a static op template field OR any of the provided optional sources
     * of op template values, including the activity parameters
     * @param name The config field name
     * @param defaultValue The default value, if the field is not defined in the op template nor the activity params
     * @param <T> The type of the field
     * @return The config value.
     */
    public <T> T takeStaticConfigOr(String name, T defaultValue) {
        return tmap.takeStaticConfigOr(name, defaultValue);
    }
    public String getStaticConfig(String name, Class<String> clazz) {
        return tmap.getStaticConfig(name, clazz);
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


    public <T> Optional<T> takeOptionalStaticValue(String field, Class<T> classOfT) {
        return Optional.ofNullable(tmap.takeStaticValue(field, classOfT));
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
        return tmap.getOpFieldNames();
    }

    /**
     * Get the op field as a {@link LongFunction} of String. This is a convenience form for
     * {@link #getAsRequiredFunction(String, Class)}
     *
     * @param name The field name which must be defined as static or dynamic
     * @return A function which can provide the named field value
     */
    public LongFunction<String> getAsRequiredFunction(String name) {
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
    @Override
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
    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getTypeAndTargetFromEnum(Class<E> enumclass, Class<V> valueClass) {
        return tmap.getOptionalTargetEnum(enumclass,valueClass);
    }

    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getOptionalTypeAndTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass
    ){
        return tmap.getOptionalTargetEnum(enumclass,valueClass);
    }

    public <E extends Enum<E>,V> Optional<TypeAndTarget<E,V>> getOptionalTypeAndTargetEnum(
        Class<E> enumclass,
        Class<V> valueClass,
        String alternateTypeField,
        String alternateValueField
    ) {
        return tmap.getOptionalTargetEnum(enumclass, valueClass, alternateTypeField, alternateValueField);
    }

    public <E extends Enum<E>,V> TypeAndTarget<E,V> getTypeAndTarget(Class<E> enumclass, Class<V> valueClass) {
        return tmap.getTargetEnum(enumclass, valueClass);
    }

    public <E extends Enum<E>,V> TypeAndTarget<E,V> getTypeAndTarget(Class<E> enumclass, Class<V> valueclass, String tname, String vname) {
        return tmap.getTargetEnum(enumclass, valueclass,tname,vname);
    }

    public <E extends Enum<E>> Optional<E> getOptionalEnumFromField(Class<E> enumclass, String fieldName) {
        return tmap.getOptionalEnumFromField(enumclass,fieldName);
    }

    public <E extends Enum<E>> E getEnumFromFieldOr(Class<E> enumClass, E defaultEnum, String fieldName) {
        return getOptionalEnumFromField(enumClass,fieldName).orElse(defaultEnum);
    }

    /**
     * <p>Enhance a {@link Function} with another <EM>required</EM> named field or function combiner OR a default value.</p>
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param defaultFe The default value of the field, if none is provided
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return an enhanced function
     */
    public <FA,FE> LongFunction<FA> enhanceDefaultFunc(
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

    /**
     * <p>Enhance a {@link Function} with a named required function, or throw an error.</p>
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return a version of the base function, optionally enhanced
     */
    public <FA,FE> LongFunction<FA> enhanceFunc(
        LongFunction<FA> func,
        String field,
        Class<FE> type,
        BiFunction<FA,FE,FA> combiner
    ) {
        LongFunction<FE> fieldEnhancerFunc = getAsRequiredFunction(field, type);
        LongFunction<FA> lfa = l -> combiner.apply(func.apply(l),fieldEnhancerFunc.apply(l));
        return lfa;
    }

    /**
     * <p>Enhance a {@link Function} with a named optional function IFF it exists.</p>
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return a version of the base function, optionally enhanced
     */
    public <FA,FE> LongFunction<FA> enhanceFuncOptionally(
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


    /**
     * <p>Enhance an {@link Optional} {@link Function} with an optional named field or value combiner,
     * IFF both functions are defined.</p>
     *
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return the enhanced optional function
     */
    public <FA,FE> Optional<LongFunction<FA>> enhanceOptionalFuncOptionally(
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

    /**
     * <p>Enhance an {@link Optional} {@link Function} with a named field or function combiner OR a default value,
     * IFF the base function is present.</p>
     *
     * <p>Create a required function for the specified field and default value, IFF the
     * main function is present. The base type of the function remains the same, and if present,
     * will be extended with the required field value or function in the provided combiner.</p>
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param defaultFe The default value of the field, if none is provided
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return the enhanced optional base function
     */
    public <FA,FE> Optional<LongFunction<FA>> enhanceOptionalDefaultFunc(
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

    /**
     * <p>Enhance a {@link Function} with an optional enum function IFF it is defined.</p>
     * @param func The base function
     * @param field The field name to derive the named enhancer function from
     * @param type The type of the field value
     * @param combiner A {@link BiFunction} which applies the field or function combiner to the base function
     * @param <FA> The base function result type
     * @param <FE> The enhancer function result type
     * @return an (optionally) enhanced base function
     */
    public <FA,FE extends Enum<FE>> LongFunction<FA> enhanceEnumOptionally(
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

    public Map<String, Object> parseStaticCmdMap(String key, String mainField) {
        return tmap.parseStaticCmdMap(key, mainField);
    }

    public List<Map<String, Object>> parseStaticCmdMaps(String key, String mainField) {
        return tmap.parseStaticCmdMaps(key, mainField);
    }

    @Override
    public String toString() {
        return this.tmap.toString();
    }

    public List<CapturePoint> getCaptures() {
        return tmap.getCaptures();
    }

    @Override
    public NBLabels getLabels() {
        return labels;
    }

    public Map<String, String> getBindPoints() {
        return null;
    }
}
