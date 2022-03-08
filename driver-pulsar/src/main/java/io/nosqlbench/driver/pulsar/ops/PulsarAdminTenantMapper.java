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

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.Set;
import java.util.function.LongFunction;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class PulsarAdminTenantMapper extends PulsarAdminMapper {
    private final LongFunction<Set<String>> adminRolesFunc;
    private final LongFunction<Set<String>> allowedClustersFunc;
    private final LongFunction<String> tenantFunc;

    public PulsarAdminTenantMapper(CommandTemplate cmdTpl,
                                   PulsarSpace clientSpace,
                                   PulsarActivity pulsarActivity,
                                   LongFunction<Boolean> asyncApiFunc,
                                   LongFunction<Boolean> adminDelOpFunc,
                                   LongFunction<Set<String>> adminRolesFunc,
                                   LongFunction<Set<String>> allowedClustersFunc,
                                   LongFunction<String> tenantFunc)
    {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc, adminDelOpFunc);
        this.adminRolesFunc = adminRolesFunc;
        this.allowedClustersFunc = allowedClustersFunc;
        this.tenantFunc = tenantFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        boolean asyncApi = asyncApiFunc.apply(value);
        boolean adminDelOp = adminDelOpFunc.apply(value);
        Set<String> adminRoleSet = adminRolesFunc.apply(value);
        Set<String> allowedClusterSet = allowedClustersFunc.apply(value);
        String tenant = tenantFunc.apply(value);

        return new PulsarAdminTenantOp(
            clientSpace,
            asyncApi,
            adminDelOp,
            adminRoleSet,
            allowedClusterSet,
            tenant);
    }
}
