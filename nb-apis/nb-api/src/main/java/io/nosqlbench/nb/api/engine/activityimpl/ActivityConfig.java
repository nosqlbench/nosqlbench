package io.nosqlbench.nb.api.engine.activityimpl;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.labels.NBLabelSpec;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Optional;

public class ActivityConfig extends NBConfiguration {

    public static final String DEFAULT_ALIAS = "UNNAMEDACTIVITY";
    public static final String DEFAULT_ATYPE = "stdout";
    public static final String DEFAULT_CYCLES = "0";
    public static final String DEFAULT_RECYCLES = "1";
    public static final int DEFAULT_THREADS = 1;
    public static final Logger logger = LogManager.getLogger(ActivityConfig.class);

    // an alias with which to control the activity while it is running
    public static final String FIELD_ALIAS = "alias";
    // a file or URL containing the activity: op templates, generator bindings, ...
    public static final String FIELD_ATYPE = "type";
    // cycles for this activity in either "M" or "N..M" form. "M" form implies "0..M"
    public static final String FIELD_CYCLES = "cycles";
    public static final String FIELD_RECYCLES = "recycles";
    // initial thread concurrency for this activity
    public static final String FIELD_THREADS = "threads";
    public static final String FIELD_LABELS = "labels";

    public static final String[] field_list = {
        FIELD_ALIAS, FIELD_ATYPE, FIELD_CYCLES, FIELD_THREADS, FIELD_RECYCLES
    };

    public ActivityConfig(NBConfiguration config) {
        this(config.getModel(), config.getMap());
    }

    public ActivityConfig(NBConfigModel model, LinkedHashMap<String, Object> validConfig)
    {
        super(model, validConfig);
        Optional<String> directAlias = getOptional("alias");
        if (!directAlias.isPresent()) {
            String indirectAlias = getOptional(ActivityConfig.FIELD_ALIAS).or(
                    () -> getOptional("workload")).or(() -> getOptional("driver"))
                .orElse("ACTIVITYNAME");
            getMap().put("alias", indirectAlias);
        }

    }

    public String getAlias() {
        return get("alias");
    }

    public NBLabels auxLabels() {
        Optional<String> auxLabelSpec = getOptional(FIELD_LABELS);
        if (auxLabelSpec.isPresent()) {
            return NBLabelSpec.parseLabels(auxLabelSpec.get());
        }
        return NBLabels.forKV();

    }

    public Optional<String> getDriver() {
        return getOptional("driver", "type");
        //        .orElseThrow(() -> new BasicError("The parameter " +
        //            "'driver=' is required."));

    }

    public void setThreads(int i) {
        update("threads", i);
    }

    public int getThreads() {
        return get(FIELD_THREADS, Integer.class);
    }

    public String summary() {
        return String.valueOf(this);
    }

    public void updateLastCycle(long maxValue) {
        CyclesSpec spec = CyclesSpec.parse(get("cycles", String.class));
        spec = spec.withLast(maxValue);
        update("cycles", spec.toString());
    }

    public CyclesSpec getCyclesSpec() {
        return CyclesSpec.parse(get("cycles", String.class));
    }
}
