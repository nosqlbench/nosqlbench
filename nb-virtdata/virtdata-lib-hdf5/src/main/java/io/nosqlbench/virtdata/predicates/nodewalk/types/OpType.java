package io.nosqlbench.virtdata.predicates.nodewalk.types;

public enum OpType {
  GT(">"),
  LT("<"),
  EQ("="),
  NE("!="),
  GE(">="),
  LE("<="),
  IN("IN");

  private final String symbol;

  OpType(String symbol) {
    this.symbol=symbol;
  }

  public String symbol() {
    return this.symbol;
  }
}
