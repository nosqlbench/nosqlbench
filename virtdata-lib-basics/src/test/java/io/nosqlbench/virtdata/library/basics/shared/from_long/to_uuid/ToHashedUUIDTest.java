package io.nosqlbench.virtdata.library.basics.shared.from_long.to_uuid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ToHashedUUIDTest {

    @Test
    public void testHashedUUID() {
        ToHashedUUID thu = new ToHashedUUID();
        UUID uuid = thu.apply(1L);
        assertThat(uuid.variant()).isEqualTo(2);
        assertThat(uuid.version()).isEqualTo(4);
        assertThat(uuid.toString()).isEqualTo("5752fae6-9d16-43da-b20f-557a1dd5c571");
    }

}