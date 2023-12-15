package io.nosqlbench.engine.core.lifecycle.scenario.container;

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


import io.nosqlbench.nb.api.components.core.NBComponent;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NBScenarioContainerBuilder implements ContainerBuilderFacets.ALL {
    private Map<String,String> params = Map.of();
    private ContainerActivitiesController controller;
    private PrintWriter out = new PrintWriter(System.out);
    private PrintWriter err = new PrintWriter(System.err);
    private Reader in = new InputStreamReader(System.in);
    private NBBufferedContainer.IOType iotype = NBBufferedContainer.IOType.traced;
    private String containerName;


    public NBScenarioContainerBuilder() {}
    public NBBufferedContainer build(NBComponent contextParentComponent) {
        return new NBBufferedContainer(contextParentComponent, containerName,iotype);
    }


    @Override
    public NBScenarioContainerBuilder controller(ContainerActivitiesController controller) {
        this.controller = controller;
        return this;
    }

    @Override
    public NBScenarioContainerBuilder out(PrintWriter out) {
        this.out = out;
        return this;
    }

    @Override
    public ContainerBuilderFacets.WantsParams err(PrintWriter err) {
        this.err = err;
        return this;
    }

    @Override
    public NBScenarioContainerBuilder in(Reader in) {
        this.in = in;
        return this;

    }

    @Override
    public NBScenarioContainerBuilder params(Map<String, String> params) {
        this.params=params;
        return this;
    }

    @Override
    public ContainerBuilderFacets.WantsParams virtualIO() {
        this.iotype= NBBufferedContainer.IOType.virtual;
        return this;
    }

    @Override
    public ContainerBuilderFacets.WantsParams connectedIO() {
        this.iotype = NBBufferedContainer.IOType.connected;
        return this;
    }

    @Override
    public ContainerBuilderFacets.WantsParams tracedIO() {
        this.iotype= NBBufferedContainer.IOType.traced;
        return this;
    }

    @Override
    public ContainerBuilderFacets.WantsController name(String contextName) {
        this.containerName = contextName;
        return this;
    }
}
