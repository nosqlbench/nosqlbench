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

package io.nosqlbench.nb.spectest.api;

import io.nosqlbench.nb.spectest.core.STNodeAssembly;
import io.nosqlbench.nb.spectest.traversal.STNodePredicates;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * A NodeScanner extracts a sequence of {@link STNodeAssembly}s, typically
 * by use of {@link STNodePredicates}
 */
public interface STPathLoader extends Function<Path, List<STNodeAssembly>> {

}
