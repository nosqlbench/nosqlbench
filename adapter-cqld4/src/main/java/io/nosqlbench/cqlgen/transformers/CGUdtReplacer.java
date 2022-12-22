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
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlColumnBase;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;
import io.nosqlbench.cqlgen.model.CqlType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CGUdtReplacer implements CGModelTransformer {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/udt-replacer");
    private String name;

    @Override
    public CqlModel apply(CqlModel model) {
        List<String> toReplace = new ArrayList<>();

        model.getTypeDefs().stream()
            .map(t -> t.getKeyspace().getName() + "." + t.getName())
            .forEach(toReplace::add);

        model.getTypeDefs().stream()
            .map(CqlType::getName)
            .forEach(toReplace::add);

        for (CqlTable table : model.getTableDefs()) {
            for (CqlColumnBase coldef : table.getColumnDefs()) {
                String typedef = coldef.getTrimmedTypedef();
                for (String searchFor : toReplace) {
                    String[] words = typedef.split("\\W+");
                    for (String word : words) {
                        if (word.toLowerCase(Locale.ROOT).equals(searchFor.toLowerCase(Locale.ROOT))) {
                            logger.info(() -> "replacing '" + typedef + "' with blob");
                            coldef.setTypeDef("blob");
                            break;
                        }
                    }
                }
            }
        }

        return model;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return this.name;
    }
}
