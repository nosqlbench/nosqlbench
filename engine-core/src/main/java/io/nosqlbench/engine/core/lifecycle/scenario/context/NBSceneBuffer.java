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
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.api.scripting.DiagReader;
import io.nosqlbench.engine.api.scripting.DiagWriter;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.Extensions;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NBSceneBuffer implements NBSceneFixtures {
    private final NBSceneFixtures fixtures;
    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    public NBSceneBuffer(NBSceneFixtures fixtures) {
        this.fixtures = fixtures;
        stdoutBuffer = new DiagWriter(fixtures.out(), " stdout ");
        stderrBuffer = new DiagWriter(fixtures.err(), " stderr ");
        stdinBuffer = new DiagReader(fixtures.in(), "  stdin ");
    }

    @Override
    public ScriptParams params() {
        return fixtures.params();
    }

    @Override
    public NBComponent component() {
        return fixtures.component();
    }

    @Override
    public ActivitiesController controller() {
        return fixtures.controller();
    }

    @Override
    public Extensions extensions() {
        return fixtures.extensions();
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

    public List<String> getTimedLogLines() {
        List<String> log = new ArrayList<String>();
        Optional.ofNullable(this.stdinBuffer).map(DiagReader::getTimedLog).ifPresent(log::addAll);
        Optional.ofNullable(this.stderrBuffer).map(DiagWriter::getTimedLog).ifPresent(log::addAll);
        Optional.ofNullable(this.stdoutBuffer).map(DiagWriter::getTimedLog).ifPresent(log::addAll);
        log = log.stream().map(l -> l.endsWith("\n") ? l : l+"\n").collect(Collectors.toList());
        return log;
    }

    public String getIOLog() {
        return String.join("",getTimedLogLines());
    }

    public NBSceneFixtures asFixtures() {
        return (NBSceneFixtures) this;
    }

    public static NBSceneBuffer init(String name) {
        TestComponent root = new TestComponent("scene", "self");
        return new NBSceneBuffer(NBDefaultSceneFixtures.ofDefault(name));
    }
}
