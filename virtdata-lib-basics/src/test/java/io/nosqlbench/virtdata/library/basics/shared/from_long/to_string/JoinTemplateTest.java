package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Mod;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinTemplateTest {

    @Test
    public void testBasicJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate("__", new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(3);
        assertThat(v).isEqualTo("three__four");
    }

    @Test
    public void testPrefixSuffixJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate("<","__", ">",new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(3);
        assertThat(v).isEqualTo("<three__four>");
    }

    @Test
    public void testIterOpFunctionJoinTemplate() {
        JoinTemplate t1 = new JoinTemplate(new Mod(5L), "<", "__",">",
                new NumberNameToString(), new NumberNameToString(), new NumberNameToString());
        String v = t1.apply(17);
        assertThat(v).isEqualTo("<two__three__four>");

    }

}