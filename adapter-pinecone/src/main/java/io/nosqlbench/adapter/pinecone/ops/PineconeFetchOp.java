package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.FetchRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeFetchOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeFetchOp.class);

    private FetchRequest request;

    public PineconeFetchOp(PineconeConnection connection, FetchRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            FetchResponse response = connection.getBlockingStub().fetch(request);
            // Do soemething with the response...
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do Fetch", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
