package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToBase64StringTest {

    @Test
    public void testApply() {
        ToBase64String f = new ToBase64String();
        String apply = f.apply(32144123454345L);
        assertThat(apply).isEqualTo("AAAdPCMPY4k=");
    }

    @Test
    public void testStringStringForm() {
        io.nosqlbench.virtdata.library.basics.shared.unary_string.ToBase64String f =
                new io.nosqlbench.virtdata.library.basics.shared.unary_string.ToBase64String();
        String r = f.apply("four score and seven years ago");
        assertThat(r).isEqualTo("Zm91ciBzY29yZSBhbmQgc2V2ZW4geWVhcnMgYWdv");

    }

}