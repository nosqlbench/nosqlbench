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

import java.util.Arrays;
import java.util.Collection;

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

    /**
     * Convenience method to apply a configuration to any object which
     * is expected to be be configurable.
     * @param cfg The cfg to apply
     * @param configurables zero or more Objects which may implement NBConfigurable
     */
    static void applyMatching(NBConfiguration cfg, Object... configurables) {
        for (Object configurable : configurables) {
            if (configurable instanceof NBConfigurable c) {
                NBConfiguration partial = c.getConfigModel().matchConfig(cfg);
                c.applyConfig(partial);
            }
        }
    }

    static NBConfigModel collectModels(Class<?> of, Collection<?> configurables) {
        ConfigModel model = ConfigModel.of(of);
        for (Object configurable : configurables) {
            if (configurable instanceof NBConfigurable c) {
                model = model.add(c.getConfigModel());
            }
        }
        return model.asReadOnly();
    }

    static NBConfigModel collectModels(Class<?> of, Object... configurables) {
        return collectModels(of, Arrays.asList(configurables));
    }

}
