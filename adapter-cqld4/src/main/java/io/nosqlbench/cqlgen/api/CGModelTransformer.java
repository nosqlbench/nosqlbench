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

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.cqlgen.model.CqlModel;

import java.util.function.Function;

/**
 * Most of the functionality of {@link CqlModel} preparation is handled with transformers.
 * The type and order of transformers is important, as one transformer may be responsible
 * for preparing the model for one or more downstream transformers.
 */
public interface CGModelTransformer extends Function<CqlModel,CqlModel>, NBNamedElement {
    @Override
    CqlModel apply(CqlModel model);
    void setName(String name);
}
