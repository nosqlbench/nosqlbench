package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.FetchRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeFetchOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeFetchOp.class);

    private final FetchRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client fetch method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link FetchRequest} built for this operation
     */
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
