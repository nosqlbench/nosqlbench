/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.extensions.example;

import io.nosqlbench.nb.api.extensions.SandboxPlugin;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;

public class ExamplePlugin extends NBBaseComponent implements SandboxPlugin {

    public ExamplePlugin(NBComponent parentComponent) {
        super(parentComponent);
    }

    public long getSum(int addend1, int addend2) {
        return addend1 + addend2;
    }
}
