package io.nosqlbench.nb.api.config.params;

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


import java.util.List;

/**
 * A Config Source knows how to read a block of data and convert it
 * into a stream of zero or more configuration elements.
 */
public interface ConfigSource {

    /**
     * Test the input data format to see if it appears valid for reading
     * with this config source.
     *
     * @param source An object of any kind
     * @return true if the text is parsable by this config source
     */
    boolean canRead(Object source);

    /**
     * Read the source of data into a collection of config elements
     *
     * @param source An object of any kind
     * @return a collection of {@link Element}s
     */
    List<ElementData> getAll(String injectedName, Object source);

    /**
     * If an element was created with a name, this name must be returned as the
     * canonical name. If it was not, then the name field can provide the name.
     * @return A name, or null if it is not given nor present in the name field
     */
    String getName();
}
