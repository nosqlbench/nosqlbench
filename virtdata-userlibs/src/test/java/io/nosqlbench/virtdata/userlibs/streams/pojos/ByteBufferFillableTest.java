package io.nosqlbench.virtdata.userlibs.streams.pojos;

import io.nosqlbench.virtdata.userlibs.streams.fillers.ByteBufferSource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferFillableTest {

    @Test
    public void testBytesFillableFromLargeBuffers() {
        ByteBufferObject a = new ByteBufferObject(537);
        ByteBufferSource byteBuffers = new ByteBufferSource(0, Long.MAX_VALUE, 1024 * 1024);
        a.fill(byteBuffers);
        assertThat(a.getBuffer().capacity()).isEqualTo(537);
        assertThat(a.getBuffer().position()).isEqualTo(0);
    }

    @Test
    public void testBytesFillableFromSmallBuffers() {
        ByteBufferObject a = new ByteBufferObject(537);
        ByteBufferSource byteBuffers = new ByteBufferSource(0, Long.MAX_VALUE, 37);
        a.fill(byteBuffers);
        assertThat(a.getBuffer().capacity()).isEqualTo(537);
        assertThat(a.getBuffer().position()).isEqualTo(0);
    }

}