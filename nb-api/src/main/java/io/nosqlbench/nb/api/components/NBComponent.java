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

package io.nosqlbench.nb.api.components;

import io.nosqlbench.nb.api.components.decorators.NBProviderSearch;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.List;

/**
 * A Component is a functional element of the NoSQLBench runtime which is:
 * <UL>
 *     <LI>Contract Oriented - Components are based on well-defined interfaces.</LI>
 *     <LI>Modular - Components are wired together by configuration.</LI>
 *     <LI>Configurable - Components have configurations which are well defined and type safe.</LI>
 *     <LI>User Facing - Components are top level constructs which users interact with.</LI>
 *     <LI>Hierarchic - Components fit together in a runtime hierarchy. Only the ROOT component is allowed to have no parents.</LI>
 *     <LI>Addressable - Each component has a set of metadata which allows it to be identified clearly under its parent.</LI>
 * </UL>
 *
 * This interface includes more aspects of above by extension going forward.
 */
public interface NBComponent extends
    AutoCloseable,
    NBLabeledElement,
    NBComponentMetrics,
    NBComponentServices,
    NBComponentEvents,
    NBProviderSearch {

    NBComponent EMPTY_COMPONENT = new NBBaseComponent(null);

    NBComponent getParent();

    NBComponent attachChild(NBComponent... children);

    NBComponent detachChild(NBComponent... children);

    List<NBComponent> getChildren();

    NBLabels getComponentOnlyLabels();

    default void beforeDetach() {}

    @Override
    void close() throws RuntimeException;

    void reportExecutionMetric(NBMetric m);
}
