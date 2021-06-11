package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.junit.jupiter.api.Test;

public class TextImageExtractTest {

    /**
     * These tests are largely illustrative for those integrating this function into their
     * binding recipes.
     */
    @Test
    public void testCtors() {

        CharBufImage f1 = new CharBufImage(10);
        System.out.println("f1:" + f1.apply(1L));

        CharBufImage f2 = new CharBufImage("abc123",15);
        System.out.println("f2:" + f2.apply(1L));

        CharBufImage f3 = new CharBufImage("abcdef",10,3L,5);
        System.out.println("f3:" + f3.apply(1L));
    }

    @Test
    public void testComposedFromStringFunc() {
        NumberNameToString nnts = new NumberNameToString();
        CharBufImage cbi = new CharBufImage(nnts, 100, 20);
        System.out.println("cbi:" + cbi.apply(1L));
    }


}
