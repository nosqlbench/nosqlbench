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

package io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets;

/**
 * This interface represents the union of interfaces needed for all of the
 * behavioral facets of a useful Op implementation. By implementing
 * these faceted interfaces by way of the OpFacets interface, an
 * implementation can be a state carrier that exposes a contextual
 * interface by declaration.
 *
 * While this not a secure method of
 * enforcing type and interface over shared state, it does provide
 * a high degree of simplification for developers who wish to constrain
 * the operational view of an object according to an implied state machine.
 *
 * @param <D> The data carrier parameter type. Any OpFacets implementation can
 *           be used  to carry any state which is needed to support a specific type of
 *           operation.
 */
public interface OpFacets<D> extends TrackedOp<D>, StartedOp<D>, SucceededOp<D>, FailedOp<D>, SkippedOp<D> {

    default int compareTo(OpFacets<D> o) {
        return Long.compare(getCycle(),o.getCycle());
    }
}
