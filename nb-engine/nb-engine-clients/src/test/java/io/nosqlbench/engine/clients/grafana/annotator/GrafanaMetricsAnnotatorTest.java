/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.clients.grafana.annotator;

import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Layer;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.system.NBStatePath;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Map;

@Tag("unit")
public class GrafanaMetricsAnnotatorTest implements NBLabeledElement {


    @Test
    @Disabled
    public void testPost() {
        String ipaddr="CHANGEME";
        NBStatePath.initialize();
        GrafanaMetricsAnnotator ganno = new GrafanaMetricsAnnotator();
        ganno.applyConfig(ganno.getConfigModel().apply(Map.of(
            "baseurl","http://"+ipaddr+":3000/",
            "tags","appname:nosqlbench",
            "timeoutms","5000",
            "onerror","warn"
        )));
        ganno.recordAnnotation(Annotation.newBuilder()
            .element(this)
            .now()
            .layer(Layer.Session)
            .build());
    }

    @Override
    public NBLabels getLabels() {
        return NBLabels.forMap(Map.of("testlabelname","testlabelvalue"));
    }
}
