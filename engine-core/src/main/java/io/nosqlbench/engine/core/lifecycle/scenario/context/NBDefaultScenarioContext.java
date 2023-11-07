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

package io.nosqlbench.engine.core.lifecycle.scenario.context;

import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponent;

import java.io.*;

/**
 * <P>An NBSceneFixtures instance represents the runtime fixtures needed to run a specific
 * scenario. It is instanced per execution and is not expected to be thread-safe nor
 * run more than once. This provides all of the required runtime support and IO boundaries
 * needed by a scenario.</P>
 *
 * <P>The properties on this context define the API used by any scenario logic,
 * whether implemented in script or directly. This should allow different
 * execution forms to read similarly, easing development and debugging of more advanced
 * scenarios.</P>
 *
 * <P>When using the fixtures within a context, they should be named <em>scene</em>
 * which suggests an episodic sequence of events.</P>
 *
 * <P>Within an execution context, scenario logic is expected to adhere to usage of
 * <i>scene.in</i>, <i>scene.out</i>, and <i>scene.error</i> instead of System.out, ...</P>
 */
public class NBDefaultScenarioContext extends NBBaseComponent implements NBScenarioContext  {
    /*
      These are parameters which are passed into the script, named scenario, etc.
     */
    private ScenarioPhaseParams params;
    /*
     * NBSession is the root component type in a NB process, and all CLI options which
     * affect global settings are expected to be properties on the session.
     */
    private NBComponent session;

    /*
     * ScenarioActivitiesController is the concurrency handling layer for activities within
     * a given scenario. A scenario doesn't complete unless until all activities
     * are complete or errored.
     */
    private ScenarioActivitiesController controller;
    /*
     * Extensions provide additional scripting capabilities which are not provided by the
     * scripting or other runtimes, or new ways of tapping into extant features.
     */

    private PrintWriter out;
    private PrintWriter err;

    private Reader in;

    public NBDefaultScenarioContext(NBComponent parent, String name, ScenarioActivitiesController controller, PrintWriter out, PrintWriter err, Reader in) {
        super(parent, NBLabels.forKV("context",name));
        this.controller = controller;
        this.out = out;
        this.err = err;
        this.in = in;
    }

//    public static NBSceneFixtures ofDefault(String name) {
//        return new NBDefaultSceneFixtures(
//            new ScenarioParams(),
//            new NBSession(
//                new TestComponent("scene", name), "scene~"+name
//            ),
//            new ScenarioActivitiesController(),
//            new PrintWriter(System.out),
//            new PrintWriter(System.err),
//            new InputStreamReader(System.in)
//        );
//    }
//

    @Override
    public ScenarioPhaseParams params() {
        return params;
    }

    @Override
    public ScenarioActivitiesController controller() {
        return controller;
    }

    @Override
    public PrintWriter out() {
        return out;
    }

    @Override
    public PrintWriter err() {
        return err;
    }

    @Override
    public Reader in() {
        return in;
    }
}
