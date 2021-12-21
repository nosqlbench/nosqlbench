/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.core.bindings;

//

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Maps a template with named bind points and specifiers onto a set of data
 * mapping function instances. Allows for streamlined calling of mapper functions
 * as a set.
 * <p>There are several ways to get generated data via this class. You must always
 * provide a base input value. Fields can be accessed by parameter position or name.
 * In some cases, you can provide an iterator stride
 * in order to get data in bulk. In other cases, you can have setters called
 * directly on your provided objects. See the detailed method docs for more information.</p>
 */
public class Bindings {
    private final static Logger logger  = LogManager.getLogger(Bindings.class);
    private final BindingsTemplate template;
    private List<DataMapper<?>> dataMappers = new ArrayList<DataMapper<?>>();
    private final transient ThreadLocal<Map<String, DataMapper<?>>> nameCache;

    public Bindings(BindingsTemplate template, List<DataMapper<?>> dataMappers) {
        this.template = template;
        this.dataMappers = dataMappers;
        nameCache = ThreadLocal.withInitial(() ->
                new HashMap<String, DataMapper<?>>() {{
                    for (int i = 0; i < template.getBindPointNames().size(); i++) {
                        put(template.getBindPointNames().get(i), dataMappers.get(i));
                    }
                }});
    }


    public String toString() {
        return template.toString() + dataMappers;
    }

    /**
     * Get a value from each data mapper in the bindings list
     *
     * @param input The long value which the bound data mappers will use as in input
     * @return An array of objects, the values yielded from each data mapper in the bindings list
     */
    public Object[] getAll(long input) {
        Object[] values = new Object[dataMappers.size()];
        int offset = 0;
        for (DataMapper dataMapper : dataMappers) {
            values[offset++] = dataMapper.get(input);
        }
        return values;
    }

    /**
     * @return {@link BindingsTemplate} associated with this set of bindings
     */
    public BindingsTemplate getTemplate() {
        return this.template;
    }

    /**
     * @param input The input value for which the values should be generated.
     * @return {@link Map} of {@link String} to {@link Object}
     */
    public Map<String, Object> getAllMap(long input) {
        Map<String, Object> values = new HashMap<>();
        setMap(values, input);
        return values;
    }

