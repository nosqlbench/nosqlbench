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
import io.nosqlbench.engine.api.scripting.DiagReader;
import io.nosqlbench.engine.api.scripting.DiagWriter;
import io.nosqlbench.engine.api.scripting.InterjectingCharArrayWriter;
import io.nosqlbench.engine.core.lifecycle.session.NBSession;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

public class NBBufferedScenarioContext extends NBBaseComponent implements NBScenarioContext {
    private final NBScenarioContext fixtures;

    public enum IOType {
        connected,
        virtual,
        traced
    }

    private final IOType iotype;
    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    public NBBufferedScenarioContext(NBComponent parent, String name, NBScenarioContext fixtures, IOType ioTypes) {
        super(parent, NBLabels.forKV("context",name));
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


    public NBBufferedScenarioContext(NBSession parent, String name, NBScenarioContext fixtures) {
        this(parent, name, fixtures, IOType.traced);
    }

//    @Override
//    public ScenarioPhaseParams params() {
//        return fixtures.params();
//    }

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

    public NBScenarioContext asFixtures() {
        return (NBScenarioContext) this;
    }


    public static ContextBuilderFacets.WantsName builder() {
        return new NBScenarioContextBuilder();
    }

    public static NBBufferedScenarioContext traced(NBComponent component) {
        return builder().name(String.valueOf(System.currentTimeMillis())).tracedIO().build(component);
    }
}
