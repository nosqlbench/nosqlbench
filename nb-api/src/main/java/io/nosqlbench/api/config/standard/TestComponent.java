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

package io.nosqlbench.api.config.standard;

import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponent;

import java.util.Arrays;

public class TestComponent extends NBBaseComponent {

    public static final NBComponent INSTANCE = new TestComponent();

    public TestComponent(String... labels) {
        super(null,NBLabels.forKV((Object[]) labels));
    }

    public TestComponent(NBComponent parent, String... labels) {
        super(parent, NBLabels.forKV((Object[]) labels));
    }

    @Override
    public String toString() {
        return getLabels().linearizeAsMetrics() + " ("+this.getClass().getSimpleName()+")";
    }

    @Override
    public NBComponent attach(NBComponent... children) {
        System.out.println("attaching children:" + Arrays.toString(children));
        return super.attach(children);
    }

    @Override
    public NBComponent detach(NBComponent... children) {
        System.out.println("detaching children:" + Arrays.toString(children));
        return super.detach(children);
    }
}
