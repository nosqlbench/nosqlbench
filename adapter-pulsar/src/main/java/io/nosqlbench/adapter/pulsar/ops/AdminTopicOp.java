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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Topics;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AdminTopicOp extends PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(AdminTopicOp.class);

    private final String topicName;
    private final boolean enablePart;
    private final int partNum;

    public AdminTopicOp(PulsarAdmin pulsarAdmin,
                        boolean asyncApi,
                        boolean adminDelOp,
                        String topicName,
                        boolean enablePart,
                        int partNum) {
        super(pulsarAdmin, asyncApi, adminDelOp);
        this.topicName = topicName;
        this.enablePart = enablePart;
        this.partNum = partNum;
    }

    @Override
    public Void apply(long value) {

        // Do nothing if the topic name is empty
        if ( !StringUtils.isBlank(topicName) ) {
            Topics topics = pulsarAdmin.topics();

            try {
                // Create the topic
                if (!adminDelOp) {
                    if (!enablePart) {
                        if (!asyncApi) {
                            topics.createNonPartitionedTopic(topicName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Successful sync creation of non-partitioned topic \"{}\"", topicName);
                            }
                        } else {
                            CompletableFuture<Void> future = topics.createNonPartitionedTopicAsync(topicName);
                            future.whenComplete((unused, throwable) -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Successful async creation of non-partitioned topic \"{}\"", topicName);
                                }
                            }).exceptionally(ex -> {
                                if (logger.isDebugEnabled()) {
                                    logger.error("Failed async creation non-partitioned topic \"{}\"", topicName);
                                    return null;
                                }
                                return  null;
                            });
                        }
                    } else {
                        if (!asyncApi) {
                            topics.createPartitionedTopic(topicName, partNum);
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Successful sync creation of partitioned topic \"{} (partition_num: {}\")",
                                    topicName, partNum);
                            }
                        } else {
                            CompletableFuture<Void> future = topics.createPartitionedTopicAsync(topicName, partNum);
                            future.whenComplete((unused, throwable) -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                        "Successful async creation of partitioned topic \"{} (partition_num: {}\")",
                                        topicName, partNum);
                                }
                            })
                            .exceptionally(ex -> {
                                if (logger.isDebugEnabled()) {
                                    logger.error(
                                        "Successful async creation of partitioned topic \"{} (partition_num: {}\")",
                                        topicName, partNum);
                                }
                                return null;
                            });
                        }
                    }
                }
                // Delete the topic
                else {
                    if (!enablePart) {
                        if (!asyncApi) {
                            topics.delete(topicName);
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Successful sync deletion of non-partitioned topic \"{}\"",
                                    topicName);
                            }
                        } else {
                            CompletableFuture<Void> future = topics.deleteAsync(topicName);
                            future.whenComplete((unused, throwable) -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                        "Successful async deletion of non-partitioned topic \"{}\"",
                                        topicName);
                                }
                            })
                            .exceptionally(ex -> {
                                if (logger.isDebugEnabled()) {
                                    logger.error(
                                        "Failed async deletion of non-partitioned topic \"{}\"",
                                        topicName);
                                }
                                return null;
                            });
                        }
                    } else {
                        if (!asyncApi) {
                            topics.deletePartitionedTopic(topicName);
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Successful sync deletion of partitioned topic \"{} (partition_num: {}\")",
                                    topicName, partNum);
                            }
                        } else {
                            CompletableFuture<Void> future = topics.deletePartitionedTopicAsync(topicName);
                            future.whenComplete((unused, throwable) -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                        "Successful async deletion of partitioned topic \"{} (partition_num: {}\")",
                                        topicName, partNum);
                                }
                            }).exceptionally(ex -> {
                                if (logger.isDebugEnabled()) {
                                    logger.error(
                                        "Failed async deletion of partitioned topic \"{} (partition_num: {}\")",
                                        topicName, partNum);
                                }
                                return null;
                            });
                        }
                    }
                }
            }
            catch (PulsarAdminException.NotFoundException nfe) {
                if (logger.isDebugEnabled()) {
                    logger.error("Topic \"{}\" doesn't exists - skip deletion!", topicName);
                }
            }
            catch (PulsarAdminException e) {
                String errMsg = String.format("Unexpected error when %s pulsar topic: %s (partition enabled: %b; partition number: %d)",
                    (!adminDelOp ? "creating" : "deleting"),
                    topicName,
                    enablePart,
                    partNum);
                throw new PulsarAdapterUnexpectedException(errMsg);
            }
        }

        return  null;
    }
}
