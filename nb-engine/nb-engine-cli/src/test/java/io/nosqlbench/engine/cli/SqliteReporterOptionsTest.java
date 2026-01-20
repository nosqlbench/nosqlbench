package io.nosqlbench.engine.cli;

/*
 * Copyright (c) nosqlbench
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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Tag("unit")
public class SqliteReporterOptionsTest {

    @Test
    public void shouldParseHistAndNohistOptionsOnly() {
        NBCLIOptions.SqliteConfigData cfg = new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo");
        assertThat(cfg.includeHistograms).isFalse();

        cfg = new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo,,,hist");
        assertThat(cfg.includeHistograms).isTrue();

        cfg = new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo,,,nohist");
        assertThat(cfg.includeHistograms).isFalse();
    }

    @Test
    public void shouldRejectHistogramOptionAliases() {
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo,,,histograms"));
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo,,,hist=true"));
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> new NBCLIOptions.SqliteConfigData("jdbc:sqlite:foo,,,no-hist"));
    }
}

