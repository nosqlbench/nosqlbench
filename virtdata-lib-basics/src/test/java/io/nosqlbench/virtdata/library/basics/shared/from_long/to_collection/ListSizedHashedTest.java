package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;


import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.NumberNameToString;
import org.junit.Test;

import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ListSizedHashedTest {

    @Test
    public void testTwoPartExample() {
        LongToIntFunction sizer = (l) -> (int) l;
        LongFunction<String> namer = (l) -> "L{" + l + "}";

        ListSizedHashed f1 = new ListSizedHashed(sizer, namer);
        List<Object> for37 = f1.apply(37L);
        assertThat(for37).hasSize(37);
        assertThat(for37.get(0)).isNotEqualTo(for37.get(36));
        for (Object o : for37) {
            System.out.println(o);
        }

    }

    @Test
    public void testFunctionSelection() {
        LongToIntFunction sizer = (l) -> (int) l;
        LongFunction<String> namer = (l) -> "L{" + l + "}";
        LongFunction<String> brackets = (l) -> "[[" + l + "]]";

        ListSizedHashed f2 = new ListSizedHashed(sizer, namer, namer, brackets, namer);
        List<Object> for53 = f2.apply(53L);
        assertThat(for53).hasSize(53);
        assertThat(for53.get(2).toString()).startsWith("[[");
        assertThat(for53.get(52)).isOfAnyClassIn(String.class);

    }

}
