package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.UpdateRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpdateResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeUpdateOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeUpdateOp.class);

    private UpdateRequest request;

    public PineconeUpdateOp(PineconeConnection connection, UpdateRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            UpdateResponse response = connection.getBlockingStub().update(request);
            // Do soemething with the response...
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do Update", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
