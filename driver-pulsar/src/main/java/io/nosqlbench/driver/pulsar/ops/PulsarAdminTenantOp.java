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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.*;
import org.apache.pulsar.common.policies.data.TenantInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PulsarAdminTenantOp extends PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminTenantOp.class);

    private final Set<String> adminRoleSet;
    private final Set<String> allowedClusterSet;
    private final String tenant;

    public PulsarAdminTenantOp(PulsarSpace clientSpace,
                               boolean asyncApi,
                               boolean adminDelOp,
                               Set<String> adminRoleSet,
                               Set<String> allowedClusterSet,
                               String tenant)
    {
        super(clientSpace, asyncApi, adminDelOp);
        this.adminRoleSet = adminRoleSet;
        this.allowedClusterSet = allowedClusterSet;
        this.tenant = tenant;
    }

    @Override
    public void run() {
        // Do nothing if the tenant name is empty
        if ( StringUtils.isBlank(tenant) ) return;

        PulsarAdmin pulsarAdmin = clientSpace.getPulsarAdmin();
        Tenants tenants = pulsarAdmin.tenants();
        Namespaces namespaces = pulsarAdmin.namespaces();

        // Admin API - create tenants and namespaces
        if (!adminDelOp) {
            TenantInfo tenantInfo = TenantInfo.builder()
                .adminRoles(adminRoleSet)
                .allowedClusters(!allowedClusterSet.isEmpty() ? allowedClusterSet : clientSpace.getPulsarClusterMetadata())
                .build();

            try {
                if (!asyncApi) {
                    tenants.createTenant(tenant, tenantInfo);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successful sync creation of tenant {}", tenant);
                    }
                } else {
                    CompletableFuture<Void> future = tenants.createTenantAsync(tenant, tenantInfo);
                    future.whenComplete((unused, throwable) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successful async creation of tenant {}", tenant);
                        }
                }).exceptionally(ex -> {
                        logger.error("Failed async creation of tenant {}", tenant);
                        return null;
                    });
                }
            }
            catch (PulsarAdminException.ConflictException ce) {
                // do nothing if the tenant already exists
            }
            catch (PulsarAdminException e) {
                e.printStackTrace();
                throw new RuntimeException("Unexpected error when creating pulsar tenant: " + tenant);
            }
        }
        // Admin API - delete tenants and namespaces
        else {
            try {
                int nsNum = namespaces.getNamespaces(tenant).size();

                // Only delete a tenant when there is no underlying namespaces
                if ( nsNum == 0 ) {
                    if (!asyncApi) {
                        tenants.deleteTenant(tenant);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successful sync deletion of tenant {}", tenant);
                        }
                    } else {
                        CompletableFuture<Void> future = tenants.deleteTenantAsync(tenant);
                        future.whenComplete((unused, throwable) -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Successful async deletion of tenant {}", tenant);
                            }
                        }).exceptionally(ex -> {
                            if (logger.isDebugEnabled()) {
                                logger.error("Failed async deletion of tenant {}", tenant);
                            }
                            return null;
                        });
                    }
                }
            }
            catch (PulsarAdminException.NotFoundException nfe) {
                // do nothing if the tenant doesn't exist
            }
            catch (PulsarAdminException e) {
                e.printStackTrace();
                throw new RuntimeException("Unexpected error when deleting pulsar tenant: " + tenant);
            }
        }
    }
}
