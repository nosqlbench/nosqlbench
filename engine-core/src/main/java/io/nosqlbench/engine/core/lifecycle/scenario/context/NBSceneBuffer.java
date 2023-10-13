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

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.api.filtering.TristateFilter;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.api.scripting.DiagReader;
import io.nosqlbench.engine.api.scripting.DiagWriter;
import io.nosqlbench.engine.api.scripting.InterjectingCharArrayWriter;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

public class NBSceneBuffer implements NBSceneFixtures {
    private final NBSceneFixtures fixtures;

    public NBSceneBuffer params(Map<String,String> params) {
        return new NBSceneBuffer(
            new NBDefaultSceneFixtures(
                ScenarioParams.of(params),
                fixtures.controller(),
                fixtures.out(),
                fixtures.err(),
                fixtures.in())
            ,iotype
        );
    }

    public enum IOType {
        connected,
        virtual,
        traced
    }

    private final IOType iotype;
    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    public NBSceneBuffer(NBSceneFixtures fixtures, IOType ioTypes) {
        this.iotype = ioTypes;
        this.fixtures = fixtures;

        switch (iotype) {
            case traced:
                stdoutBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stdout "), fixtures.out());
                stderrBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stderr "), fixtures.err());
                stdinBuffer = new DiagReader(fixtures.in(), "  stdin ");
                break;
            case virtual:
                stdoutBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stdout "));
                stderrBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stderr "));
                stdinBuffer = new DiagReader(new StringReader(""), "  stdin ");
                break;
            case connected:
                stdoutBuffer = new DiagWriter(fixtures.out());
                stderrBuffer = new DiagWriter(fixtures.err());
                stdinBuffer = new DiagReader(fixtures.in());
                break;

        }
    }


    public NBSceneBuffer(NBSceneFixtures fixtures) {
        this(fixtures, IOType.traced);
    }

    @Override
    public ScenarioParams params() {
        return fixtures.params();
    }

    @Override
    public ScenarioActivitiesController controller() {
        return fixtures.controller();
    }

    @Override
    public PrintWriter out() {
        return stdoutBuffer;
    }

    @Override
    public PrintWriter err() {
        return stderrBuffer;
    }

    @Override
    public Reader in() {
        return stdinBuffer;
    }

    public String getIOLog() {
        return this.stdoutBuffer.getTimedLog() + this.stderrBuffer.getTimedLog();
    }

    public NBSceneFixtures asFixtures() {
        return (NBSceneFixtures) this;
    }


    public static SceneBuilderFacets.WantsController builder() {
        return new SceneBuilder();
    }

    public static NBSceneBuffer traced(NBComponent component) {
        return builder().tracedIO().build(component);
    }
}
