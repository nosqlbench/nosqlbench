package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.Test;

import java.util.List;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ListFunctionsTest {

    @Test
    public void ListFunctions() {
        LongFunction<String> f1 = (long l) -> "long[" + l + "]";
        ListFunctions func = new ListFunctions(f1);
        List<Object> value = func.apply(234L);
        assertThat(value).hasSize(1);
        assertThat(value.get(0)).isOfAnyClassIn(String.class);
    }

}
