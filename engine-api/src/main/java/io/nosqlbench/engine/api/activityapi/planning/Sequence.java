package io.nosqlbench.engine.api.activityapi.planning;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sequence<T> implements OpSequence<T> {
    private final SequencerType type;
    private final List<T> elems;
    private final int[] seq;

    Sequence(SequencerType type, List<T> elems, int[] seq) {
        this.type = type;
        this.elems = elems;
        this.seq = seq;
    }

    @Override
    public T get(long selector) {
        int index = (int) (selector % seq.length);
        index = seq[index];
        return elems.get(index);
    }

    @Override
    public List<T> getOps() {
        return elems;
    }

    @Override
    public int[] getSequence() {
        return seq;
    }

    public SequencerType getSequencerType() {
        return type;
    }

    @Override
    public <U> Sequence<U> transform(Function<T, U> func) {
        return new Sequence<U>(type, elems.stream().map(func).collect(Collectors.toList()), seq);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.seq);
    }
}
