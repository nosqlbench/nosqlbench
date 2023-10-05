/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.extensions.scriptingmetrics;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.config.LabeledScenarioContext;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.engine.metrics.DoubleSummaryGauge;
import io.nosqlbench.api.engine.metrics.wrappers.RelevancyMeasures;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ScriptingMetrics {
    private final Logger logger;
    private final MetricRegistry metricRegistry;
    private final LabeledScenarioContext scriptContext;

    public ScriptingMetrics(final Logger logger, final MetricRegistry metricRegistry, final LabeledScenarioContext scriptContext) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.scriptContext = scriptContext;
    }

    public ScriptingGauge newStaticGauge(final String name, final double initialValue) {
        final ScriptingGauge scriptingGauge = new ScriptingGauge(name, initialValue);
        ActivityMetrics.gauge(this.scriptContext,name, scriptingGauge);
        this.logger.info(() -> "registered scripting gauge:" + name);
        return scriptingGauge;
    }

    public DoubleSummaryGauge newSummaryGauge(final String name) {
        final DoubleSummaryGauge summaryGauge = ActivityMetrics.summaryGauge(scriptContext,name);
        this.logger.info(() -> "registered summmary gauge:" + name);
        return summaryGauge;
    }

    public DoubleSummaryGauge newSummaryGauge(NBLabeledElement context, final String name) {
        final DoubleSummaryGauge summaryGauge = ActivityMetrics.summaryGauge(context,name);
        this.logger.info(() -> "registered summmary gauge:" + name);
        return summaryGauge;
    }

//    public RelevancyMeasures newRelevancyMeasures(NBLabeledElement parent, Map<String,String> labels) {
//        return new RelevancyMeasures(parent,labels);
//    }
    public RelevancyMeasures newRelevancyMeasures(NBLabeledElement parent) {
        return new RelevancyMeasures(parent);
    }
//    public RelevancyMeasures newRelevancyMeasures(NBLabeledElement parent, Object... labels) {
//        return new RelevancyMeasures(parent,labels);
//    }



}
