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
    private final DriverAdapter<?, SelfT> adapter;

    public BaseSpace(DriverAdapter<?,SelfT> adapter, String spaceName) {
        this.spaceName = spaceName;
        this.adapter = adapter;
    }

    /**
     * Interpolate a template string with space-specific values.
     * Replaces {SPACEID}, {SPACE}, and {SPACENAME} with their respective values.
     *
     * @param template The template string to interpolate
     * @return The interpolated string
     */
    public String interpolateSpace(String template) {
        if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee][Ii][Dd]\\}.*")) {
            template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee][Ii][Dd]\\}", getName());
        }
        if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee]}.*")) {
            template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee]}", getName());
        }
        if (template.matches(".*\\{[Ss][Pp][Aa][Cc][Ee][Nn][Aa][Mm][Ee]\\}.*")) {
            template = template.replaceAll("\\{[Ss][Pp][Aa][Cc][Ee][Nn][Aa][Mm][Ee]}", getName());
        }
        return template;
    }

    @Override
    public String getName() {
        return spaceName;
    }

    public static class BasicSpace extends BaseSpace<BasicSpace> implements Space {
        public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx) {
            super(adapter, String.valueOf(idx));
        }

        public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx, String originalName) {
            super(adapter, originalName);
        }
    }
}
