/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.api.config.standard.NBConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * An Op Template Supplier can provide its own source of op templates instead
 * of relying on the built-in mechanism. By default, the built-in mechanism
 * will read op definitions from parameters first, then any ops (statements)
 * from yaml files provided in the workload= activity parameters.
 */
public interface OpTemplateSupplier extends DriverAdapterDecorators {

    Optional<List<OpTemplate>> loadOpTemplates(NBConfiguration cfg);
}
