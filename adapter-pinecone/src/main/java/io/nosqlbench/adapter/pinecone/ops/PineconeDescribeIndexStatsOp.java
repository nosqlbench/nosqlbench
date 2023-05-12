package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.PineconeConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeDescribeIndexStatsOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeDescribeIndexStatsOp.class);

    private final DescribeIndexStatsRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client describeIndexStats method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link DescribeIndexStatsRequest} built for this operation
     */
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
