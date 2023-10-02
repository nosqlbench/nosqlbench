/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.components;

import io.nosqlbench.api.config.standard.TestComponent;

public class NBComponentSubScope implements AutoCloseable {

    private NBComponent[] components;

    public NBComponentSubScope(NBComponent... components) {
        this.components = components;
    }
    @Override
    public void close() throws RuntimeException {
        for (NBComponent component : components) {
            component.beforeDetach();
            NBComponent parent = component.getParent();
            if (parent!=null) {
                parent.detachChild(component);
            }
        }
    }

    public void add(TestComponent... adding) {
        NBComponent[] newAry = new NBComponent[components.length+adding.length];
        System.arraycopy(components,0,newAry,0,components.length);
        System.arraycopy(adding,0,newAry,components.length,adding.length);
        this.components = newAry;
    }
}
