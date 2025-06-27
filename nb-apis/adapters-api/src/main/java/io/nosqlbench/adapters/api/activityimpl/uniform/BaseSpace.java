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

/**
 * This example of a space uses the <EM>SelfT</EM> technique to enable
 * the self type to be used in method signatures and return types.
 *
 * {@inheritDoc}
 *
 * @param <SelfT>
 */
public class BaseSpace<SelfT extends BaseSpace<SelfT> > implements Space {

    private final String spaceName;
    private String originalName;
    private final DriverAdapter<?, SelfT> adapter;

    public BaseSpace(DriverAdapter<?,SelfT> adapter, long idx) {
        this(adapter, idx, String.valueOf(idx));
    }

    public BaseSpace(DriverAdapter<?,SelfT> adapter, long idx, String originalName) {
        this.spaceName = String.valueOf(idx);
        this.adapter = adapter;
        this.originalName = originalName;
    }

    /**
     * Set the original name (string representation of the key) for this space.
     * This is used when a space is created via ConcurrentIndexCacheWrapperWithName.
     *
     * @param originalName The original name for this space
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /**
     * Get the original name (string representation of the key) for this space.
     * This may be null if the space was not created via ConcurrentIndexCacheWrapperWithName.
     *
     * @return The original name for this space, or null if not set
     */
    public String getOriginalName() {
        return originalName;
    }

    @Override
    public String getName() {
        return spaceName;
    }

    public static class BasicSpace extends BaseSpace<BasicSpace> implements Space {
        public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx) {
            super(adapter, idx);
        }

        public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx, String originalName) {
            super(adapter, idx, originalName);
        }
    }
}
