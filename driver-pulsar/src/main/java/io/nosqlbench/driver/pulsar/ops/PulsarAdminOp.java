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

package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PulsarAdminOp extends SyncPulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminOp.class);

    protected final PulsarSpace clientSpace;
    protected final boolean asyncApi;
    protected final boolean adminDelOp;

    protected PulsarAdminOp(PulsarSpace clientSpace,
                         boolean asyncApi,
                         boolean adminDelOp)
    {
        this.clientSpace = clientSpace;
        this.asyncApi = asyncApi;
        this.adminDelOp = adminDelOp;
    }
}
