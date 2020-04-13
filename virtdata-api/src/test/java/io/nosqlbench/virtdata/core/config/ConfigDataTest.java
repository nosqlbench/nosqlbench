package io.nosqlbench.virtdata.core.config;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigDataTest {

    @Test
    public void testLayer() {
        ConfigData conf = new ConfigData();
        conf.put("test1", List.of("t","e","s","t","1"));
        Optional<List> test1 = conf.get("test1", List.class);
        assertThat(test1).isPresent();
        assertThat(test1.get()).containsExactly("t","e","s","t","1");
        ConfigData layer2 = conf.layer(Map.of("test1",List.of("another")));
        Optional<List> test2 = layer2.get("test1", List.class);
        assertThat(test2).isPresent();
        assertThat(test2.get()).containsExactly("another");
    }

    @Test
    public void testList() {
        ConfigData conf = new ConfigData();
        conf.put("test1", List.of("t","e","s","t","1"));
        Optional<List<String>> test1 = conf.getList("test1", String.class);
        assertThat(test1).isPresent();
        assertThat(test1.get()).containsExactly("t","e","s","t","1");
    }

}
