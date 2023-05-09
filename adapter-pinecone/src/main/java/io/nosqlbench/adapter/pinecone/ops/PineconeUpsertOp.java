package io.nosqlbench.adapter.pinecone.ops;

import io.pinecone.proto.UpsertRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpsertResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeUpsertOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeUpsertOp.class);

    private UpsertRequest request;

    public PineconeUpsertOp(PineconeConnection connection, UpsertRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            UpsertResponse response = connection.getBlockingStub().upsert(request);
            LOGGER.info("Put " + response.getUpsertedCount() + " vectors into the index");
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do upsert", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
