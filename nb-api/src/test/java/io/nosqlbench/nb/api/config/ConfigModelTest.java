package io.nosqlbench.nb.api.config;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigModelTest {

    @Test
    public void testMultipleParams() {
        ConfigModel cm = ConfigModel.of(ConfigModelTest.class,
            Param.defaultTo(List.of("a","b"),"value").setRequired(false),
            Param.required("c",int.class));
        NBConfiguration cfg = cm.apply(Map.of("c", 232));
        assertThat(cfg.getOptional("a")).isEmpty();
        assertThat(cfg.get("c",int.class)).isEqualTo(232);

    }
}
