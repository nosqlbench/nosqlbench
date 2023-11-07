package io.nosqlbench.engine.core.lifecycle.scenario.context;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.components.NBComponent;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public interface ContextBuilderFacets {
    public interface ALL extends
        WantsName,
        WantsController,
        WantsStderr,
        WantsStdout,
        WantsStdin,
        WantsParams,
        CanBuild,
        WantsIoType {
    }

    public interface WantsName {
        public WantsController name(String name);
    }

    public interface WantsController extends WantsStdin, WantsIoType {
        public WantsStdin controller(ScenarioActivitiesController controller);
    }


    public interface WantsStdin extends WantsIoType {
        public WantsStdout in(Reader in);
    }

    public interface WantsStdout extends CanBuild {
        public WantsStderr out(PrintWriter out);
    }

    public interface WantsStderr extends CanBuild {
        public WantsParams err(PrintWriter err);
    }

    public interface WantsIoType extends CanBuild {
        /**
         * If you want the stdin, stdout, stderr streams to be contained only within the scenario's
         * execution environment, not connected to the outside world, do this.
         */
        public WantsParams virtualIO();

        /**
         * If you want to connect stdin, stdout, stderr streams to the system in, out and error streams,
         * do this.
         */
        public WantsParams connectedIO();

        /**
         * If you want to connect the console IO streams to the outside world, but also capture them for
         * diagnostics or other purposes, do this.
         */
        public WantsParams tracedIO();
    }

    public interface WantsParams extends CanBuild {
        public CanBuild params(Map<String, String> params);
    }

    public interface CanBuild {
        NBBufferedScenarioContext build(NBComponent forComponent);
    }

}

