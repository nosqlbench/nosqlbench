package io.nosqlbench.extensions.optimizers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MVParams implements Iterable<MVParams.MVParam> {
    private final List<MVParam> paramList = new ArrayList<>();

    public MVParams addParam(String name, double min, double max) {
        paramList.add(new MVParam(name,min,max));
        return this;
    }

    public int size() {
        return paramList.size();
    }

    @Override
    public Iterator<MVParam> iterator() {
        return paramList.iterator();
    }

    public MVParam get(int index) {
        return paramList.get(index);
    }

    public static class MVParam {
        public final String name;
        public final double min;
        public final double max;

        public MVParam(String name, double min, double max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }
    }
}
