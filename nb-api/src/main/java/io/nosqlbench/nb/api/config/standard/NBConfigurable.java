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

package io.nosqlbench.nb.api.config.standard;

/**
 * All implementation types which wish to have a type-marshalled configuration
 * should implement this interface.
 *
 * When a type which implements this interface is instantiated, and the
 * {@link NBConfiguration} was not injected into its constructor,
 * the builder should call {@link #applyConfig(NBConfiguration)} immediately
 * after calling the constructor.
 */
public interface NBConfigurable extends NBCanConfigure, NBConfigModelProvider {

    /**
     * Implementors should take care to ensure that this can be called after
     * initial construction without unexpected interactions between
     * construction parameters and configuration parameters.
     * @param cfg The configuration data to be applied to a new instance
     */
    @Override
    void applyConfig(NBConfiguration cfg);

    /**
     * Implement this method by returning an instance of {@link ConfigModel}.
     * Any configuration which is provided to the {@link #applyConfig(NBConfiguration)}
     * method will be validated through this model. A configuration model
     * is <em>required</em> in order to build a validated configuration
     * from source data provided by a user.
     * @return A valid configuration model for the implementing class
     */
    @Override
    NBConfigModel getConfigModel();
}
