/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.labels;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/**
 * <P>
 * The NBLabels type represents sets of identifying label names and values for any element in the
 * NoSQLBench runtime which needs to be named. This allows for hierarchic naming for instances by including
 * the naming elements of parents as owned objects are created.
 * </P>
 *
 * <p>
 * The recommended way to use this type is to require a parent element in constructors, and to combine
 * instance data at that time into a cached view. This means that further processing will be minimized,
 * since these elements will be consulted frequently, such as when rendering metrics values.
 * </P>
 */
public interface NBLabels {

    /**
     * Create a string representation of the label data, including only the values.
     * Each value is concatenated with the others using the delim character.
     * If a specified label name is included in square brackets like {@pre [name]}, then
     * it is considered optional, and will be skipped over gracefully if it is not present
     * in the label set. Otherwise all names are considered required.
     *
     * @param delim
     *     A single character
     * @param included
     *     Which fields from the label set to include in the rendered string. If none are specified then all are
     *     included.
     * @return A string representation of the labels
     * @throws RuntimeException if a required label name is not present, or its value is null.
     */
    String linearizeValues(char delim, String... included);


    /**
     * This is equivalent to call ing {@link #linearizeValues(char, String...)} with the '.' character.
     *
     * @param included
     *     Which fields from the label set to include in the rendered string. If none are specified, then all are
     *     included.
     * @return A string representation of the labels
     */
    default String linearizeValues(final String... included) {
        return this.linearizeValues('.', included);
    }

    String linearize_bare(String... barewords);

    /**
     * Render a string representation of the label set according to the prometheus exposition naming format.
     * This means that a label set which includes the JSON data:
     * <pre>{@code
     * {
     *   "name": "fooname",
     *   "label1": "label1value"
     * }
     * }</pre> would render to <pre>{@code fooname{label1="label1value"}}</pre> IF called as {@code linearize("name")}.
     * <p>
     * The included fields are added to the label set. If none are specified then all are included by default.
     *
     * @param barename
     *     The field from the label set to use as the nominal <em>metric family name</em> part.
     * @param included
     *     Fields to be used in rendered label set.
     * @return A string representation of the labels that is parsable in the prometheus exposition format.
     */
    String linearize(String barename, String... included);

    /**
     * Create an NBLabels instance from the given map.
     *
     * @param labels
     *     label data
     * @return a new NBLabels instance
     */
    static NBLabels forMap(final Map<String,String> labels) {
        return new MapLabels(labels);
    }

    /**
     * Create an NBLabels instance from the given keys and values (even,odd,...)
     *
     * @param keysAndValues
     *     Keys and values such as "key1", "value1", "key2", "value2", ...
     * @return a new NBLabels instance
     */
    static NBLabels forKV(final Object... keysAndValues) {
        if (0 != (keysAndValues.length % 2))
            throw new RuntimeException("keys and values must be provided in pairs, not as: " + Arrays.toString(keysAndValues));
        final LinkedHashMap<String,String> labels = new LinkedHashMap<>(keysAndValues.length >> 1);
        for (int i = 0; i < keysAndValues.length; i += 2) labels.put(keysAndValues[i].toString(), keysAndValues[i + 1].toString());
        return new MapLabels(labels);
    }

    /**
     * Return a new NBLabels value with the specified key transformed according to the provided Lambda.
     * The associated value is not modified.
     *
     * @param element
     *     The key to modify
     * @param transform
     *     A Lambda which will modify the existing key name.
     * @return A new NBLabels value, separate from the original
     */
    NBLabels modifyName(String element, Function<String, String> transform);

    /**
     * Return a new NBLabels value with the specified value transformed according to the provided Lambda.
     * The associated key name is not modified.
     * @param labelName The named label to modify
     * @param transform A Lambda which will modify the existing value.
     * @return A new NBLabels value, separate from the original
     * @throws RuntimeException if either the key is not found or the values is null.
     */
    NBLabels modifyValue(String labelName, Function<String,String> transform);

    String linearizeAsMetrics();

    String linearizeAsKvString();

    /**
     * Create a new NBLabels value with the additional keys and values appended.
     *
     * @param typeLabelsAndValues
     *     Keys and values in "key1", "value1", "key2", "value2", ... form
     * @return A new NBLabels instance
     */
    NBLabels and(Object... typeLabelsAndValues);

    NBLabels and(NBLabels labels);
    /**
     * Create a new NBLabels value with the additional keys and values appended.
     * @param typeLabelsAndValues a map of keys and values
     * @return A new NBLabels instance
     */
    NBLabels and(Map<String, String> typeLabelsAndValues);

    /**
     * Return the value of the specified label key.
     *
     * @param name
     *     The label name
     * @return The named label's value
     * @throws RuntimeException
     *     if the specified label does not exist in the set, or the value is null.
     */
    String valueOf(String name);

    Optional<String> valueOfOptional(String name);

    /**
     * Return a map representation of the label set, regardless of the underlying form.
     *
     * @return a {@link Map} of keys and values, in deterministic order
     */
    Map<String, String> asMap();

    /**
     * Return a String representation of the metric's labels as you would see it in an openmetrics filter,
     * like <PRE>{@code {__name__="metric_family_name",k="20"}}</PRE>
     * @return a String
     */

    NBLabels intersection(NBLabels labelset);

    NBLabels difference(NBLabels otherLabels);

    boolean isEmpty();

    NBLabels andDefault(String name, String value);
    NBLabels andDefault(NBLabels defaults);
}
