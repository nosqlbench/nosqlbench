package io.virtdata.datamappers;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_string.Template;

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
