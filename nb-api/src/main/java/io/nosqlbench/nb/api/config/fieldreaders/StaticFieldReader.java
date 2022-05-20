package io.nosqlbench.nb.api.config.fieldreaders;

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


import java.util.Optional;

public interface StaticFieldReader {

    /**
     * @param field The requested field name
     * @return true, if the field is defined.
     */
    boolean isDefined(String field);

    /**
     * @param field The requested field name
     * @param type The required type of the field value
     * @return true if the field is defined <em>and</em> its value is statically defined as assignable to the given type
     */
    boolean isDefined(String field, Class<?> type);

    /**
     * @param fields The requested field names
     * @return true if the field names are all defined
     */
    boolean isDefined(String... fields);

    <T> T getStaticValue(String field, Class<T> classOfT);

    <T> T getStaticValue(String field);

    <T> T getStaticValueOr(String name, T defaultValue);

    <T> Optional<T> getOptionalValue(String field, Class<T> classOfT);

    void assertDefinedStatic(String... fields);
}
