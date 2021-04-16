package io.nosqlbench.virtdata.library.basics.shared.conversions.from_any;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Convert the input object to a JSON string with Gson.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToJSON implements Function<Object,String> {
    private final static Gson gson = new GsonBuilder().create();

    @Override
    public String apply(Object o) {
        return gson.toJson(o);
    }
}
