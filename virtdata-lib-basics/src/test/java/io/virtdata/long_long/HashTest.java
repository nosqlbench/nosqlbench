package io.virtdata.long_long;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashTest {

    @Test
    public void testFunctionalResult() {
        io.virtdata.libbasics.shared.from_long.to_long.Hash intHash = new io.virtdata.libbasics.shared.from_long.to_long.Hash();
        assertThat(intHash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(intHash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(intHash.applyAsLong(2L)).isEqualTo(8218881827949364593L);
        assertThat(intHash.applyAsLong(3L)).isEqualTo(8048510690352527683L);
        assertThat(intHash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(intHash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(intHash.applyAsLong(2L)).isEqualTo(8218881827949364593L);
        assertThat(intHash.applyAsLong(3L)).isEqualTo(8048510690352527683L);
        io.virtdata.libbasics.shared.from_long.to_int.Hash longHash = new io.virtdata.libbasics.shared.from_long.to_int.Hash();
        assertThat(longHash.applyAsInt(0L)).isEqualTo(1280820171);
        assertThat(longHash.applyAsInt(1L)).isEqualTo(488002522);
        assertThat(longHash.applyAsInt(2L)).isEqualTo(500548977);
        assertThat(longHash.applyAsInt(3L)).isEqualTo(2103802179);
        assertThat(longHash.applyAsInt(0L)).isEqualTo(1280820171);
        assertThat(longHash.applyAsInt(1L)).isEqualTo(488002522);
        assertThat(longHash.applyAsInt(3L)).isEqualTo(2103802179);
        assertThat(longHash.applyAsInt(2L)).isEqualTo(500548977);
        io.virtdata.libbasics.shared.from_long.to_int.Hash longIntHash = new io.virtdata.libbasics.shared.from_long.to_int.Hash();
        assertThat(longIntHash.applyAsInt(0L)).isEqualTo(1280820171);
        assertThat(longIntHash.applyAsInt(1L)).isEqualTo(488002522);
        assertThat(longIntHash.applyAsInt(2L)).isEqualTo(500548977);
        assertThat(longIntHash.applyAsInt(3L)).isEqualTo(2103802179);
        assertThat(longIntHash.applyAsInt(0L)).isEqualTo(1280820171);
        assertThat(longIntHash.applyAsInt(1L)).isEqualTo(488002522);
        assertThat(longIntHash.applyAsInt(3L)).isEqualTo(2103802179);
        assertThat(longIntHash.applyAsInt(2L)).isEqualTo(500548977);
        for (int i = 0; i < 1000; i++) {
            assertThat(longIntHash.applyAsInt(i)).isGreaterThan(0);
            assertThat(longHash.applyAsInt(i)).isGreaterThan(0);
            assertThat(intHash.applyAsLong(i)).isGreaterThan(0);
        }

    }

}