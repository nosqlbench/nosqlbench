package io.nosqlbench.adapters.api.activityimpl.uniform;

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


import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

public class BaseSpace<SelfT extends BaseSpace<SelfT> > implements Space {

    private final String spaceName;
    private final DriverAdapter<?, SelfT> adapter;

    public BaseSpace(DriverAdapter<?,SelfT> adapter, long idx) {
        this.spaceName = String.valueOf(idx);
        this.adapter = adapter;
    }

    @Override
    public String getName() {
        return spaceName;
    }

    public static class BasicSpace extends BaseSpace<BasicSpace> implements Space {
        public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx) {
            super(adapter, idx);
        }
    }
}
