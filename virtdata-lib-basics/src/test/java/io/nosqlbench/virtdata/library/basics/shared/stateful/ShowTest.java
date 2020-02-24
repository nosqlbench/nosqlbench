package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShowTest {

    @Test
    public void testBasicStateSupport() {
        new Clear().apply(0L);
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save saveFoo = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save("foo");
        saveFoo.applyAsLong(23);
        new Save("cycle").applyAsLong(-1L);
        Show showAll=new Show();
        String shown = showAll.apply(234L);
        assertThat(shown).isEqualTo("{foo=23, cycle=-1}");
        io.nosqlbench.virtdata.library.basics.shared.unary_string.Save saveBar = new io.nosqlbench.virtdata.library.basics.shared.unary_string.Save("bar");
        saveBar.apply("Bar");
        Show showFoo = new Show("foo");
        Show showBar = new Show("bar");
        assertThat(showFoo.apply(2342343L)).isEqualTo("{foo=23}");
        assertThat(showBar.apply(23423L)).isEqualTo("{bar=Bar}");
        new Clear().apply(234);
        assertThat(showAll.apply("234").isEmpty());
    }


}