package io.virtdata.libbasics.shared.from_string;

import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchFuncTest {

    @Test
    public void testMatchFunc() {
        Function<String,String> s = t -> t+"-suffix";
        Function<String,String> p = t -> "prefix-"+t;

        MatchFunc matchFunc = new MatchFunc("add-prefix", p, "add-suffix", s, ".*", "none");
        assertThat(matchFunc.apply("add-nothing")).isEqualTo("none");
        assertThat(matchFunc.apply("add-prefix")).isEqualTo("prefix-add-prefix");
        assertThat(matchFunc.apply("add-suffix")).isEqualTo("add-suffix-suffix");
    }

}