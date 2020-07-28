package io.nosqlbench.virtdata.userlibs.streams.fillers;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.LongFunction;

public class LongFunctionIterable<T> implements Iterable<T> {

    private final long startCycle;
    private final LongFunction<T> f;
    private final long endCycle;

    public LongFunctionIterable(long startCycle,long endCycle, LongFunction<T> f) {
        this.startCycle = startCycle;
        this.endCycle = endCycle;
        this.f = f;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new LongFunctionIterator(startCycle, endCycle, f);
    }

    private class LongFunctionIterator implements Iterator<T> {
        private final long endCycle;
        private long cycle;
        private final LongFunction<T> f;

        public LongFunctionIterator(long startCycle, long endCycle, LongFunction<T> f) {
            this.cycle = startCycle;
            this.endCycle = endCycle;
            this.f = f;
        }

        @Override
        public boolean hasNext() {
            return cycle<endCycle;
        }

        @Override
        public T next() {
            return f.apply(cycle++);
        }
    }
}
