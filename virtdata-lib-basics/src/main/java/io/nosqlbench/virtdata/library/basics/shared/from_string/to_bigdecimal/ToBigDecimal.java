package io.nosqlbench.virtdata.library.basics.shared.from_string.to_bigdecimal;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.util.MathContextReader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.conversion)
public class ToBigDecimal implements Function<String, BigDecimal> {

    private final MathContext context;

    @Example({"Convert strings to BigDecimal according to default precision (unlimited) and rounding (HALF_UP)"})
    public ToBigDecimal() {
        this.context = MathContext.UNLIMITED;
    }

    /**
     * Convert all input values to BigDecimal values with a specific MathContext. This form is only
     * supported for scale=0, meaning whole numbers. The value for context can be one of UNLIMITED,
     * DECIMAL32, DECIMAL64, DECIMAL128, or any valid configuration supported by
     * {@link MathContext#MathContext(String)}, such as {@code "precision=32 roundingMode=CEILING"}.
     * In the latter form, roundingMode can be any valid value for {@link RoundingMode}, like
     * UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, or UNNECESSARY.
     */
    public ToBigDecimal(String context) {
        this.context =
            MathContextReader.getMathContext(context);
    }

    @Override
    public BigDecimal apply(String s) {
        return new BigDecimal(s, context);

    }
}
