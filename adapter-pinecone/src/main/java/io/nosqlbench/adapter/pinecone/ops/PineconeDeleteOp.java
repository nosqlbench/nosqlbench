package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DeleteResponse;
import io.pinecone.PineconeConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeDeleteOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeDeleteOp.class);

    private final DeleteRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client delete method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link DeleteRequest} built for this operation
     */
    public PineconeDeleteOp(PineconeConnection connection, DeleteRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            DeleteResponse response = connection.getBlockingStub().delete(request);
            LOGGER.info(response.toString());
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do delete", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
