package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Topics;

import java.util.ArrayList;
import java.util.List;


public class PulsarAdminCrtTopOp extends SyncPulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarAdminCrtTopOp.class);

    private final PulsarSpace clientSpace;
    private final String topicUri;
    private final boolean partitionTopic;
    private final int partitionNum;
    private final String fullNsName;

    public PulsarAdminCrtTopOp(PulsarSpace clientSpace,
                               String topicUri,
                               boolean partitionTopic,
                               int partitionNum) {
        this.clientSpace = clientSpace;
        this.topicUri = topicUri;
        this.partitionTopic = partitionTopic;
        this.partitionNum = partitionNum;

        // Get tenant/namespace string
        // - topicUri   : persistent://<tenant>/<namespace>/<topic>
        // - tmpStr     : <tenant>/<namespace>/<topic>
        // - fullNsName : <tenant>/<namespace>
        String tmpStr = StringUtils.substringAfter(this.topicUri,"://");
        this.fullNsName = StringUtils.substringBeforeLast(tmpStr, "/");
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
        PulsarAdmin pulsarAdmin = clientSpace.getPulsarAdmin();

        Topics topics = pulsarAdmin.topics();
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
            processPulsarAdminException(e, "Failed to retrieve topic info.for pulsar namespace: " + fullNsName);
        }

        // If the topic doesn't exist, create it.
        if (topicListWorkingArea.isEmpty() || !topicListWorkingArea.contains(topicUri)) {
            try {
                if (!partitionTopic) {
                    topics.createNonPartitionedTopic(topicUri);
                }
                else {
                    topics.createPartitionedTopic(topicUri, partitionNum);
                }
            } catch (PulsarAdminException e) {
                String errMsg = String.format("Failed to create pulsar topic: %s (partition topic: %b; partition number: %d",
                    topicUri,
                    partitionTopic,
                    partitionNum);

                processPulsarAdminException(e, errMsg);
            }
        }
    }
}
