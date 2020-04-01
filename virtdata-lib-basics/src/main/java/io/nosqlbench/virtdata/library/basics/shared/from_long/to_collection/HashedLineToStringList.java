package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedLineToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Creates a List&lt;String&gt; from a list of words in a file.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedLineToStringList implements LongFunction<List> {

    private final HashedLineToString hashedLineToString;
    private final HashRange hashRange;

    public HashedLineToStringList(String filename, int minSize, int maxSize) {
        this.hashedLineToString = new HashedLineToString(filename);
        this.hashRange = new HashRange(minSize,maxSize);
    }

    @Override
    public List apply(long value) {
        long size = hashRange.applyAsLong(value);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(hashedLineToString.apply(value+i));
        }
        return list;
    }
}
