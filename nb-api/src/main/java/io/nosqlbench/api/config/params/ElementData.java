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

package io.nosqlbench.api.config.params;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A generic type-safe reader interface for parameters.
 * TODO: This should be consolidated with the design of ConfigLoader once the features of these two APIs are stabilized.
 *
 * The source data for a param reader is intended to be a collection of something, not a single value.
 * As such, if a single value is provided, an attempt will be made to convert it from JSON if it starts with
 * object or array notation. If not, the value is assumed to be in the simple ParamsParser form.
 */
public interface ElementData {
    String NAME = "name";
    List<Class<?>> COMMON_TYPES = List.of(
        String.class,
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        double.class, Double.class,
        float.class, Float.class,
        Map.class, Set.class, List.class
    );

    static Optional<Object> asCommonType(Object src) {
        for (Class<?> commonType : COMMON_TYPES) {
            if (commonType.isAssignableFrom(src.getClass())) {
                return Optional.of(commonType.cast(src));
            }
        }
        return Optional.empty();
    }

    Object get(String name);

    Set<String> getKeys();

    boolean containsKey(String name);

    default String getName() {
        String name = getGivenName();
        if (name!=null) {
            return name;
        }
        return extractElementName();
    }

    String getGivenName();

    default String extractElementName() {
        if (containsKey(NAME)) {
            Object o = get(NAME);
            if (o instanceof CharSequence) {
                return ((CharSequence)o).toString();
            }
        }
        return null;
    }

    default <T> T convert(Object input, Class<T> type) {
        if (type!=null) {
            if (type.isAssignableFrom(input.getClass())) {
                return type.cast(input);
            } else {
                throw new RuntimeException("Conversion from " + input.getClass().getSimpleName() + " to " + type.getSimpleName() +
                    " is not supported natively. You need to add a type converter to your ElementData implementation for " + getClass().getSimpleName());
            }
        } else {
            return (T) input;
        }
    }

    default <T> T get(String name, Class<T> type) {
        Object o = get(name);
        if (o!=null) {
            return convert(o,type);
        } else {
            return null;
        }
    }

    default <T> T lookup(String name, Class<T> type) {
        int idx=name.indexOf('.');
        while (idx>0) { // TODO: What about when idx==0 ?
            // Needs to iterate through all terms
            String parentName = name.substring(0,idx);
            if (containsKey(parentName)) {
                Object o = get(parentName);
                ElementData parentElement = DataSources.element(parentName, o);
                String childName = name.substring(idx+1);
                int childidx = childName.indexOf('.');
                while (childidx>0) {
                    String branchName = childName.substring(0,childidx);
                    Object branchObject = parentElement.lookup(branchName,type);
                    if (branchObject!=null) {
                        ElementData branch = DataSources.element(branchName, branchObject);
                        String leaf=childName.substring(childidx+1);
                        T found = branch.lookup(leaf, type);
                        if (found!=null) {
                            return found;
                        }
                    }
                    childidx=childName.indexOf('.',childidx+1);
                }
                T found = parentElement.lookup(childName,type);
                if (found!=null) {
                    return found;
                }
            }
            idx=name.indexOf('.',idx+1);
        }
        return get(name,type);
    }

    /**
     * <p>Get the value for the key, but ensure that the type of value that is returned
     * is in one of the sanctioned {@link #COMMON_TYPES}.
     *
     * <p>If possible, the value provided should be a wrapper type around the actual backing
     * type, such that mutability is preserved.</p>
     *
     * <p>If the backing type is a structured type object graph which defies direct
     * conversion to one of the types above, then an error should be thrown.</p>
     *
     * <p>If the type is a collection type, then type conversion should be provided all the way
     * down to each primitive value.</p>
     *
     * <p>If no value by the given name exists, the null should be returned.</p>
     *
     * @param key The key of the value to retrieve
     * @return The value as a Java primitive, Boxed primitive, or Set, List, or Map of String to Object.
     */
    Object getAsCommon(String key);
}
