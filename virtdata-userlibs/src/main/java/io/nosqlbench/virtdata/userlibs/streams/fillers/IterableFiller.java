package io.nosqlbench.virtdata.userlibs.streams.fillers;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class IterableFiller<T> implements Iterable<Fillable> {

    @NotNull
    @Override
    public Iterator<Fillable> iterator() {
        return new FillerIterator<Fillable>();
    }

    private static class FillerIterator<T> implements Iterator<Fillable> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Fillable next() {
            return null;
        }
    }
}
