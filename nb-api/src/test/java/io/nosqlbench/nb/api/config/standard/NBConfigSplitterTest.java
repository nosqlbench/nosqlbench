package io.nosqlbench.nb.api.config.standard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBConfigSplitterTest {

    @Test
    public void testSplitConfigs() {
        List<String> strings = NBConfigSplitter.splitConfigLoaders("http://config.example.com/asdf,file:foo,{inline config},c://file,/tmp/test");
        assertThat(strings).containsExactly(
            "http://config.example.com/asdf",
            "file:foo",
            "{inline config}",
            "c://file",
            "/tmp/test"
        );
    }

    @Test
    public void testSplitConfigs2() {
        List<String> strings = NBConfigSplitter.splitConfigLoaders("http://config.example.com/asdf,{\"inline1\":\"config1\"},{\"inline2\":\"config2\"}");
        assertThat(strings).containsExactly(
            "http://config.example.com/asdf",
            "{\"inline1\":\"config1\"}",
            "{\"inline2\":\"config2\"}"
        );
    }

}
