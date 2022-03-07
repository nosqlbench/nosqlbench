package io.nosqlbench.datamappers.functions.long_localdate;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class EpochMillisToJavaLocalDateTest {

    @Test
    public void testDayAt2020Start() {
        EpochMillisToJavaLocalDate func = new EpochMillisToJavaLocalDate();
        LocalDate v1 = func.apply(0);
        LocalDate zerodate = LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertThat(v1).isEqualTo(zerodate);

        LocalDate v2 = func.apply(533664000002000L);
        DateTime dt2 = new DateTime(533664000002000L);
        LocalDate d2instant = LocalDate.ofInstant(Instant.ofEpochMilli(533664000002000L), ZoneId.systemDefault());
        assertThat(v2).isEqualTo(d2instant);
    }

}
