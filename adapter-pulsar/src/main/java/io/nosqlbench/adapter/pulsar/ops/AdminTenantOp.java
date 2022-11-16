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

package io.nosqlbench.adapter.pulsar.ops;

import io.nosqlbench.adapter.pulsar.PulsarDriverAdapter;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.*;
import org.apache.pulsar.common.policies.data.TenantInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AdminTenantOp extends PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(AdminTenantOp.class);

    private final Set<String> adminRoles;
    private final Set<String> allowedClusters;
    private final String tntName;

    public AdminTenantOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                         PulsarAdmin pulsarAdmin,
                         boolean asyncApi,
                         boolean adminDelOp,
                         String tntName,
                         Set<String> adminRoles,
                         Set<String> allowedClusters) {
        super(pulsarAdapterMetrics, pulsarAdmin, asyncApi, adminDelOp);

        this.tntName = tntName;
        this.adminRoles = adminRoles;
        this.allowedClusters = allowedClusters;
    }

    @Override
    public Void apply(long value) {

        // Do nothing if the tenant name is empty
        if ( !StringUtils.isBlank(tntName) ) {
            Tenants tenants = pulsarAdmin.tenants();

            // Admin API - create tenants and namespaces
            if (!adminDelOp) {
                try {
                    Set<String> existingPulsarClusters = new HashSet<>();
                    Clusters clusters = pulsarAdmin.clusters();
                    CollectionUtils.addAll(existingPulsarClusters, clusters.getClusters().listIterator());

                    TenantInfo tenantInfo = TenantInfo.builder()
                        .adminRoles(adminRoles)
                        .allowedClusters(!allowedClusters.isEmpty() ? allowedClusters : existingPulsarClusters)
                        .build();

                    if (!asyncApi) {
                        tenants.createTenant(tntName, tenantInfo);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successful sync creation of tenant \"{}\"", tntName);
                        }
                    }
                    else {
                        CompletableFuture<Void> future = tenants.createTenantAsync(tntName, tenantInfo);
                        future.whenComplete((unused, throwable) -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Successful async creation of tenant \"{}\"", tntName);
                            }
                        }).exceptionally(ex -> {
                            if (logger.isDebugEnabled()) {
                                logger.error("Failed async creation of tenant \"{}\"", tntName);
                            }
                            return null;
                        });
                    }
                }
                catch (PulsarAdminException.ConflictException ce) {
                    if (logger.isDebugEnabled()) {
                        logger.error("Tenant \"{}\" already exists - skip creation!", tntName);
                    }
                }
                catch (PulsarAdminException e) {
                    throw new PulsarAdapterUnexpectedException(
                        "Unexpected error when creating pulsar tenant \"" + tntName + "\"");
                }
            }
            // Admin API - delete tenants and namespaces
            else {
                try {
                    Namespaces namespaces = pulsarAdmin.namespaces();
                    int nsNum = namespaces.getNamespaces(tntName).size();

                    // Only delete a tenant when there is no underlying namespaces
                    if ( nsNum == 0 ) {
                        if (!asyncApi) {
                            tenants.deleteTenant(tntName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Successful sync deletion of tenant \"{}\"", tntName);
                            }
                        }
                        else {
                            CompletableFuture<Void> future = tenants.deleteTenantAsync(tntName);
                            future.whenComplete((unused, throwable) -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Successful async deletion of tenant \"{}\"", tntName);
                                }
                            }).exceptionally(ex -> {
                                if (logger.isDebugEnabled()) {
                                    logger.error("Failed async deletion of tenant \"{}\"", tntName);
                                }
                                return null;
                            });
                        }
                    }
                }
                catch (PulsarAdminException.NotFoundException nfe) {
                    if (logger.isDebugEnabled()) {
                        logger.error("Tenant \"{}\" doesn't exists - skip deletion!", tntName);
                    }
                }
                catch (PulsarAdminException e) {
                    throw new PulsarAdapterUnexpectedException(
                        "Unexpected error when deleting pulsar tenant \"" + tntName + "\"");
                }
            }
        }

        return null;
    }
}
