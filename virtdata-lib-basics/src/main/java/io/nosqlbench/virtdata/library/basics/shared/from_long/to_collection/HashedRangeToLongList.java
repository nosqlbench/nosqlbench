package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Create a list of longs.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedRangeToLongList implements LongFunction<List<Long>> {

    private final HashRange valueRange;
    private final HashRange sizeRange;

    public HashedRangeToLongList(int minVal, int maxVal, int minSize, int maxSize) {
        if (minSize>=maxSize || minSize>=maxSize) {
            throw new RuntimeException("HashedRangeToLongList must have minval, maxval, minsize, maxsize, where min<max.");
        }
        this.valueRange = new HashRange(minVal,maxVal);
        this.sizeRange = new HashRange(minSize,maxSize);
    }

    @Override
    public List<Long> apply(long value) {
        long listSize = sizeRange.applyAsLong(value);
        ArrayList<Long> longList = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            longList.add(valueRange.applyAsLong(value+i));
        }
        return longList;
    }
}
