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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import io.nosqlbench.cqlgen.core.CGSchemaStats;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public class CGGenStatsInjector implements CGModelTransformer, CGTransformerConfigurable {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/stats-injector");

    private CGSchemaStats schemaStats = null;
    private String name;

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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(Object configObject) {
        if (configObject instanceof Map config) {

            String histogramPath = config.get("path").toString();
            if (histogramPath != null) {
                if (!Files.exists(Path.of(histogramPath))) {
                    logger.info(() -> "No tablestats file was found. at '" + histogramPath + "'.");
                    Object onmissing = config.get("onmissing");
                    if (onmissing==null || !String.valueOf(onmissing).toLowerCase(Locale.ROOT).equals("skip")) {
                        logger.error("Unable to load tablestats file from '" + histogramPath + "' because it doesn't exists, and onmissing!=skip.");
                        throw new RuntimeException("Unable to continue. onmissing=" + onmissing.toString());
                    } else {
                        return;
                    }
                }
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

    @Override
    public String getName() {
        return this.name;
    }
}
