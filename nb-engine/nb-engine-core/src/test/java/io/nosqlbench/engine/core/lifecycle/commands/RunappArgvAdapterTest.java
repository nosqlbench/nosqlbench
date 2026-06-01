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

import io.nosqlbench.nb.api.errors.BasicError;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Tag("unit")
public class RunappArgvAdapterTest {

    @Command(name = "fixture")
    static class Fixture {
        @Option(names = "--sqlite") String sqlite;
        @Option(names = "--logs-dir") String logsDir;
        @Option(names = "--ab") boolean ab;
    }

    @Command(name = "instant")
    static class InstantSub {
        @Option(names = {"--metric", "-m"}) String metric;
    }

    private static Optional<CommandLine> model() {
        return Optional.of(new CommandLine(new Fixture()).addSubcommand("instant", new InstantSub()));
    }

    private static Map<String, String> params(String... kv) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    @Test
    public void mapsNamedParamsToOptionsInOrder() {
        String[] argv = RunappArgvAdapter.toArgv(params("sqlite", "db.sqlite", "logs-dir", "logs"), model());
        assertThat(argv).containsExactly("--sqlite=db.sqlite", "--logs-dir=logs");
    }

    @Test
    public void booleanOptionEmittedAsAssignedValue() {
        String[] argv = RunappArgvAdapter.toArgv(params("ab", "true"), model());
        assertThat(argv).containsExactly("--ab=true");
    }

    @Test
    public void unknownParameterIsRejected() {
        assertThatExceptionOfType(BasicError.class)
            .isThrownBy(() -> RunappArgvAdapter.toArgv(params("bogus", "1"), model()))
            .withMessageContaining("unknown parameter 'bogus'");
    }

    @Test
    public void routesSubcommandViaCommandParam() {
        String[] argv = RunappArgvAdapter.toArgv(params("command", "instant", "metric", "ops_total"), model());
        assertThat(argv).containsExactly("instant", "--metric=ops_total");
    }

    @Test
    public void unknownSubcommandIsRejected() {
        assertThatExceptionOfType(BasicError.class)
            .isThrownBy(() -> RunappArgvAdapter.toArgv(params("command", "nope"), model()))
            .withMessageContaining("unknown subcommand 'nope'");
    }

    @Test
    public void emptyModelFallsBackToLiteralMapping() {
        String[] argv = RunappArgvAdapter.toArgv(params("foo", "bar", "baz", "qux"), Optional.empty());
        assertThat(argv).containsExactly("--foo=bar", "--baz=qux");
    }
}
