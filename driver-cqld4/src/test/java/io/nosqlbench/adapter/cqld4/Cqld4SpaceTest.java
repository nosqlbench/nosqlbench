package io.nosqlbench.adapter.cqld4;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Cqld4SpaceTest {

    @Test
    public void testSplitConfigs() {
        List<String> strings = Cqld4Space.splitConfigLoaders("http://config.example.com/asdf,file:foo,{inline config},c:\\file,/tmp/test");
        assertThat(strings).containsExactly(
            "http://config.example.com/asdf",
            "file:foo",
            "{inline config}",
            "c:\\file",
            "/tmp/test"
        );
    }

}
