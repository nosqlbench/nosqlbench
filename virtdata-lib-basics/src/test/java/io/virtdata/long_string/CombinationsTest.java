package io.virtdata.long_string;

import io.virtdata.libbasics.shared.from_long.to_string.Combinations;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CombinationsTest {

    @Test
    public void testSimplePrimes() {
        Combinations combinations = new Combinations("0-1;0-2;0-4");
        assertThat(combinations.apply(0)).isEqualTo("000");
        assertThat(combinations.apply(15)).isEqualTo("100");
        assertThat(combinations.apply(29)).isEqualTo("124");
        assertThat(combinations.apply(30)).isEqualTo("000");
    }

    @Test
    public void testBinary() {
        Combinations binaryByte = new Combinations("0-1;0-1;0-1;0-1;0-1;0-1;0-1;0-1;");
        assertThat(binaryByte.apply(37)).isEqualTo("00100101");
        for (int i = 0; i < 512; i++) {
            System.out.println("i:" + i + " = b:" + binaryByte.apply(i));
        }
    }

    @Test
    public void testTemplateFinesse() {
        Combinations combinations = new Combinations("0-9;0-9;0-9;-;0-9;0-9;0-9;-;0-9;0-9;0-9;0-9");
        assertThat(combinations.apply(5158675309L)).isEqualTo("515-867-5309");
    }

    @Test
    public void testHexaDecimalDecimalOctalBinaryUnaryInsanity() {
        Combinations combinations = new Combinations(
                "0-9A-F;0-9;0-7;0-1;1"
        );
        assertThat(combinations.apply(1)).isEqualTo("00011");
    }

    @Test
    public void test090909() {
        Combinations combinations = new Combinations("0-9;0-9;0-9");
        assertThat(combinations.apply(0)).isEqualTo("000");
        assertThat(combinations.apply(11)).isEqualTo("011");
        assertThat(combinations.apply(99)).isEqualTo("099");
        assertThat(combinations.apply(199)).isEqualTo("199");
    }

    @Test
    public void testHexadecimalWoot() {
        Combinations combinations = new Combinations("0-9ABCDEF;0-9A-F;0-9A-F;0-9A-F");
        assertThat(combinations.apply(0)).isEqualTo("0000");
        assertThat(combinations.apply(255)).isEqualTo("00FF");
        assertThat(combinations.apply(256)).isEqualTo("0100");
        assertThat(combinations.apply(4095)).isEqualTo("0FFF");
        assertThat(combinations.apply(8191)).isEqualTo("1FFF");
        assertThat(combinations.apply(32767)).isEqualTo("7FFF");
        assertThat(combinations.apply(65535)).isEqualTo("FFFF");
    }

    @Test
    public void testAZ09() {
        Combinations combinations = new Combinations("A-Z;0-9");
        assertThat(combinations.apply(0)).isEqualTo("A0");
        assertThat(combinations.apply(10)).isEqualTo("B0");
        assertThat(combinations.apply(26)).isEqualTo("C6");
        assertThat(combinations.apply(31)).isEqualTo("D1");
    }

    @Test(expected = ArithmeticException.class)
    public void testOverflow() {
        // (104^9 / 2^63) < 1.0
        // (104^10 / 2^63) > 1.0
        Combinations combinations = new Combinations(
                "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
                        + "A-ZA-ZA-ZA-Z;"
        );

        combinations.apply(Long.MAX_VALUE);
    }


    @Test
    public void testOverflowLong() {
        Combinations c = new Combinations("a-z;a-z;a-z;a-z;a-z;a-z;a-z;a-z;0-9;");
        String value ;
        value = c.apply(((long) Integer.MAX_VALUE) + 1L);
        //value = c.apply((int)2945182322382062539L);
        value = c.apply(Long.MAX_VALUE);
        value = c.apply(((long) Integer.MAX_VALUE) *2);
        value = c.apply(((long) Integer.MAX_VALUE) *4);
        value = c.apply(((long) Integer.MAX_VALUE) *8);
        value = c.apply(((long) Integer.MAX_VALUE) *16);
    }

}