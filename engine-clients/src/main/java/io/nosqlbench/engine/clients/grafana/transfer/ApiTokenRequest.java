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

package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.Set;

public class ApiTokenRequest {

    public static final Set<String> VALID_ROLES = Set.of("Admin", "Editor", "Viewer");
    private final String name;
    private final String role;
    private final long ttl;

    public ApiTokenRequest(String name, String role, long ttl) {
        this.name = name;
        this.role = checkRole(role);
        this.ttl = ttl;
    }

    private String checkRole(String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new RuntimeException("Role '" + role + "' must be one of " + VALID_ROLES);
        }
        return role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public long getTtl() {
        return ttl;
    }
}
