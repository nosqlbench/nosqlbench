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
 */

package io.nosqlbench.engine.clients.grafana;

import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.api.system.NBEnvironment;
import picocli.CommandLine;

//@Service(value = BundledApp.class,selector = "grafana-apikey")
//@CommandLine.Command(
//    name="gafana-apikey",
//    description = "create and cache a grafana apikey for a given grafana server"
//)
public class GrafanaTokenAuthenticator implements BundledApp {

    @CommandLine.Parameters
    private final String keyfile = NBEnvironment.INSTANCE.get("apikeyfile");
    @Override
    public int applyAsInt(String[] value) {
        return 0;
    }

    public GrafanaTokenAuthenticator() {

    }
}
