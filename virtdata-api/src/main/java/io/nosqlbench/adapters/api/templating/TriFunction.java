package io.nosqlbench.adapters.api.templating;

public interface TriFunction <A,B,C> {
    public A apply(A a,B b,C c);
}
