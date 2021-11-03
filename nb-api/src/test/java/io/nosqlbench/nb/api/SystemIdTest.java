package io.nosqlbench.nb.api;

import io.nosqlbench.nb.api.metadata.SystemId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemIdTest {

    @Test
    public void testHostInfo() {
        String info = SystemId.getHostSummary();
        System.out.println(info);
    }

    @Test
    public void testNostId() {
        String info = SystemId.getNodeId();
        assertThat(info).matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    @Test
    public void testNodeFingerprint() {
        String hash = SystemId.getNodeFingerprint();
        assertThat(hash).matches("[A-Z0-9]+");
    }

}
