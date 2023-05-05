package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.PineconeConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeDescribeIndexStatsOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeDescribeIndexStatsOp.class);

    private DescribeIndexStatsRequest request;

    public PineconeDescribeIndexStatsOp(PineconeConnection connection, DescribeIndexStatsRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            DescribeIndexStatsResponse response = connection.getBlockingStub().describeIndexStats(request);
            // Do soemething with the response...
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do DescribeIndexStats", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }

}
