/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.nosqlbench.virtdata.predicates.nodewalk.types;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public record PredicateNode(
    /* encoded as byte */
    int field,
    /* encoded as byte */
    OpType op,
    /* encoded as short len, and then longs */
    long... v
) implements BBWriter<PredicateNode>, Node<PredicateNode>
{
  public PredicateNode(byte type, int field, OpType op, long... v) {
    this(field, op, v);
  }
  public PredicateNode(ByteBuffer b) {
    this(
        b.get(),
        b.get(),
        OpType.values()[b.get()],
        readValues(b)
    );
  }

  private static long[] readValues(ByteBuffer b) {
    int len = b.getShort();
    long[] values = new long[len];
    for (int i = 0; i < values.length; i++) {
      values[i] = b.getLong();
    }
    return values;
  }

  @Override
  public ByteBuffer encode(ByteBuffer out) {
    out.put((byte) ConjugateType.PRED.ordinal());
    out.put((byte) field).put((byte) op.ordinal());
    out.putShort((short) v.length);
    for (long l : v) {
      out.putLong(l);
    }
    return out;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PredicateNode(int fieldOffset, OpType op1, long[] v1)))
      return false;
    return field == fieldOffset && Arrays.equals(v, v1) && op == op1;
  }

  @Override
  public int hashCode() {
    int result = field;
    result = 31 * result + Objects.hashCode(op);
    result = 31 * result + Arrays.hashCode(v);
    return result;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("PredicateNode{");
    sb.append("field=").append(field);
    sb.append(", op=").append(op);
    sb.append(", v=");
    if (v == null)
      sb.append("null");
    else {
      sb.append('[');
      for (int i = 0; i < v.length; ++i)
        sb.append(i == 0 ? "" : ", ").append(v[i]);
      sb.append(']');
    }
    sb.append('}');
    return sb.toString();
  }
}
