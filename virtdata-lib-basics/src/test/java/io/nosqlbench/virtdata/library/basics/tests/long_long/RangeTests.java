package io.nosqlbench.virtdata.library.basics.tests.long_long;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.AddCycleRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.AddHashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.CycleRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangeTests {

    @Test
    public void testHashRange() {
        HashRange hr = new HashRange(10,13);
        assertThat(hr.applyAsLong(0)).isEqualTo(11);
        assertThat(hr.applyAsLong(10)).isEqualTo(12);
        assertThat(hr.applyAsLong(100)).isEqualTo(12);
        assertThat(hr.applyAsLong(1000)).isEqualTo(11);
        assertThat(hr.applyAsLong(10000)).isEqualTo(10);
    }

    @Test
    public void testCycleRange() {
        CycleRange cr = new CycleRange(10,13);
        assertThat(cr.applyAsLong(0)).isEqualTo(10);
        assertThat(cr.applyAsLong(1)).isEqualTo(11);
        assertThat(cr.applyAsLong(2)).isEqualTo(12);
        assertThat(cr.applyAsLong(3)).isEqualTo(10);
    }

    @Test
    public void testAddHashRange() {
        AddHashRange ahr = new AddHashRange(14,17);
        assertThat(ahr.applyAsLong(1)).isEqualTo(16);
        assertThat(ahr.applyAsLong(2)).isEqualTo(16);
        assertThat(ahr.applyAsLong(3)).isEqualTo(18);
        assertThat(ahr.applyAsLong(4)).isEqualTo(20);
        assertThat(ahr.applyAsLong(5)).isEqualTo(19);
    }

    @Test
    public void testAddCycleRange() {
        AddCycleRange acr = new AddCycleRange(3,6);
        assertThat(acr.applyAsLong(0)).isEqualTo(3);
        assertThat(acr.applyAsLong(1)).isEqualTo(5);
        assertThat(acr.applyAsLong(2)).isEqualTo(7);
        assertThat(acr.applyAsLong(3)).isEqualTo(6);
        assertThat(acr.applyAsLong(4)).isEqualTo(8);
        assertThat(acr.applyAsLong(5)).isEqualTo(10);

    }



}