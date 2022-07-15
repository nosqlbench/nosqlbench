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

import java.util.Map;
import java.util.Optional;

/**
 * A generic type-safe reader interface for parameters.
 */
public interface Element {

    String getElementName();

    /**
     * <p>Hierarchic get of a named variable. If the name is a simple word with
     * no dots, such as param2, then this is a simple lookup. If it is a
     * hierarchic name such as car.cabin.dashlight1, then multiple
     * representations of the structure could suffice to serve the request.
     * This is enabled with name flattening.</p>
     *
     * <p>Name flattening - It is possible that multiple representations
     * of backing data can suffice to hold the same logically named element.
     * For example the three JSON objects below are all semantically equivalent.
     *
     * <pre>{@code
     * [
     *   {
     *     "car": {
     *         "cabin": {
     *             "dashlight1": "enabled"
     *         }
     *     }
     *   },
     *   {
     *     "car": {
     *         "cabin.dashlight1": "enabled"
     *     }
     *   },
     *   {
     *     "car.cabin": {
     *         "dashlight1": "enabled"
     *     }
     *   },
     *   {
     *       "car.cabin.dashlight": "enabled"
     *   }
     * ]
     * }</pre>
     *
     * <p>It is necessary to honor all of these representations due to the various ways that
     * users may provide the constructions of their configuration data. Some of them will
     * be long-form property maps from files, others will be programmatic data structures
     * passed into an API. This means that we must also establish a clear order of precedence
     * between them.</p>
     *
     * <p><em>The non-collapsed form takes precedence, starting from the root level.</em> That is,
     * if there are multiple backing data structures for the same name, the one with a flattened name
     * will <em>NOT</em> be seen if there is another at the same level which is not flattened --
     * even if the leaf node is fully defined under the flattened name.</p>
     *
     * <p>Thus the examples above
     * are actually in precedence order. The first JSON object has the most complete name
     * defined at the root level, so it is what will be found first. All implementations
     * should ensure that this order is preserved.</p>
     *
     * <p>This method requires a type which will be given to the underlying {@link ElementData}
     * implementation for contextual type conversion.</p>
     *
     * @param name     The simple or hierarchic variable name to resolve
     * @param classOfT The type of value which the resolved value is required to be assignable to
     * @param <T>      The value type parameter
     * @return An optional value of type T
     */
    <T> Optional<T> get(String name, Class<? extends T> classOfT);

    /**
     * Perform the same lookup as {@link #get(String, Class)}, except allow for full type
     * inferencing when possible. The value asked for will be cast to the type T at runtime,
     * as with type erasure there is no simple way to capture the requested type without
     * reifying it into a runtime instance in the caller. Thus, this method is provided
     * as a syntactic convenience at best. It will almost always be better to use
     * {@link #get(String, Class)}
     *
     * @param name The simple or hierarchic variable name to resolve
     * @param <T>  The value type parameter
     * @return An optional value of type T
     */
    <T> Optional<T> get(String name);

    /**
     * Perform the same lookup as {@link #get(String, Class)}, but return the default value
     * when a value isn't found.
     *
     * @param name         The simple or hierarchic variable name to resolve
     * @param defaultValue The default value to return if the named variable is not found
     * @param <T>          The value type parameter
     * @return Either the found value or the default value provided
     */
    <T> T getOr(String name, T defaultValue);

    /**
     * Return the backing data for this element in map form.
     *
     * @return A Map of data.
     */
    Map<String, Object> getMap();
}
