package io.nosqlbench.virtdata.library.basics.tests.long_long;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.SignedHash;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignedHashTest {

    @Test
    public void testFunctionalResult() {

        SignedHash hash = new SignedHash();
        assertThat(hash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(hash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(hash.applyAsLong(2L)).isEqualTo(-8218881827949364593L);
        assertThat(hash.applyAsLong(3L)).isEqualTo(-8048510690352527683L);
        assertThat(hash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(hash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(hash.applyAsLong(2L)).isEqualTo(-8218881827949364593L);
        assertThat(hash.applyAsLong(3L)).isEqualTo(-8048510690352527683L);

    }

    @Test
    public void illustrateFirstTen() {

        SignedHash hash = new SignedHash();
        for (int i = 0; i < 10; i++) {
            long l = hash.applyAsLong(i) % 50L;
            System.out.println("i=" + i + " result=" + l);
        }

        for (int i = 0; i < 10; i++) {
            long l = hash.applyAsLong(i+1000000L) % 50L;
            System.out.println("i=" + i + " result=" + l);
        }

    }

}