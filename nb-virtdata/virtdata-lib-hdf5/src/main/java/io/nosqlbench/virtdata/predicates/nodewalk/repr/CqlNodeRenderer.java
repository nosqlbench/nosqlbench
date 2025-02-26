package io.nosqlbench.virtdata.predicates.nodewalk.repr;


import io.nosqlbench.virtdata.predicates.nodewalk.types.ConjugateNode;
import io.nosqlbench.virtdata.predicates.nodewalk.types.Node;
import io.nosqlbench.virtdata.predicates.nodewalk.types.NodeRepresenter;
import io.nosqlbench.virtdata.predicates.nodewalk.types.PredicateNode;

public class CqlNodeRenderer implements NodeRepresenter {
    private final String[] schema;

    public CqlNodeRenderer(String[] schema) {
        this.schema = schema;
    }

    @Override
  public String apply(Node<?> node) {
    return switch (node) {
      case ConjugateNode n -> reprConjugate(n);
      case PredicateNode p -> reprPredicate(p);
    };
  }

  private String reprPredicate(PredicateNode p) {
    StringBuilder sb = new StringBuilder();
    sb.append(schema[p.field()]);
    if (isChar(p.op().symbol())) {
      sb.append(" ");
    }
    sb.append(p.op().symbol());
    if (p.v().length>1) {
      sb.append("(");
      String delim="";
      for (long v : p.v()) {
        sb.append(delim);
        sb.append(v);
        delim=",";
      }
      sb.append(")");
    } else {
      sb.append(p.v()[0]);
    }
    return sb.toString();
  }

  private boolean isChar(String symbol) {
    char c = symbol.charAt(0);
    return (c >= 'A' && c <= 'Z') || (c>='a' && c<='z');
  }

  private String reprConjugate(ConjugateNode n) {
    return switch (n.type()) {
      case AND,OR -> concatenate(n.type().name(),n.values());
      case PRED -> throw new RuntimeException("impossible unless broken code");
    };
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
