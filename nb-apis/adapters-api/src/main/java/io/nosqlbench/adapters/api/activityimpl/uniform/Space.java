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


import io.nosqlbench.nb.api.components.core.NBNamedElement;

/**
 * <P>A space is simply a separate namespace associated with an instance of a
 * native client or driver. This allows for the emulation of many clients
 * in testing scenarios. Within the operations for an adapter, the space
 * may be needed, for example, to construct prepared statements, or other
 * 'session-attached' objects. Put any state that you would normally
 * associate with an instance of a native driver into a space.
 * A function to access the cycle-specific space instance is provided where
 * you might needed, such as in the mapping or dispensing APIs.
 */
public interface Space extends NBNamedElement, AutoCloseable {

    @Override
    default void close() throws Exception {
    }
}
