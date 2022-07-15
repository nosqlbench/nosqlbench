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

package io.nosqlbench.api.config.standard;

import java.util.List;
import java.util.Map;

/**
 * <p>This configuration model describes what is valid to submit
 * for configuration for a given configurable object. Once this
 * is provided by a configurable element, it is used internally
 * by NoSQLBench to ensure that only valid configuration are
 * given to newly built objects.</p>
 *
 * <p>It is conventional to put the config model at the bottom of any
 * implementing class for quick reference.</p>
 */
public interface NBConfigModel {

    Map<String, Param<?>> getNamedParams();

    List<Param<?>> getParams();

    Class<?> getOf();

    void assertValidConfig(Map<String, ?> config);

    NBConfiguration apply(Map<String, ?> config);

    <V> Param<V> getParam(String... name);

    /**
     * Extract the fields from the shared config into a separate config,
     * removing those that are defined in this model and leaving
     * extraneous config fields in the provided model.
     *
     * <em>This method mutates the map that is provided.</em>
     *
     * @param sharedConfig A config map which can provide fields to multiple models
     * @return A new configuration for the extracted fields only.
     */
    NBConfiguration extractConfig(Map<String, ?> sharedConfig);

    /**
     * Extract the fields from the shared config into a separate config,
     * removing those that are defined in this model and leaving
     * extraneous config fields in the provided model.
     *
     * <em>This method mutates the map that is provided.</em>
     *
     * @param cfg A config map which can provide fields to multiple models
     * @return A new configuration for the extracted fields only.
     */
    NBConfiguration extractConfig(NBConfiguration cfg);

    NBConfiguration matchConfig(NBConfiguration cfg);

    NBConfiguration matchConfig(Map<String, ?> sharedConfig);

    NBConfigModel add(NBConfigModel otherModel);

}
