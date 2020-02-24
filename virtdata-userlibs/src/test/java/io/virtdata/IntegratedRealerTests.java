package io.virtdata;

import io.nosqlbench.virtdata.api.DataMapper;
import io.nosqlbench.virtdata.api.VirtData;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class IntegratedRealerTests {

    public void testLastNames() {
        DataMapper mapper = VirtData.getOptionalMapper("LastNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Miracle");
    }

    public void testFirstNames() {
        DataMapper mapper = VirtData.getOptionalMapper("FirstNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Norman");
    }

    public void testFullNames() {
        DataMapper mapper = VirtData.getOptionalMapper("FullNames()").orElse(null);
        assertThat(mapper).isNotNull();
        assertThat(mapper.get(0L)).isEqualTo("Norman Wolf");
    }



}
