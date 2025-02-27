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
package io.nosqlbench.virtdata.predicates.nodewalk.repr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.predicates.nodewalk.types.ConjugateNode;
import io.nosqlbench.virtdata.predicates.nodewalk.types.Node;
import io.nosqlbench.virtdata.predicates.nodewalk.types.NodeRepresenter;
import io.nosqlbench.virtdata.predicates.nodewalk.types.PredicateNode;

public class H5JsonNodeRenderer implements NodeRepresenter {
    private final String[] schema;
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public H5JsonNodeRenderer(String[] schema) {
        this.schema = schema;
    }

    @Override
  public String apply(Node<?> node) {
    return switch (node) {
      case ConjugateNode n -> renderConjugate(n);
      case PredicateNode p -> renderPredicate(p);
    };
  }

  private String renderPredicate(PredicateNode p) {
      return gson.toJson(p);
  }

  private boolean isChar(String symbol) {
    char c = symbol.charAt(0);
    return (c >= 'A' && c <= 'Z') || (c>='a' && c<='z');
  }

  private String renderConjugate(ConjugateNode n) {
      return gson.toJson(n);
  }

  private String concatenate(String name, Node<?>[] values) {
    StringBuilder sb = new StringBuilder();

      for (Node<?> value : values) {
          String nodeRep = apply(value);
          if (!sb.isEmpty()) {
              sb.append(" ").append(name).append(" ");
          }
          sb.append(nodeRep);
      }
    return sb.toString();
  }

}
