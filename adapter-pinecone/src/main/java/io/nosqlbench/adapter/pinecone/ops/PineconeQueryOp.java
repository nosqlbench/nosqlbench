package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.QueryRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.QueryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeQueryOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeQueryOp.class);

    private QueryRequest request;

    public PineconeQueryOp(PineconeConnection connection, QueryRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            QueryResponse response = connection.getBlockingStub().query(request);
            // Do soemething with the response...
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do Query", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
