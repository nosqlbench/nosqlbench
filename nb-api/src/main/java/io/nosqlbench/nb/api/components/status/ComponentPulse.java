/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.nb.api.components.status;

import io.nosqlbench.nb.api.components.core.UnstartedPeriodicTaskComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ComponentPulse extends UnstartedPeriodicTaskComponent {
    private final static Logger logger = LogManager.getLogger(ComponentPulse.class);
    private final Path hbpath;
    private final NBLiveComponent pulseOf;

    public ComponentPulse(NBLiveComponent pulseOf, NBLabels extraLabels, String fileNameLabel, long millis) {
        super(
            pulseOf,
            extraLabels,
            millis,
            "PULSE-" + pulseOf.description(),
            FirstReport.Immediately,
            LastReport.OnInterrupt
        );
        this.pulseOf = pulseOf;
        String logsdir = getComponentProp("logsdir").orElseThrow();
        this.hbpath = Path.of(logsdir).resolve(pulseOf.getLabels().valueOf(fileNameLabel)+"_status.yaml");
        start();
    }

    @Override
    protected void task() {
        logger.debug("emitting pulse for :" + this.pulseOf.description());
        Heartbeat heartbeat = pulseOf.heartbeat().withHeartbeatDetails(intervalmillis,System.currentTimeMillis());
        try {
            Files.writeString(hbpath, heartbeat.toYaml(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to write heartbeat data to " + hbpath.toString() + ": " + e);
        }
    }
}
