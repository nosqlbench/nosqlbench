package io.nosqlbench.virtdata.library.realer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Template;

import java.util.function.LongFunction;

/**
 * Combines the FirstNames and LastNames functions into one that
 * simply concatenates them with a space between.
 * This function is a shorthand equivalent of {@code Template('{} {}', FirstNames(), LastNames())}
 */
@ThreadSafeMapper
@Categories({Category.premade})
public class FullNames extends Template implements LongFunction<String> {

    public FullNames() {
        super("{} {}", new FirstNames(), new LastNames());
    }
}
