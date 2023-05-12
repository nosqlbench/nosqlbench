package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PineconeSpace {

    private final static Logger LOGGER = LogManager.getLogger(PineconeSpace.class);
    private final String apiKey;
    private final String environment;
    private final String projectName;
    private final String name;

    private final PineconeClient client;
    private final PineconeClientConfig config;

    private final Map<String,PineconeConnection> connections = new HashMap<String,PineconeConnection>();

    /**
     * Create a new PineconeSpace Object which stores all stateful contextual information needed to interact
     * with the Pinecone database instance.
     *
     * @param name  The name of this space
     * @param cfg   The configuration ({@link NBConfiguration}) for this nb run
     */
    public PineconeSpace(String name, NBConfiguration cfg) {
        this.apiKey = cfg.get("apiKey");
        this.environment = cfg.get("environment");
        this.projectName = cfg.get("projectName");
        this.name = name;

        config = new PineconeClientConfig()
            .withApiKey(apiKey)
            .withEnvironment(environment)
            .withProjectName(projectName);

        this.client = new PineconeClient(config);
    }

    /**
     * Connections are index-specific so we need to allow for multiple connection management across indices.
     * However, note that a single connection object is thread safe and can be used by multiple clients.
     *
     * @param index     The database index for which a connection is being requested
     * @return          The {@link PineconeConnection} for this database index
     */
    public synchronized PineconeConnection getConnection(String index) {
        PineconeConnection connection = connections.get(index);
        if (connection == null) {
            PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig().withIndexName(index);
            connection = client.connect(connectionConfig);
            connections.put(index, connection);
        }
        return connection;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(PineconeSpace.class);
    }

}
