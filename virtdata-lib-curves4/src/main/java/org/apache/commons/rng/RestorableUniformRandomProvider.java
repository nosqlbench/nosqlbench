/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.rng;

/**
 * Applies to generators whose internal state can be saved and restored.
 *
 * @since 1.0
 */
public interface RestorableUniformRandomProvider extends UniformRandomProvider {
    /**
     * Saves the state of a generator.
     *
     * @return the current state of this instance. It is a value that can
     * subsequently be passed to the {@link #restoreState(RandomProviderState)
     * restore} method.
     * @throws UnsupportedOperationException if the underlying source of
     * randomness does not support this functionality.
     */
    RandomProviderState saveState();

    /**
     * Restores the state of a generator.
     *
     * @param state State which this instance will be set to.
     * This parameter would usually have been obtained by a call to
     * {@link #saveState() saveState} performed either on the same
     * object as this one, or an object of the exact same class.
     * @throws UnsupportedOperationException if the underlying source of
     * randomness does not support this functionality.
     * @throws IllegalArgumentException if it was detected that the
     * {@code state} argument is incompatible with this intance.
     */
    void restoreState(RandomProviderState state);
}
