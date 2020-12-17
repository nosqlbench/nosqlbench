package io.nosqlbench.nb.api.config;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigLoaderTest {

    @Test
    public void testSingleParams() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("a=b c=234", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));
    }

    @Test
    public void testSingleJsonObject() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("{a:'b', c:'234'}", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));
    }

    @Test
    public void testJsonArray() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("[{a:'b', c:'234'}]", Map.class);
        assertThat(cfg1).contains(Map.of("a", "b", "c", "234"));

    }

    @Test
    public void testImportSingle() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> imported = cl.load("IMPORT{importable-config.json}", Map.class);
        assertThat(imported).contains(Map.of("a", "B", "b", "C", "c", 123.0, "d", 45.6));
    }

    @Test
    public void testEmpty() {
        ConfigLoader cl = new ConfigLoader();
        List<Map> cfg1 = cl.load("", Map.class);
        assertThat(cfg1).isNull();

    }
}