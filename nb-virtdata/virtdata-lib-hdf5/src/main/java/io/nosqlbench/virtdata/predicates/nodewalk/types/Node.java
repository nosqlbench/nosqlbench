package io.nosqlbench.virtdata.predicates.nodewalk.types;

/// [T] is the node type, from
public sealed interface Node<T> extends BBWriter<T> permits ConjugateNode, PredicateNode {
}
