package io.nosqlbench.nb.api.markdown.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class ListSplitterWhyDoesJavaNotDoThisAlready {
    public static <T> List<? extends T> partition(List<? extends T> source, Predicate<T> filterout) {
        ArrayList<T> filtered = new ArrayList<>();
        ListIterator<? extends T> it = source.listIterator();
        while (it.hasNext()) {
            T element = it.next();
            if (filterout.test(element)) {
                it.remove();
                filtered.add(element);
            }
        }
        return filtered;
    }
}
