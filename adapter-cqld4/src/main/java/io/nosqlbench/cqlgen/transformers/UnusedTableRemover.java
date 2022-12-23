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
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class UnusedTableRemover implements CGModelTransformer, CGTransformerConfigurable {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/unused-table-remover");
    private double minimumThreshold = 0.0001;
    private String name;

    @Override
    public CqlModel apply(CqlModel model) {
        if (!model.hasStats()) {
            logger.warn("Unused table remover is not active since there are no stats provided.");
            return model;
        }

        List<CqlTable> tableDefs = model.getTableDefs();

        for (CqlTable table : tableDefs) {
            String weightedOpsSpec = table.getTableAttributes().getAttribute("weighted_ops");
            double weightedOps = Double.parseDouble(weightedOpsSpec);
            if (weightedOps < minimumThreshold) {
                logger.info(() -> String.format(
                    "removing table " + table.getKeyspace().getName() + "." + table.getName() + " with minimum weighted_ops of %1.5f under %1.5f",
                    weightedOps, minimumThreshold)
                );
                table.getKeyspace().removeTable(table);
            }
        }
        return model;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(Object cfgObj) {
        if (cfgObj instanceof Map stringMap) {
            Object fractionalThresholdSpec = stringMap.get("percentage_threshold");
            if (fractionalThresholdSpec != null) {
                this.minimumThreshold = Double.parseDouble(fractionalThresholdSpec.toString());
            }
        } else {
            throw new RuntimeException("unused table remover requires a Map for its config value.");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }
}
