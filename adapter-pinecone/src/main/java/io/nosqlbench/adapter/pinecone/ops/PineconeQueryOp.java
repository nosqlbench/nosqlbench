package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.QueryRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.QueryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeQueryOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeQueryOp.class);

    private final QueryRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client query method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link QueryRequest} built for this operation
     */
    public PineconeQueryOp(PineconeConnection connection, QueryRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            QueryResponse response = connection.getBlockingStub().query(request);
            LOGGER.info("got query result ids: "
                + response.getResultsList().get(0).getMatchesList());
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do Query", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
