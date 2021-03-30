package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.common.policies.data.TenantInfo;

import java.util.Set;

public class PulsarAdminCrtTennamOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminCrtTennamOp.class);

    private final PulsarSpace clientSpace;
    private final Set<String> adminRoleSet;
    private final Set<String> allowedClusterSet;
    private final String tenant;
    private final String namespace;

    public PulsarAdminCrtTennamOp(PulsarSpace clientSpace,
                                  Set<String> adminRoleSet,
                                  Set<String> allowedClusterSet,
                                  String tenant,
                                  String namespace) {
        this.clientSpace = clientSpace;
        this.adminRoleSet = adminRoleSet;
        this.allowedClusterSet = allowedClusterSet;
        this.tenant = tenant;
        this.namespace = namespace;
    }

    private void processPulsarAdminException(PulsarAdminException e, String finalErrMsg) {
        int statusCode = e.getStatusCode();

        // 409 conflict: resource already exists
        if ( (statusCode >= 400) && (statusCode != 409) ) {
            throw new RuntimeException(finalErrMsg);
        }
    }

    @Override
    public void run() {
        if (StringUtils.isBlank(tenant) && !StringUtils.isBlank(namespace)) {
            throw new RuntimeException("Can't create a namespace without a tenant!");
        }

        PulsarAdmin pulsarAdmin = clientSpace.getPulsarAdmin();
        if (!StringUtils.isBlank(tenant)) {
            TenantInfo tenantInfo = new TenantInfo();
            tenantInfo.setAdminRoles(adminRoleSet);

            if ( !allowedClusterSet.isEmpty() ) {
                tenantInfo.setAllowedClusters(allowedClusterSet);
            }
            else {
                tenantInfo.setAllowedClusters(clientSpace.getPulsarClusterMetadata());
            }

            try {
                pulsarAdmin.tenants().createTenant(tenant, tenantInfo);
            } catch (PulsarAdminException e) {
                processPulsarAdminException(e, "Failed to create pulsar tenant: " + tenant);
            }
        }

        if (!StringUtils.isBlank(namespace)) {
            try {
                pulsarAdmin.namespaces().createNamespace(tenant + "/" + namespace);
            } catch (PulsarAdminException e) {
                processPulsarAdminException(e, "Failed to create pulsar namespace: " + tenant + "/" + namespace);
            }
        }
    }
}
