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

package io.nosqlbench.cqlgen.api;

import io.nosqlbench.cqlgen.binders.Binding;
import io.nosqlbench.cqlgen.model.CqlColumnBase;

import java.util.Optional;

/**
 * A bindings library is simply a service point for a specific way
 * to map a column definition to a binding function.
 */
public interface BindingsLibrary {
    Optional<Binding> resolveBindingsFor(CqlColumnBase def);
}
