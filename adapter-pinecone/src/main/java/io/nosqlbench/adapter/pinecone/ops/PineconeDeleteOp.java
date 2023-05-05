package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DeleteResponse;
import io.pinecone.PineconeConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeDeleteOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeDeleteOp.class);

    private DeleteRequest request;

    public PineconeDeleteOp(PineconeConnection connection, DeleteRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            DeleteResponse response = connection.getBlockingStub().delete(request);
            // Do soemething with the response...
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do delete", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
