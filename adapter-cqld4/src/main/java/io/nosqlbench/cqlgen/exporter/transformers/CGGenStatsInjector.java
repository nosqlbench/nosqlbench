/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.cqlgen.exporter.transformers;

import io.nosqlbench.cqlgen.exporter.CGSchemaStats;
import io.nosqlbench.cqlgen.model.CqlModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class CGGenStatsInjector implements CGModelTransformer, CGTransformerConfigurable {
    private CGSchemaStats schemaStats = null;

    public CGGenStatsInjector() {
    }

    @Override
    public CqlModel apply(CqlModel model) {
        if (schemaStats != null) {
            model.setKeyspaceAttributes(schemaStats);
        }
        return model;
    }

    @Override
    public void accept(Object configObject) {
        if (configObject instanceof Map config) {

            String histogramPath = config.get("path").toString();
            if (histogramPath != null) {
                CGSchemaStats schemaStats = null;
                Path statspath = Path.of(histogramPath);
                try {
                    CqlSchemaStatsParser parser = new CqlSchemaStatsParser();
                    schemaStats = parser.parse(statspath);
                    this.schemaStats = schemaStats;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            } else schemaStats = null;
        } else {
            throw new RuntimeException("stats injector requires a map for it's config value");
        }
    }
}