    /**
     * Generate a list of maps over a range of inputs.
     * <p>For example, calling getIteratedMaps(5,3) with bindings named
     * alpha and gamma might produce something like:
     * <ol start="0">
     * <li>
     * <ul>
     * <li>alpha -&gt; val1</li>
     * <li>gamma -&gt; val2</li>
     * </ul>
     * </li>
     * <li>
     * <ul>
     * <li>alpha -&gt; val3</li>
     * <li>gamma -&gt; val4</li>
     * </ul>
     * </li>
     * <li>
     * <ul>
     * <li>alpha -&gt; val5</li>
     * <li>gamma -&gt; val6</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param input The base value for which the values should be generated.
     * @param count The number of iterations, starting at input, to be generated
     * @return {@link List} of {@link Map} of {@link String} to {@link Object}
     */
    public List<Map<String, Object>> getIteratedMaps(long input, int count) {
        List<Map<String, Object>> listOfMaps = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Map<String, Object> suffixedMap = new HashMap<>();
            setMap(suffixedMap, input + i);
            listOfMaps.add(suffixedMap);
        }
        return listOfMaps;
    }

    /**
     * Generate a map containing the results from multiple iterations, suffixing
     * the keys in the map with the iterations from 0 to count-1.
     * <p>For example, calling getIteratedSuffixMap(5, 3) with generators named
     * alpha and gamma might yield results like
     * <ul>
     * <li>alpha0 -&gt; val1</li>
     * <li>gamma0 -&gt; val2</li>
     * <li>alpha1 -&gt; val3</li>
     * <li>gamma1 -&gt; val4</li>
     * <li>alpha2 -&gt; val5</li>
     * <li>gamma2 -&gt; val6</li>
     * </ul>
     *
     * @param input The base input value for which the values should be generated
     * @param count The count of maps that should be added to the final map
     * @return {@link Map} of {@link String} to {@link Object}
     */
    public Map<String, Object> getIteratedSuffixMap(long input, int count) {
        Map<String, Object> suffixedMap = new LinkedHashMap<>(count * this.dataMappers.size());
        setIteratedSuffixMap(suffixedMap, input, count);
        return suffixedMap;
    }

    /**
     * This is a version of the {@link #setIteratedSuffixMap(Map, long, int, String[])} which creates
     * a new map for each call.
     * @param input The base input value for which the values should be generated
     * @param count The count of maps that should be added to the final map
     * @param fieldNames The field names which are used to look up the functions in the binding
     * @return A newly created map with the generated names and values.
     */
    public Map<String,Object> getIteratedSuffixMap(long input, int count, String... fieldNames) {
        Map<String, Object> suffixedMap = new LinkedHashMap<>(count * fieldNames.length);
        setIteratedSuffixMap(suffixedMap, input, count, fieldNames);
        return suffixedMap;
    }

    /**
     * Populate a map of values with a two-dimensional set of generated key and value names. This is a basic
     * traversal over all the provided field names and a range of input values from input to input+count.
     * The key names for the map are created by adding a numeric suffix to the field name.
     *
     * For example, with field names aleph and gamma, with input 53 and count 2, the key names will
     * be created as aleph0, gamma0, aleph1, gamma1, and the input values which will be used to
     * create the generated values for these keys will be 53, 53, and 54, 54, respectively.
     *
     * Symbolically, this does the same as the sketch below:
     *
     * <ol>
     *     <li>map{aleph0}=funcFor("aleph")(53)</li>
     *     <li>map{gamma0}=funcFor("gamma")(53)</li>
     *     <li>map{aleph1}=funcFor("aleph")(54)</li>
     *     <li>map{gamma1}=funcFor("gamma")(54)</li>
     * </ol>
     *
     * @param suffixedMap A donor map which is to be populated. The values do not clear the map, but merely overwrite
     *                    values of the same name.
     * @param input The base input value for which the values should be generated
     * @param count The count of maps that should be added to the final map
     * @param fieldNames The field names which are used to look up the functions in the binding
     */
    private void setIteratedSuffixMap(Map<String, Object> suffixedMap, long input, int count, String[] fieldNames) {
        for (int i = 0; i < count; i++) {
            for (String f : fieldNames) {
                suffixedMap.put(f+i,get(f,input+i));
            }
        }
    }

    /**
     * Get a value for the data mapper in slot i
     *
     * @param i     the data mapper slot, 0-indexed
     * @param input the long input value which the bound data mapper will use as input
     * @return a single object, the value yielded from the indexed data mapper in the bindings list
     */
    public Object get(int i, long input) {
        return dataMappers.get(i).get(input);
    }

    /**
     * Get a value for the cached mapper name, using the name to mapper index cache.
     * @param name The field name in the data mapper
     * @param input the long input value which the bound data mapper will use as an input
     * @return a single object, the value yielded from the named and indexed data mapper in the bindings list.
     */
    public Object get(String name, long input) {
        DataMapper<?> dataMapper = nameCache.get().get(name);
        return dataMapper.get(input);
    }

    /**
     * Generate all values in the bindings template, and set each of them in
     * the map according to their bind point name.
     *
     * @param donorMap - a user-provided Map&lt;String,Object&gt;
     * @param cycle    - the cycle for which to generate the values
     */
    public void setMap(Map<String, Object> donorMap, long cycle) {
        Object[] all = getAll(cycle);
        for (int i = 0; i < all.length; i++) {
            donorMap.put(template.getBindPointNames().get(i), all[i]);
        }
    }

    /**
     * Set the values in a provided map, with bound names suffixed with
     * some value. No non-overlapping keys in the map will be affected.
     *
     * @param donorMap an existing {@link Map} of {@link String} to {@link Object}
     * @param cycle    the cycle for which values should be generated
     * @param suffix   a string suffix to be appended to any map keys
     */
    public void setSuffixedMap(Map<String, Object> donorMap, long cycle, String suffix) {
        Object[] all = getAll(cycle);
        for (int i = 0; i < all.length; i++) {
            donorMap.put(template.getBindPointNames().get(i) + suffix, all[i]);
        }
    }

    /**
     * Set the values in a provided map, with the bound names suffixed with
     * an internal iteration value.
     *
     * @param donorMap an existing {@link Map} of {@link String} to {@link Object}
     * @param input    the base cycle for which values should be generated
     * @param count    the number of iterations to to generate values and keynames for
     */
    public void setIteratedSuffixMap(Map<String, Object> donorMap, long input, long count) {
        for (int i = 0; i < count; i++) {
            setSuffixedMap(donorMap, input + i, String.valueOf(i));
        }
    }

    /**
     * Generate only the values which have matching keys in the provided
     * map according to their bind point names, and assign them to the
     * map under that name. It is an error for a key name to be defined
     * in the map for which there is no mapper.
     *
     * @param donorMap - a user-provided Map&lt;String,Object&gt;
     * @param input    - the input for which to generate the values
     */
    public void updateMap(Map<String, Object> donorMap, long input) {
        for (String s : donorMap.keySet()) {
            donorMap.put(s, get(s, input));
        }
    }

    /**
     * Generate only the values named in fieldNames, and then call the user-provided
     * field setter for each name and object generated.
     *
     * @param fieldSetter user-provided object that implements {@link FieldSetter}.
     * @param input       the input for which to generate values
     * @param fieldName   A varargs list of field names, or a String[] of names to set
     */
    public void setNamedFields(FieldSetter fieldSetter, long input, String... fieldName) {
        for (String s : fieldName) {
            fieldSetter.setField(s, get(s, input));
        }
    }

    /**
     * Generate all the values named in the bindings for a number of iterations, calling
     * a user-provided field setter for each name and object generated, with the
     * iteration number appended to the fieldName, but only for the named bindings.
     *
     * @param fieldSetter user-provided object that implements {@link FieldSetter}
     * @param input       the base input value for which the objects should be generated
     * @param count       the number of iterations to generate values and names for
     * @param fieldName   the field names for which to generate values and names
     */
    public void setNamedFieldsIterated(FieldSetter fieldSetter, long input, int count, String... fieldName) {
        for (int i = 0; i < count; i++) {
            for (String s : fieldName) {
                fieldSetter.setField(s + i, get(s, input + i));
            }
        }
    }

    /**
     * Generate all the values named in the bind point names, then call the user-provided
     * field setter for each name and object generated.
     *
     * @param fieldSetter user-provided object that implements {@link FieldSetter}
     * @param input       the input for which to generate values
     */
    public void setAllFields(FieldSetter fieldSetter, long input) {
        Object[] all = getAll(input);
        for (int i = 0; i < all.length; i++) {
            fieldSetter.setField(template.getBindPointNames().get(i), all[i]);
        }
    }

    /**
     * Generate all the values named in the bindings for a number of iterations, calling
     * a user-provided field setter for each name and object generated, with the
     * iteration number appended to the fieldName.
     *
     * @param fieldSetter user-provided object that implements {@link FieldSetter}
     * @param input       the base input value for which the objects should be generated
     * @param count       the number of iterations to generate values and names for
     */
    public void setAllFieldsIterated(FieldSetter fieldSetter, long input, int count) {
        for (int i = 0; i < count; i++) {
            Object[] all = getAll(input+i);
            for (int j = 0; j < all.length; j++) {
                fieldSetter.setField(template.getBindPointNames().get(i) + i, all[i]);
            }
        }
    }

    public LazyValuesMap getLazyMap(long input) {
        return new LazyValuesMap(this, input);
    }

    public interface FieldSetter {
        void setField(String name, Object value);
    }


}
