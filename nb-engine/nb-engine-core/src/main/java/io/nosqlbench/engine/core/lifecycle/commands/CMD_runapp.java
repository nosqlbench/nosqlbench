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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/// Runs a bundled app ({@link BundledApp}) as a step in the NoSQLBench command stream, so that
/// apps can be sequenced alongside `run`/`start`/etc. and used inside named scenarios.
///
/// The app is selected with the reserved `appname=<selector>` parameter. The remaining
/// `name=value` parameters are adapted into the app's argv by {@link RunappArgvAdapter}, using the
/// app's own picocli model when it exposes one. The app's integer exit code is honored: a non-zero
/// exit raises a {@link BasicError} so the command stream halts, consistent with how the session
/// treats any other failing command.
///
/// Example (CLI): `nb5 runapp appname=stress-report logs-dir=logs`
/// Example (scenario step): `report: runapp appname=stress-report sqlite=logs/metrics.db`
@Service(value = NBBaseCommand.class, selector = "runapp")
public class CMD_runapp extends NBBaseCommand {
    private final static Logger logger = LogManager.getLogger("runapp");

    /// The parameter naming the bundled app to invoke.
    public static final String APPNAME_PARAM = "appname";

    public CMD_runapp(NBBufferedContainer parentComponent, String stepName, String targetScenario) {
        super(parentComponent, stepName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Map<String, String> appParams = new LinkedHashMap<>(params);
        String appName = appParams.remove(APPNAME_PARAM);
        if (appName == null || appName.isBlank()) {
            throw new BasicError("runapp requires an '" + APPNAME_PARAM
                + "=<selector>' parameter naming the bundled app to run. Use --list-apps to see available apps.");
        }

        BundledApp app = ServiceSelector.of(appName, ServiceLoader.load(BundledApp.class)).get()
            .orElseThrow(() -> new BasicError("No bundled app named '" + appName
                + "'. Use --list-apps to see available apps."));

        String[] argv = RunappArgvAdapter.toArgv(appParams, app.getCommandModel());
        logger.info(() -> "runapp invoking '" + appName + "' with argv: " + String.join(" ", argv));

        int exitCode = app.applyAsInt(argv);
        if (exitCode != 0) {
            throw new BasicError("bundled app '" + appName + "' exited with non-zero code " + exitCode);
        }
        return exitCode;
    }
}
