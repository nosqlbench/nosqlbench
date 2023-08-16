/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.loader.hdf.writers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.data.CqlVector;
import io.nosqlbench.loader.hdf.config.LoaderConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.Map;

public class AstraVectorWriter extends AbstractVectorWriter {
    private static final Logger logger = LogManager.getLogger(AstraVectorWriter.class);
    private final CqlSession session;
    PreparedStatement insert_vector;

    public AstraVectorWriter(LoaderConfig config) {
        Map<String,String> astraParams = config.getAstra();
        session = CqlSession.builder()
            .withCloudSecureConnectBundle(Paths.get(astraParams.get("scb")))
            .withAuthCredentials(astraParams.get("clientId"), astraParams.get("clientSecret"))
            .withKeyspace(astraParams.get("keyspace"))
            .build();
        logger.info("Astra session initialized");
        insert_vector = session.prepare(astraParams.get("query"));
    }
//TODO: this is insanely slow. Needs work on threading/batching
    @Override
    protected void writeVector(float[] vector) {
        Float[] vector2 = new Float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = vector[i];
        }
        CqlVector.Builder vectorBuilder = CqlVector.builder();
        vectorBuilder.add(vector2);
        session.execute(insert_vector.bind(getPartitionValue(vector), vectorBuilder.build()));
    }

    private String getPartitionValue(float[] vector) {
        float sum = 0;
        for (float f : vector) {
            sum += f;
        }
        return String.valueOf(sum);
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }
}
