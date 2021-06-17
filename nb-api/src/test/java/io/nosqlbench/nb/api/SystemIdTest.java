package io.nosqlbench.nb.api;

import org.junit.jupiter.api.Test;

public class SystemIdTest {

    @Test
    public void testHostInfo() {
        String info = SystemId.getHostSummary();
        System.out.println(info);
    }
}
