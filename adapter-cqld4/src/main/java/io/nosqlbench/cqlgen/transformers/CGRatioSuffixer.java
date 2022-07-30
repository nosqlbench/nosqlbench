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

import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.core.CGSchemaStats;
import io.nosqlbench.cqlgen.model.CqlModel;
import io.nosqlbench.cqlgen.model.CqlTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CGRatioSuffixer implements CGModelTransformer, NBConfigurable {

    private double resolution;
    private String format;
    private String name;

    @Override
    public CqlModel apply(CqlModel model) {
        CGSchemaStats schemastats = model.getStats();
        if (schemastats == null) {
            return model;
        }

        for (CqlTable tableDef : model.getTableDefs()) {
            double opshare = tableDef.getComputedStats().getOpShareOfTotalOps();
            double multiplier = Math.pow(10.0, resolution+1);
            long value = (long) (opshare*multiplier);
            String newname = String.format(this.format, tableDef.getName(), value);
            tableDef.setName(newname);
        }

        return model;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.format = cfg.get("format", String.class);
        if (!format.contains("%1$s")) {
            throw new RuntimeException("format config param for the CGRatioSuffixer must contain '%1$s', but it is '" + format + "'");
        }
        Pattern pattern = Pattern.compile(".*?%2\\$(?<resolution>\\d+)d.*");
        Matcher matcher = pattern.matcher(format);
        if (!matcher.matches()) {
            throw new RuntimeException("Could not find the required decimal format specifier for the format config parameter of " + CGRatioSuffixer.class);
        }
        this.resolution = Double.parseDouble(matcher.group("resolution"))-1;
        this.resolution=Math.max(resolution,2);

    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(CGRatioSuffixer.class)
            .add(Param.defaultTo("format","%1$s_%2$5d").setDescription(
                "The format specifier as in Java String.format, with a required string format for the first arg, and a required decimal format for the second."
            ))
            .asReadOnly();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
