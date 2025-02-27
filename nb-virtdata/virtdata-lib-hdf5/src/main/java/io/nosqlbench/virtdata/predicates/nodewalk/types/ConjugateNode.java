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

public record ConjugateNode(ConjugateType type, Node<?>[] values)
    implements BBWriter<ConjugateNode>, Node<ConjugateNode>
{

  public ConjugateNode(ByteBuffer b) {
    this(ConjugateType.values()[b.get()], readValues(b));
  }

  private static Node<?>[] readValues(ByteBuffer b) {
    byte count = b.get();
    Node<?>[] elements = new Node[count];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = readValue(b);
    }
    return elements;
  }

  private static Node readValue(ByteBuffer b) {
    ConjugateType eType = ConjugateType.values()[b.get()];
    b.position(b.position() - 1);
    return switch (eType) {
      case PRED -> new PredicateNode(b);
      default -> new ConjugateNode(b);
    };
  }

  @Override
  public ByteBuffer encode(ByteBuffer out) {
    out.put((byte) this.type.ordinal()).put((byte) this.values.length);
    for (Node<?> element : values) {
      element.encode(out);
    }
    return out;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ConjugateNode{");
    sb.append("type=").append(type);
    sb.append(", v=").append(values == null ? "null" : Arrays.asList(values).toString());
    sb.append('}');
    return sb.toString();
  }
}
