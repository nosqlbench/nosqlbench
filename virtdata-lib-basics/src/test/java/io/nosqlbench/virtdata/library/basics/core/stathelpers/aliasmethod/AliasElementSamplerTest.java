package io.nosqlbench.virtdata.library.basics.core.stathelpers.aliasmethod;

import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasElementSampler;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.ElemProbD;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasElementSamplerTest {

    @Test
    public void testAliasSamplerBinaryFractions() {
        List<ElemProbD<Integer>> events = new ArrayList<>();
        events.add(new ElemProbD<>(1,1.0D));
        events.add(new ElemProbD<>(2,1.0D));
        events.add(new ElemProbD<>(3,2.0D));
        events.add(new ElemProbD<>(4,4.0D));
        events.add(new ElemProbD<>(5,8.0D));
        events.add(new ElemProbD<>(6,16.0D));
        events.add(new ElemProbD<>(7,32.0D));
        events.add(new ElemProbD<>(8,64.0D));

        AliasElementSampler<Integer> as = new AliasElementSampler<>(events);
        int[] stats = new int[9];
        for (int i = 0; i < 10000; i++) {
            double v = (double)i / 10000D;
            Integer idx = as.apply(v);
            stats[idx]++;
        }
        System.out.println(Arrays.toString(stats));
        assertThat(stats).containsExactly(0,79,79,157,313,626,1250,2499,4997);

    }

}