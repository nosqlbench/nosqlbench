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

import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.Namespaces;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;

import java.util.concurrent.CompletableFuture;

public class AdminNamespaceOp extends  PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(AdminNamespaceOp.class);

    // in format: <tenant>/<namespace>
    private final String nsName;

    public AdminNamespaceOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                            PulsarAdmin pulsarAdmin,
                            boolean asyncApi,
                            boolean adminDelOp,
                            String nsName) {
        super(pulsarAdapterMetrics, pulsarAdmin, asyncApi, adminDelOp);
        this.nsName = nsName;
    }

    @Override
    public Void apply(long value) {

        // Do nothing if the namespace name is empty
        if ( !StringUtils.isBlank(nsName) ) {

            Namespaces namespaces = pulsarAdmin.namespaces();

            // Admin API - create tenants and namespaces
            if (!adminDelOp) {
                try {
                    if (!asyncApi) {
                        namespaces.createNamespace(nsName);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successful sync creation of namespace \"{}\"", nsName);
                        }
                    } else {
                        CompletableFuture<Void> future = namespaces.createNamespaceAsync(nsName);
                        future.whenComplete((unused, throwable) -> {
                            if (logger.isDebugEnabled()) {
                                logger.trace("Successful async creation of namespace \"{}\"", nsName);
                            }
                        }).exceptionally(ex -> {
                            if (logger.isDebugEnabled()) {
                                logger.error("Failed async creation of namespace \"{}\"", nsName);
                            }
                            return null;
                        });
                    }
                }
                catch (PulsarAdminException.ConflictException ce) {
                    if (logger.isDebugEnabled()) {
                        logger.error("Namespace \"{}\" already exists - skip creation!", nsName);
                    }
                }
                catch (PulsarAdminException e) {
                    throw new PulsarAdapterUnexpectedException(
                        "Unexpected error when creating pulsar namespace \"" + nsName + "\"");
                }
            }
            // Admin API - delete tenants and namespaces
            else {
                try {
                    if (!asyncApi) {
                        namespaces.deleteNamespace(nsName);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successful sync deletion of namespace \"{}\"", nsName);
                        }
                    } else {
                        CompletableFuture<Void> future = namespaces.deleteNamespaceAsync(nsName, true);
                        future.whenComplete((unused, throwable) -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Successful sync deletion of namespace \"{}\"", nsName);
                            }
                        }).exceptionally(ex -> {
                            if (logger.isDebugEnabled()) {
                                logger.error("Failed async deletion of namespace \"{}\"", nsName);
                            }
                            return null;
                        });
                    }
                }
                catch (PulsarAdminException.NotFoundException nfe) {
                    if (logger.isDebugEnabled()) {
                        logger.error("Namespace \"{}\" doesn't exists - skip deletion!", nsName);
                    }
                }
                catch (PulsarAdminException e) {
                    throw new PulsarAdapterUnexpectedException(
                        "Unexpected error when deleting pulsar namespace \"" + nsName + "\"");
                }
            }
        }

        return null;
    }
}
