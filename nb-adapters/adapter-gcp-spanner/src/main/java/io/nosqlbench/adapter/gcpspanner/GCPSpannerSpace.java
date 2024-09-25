/*
 * Copyright (c) 2020-2024 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.gcpspanner;

import com.google.cloud.spanner.*;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @see <a href="https://cloud.google.com/spanner/docs/find-approximate-nearest-neighbors">ANN Docs</a>
 * @see <a href="https://github.com/googleapis/java-spanner">Spanner Java Client</a>
 * @see <a href="https://cloud.google.com/spanner/docs/getting-started/java">Getting started in Java</a>
 * @see <a href="https://cloud.google.com/docs/authentication#getting_credentials_for_server-centric_flow">Authentication methods at Google</a>
 * @see <a href="https://cloud.google.com/java/docs/reference/google-cloud-spanner/latest/overview">Library Reference Doc</a>
 * @see <a href="https://cloud.google.com/spanner/docs/reference/standard-sql/dml-syntax">DML Syntax</a>
 * @see <a href="https://cloud.google.com/spanner/docs/reference/rpc">spanner rpc api calls</a>
 * @see <a href="https://cloud.google.com/spanner/docs/reference/standard-sql/data-definition-language#vector_index_statements">SQL functionality related to vector indices</a>
 * @see <a href=""></a>
 * @see <a href=""></a>
 * @see <a href=""></a>
 */
public class GCPSpannerSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(GCPSpannerSpace.class);
    private final String name;
    private final NBConfiguration cfg;
    protected Spanner spanner;
    protected DatabaseAdminClient dbAdminClient;
    protected DatabaseClient dbClient;

    /**
     * Create a new {@code GCPSpannerSpace} Object which stores all stateful
     * contextual information needed to interact with the <b>Google Spanner</b>
     * database instance.
     *
     * @param name The name of this space
     * @param cfg  The configuration ({@link NBConfiguration}) for this nb run
     */
    public GCPSpannerSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
    }

    public synchronized Spanner getSpanner() {
        if (spanner == null) {
            createSpanner();
        }
        return spanner;
    }

    public synchronized DatabaseAdminClient getDbAdminClient() {
        return dbAdminClient;
    }

    public synchronized DatabaseClient getDbClient() {
        return dbClient;
    }

    public DatabaseId getDatabaseId() {
        return DatabaseId.of(cfg.get("project_id"), cfg.get("instance_id"), cfg.get("database_id"));
    }

    public String getInstanceId() {
        return cfg.get("instance_id");
    }

    public String getDatabaseIdString() {
        return cfg.get("database_id");
    }

    private void createSpanner() {
        if (
            cfg.getOptional("database_id").isEmpty() ||
            cfg.getOptional("project_id").isEmpty() ||
            cfg.getOptional("instance_id").isEmpty()) {
            throw new RuntimeException("You must provide all 'service_account_file', 'project_id', 'instance_id' & 'database_id' to configure a Google Spanner client");
        }
        String projectId = cfg.get("project_id");
        String instanceId = cfg.get("instance_id");
        String databaseId = cfg.get("database_id");
        spanner = SpannerOptions.newBuilder().setProjectId(projectId).build().getService();
        dbAdminClient = spanner.getDatabaseAdminClient();
        dbClient = spanner.getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(GCPSpannerSpace.class)
            .add(Param.optional("service_account_file", String.class, "the file to load the api token/key from. See https://cloud.google.com/docs/authentication/provide-credentials-adc#service-account"))
            .add(Param.optional("project_id", String.class,"Project ID containing the Spanner database. See https://cloud.google.com/resource-manager/docs/creating-managing-projects"))
            .add(Param.optional("instance_id", String.class, "Spanner database's Instance ID containing. See https://cloud.google.com/spanner/docs/getting-started/java#create_an_instance"))
            .add(Param.optional("database_id", String.class, "Spanner Database ID. See https://cloud.google.com/spanner/docs/getting-started/java#create_a_database"))
            .asReadOnly();
    }
    @Override
    public void close() {
        if (spanner != null) {
            spanner.close();
        }
        spanner = null;
    }
}
