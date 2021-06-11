package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.NumberNameToString;
import io.nosqlbench.virtdata.library.basics.shared.stateful.Clear;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class SaveTest {

    @Test
    public void testSaveAndLoadByGeneratedName() {
        new Clear().apply(0L);
        long l1=234L;
        long l2=123L;

        NumberNameToString numberNameToString = new NumberNameToString();
        Function<Object,Object> namer = (o) -> numberNameToString.apply((long) o);

        String two34 = numberNameToString.apply(l1);
        assertThat(two34).isEqualTo("two hundred and thirty four");
        String one23 = numberNameToString.apply(l2);
        assertThat(one23).isEqualTo("one hundred and twenty three");

        Save save = new Save(namer);
        long lv1=save.applyAsLong(l1);
        assertThat(lv1).isEqualTo(l1);
        long lv2=save.applyAsLong(l2);
        assertThat(lv2).isEqualTo(l2);

        Load load = new Load(namer);
        long lv3 = load.applyAsLong(l1);
        assertThat(lv3).isEqualTo(l1);
        long lv4 = load.applyAsLong(l2);
        assertThat(lv4).isEqualTo(l2);
    }

    @Test
    public void testSaveUnaryString() {
        new Clear().apply(0L);
        io.nosqlbench.virtdata.library.basics.shared.unary_string.Save saver = new io.nosqlbench.virtdata.library.basics.shared.unary_string.Save("stringname");
        String passiveOutput = saver.apply("string1");
        assertThat(passiveOutput).isEqualTo("string1");
        io.nosqlbench.virtdata.library.basics.shared.unary_string.Load loader = new io.nosqlbench.virtdata.library.basics.shared.unary_string.Load("stringname");
        String loaded = loader.apply("3434434");
        assertThat(loaded).isEqualTo("string1");

        io.nosqlbench.virtdata.library.basics.shared.unary_string.Load dloader = new io.nosqlbench.virtdata.library.basics.shared.unary_string.Load("stringnam","e");
        String defaulted = dloader.apply("2342343242");
        assertThat(defaulted).isEqualTo("e");
    }

    @Test
    public void testSaveUnaryDouble() {
        new Clear().apply(0L);
        io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Save saver = new io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Save("doublename");
        double v = saver.applyAsDouble(2345.678D);
        assertThat(v).isCloseTo(2345.678D, Offset.offset(0.0003D));
        io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Load loader = new io.nosqlbench.virtdata.library.basics.shared.from_double.to_double.Load("doublename");
        double loaded = loader.applyAsDouble(34434D);
        assertThat(loaded).isCloseTo(2345.678D,Offset.offset(0.0003D));
    }


}