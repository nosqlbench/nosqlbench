package io.nosqlbench.adapter.diag;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;

/**
 * <P>This service allows for the dynamic instancing of {@link DriverAdapter}s as services,
 * using a well-defined method signature instead of (just) a no-args constructor. Since all key elements
 * of the nosqlbench runtime are assembled into a component tree, each one requires an attachment
 * point (parent) and child identifiers (label names and values) that uniquely describe it. This would typically be
 * encoded as a constructor signature, however, there is no SPI mechanism which makes this easy to manage across JDPA
 * and non-JDPA runtimes. So instead, we indirect this to a higher level service which has one and only one
 * responsibility: to provide an instance through the well-defined API of {@link #load(NBComponent, NBLabels)}.
 * </P>
 */
public interface DriverAdapterLoader {
    public <A extends CycleOp<?>, B extends Space> DriverAdapter<A, B> load(NBComponent parent, NBLabels childLabels);
}
