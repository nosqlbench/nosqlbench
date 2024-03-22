/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.milvus;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.labels.NBLabels;

import static io.nosqlbench.adapter.milvus.MilvusAdapterUtils.MILVUS;

@Service(value = DriverAdapterLoader.class, selector = MILVUS)
public class MilvusDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public MilvusDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new MilvusDriverAdapter(parent, childLabels);
    }
}
