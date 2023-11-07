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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public class NBScenarioContextBuilder implements ContextBuilderFacets.ALL {
    private Map<String,String> params = Map.of();
    private ScenarioActivitiesController controller;
    private PrintWriter out = new PrintWriter(System.out);
    private PrintWriter err = new PrintWriter(System.err);
    private Reader in = new InputStreamReader(System.in);
    private NBBufferedScenarioContext.IOType iotype = NBBufferedScenarioContext.IOType.traced;
    private NBComponent parentComponent;
    private String contextName;


    public NBScenarioContextBuilder() {}
    public NBBufferedScenarioContext build(NBComponent forScenario) {
        NBDefaultScenarioContext fixtures =
            new NBDefaultScenarioContext(
                parentComponent,
                contextName,
                ((this.controller!=null) ? this.controller : new ScenarioActivitiesController(forScenario)),
                this.out,
                this.err,
                this.in);
        return new NBBufferedScenarioContext(parentComponent,contextName,fixtures,iotype);
    }


    @Override
    public NBScenarioContextBuilder controller(ScenarioActivitiesController controller) {
        this.controller = controller;
        return this;
    }

    @Override
    public NBScenarioContextBuilder out(PrintWriter out) {
        this.out = out;
        return this;
    }

    @Override
    public ContextBuilderFacets.WantsParams err(PrintWriter err) {
        this.err = err;
        return this;
    }

    @Override
    public NBScenarioContextBuilder in(Reader in) {
        this.in = in;
        return this;

    }

    @Override
    public NBScenarioContextBuilder params(Map<String, String> params) {
        this.params=params;
        return this;
    }

    @Override
    public ContextBuilderFacets.WantsParams virtualIO() {
        this.iotype= NBBufferedScenarioContext.IOType.virtual;
        return this;
    }

    @Override
    public ContextBuilderFacets.WantsParams connectedIO() {
        this.iotype = NBBufferedScenarioContext.IOType.connected;
        return this;
    }

    @Override
    public ContextBuilderFacets.WantsParams tracedIO() {
        this.iotype= NBBufferedScenarioContext.IOType.traced;
        return this;
    }

    @Override
    public ContextBuilderFacets.WantsController name(String contextName) {
        this.contextName = contextName;
        return this;
    }
}
