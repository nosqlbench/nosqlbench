package io.virtdata;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegratedRealerTests {

    @Test
    public void testLastNames() {
        DataMapper<?> mapper = VirtData.getOptionalMapper("LastNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Miracle");
    }

    @Test
    public void testFirstNames() {
        DataMapper<?> mapper = VirtData.getOptionalMapper("FirstNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Norman");
    }

    @Test
    public void testFullNames() {
        DataMapper<?> mapper = VirtData.getOptionalMapper("FullNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Norman Wolf");
    }

}
