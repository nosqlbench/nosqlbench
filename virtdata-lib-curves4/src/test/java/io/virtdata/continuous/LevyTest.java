package io.virtdata.continuous;

import io.virtdata.continuous.long_double.Levy;
import org.assertj.core.data.Offset;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class LevyTest {

    @Test
    public void testLevy() {
        Levy levy = new Levy(2.3d, 1.0d);
        assertThat(levy.applyAsDouble(10L)).isCloseTo(2.938521849905433, Offset.offset(0.000001d));
    }

}