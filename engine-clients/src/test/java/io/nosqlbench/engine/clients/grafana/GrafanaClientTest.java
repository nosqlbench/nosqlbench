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

package io.nosqlbench.engine.clients.grafana;

import io.nosqlbench.engine.clients.grafana.transfer.GAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GrafanaClientTest {
    private final static Logger logger = LogManager.getLogger(GrafanaClientTest.class);

    private static final String testurl = "http://localhost:3000/";

    @Test
    @Disabled
    public void testCreateAnnotation() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        GAnnotation a = new GAnnotation();
        a.setDashboardId(2);
        a.setText("testingAnnotation");
        GAnnotation created = client.createAnnotation(a);
        logger.info(created);
    }

    @Test
    @Disabled
    public void testFindAnnotations() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        List<GAnnotation> annotations = client.findAnnotations(By.id(1));
        logger.info(annotations);
    }

    @Test
    @Disabled
    public void testGetApiToken() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        ApiToken token = client.createApiToken("nosqlbench", "Admin", Long.MAX_VALUE);
        logger.info(token);
    }
}
