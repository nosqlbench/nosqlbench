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
import org.apache.pulsar.client.admin.Namespaces;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;

import java.util.concurrent.CompletableFuture;

public class PulsarAdminNamespaceOp extends PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminNamespaceOp.class);

    private final String fullNsName;

    public PulsarAdminNamespaceOp(PulsarSpace clientSpace,
                                  boolean asyncApi,
                                  boolean adminDelOp,
                                  String fullNsName)
    {
        super(clientSpace, asyncApi, adminDelOp);
        this.fullNsName = fullNsName;
    }

    @Override
    public void run() {
        // Do nothing if the namespace name is empty
        if ( StringUtils.isBlank(fullNsName) ) return;

        PulsarAdmin pulsarAdmin = clientSpace.getPulsarAdmin();
        Namespaces namespaces = pulsarAdmin.namespaces();

        // Admin API - create tenants and namespaces
        if (!adminDelOp) {
            try {
                if (!asyncApi) {
                    namespaces.createNamespace(fullNsName);
                    logger.trace("Successfully created namespace \"" + fullNsName + "\" synchronously!");
                } else {
                    CompletableFuture<Void> future = namespaces.createNamespaceAsync(fullNsName);
                    future.whenComplete((unused, throwable) ->
                        logger.trace("Successfully created namespace \"" + fullNsName + "\" asynchronously!"))
                    .exceptionally(ex -> {
                        logger.error("Failed to create namespace \"" + fullNsName + "\" asynchronously!:" + ex.getMessage());
                        return null;
                    });
                }
            }
            catch (PulsarAdminException.ConflictException ce) {
                // do nothing if the namespace already exists
            }
            catch (PulsarAdminException e) {
                e.printStackTrace();
                throw new RuntimeException("Unexpected error when creating pulsar namespace: " + fullNsName);
            }
        }
        // Admin API - delete tenants and namespaces
        else {
            try {
                if (!asyncApi) {
                    namespaces.deleteNamespace(fullNsName, true);
                    logger.trace("Successfully deleted namespace \"" + fullNsName + "\" synchronously!");
                } else {
                    CompletableFuture<Void> future = namespaces.deleteNamespaceAsync(fullNsName, true);
                    future.whenComplete((unused, throwable) ->
                        logger.trace("Successfully deleted namespace \"" + fullNsName + "\" asynchronously!"))
                    .exceptionally(ex -> {
                        logger.error("Failed to delete namespace \"" + fullNsName + "\" asynchronously!");
                        return null;
                    });
                }
            }
            catch (PulsarAdminException.NotFoundException nfe) {
                // do nothing if the namespace doesn't exist
            }
            catch (PulsarAdminException e) {
                e.printStackTrace();
                throw new RuntimeException("Unexpected error when deleting pulsar namespace: " + fullNsName);
            }
        }
    }
}
