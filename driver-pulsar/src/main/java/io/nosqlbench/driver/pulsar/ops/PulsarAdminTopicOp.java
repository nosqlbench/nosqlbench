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
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Topics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class PulsarAdminTopicOp extends PulsarAdminOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminTopicOp.class);

    private final String topicUri;
    private final boolean partitionTopic;
    private final int partitionNum;
    private final String fullNsName;

    public PulsarAdminTopicOp(PulsarSpace clientSpace,
                              String topicUri,
                              boolean partitionTopic,
                              int partitionNum,
                              boolean asyncApi,
                              boolean adminDelOp)
    {
        super(clientSpace, asyncApi, adminDelOp);
        this.topicUri = topicUri;
        this.partitionTopic = partitionTopic;
        this.partitionNum = partitionNum;
        this.fullNsName = PulsarActivityUtil.getFullNamespaceName(this.topicUri);
    }

    // Check whether the specified topic already exists
    private boolean checkTopicExistence(Topics topics, String topicUri) {
        // Check the existence of the topic
        List<String> topicListWorkingArea = new ArrayList<>();
        try {
            if (!partitionTopic) {
                topicListWorkingArea = topics.getList(fullNsName);
            }
            else {
                topicListWorkingArea = topics.getPartitionedTopicList(fullNsName);
            }
        }
        catch (PulsarAdminException.NotFoundException nfe) {
            // do nothing
        }
        catch (PulsarAdminException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve topic info.for pulsar namespace: " + fullNsName);
        }

        return ( !topicListWorkingArea.isEmpty() && topicListWorkingArea.contains(topicUri) );
    }

    @Override
    public void run() {
        PulsarAdmin pulsarAdmin = clientSpace.getPulsarAdmin();
        Topics topics = pulsarAdmin.topics();

        try {
            // Create the topic
            if (!adminDelOp) {
                if (!partitionTopic) {
                    if (!asyncApi) {
                        topics.createNonPartitionedTopic(topicUri);
                        logger.trace("Successfully created non-partitioned topic \"" + topicUri + "\" synchronously!");
                    } else {
                        CompletableFuture<Void> future = topics.createNonPartitionedTopicAsync(topicUri);
                        future.whenComplete((unused, throwable)
                            -> logger.trace("Successfully created non-partitioned topic \"" + topicUri + "\" asynchronously!"))
                        .exceptionally(ex -> {
                            logger.error("Failed to create non-partitioned topic \"" + topicUri + "\" asynchronously!");
                            return null;
                        });
                    }
                } else {
                    if (!asyncApi) {
                        topics.createPartitionedTopic(topicUri, partitionNum);
                        logger.trace("Successfully created partitioned topic \"" + topicUri + "\"" +
                            "(partition_num: " + partitionNum + ") synchronously!");
                    } else {
                        CompletableFuture<Void> future = topics.createPartitionedTopicAsync(topicUri, partitionNum);
                        future.whenComplete((unused, throwable)
                            -> logger.trace("Successfully created partitioned topic \"" + topicUri + "\"" +
                            "(partition_num: " + partitionNum + ") asynchronously!"))
                        .exceptionally(ex -> {
                            logger.error("Failed to create partitioned topic \"" + topicUri + "\"" +
                                "(partition_num: " + partitionNum + ") asynchronously!");
                            return null;
                        });
                    }
                }
            }
            // Delete the topic
            else {
                if (!partitionTopic) {
                    if (!asyncApi) {
                        topics.delete(topicUri, true);
                        logger.trace("Successfully deleted non-partitioned topic \"" + topicUri + "\" synchronously!");
                    } else {
                        CompletableFuture<Void> future = topics.deleteAsync(topicUri, true);
                        future.whenComplete((unused, throwable)
                            -> logger.trace("Successfully deleted non-partitioned topic \"" + topicUri + "\" asynchronously!"))
                        .exceptionally(ex -> {
                            logger.error("Failed to delete non-partitioned topic \"" + topicUri + "\" asynchronously!");
                            return null;
                        });
                    }
                } else {
                    if (!asyncApi) {
                        topics.deletePartitionedTopic(topicUri, true);
                        logger.trace("Successfully deleted partitioned topic \"" + topicUri + "\" synchronously!");
                    } else {
                        CompletableFuture<Void> future = topics.deletePartitionedTopicAsync(topicUri, true);
                        future.whenComplete((unused, throwable)
                            -> logger.trace("Successfully deleted partitioned topic \"" + topicUri + "\" asynchronously!"))
                        .exceptionally(ex -> {
                            logger.error("Failed to delete partitioned topic \"" + topicUri + "\" asynchronously!");
                            return null;
                        });
                    }
                }
            }
        }
        catch (PulsarAdminException e) {
            e.printStackTrace();
            String errMsg = String.format("Unexpected error when %s pulsar topic: %s (partition topic: %b; partition number: %d)",
                (!adminDelOp ? "creating" : "deleting"),
                topicUri,
                partitionTopic,
                partitionNum);
            throw new RuntimeException(errMsg);
        }
    }
}
