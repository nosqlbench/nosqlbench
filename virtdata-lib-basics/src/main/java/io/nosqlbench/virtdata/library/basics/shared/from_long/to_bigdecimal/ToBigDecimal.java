package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bigdecimal;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.util.MathContextReader;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.LongFunction;

/**
 * Convert values to BigDecimals at configurable scale or precision.
 *
 * <p>
 * ToBigDecimal(...) functions which take whole-numbered inputs may have
 * a scale parameter or a custom MathContext, but not both. The scale parameter
 * is not supported for String or Double input forms.
 * </p>
 */
@ThreadSafeMapper
@Categories(Category.conversion)
public class ToBigDecimal implements LongFunction<BigDecimal> {

    private final MathContext mathContext;
    private final int scale;

    @Example({"ToBigDecimal()", "Convert all long values to whole-numbered BigDecimal values"})
    public ToBigDecimal() {
        this(0);
    }

    @Example({"ToBigDecimal(0)", "Convert all long values to whole-numbered BigDecimal values"})
    @Example({"ToBigDecimal(2)", "Convert long 'pennies' BigDecimal with 2 digits after decimal point"})
    public ToBigDecimal(int scale) {
        this.scale = scale;
        this.mathContext=null;
    }

    /**
     * Convert all input values to BigDecimal values with a specific MathContext. This form is only
     * supported for scale=0, meaning whole numbers. The value for context can be one of UNLIMITED,
     * DECIMAL32, DECIMAL64, DECIMAL128, or any valid configuration supported by
     * {@link MathContext#MathContext(String)}, such as {@code "precision=32 roundingMode=CEILING"}.
     * In the latter form, roundingMode can be any valid value for {@link RoundingMode}, like
     * UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, or UNNECESSARY.
     */
    @Example({"ToBigDecimal('DECIMAL32')","IEEE 754R Decimal32 format, 7 digits, HALF_EVEN"})
    @Example({"ToBigDecimal('DECIMAL64'),","IEEE 754R Decimal64 format, 16 digits, HALF_EVEN"})
    @Example({"ToBigDecimal('DECIMAL128')","IEEE 754R Decimal128 format, 34 digits, HALF_EVEN"})
    @Example({"ToBigDecimal('UNLIMITED')","unlimited precision, HALF_UP"})
    @Example({"ToBigDecimal('precision=17 roundingMode=UNNECESSARY')","Custom precision with no rounding performed"})
    public ToBigDecimal(String context) {
        this.scale=Integer.MIN_VALUE;
        this.mathContext = MathContextReader.getMathContext(context);
    }

    @Override
    public BigDecimal apply(long value) {
        if (mathContext!=null) {
            return new BigDecimal(value,mathContext);
        } else {
            return BigDecimal.valueOf(value,scale);
        }
    }
}
