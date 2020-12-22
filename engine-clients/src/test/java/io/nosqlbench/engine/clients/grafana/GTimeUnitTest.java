package io.nosqlbench.engine.clients.grafana;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GTimeUnitTest {

    @Test
    public void testParseBasic() {
        long result = GTimeUnit.epochSecondsFor("now");
    }

    @Test
    public void testParseRelative() {
        long result = GTimeUnit.epochSecondsFor("now-1w");
        assertThat(result).isCloseTo((System.currentTimeMillis() / 1000) - (86400L * 7L), Offset.offset(60L));
    }

}