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

import java.util.Collection;

/**
 * All implementation types which wish to have a type-marshalled configuration
 * should implement this interface IFF they wish to support follow-on configuration
 * after initialization. This is distinct and separate from initial configuration
 * via {@link NBConfigurable}. A type may be NBReconfigurable without implementing
 * the NBConfigurable interface, given that initialization for a type may happen
 * via constructor or other means.
 *
 * When a type which implements this interface is instantiated, and the
 * {@link NBConfiguration} was not injected into its constructor,
 * the builder should call
 * {@link NBConfigurable#applyConfig(NBConfiguration)} immediately
 * after calling the constructor.
 *
 * Subsequently, when an owning instance has a configuration update to provide to
 * the original NBConfigurable which <EM>ALSO</EM> implements NBReconfigurable, then
 * {@link NBReconfigurable#applyReconfig(NBConfiguration)} should be called.
 * The helper methods {@link #collectModels(Class, Collection)} and
 * {@link #applyMatching(NBConfiguration, Collection)} can be used to apply
 * reconfigurations to groups of elements with a shared configuration model.
 */
public interface NBReconfigurable extends NBCanReconfigure, NBReconfigModelProvider {

    /**
     * This applies a configuration to an element <EM>AFTER</EM> the initial
     * configuration from {@link NBConfigurable}.
     * @param recfg The configuration data to be applied to a new instance
     */
    @Override
    void applyReconfig(NBConfiguration recfg);

    /**
     * Implement this method by returning an instance of {@link ConfigModel}.
     * Any configuration which is provided to the {@link #applyReconfig(NBConfiguration)}
     * method will be validated through this model. A configuration model
     * is <em>required</em> in order to build a validated configuration
     * from source data provided by a user.
     * @return A valid configuration model for the implementing class
     */
    @Override
    NBConfigModel getReconfigModel();

    /**
     * Convenience method to apply a configuration to any object which
     * is expected to be be configurable.
     * @param cfg The cfg to apply
     * @param configurables zero or more Objects which may implement NBConfigurable
     */
    static void applyMatching(NBConfiguration cfg, Collection<?> configurables) {
        for (Object configurable : configurables) {
            if (configurable instanceof NBReconfigurable c) {
                NBConfiguration partial = c.getReconfigModel().matchConfig(cfg);
                c.applyReconfig(partial);
            }
        }
    }

    /**
     * Create a composite configuration model from all the provided elements
     * of the collection which implement {@link NBReconfigurable}
     *
     * @param of The nominal type of the composite configuration model
     * @param configurables zero or more elements which may implement {@link NBReconfigurable}
     * @return the combined model
     */
    static NBConfigModel collectModels(Class<?> of, Collection<?> configurables) {
        ConfigModel model = ConfigModel.of(of);
        for (Object configurable : configurables) {
            if (configurable instanceof NBReconfigurable c) {
                model = model.add(c.getReconfigModel());
            }
        }
        return model.asReadOnly();
    }

}
