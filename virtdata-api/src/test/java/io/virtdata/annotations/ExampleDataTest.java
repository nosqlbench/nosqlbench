package io.virtdata.annotations;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleDataTest {

    @Test
    public void testRangeFormats() {
        ExampleData e = new ExampleData(new String[]{"Example('a')", "an example", "[1..5]"});
        long[] onetofive = e.getLongInputs();
        assertThat(onetofive).containsExactly(1L,2L,3L,4L,5L);
    }

    @Test
    public void testNegativeIncrRange() {
        ExampleData e2 = new ExampleData(new String[]{"Example('b')","e2","[10..-10 -5]"});
        long[] downBy5 = e2.getLongInputs();
        assertThat(downBy5).containsExactly(10L,5L,0L,-5L,-10L);
    }

    @Test
    public void testSequence() {
        ExampleData e3 = new ExampleData(new String[]{"Example('c')","e3","[1,2,-3]"});
        long[] afew = e3.getLongInputs();
        assertThat(afew).containsExactly(1L,2L,-3L);
    }

}