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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.nb.api.config.standard.NBConfigurable;
import io.nosqlbench.nb.api.config.standard.NBReconfigurable;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.components.core.NBParentComponentInjection;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * The base type for building Diagnostic Op tasks, which are composed into
 * a diagnostic operation in order. This allows for a very flexible compositional
 * structure which can be extended as needed. The diag driver is meant to
 * be the core driver for the purposes of validating the NoSQLBench engine
 * and associated libraries and extensions.
 *
 * The tasks are retained as shared across threads by default.
 *
 * Tasks must provide a no-args constructor (or no constructor, which does the same).
 * Tasks must specify their configuration requirements via the {@link NBConfigurable}
 * interface.
 *
 * Tasks may be evented for updates from the activity params at runtime by implementing
 * the {@link NBReconfigurable} interface.
 */
public interface DiagTask extends
    BiFunction<Long,Map<String,Object>, Map<String,Object>>,
    NBConfigurable,
    NBLabeledElement,
    NBParentComponentInjection
{
    Map<String, Object> apply(Long cycle, Map<String, Object> opstate);

    void setName(String opname);

    void setLabelsFrom(NBLabeledElement labeledElement);

    NBLabeledElement getParentLabels();
}
