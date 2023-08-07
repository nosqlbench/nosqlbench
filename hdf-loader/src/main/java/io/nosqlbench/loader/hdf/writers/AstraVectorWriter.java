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
 *
 */

package io.nosqlbench.loader.hdf.writers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import io.nosqlbench.loader.hdf.config.LoaderConfig;

import java.nio.file.Paths;
import java.util.Map;

public class AstraVectorWriter extends AbstractVectorWriter {
    private CqlSession session;
    PreparedStatement insert_vector;

    public AstraVectorWriter(LoaderConfig config) {
        Map<String,String> astraParams = config.getAstra();
        session = CqlSession.builder()
            .withCloudSecureConnectBundle(Paths.get(astraParams.get("scb")))
            .withAuthCredentials(astraParams.get("clientId"), astraParams.get("clientSecret"))
            .withKeyspace(astraParams.get("keyspace"))
            .build();
        insert_vector = session.prepare(astraParams.get("query"));
    }

    @Override
    protected void writeVector(float[] vector) {
        session.execute(insert_vector.bind(getPartitionValue(vector), vector));
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
