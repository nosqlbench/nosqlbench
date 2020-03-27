package io.nosqlbench.engine.api.util;

import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBPathWalkerTest {

    @Test
    public void testBasicPathmatching() {
        List<Path> found = NBPathWalker.findEndMatching(
            Path.of("testdocs"),
            Path.of("identity.yaml")
        );

        assertThat(found).containsExactly(Path.of("flsd"));
    }
}
