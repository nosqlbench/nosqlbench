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
            throw new RuntimeException("Role '" + role + "' must be one of " + VALID_ROLES.toString());
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
