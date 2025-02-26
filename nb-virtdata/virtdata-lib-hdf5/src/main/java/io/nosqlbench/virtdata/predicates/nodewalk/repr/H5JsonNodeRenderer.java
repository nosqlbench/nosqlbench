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
