package io.nosqlbench.engine.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

