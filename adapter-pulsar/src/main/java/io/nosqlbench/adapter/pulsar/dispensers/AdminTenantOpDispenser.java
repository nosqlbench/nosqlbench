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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.adapter.pulsar.ops.AdminTenantOp;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.pulsar.client.admin.PulsarAdmin;

import java.util.*;
import java.util.function.LongFunction;

public class AdminTenantOpDispenser extends PulsarAdminOpDispenser {

    private final LongFunction<Set<String>> adminRolesFunc;
    private final LongFunction<Set<String>> allowedClustersFunc;
    public AdminTenantOpDispenser(DriverAdapter adapter,
                                  ParsedOp op,
                                  LongFunction<String> tgtNameFunc,
                                  PulsarAdmin pulsarAdmin) {
        super(adapter, op, tgtNameFunc, pulsarAdmin);

        adminRolesFunc = lookupStaticStrSetOpValueFunc("admin_roles");
        allowedClustersFunc = lookupStaticStrSetOpValueFunc("allowed_clusters");
    }

    @Override
    public AdminTenantOp apply(long cycle) {
        return new AdminTenantOp(
            pulsarAdmin,
            asyncApiFunc.apply(cycle),
            adminDelOpFunc.apply(cycle),
            tgtNameFunc.apply(cycle),
            adminRolesFunc.apply(cycle),
            allowedClustersFunc.apply(cycle));
    }
}
