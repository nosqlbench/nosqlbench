package io.nosqlbench.virtdata.library.basics.shared;

import io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.DoubleFlow;
import io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Max;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Add;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.LongFlow;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Combinations;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Template;
import io.nosqlbench.virtdata.library.basics.shared.functionadapters.Flow;
import io.nosqlbench.virtdata.library.basics.shared.unary_int.IntFlow;
import io.nosqlbench.virtdata.library.basics.shared.unary_int.Mul;
import io.nosqlbench.virtdata.library.basics.shared.unary_string.Prefix;
import io.nosqlbench.virtdata.library.basics.shared.unary_string.StringFlow;
import io.nosqlbench.virtdata.library.basics.shared.unary_string.Suffix;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.function.IntUnaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class IntFlowTest {

    @Test
    public void testLongFlow() {
        LongFlow lf =
                new LongFlow(
                        new Add(3L),
                        new Add(4L)
                );
        assertThat(lf.applyAsLong(3L)).isEqualTo(10L);

    }

    @Test
    public void testIntegerFlow() {
        Mul imul3 = new Mul(3);
        IntFlow ifl =
                new IntFlow(imul3, imul3);
        assertThat(ifl.applyAsInt(2)).isEqualTo(18);
    }

    @Test
    public void testDoubleFlow() {
        Max dmax12 = new Max(12D);
        Max dmax100 = new Max(100D);
        DoubleFlow dmax =
                new DoubleFlow(dmax12,dmax100);
        assertThat(dmax.applyAsDouble(13D)).isCloseTo(100D, Offset.offset(0.0001D));

    }

    @Test
    public void testStringFlow() {
        Prefix pf = new Prefix("->");
        Suffix sf = new Suffix ("<-");
        StringFlow flow = new StringFlow(pf,sf);
        assertThat(flow.apply("woot")).isEqualTo("->woot<-");
    }

    @Test
    public void testGenericFlow() {
        Combinations fc = new Combinations("A-Z");
        Template tc = new Template(
                "{}-{}",
                (LongFunction)(l ->l+3),
                (LongFunction)(m -> m+21L)
        );
        IntUnaryOperator ints = i -> 12 + i;
        LongUnaryOperator longs = l -> 32L + l;

        Flow f = new Flow(ints, longs, ints, fc);
        for (int i = 0; i < 100; i++) {
            Object r = f.apply(i);
            System.out.print(r);
        }
        System.out.println();
    }
}