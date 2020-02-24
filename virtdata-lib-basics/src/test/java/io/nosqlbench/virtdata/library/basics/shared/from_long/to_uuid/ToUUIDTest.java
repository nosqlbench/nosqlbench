package io.nosqlbench.virtdata.library.basics.shared.from_long.to_uuid;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ToUUIDTest {

    @Test
    public void testToUUIDVersionAndVariant() {
        ToUUID tu = new ToUUID(0L);
        UUID one = tu.apply(1L);
        assertThat(one.toString()).isEqualTo("00000000-0000-4000-8000-000000000001");
        UUID max = tu.apply(Long.MAX_VALUE>>>3);
        assertThat(max.toString()).isEqualTo("00000000-0000-4000-8fff-ffffffffffff");
    }

    @Test
    public void testToUUIDAllBits() {
        ToUUID tu = new ToUUID(0xffffffffffffffffL);
        UUID maxbits = tu.apply(Long.MAX_VALUE);
        // assuming msb extension of lsbs to x "don't care" field of 2-3 bit variant as per RFC
        assertThat(maxbits.toString()).isEqualTo("ffffffff-ffff-4fff-9fff-ffffffffffff");
    }

}