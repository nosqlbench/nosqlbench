package io.nosqlbench.adapter.pinecone;

import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PineConeSpace {

    private final static Logger LOGGER = LogManager.getLogger(PineConeSpace.class);
    private final String apiKey;
    private final String environment;
    private final String projectName;
    private final String name;

    private final PineconeClient client;
    private PineconeClientConfig config;

    /**
     * Connections are index-specific so we need to allow for multiple connection management across indices.
     * However, note that a single connection object is thread safe and can be used by multiple clients.
     */
    private Map<String,PineconeConnection> connections = new HashMap<String,PineconeConnection>();

    public PineConeSpace(String apiKey, String environment, String projectName, String name) {
        this.apiKey = apiKey;
        this.environment = environment;
        this.projectName = projectName;
        this.name = name;

        config = new PineconeClientConfig()
            .withApiKey(apiKey)
            .withEnvironment(environment)
            .withProjectName(projectName);

        this.client = new PineconeClient(config);
    }

    public synchronized PineconeConnection getConnection(String index) {
        PineconeConnection connection = connections.get(index);
        if (connection == null) {
            PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig().withIndexName(index);
            connection = client.connect(connectionConfig);
            connections.put(index, connection);
        }
        return connection;
    }

}
