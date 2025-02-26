package io.nosqlbench.virtdata.predicates.nodewalk.types;

import java.nio.ByteBuffer;

public interface BBWriter<T> {
  ByteBuffer encode(ByteBuffer out);
}
