package io.nosqlbench.virtdata.api.bindings;

import java.nio.CharBuffer;
import java.util.function.*;

/**
 * <h2>Synopsis</h2>
 * <p>Static methods on this class are intended to adapt one java Functional type to another.
 * The fill-in logic which bridges functions in this class are intended to be flexible, but this means that strict data
 * type handling will be secondary to having functions that will return a value even when an overflow would normally
 * occur.
 * </p>
 *
 * <h2>Handling Overflow</h2>
 * <p>
 * In any case where an overflow error would occur, or a loss of numeric precision due to a narrowing conversion, the
 * value which is too large for the smaller register is reduced in magnitude to fit into the smaller data type using
 * modulo division by the maximum positive value of the smaller type.
 * <p>
 * This is suitable for testing cases where you need voluminous data according to a sketch or recipe, but it is not
 * appropriate for use in rigorous mathematical or algebraic scenarios. Use these methods appropriately, i.e. for
 * generating bulk test data only.
 * </p>
 *
 * <h2>Method Naming</h2>
 * <p>
 * The method naming used in this class follows a pattern to make reflective lookup easy and type aware. However, the
 * second and subsequent parameters are not used in the call to the function. They will be null. This allows for explicit
 * method matching, including with respect to variant types within generic parameters in target types. Thus, target
 * types with one generic parameter will have a signature like {@code (func, targetclass, generic0)} while those with
 * two generic parameters will have a signature like {@code (func, targetclass, generic0, generic1)}. What matters is
 * that the return type is accurate and type compatible with the actual function that is returned.
 * <p>
 * Alternative methods were explored, but this method works well and is actually easier to follow than most other
 * methods considered.
 * </p>
 *
 * <em>To ensure coverage of function types, add to the VirtDataConversionsTest class.</em>
 */
public class AdaptFunctionsFlexibly {

    public static LongUnaryOperator adapt(LongToDoubleFunction f, LongUnaryOperator placeholder) {
        return v -> (long) (f.applyAsDouble(v) % Long.MAX_VALUE);
    }

    public static LongToIntFunction adapt(LongToDoubleFunction f, LongToIntFunction placeholder) {
        return v -> (int) (f.applyAsDouble(v) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(LongToDoubleFunction f, LongFunction<Double> placeholder, Double output) {
        return f::applyAsDouble;
    }

    public static LongFunction<Long> adapt(LongToDoubleFunction f, LongFunction<Long> ignore0, Long ignore1) {
        return v -> (long) (f.applyAsDouble(v) % Long.MAX_VALUE);
    }

    public static LongFunction<Integer> adapt(LongToDoubleFunction f, LongFunction<Integer> ignore0, Integer ignore1) {
        return v -> (int) (f.applyAsDouble(v) % Integer.MAX_VALUE);
    }

    public static LongUnaryOperator adapt(LongToIntFunction f, LongUnaryOperator ignore0) {
        return f::applyAsInt;
    }

    public static LongToDoubleFunction adapt(LongToIntFunction f, LongToDoubleFunction ignore0) {
        return f::applyAsInt;
    }

    public static LongFunction<Double> adapt(LongToIntFunction f, LongFunction<Double> ignore0, Double ignore1) {
        return v -> (double) f.applyAsInt(v);
    }

    public static LongFunction<Long> adapt(LongToIntFunction f, LongFunction<Long> ignore0, Long ignore1) {
        return v -> (long) f.applyAsInt(v);
    }

    public static LongFunction<Integer> adapt(LongToIntFunction f, LongFunction<Integer> ignore0, Integer ignore1) {
        return f::applyAsInt;
    }

    public static LongToDoubleFunction adapt(LongUnaryOperator f, LongToDoubleFunction ignore0) {
        return f::applyAsLong;
    }

    public static LongToIntFunction adapt(LongUnaryOperator f, LongToIntFunction ignore0) {
        return v -> (int) (f.applyAsLong(v) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(LongUnaryOperator f, LongFunction<Double> ignore0, Double ignore1) {
        return v -> (double) f.applyAsLong(v);
    }

    public static LongFunction<Long> adapt(LongUnaryOperator f, LongFunction<Long> ignore0, Long ignore1) {
        return f::applyAsLong;
    }

    public static LongFunction<Integer> adapt(LongUnaryOperator f, LongFunction<Integer> ignore0, Integer ignore1) {
        return v -> (int) (f.applyAsLong(v) % Integer.MAX_VALUE);
    }

    public static LongUnaryOperator adapt(LongFunction<String> f, String i1, LongUnaryOperator i2) {
        return v -> Long.parseLong(f.apply(v));
    }

    public static LongToDoubleFunction adapt(LongFunction<String> f, String i1, LongToDoubleFunction i2) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static LongToIntFunction adapt(LongFunction<String> f, String i1, LongToIntFunction i2) {
        return v -> Integer.parseInt(f.apply(v));
    }

    public static LongUnaryOperator adapt(LongFunction<Double> f, Double i1, LongUnaryOperator i2) {
        return v -> (long) (f.apply(v) % Long.MAX_VALUE);
    }

    public static LongToDoubleFunction adapt(LongFunction<Double> f, Double i1, LongToDoubleFunction i2) {
        return f::apply;
    }

    public static LongToIntFunction adapt(LongFunction<Double> f, Double i1, LongToIntFunction i2) {
        return v -> (int) (f.apply(v) % Integer.MAX_VALUE);
    }

    public static LongUnaryOperator adapt(IntFunction<Integer> f, Integer i1, LongUnaryOperator i2) {
        return v -> (long) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToDoubleFunction adapt(IntFunction<Integer> f, Integer i1, LongToDoubleFunction i2) {
        return v -> (double) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToIntFunction adapt(IntFunction<Integer> f, Integer i1, LongToIntFunction i2) {
        return v -> (int) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongUnaryOperator adapt(IntFunction<Double> f, Double i1, LongUnaryOperator i2) {
        return v -> (long) ((f.apply((int) (v % Integer.MAX_VALUE))) % Long.MAX_VALUE);
    }

    public static LongToDoubleFunction adapt(IntFunction<Double> f, Double i1, LongToDoubleFunction i2) {
        return v -> (double) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToIntFunction adapt(IntFunction<Double> f, Double i1, LongToIntFunction i2) {
        return v -> (int) ((f.apply((int) (v % Integer.MAX_VALUE))) % Integer.MAX_VALUE);
    }

    public static LongUnaryOperator adapt(IntUnaryOperator f, LongUnaryOperator i1) {
        return v -> (long) f.applyAsInt((int) (v % Integer.MAX_VALUE));
    }

    public static LongToDoubleFunction adapt(IntUnaryOperator f, LongToDoubleFunction i1) {
        return v -> (double) (f.applyAsInt((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToIntFunction adapt(IntUnaryOperator f, LongToIntFunction i1) {
        return v -> f.applyAsInt((int) (v % Integer.MAX_VALUE));
    }

    public static LongUnaryOperator adapt(DoubleUnaryOperator f, LongUnaryOperator i1) {
        return v -> (long) (f.applyAsDouble(v) % Long.MAX_VALUE);
    }

    public static LongToDoubleFunction adapt(DoubleUnaryOperator f, LongToDoubleFunction i1) {
        return f::applyAsDouble;
    }

    public static LongToIntFunction adapt(DoubleUnaryOperator f, LongToIntFunction i1) {
        return v -> (int) (f.applyAsDouble(v) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(LongFunction<String> f, String i1, LongFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static LongFunction<Double> adapt(IntFunction<Integer> f, Integer i1, LongFunction i2, Double i3) {
        return v -> (double) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Double> adapt(IntFunction<Double> f, Double i1, LongFunction i2, Double i3) {
        return v -> f.apply((int) (v % Integer.MAX_VALUE));
    }

    public static LongFunction<Double> adapt(IntUnaryOperator f, LongFunction i1, Double i2) {
        return v -> (double) (f.applyAsInt((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Double> adapt(DoubleUnaryOperator f, LongFunction i1, Double i2) {
        return f::applyAsDouble;
    }

    public static LongFunction<Integer> adapt(LongFunction<String> f, String i1, LongFunction i2, Integer i3) {
        return v -> (int) Integer.parseInt((f.apply(v)));
    }

    public static LongFunction<Integer> adapt(LongFunction<Double> f, Double i1, LongFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static LongFunction<Integer> adapt(IntFunction<Integer> f, Integer i1, LongFunction i2, Integer i3) {
        return v -> (int) ((f.apply((int) (v % Integer.MAX_VALUE))));
    }

    public static LongFunction<Integer> adapt(IntFunction<Double> f, Double i1, LongFunction i2, Integer i3) {
        return v -> (int) ((f.apply((int) (v % Integer.MAX_VALUE))) % Integer.MAX_VALUE);
    }

    public static LongFunction<Integer> adapt(IntUnaryOperator f, LongFunction i1, Integer i2) {
        return v -> (int) (f.applyAsInt((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Integer> adapt(DoubleUnaryOperator f, LongFunction i1, Integer i2) {
        return v -> (int) ((f.applyAsDouble(v)) % Integer.MAX_VALUE);
    }

    public static IntUnaryOperator adapt(LongFunction<String> f, String i1, IntUnaryOperator i2) {
        return v -> Integer.parseInt(f.apply(v));
    }

    public static IntFunction<Long> adapt(LongFunction<String> f, String i1, IntFunction i2, Long i3) {
        return v -> Long.parseLong(f.apply(v));
    }

    public static IntFunction<Integer> adapt(LongFunction<String> f, String i1, IntFunction i2, Integer i3) {
        return v -> Integer.parseInt(f.apply(v));
    }

    public static IntFunction<Double> adapt(LongFunction<String> f, String i1, IntFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static DoubleUnaryOperator adapt(LongFunction<String> f, String i1, DoubleUnaryOperator i2) {
        return v -> Double.parseDouble(f.apply((long) (v % Long.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(LongFunction<Double> f, Double i1, IntUnaryOperator i2) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(LongFunction<Double> f, Double i1, IntFunction i2, Long i3) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static IntFunction<Integer> adapt(LongFunction<Double> f, Double i1, IntFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(LongFunction<Double> f, Double i1, IntFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static DoubleUnaryOperator adapt(LongFunction<Double> f, Double i1, DoubleUnaryOperator i2) {
        return v -> (double) (f.apply((long) (v % Long.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(LongToDoubleFunction f, IntUnaryOperator i1) {
        return v -> (int) ((f.applyAsDouble(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(LongToDoubleFunction f, IntFunction i1, Long i2) {
        return v -> (long) (f.applyAsDouble(v));
    }

    public static IntFunction<Integer> adapt(LongToDoubleFunction f, IntFunction i1, Integer i2) {
        return v -> (int) ((f.applyAsDouble(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(LongToDoubleFunction f, IntFunction i1, Double i2) {
        return v -> (double) f.applyAsDouble(v);
    }

    public static DoubleUnaryOperator adapt(LongToDoubleFunction f, DoubleUnaryOperator i1) {
        return v -> f.applyAsDouble((long) (v % Long.MAX_VALUE));
    }

    public static IntUnaryOperator adapt(LongToIntFunction f, IntUnaryOperator i1) {
        return f::applyAsInt;
    }

    public static IntFunction<Long> adapt(LongToIntFunction f, IntFunction i1, Long i2) {
        return v -> (long) (f.applyAsInt(v));
    }

    public static IntFunction<Integer> adapt(LongToIntFunction f, IntFunction i1, Integer i2) {
        return v -> (int) (f.applyAsInt(v));
    }

    public static IntFunction<Double> adapt(LongToIntFunction f, IntFunction i1, Double i2) {
        return v -> (double) (f.applyAsInt(v));
    }

    public static DoubleUnaryOperator adapt(LongToIntFunction f, DoubleUnaryOperator i1) {
        return v -> (double) (f.applyAsInt((long) (v % Long.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(LongUnaryOperator f, IntUnaryOperator i1) {
        return v -> (int) (f.applyAsLong(v));
    }

    public static IntFunction<Long> adapt(LongUnaryOperator f, IntFunction i1, Long i2) {
        return f::applyAsLong;
    }

    public static IntFunction<Integer> adapt(LongUnaryOperator f, IntFunction i1, Integer i2) {
        return v -> (int) ((f.applyAsLong(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(LongUnaryOperator f, IntFunction i1, Double i2) {
        return v -> (double) (f.applyAsLong(v));
    }

    public static DoubleUnaryOperator adapt(LongUnaryOperator f, DoubleUnaryOperator i1) {
        return v -> (double) (f.applyAsLong((long) (v % Long.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(IntFunction<Integer> f, Integer i1, IntUnaryOperator i2) {
        return f::apply;
    }

    public static IntFunction<Long> adapt(IntFunction<Integer> f, Integer i1, IntFunction i2, Long i3) {
        return v -> (long) (f.apply(v));
    }

    public static IntFunction<Double> adapt(IntFunction<Integer> f, Integer i1, IntFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static DoubleUnaryOperator adapt(IntFunction<Integer> f, Integer i1, DoubleUnaryOperator i2) {
        return v -> (double) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(IntFunction<Double> f, Double i1, IntUnaryOperator i2) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(IntFunction<Double> f, Double i1, IntFunction i2, Long i3) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static IntFunction<Integer> adapt(IntFunction<Double> f, Double i1, IntFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static DoubleUnaryOperator adapt(IntFunction<Double> f, Double i1, DoubleUnaryOperator i2) {
        return v -> (double) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static IntFunction<Long> adapt(IntUnaryOperator f, IntFunction i1, Long i2) {
        return v -> (long) (f.applyAsInt(v));
    }

    public static IntFunction<Integer> adapt(IntUnaryOperator f, IntFunction i1, Integer i2) {
        return f::applyAsInt;
    }

    public static IntFunction<Double> adapt(IntUnaryOperator f, IntFunction i1, Double i2) {
        return v -> (double) (f.applyAsInt(v));
    }

    public static DoubleUnaryOperator adapt(IntUnaryOperator f, DoubleUnaryOperator i1) {
        return v -> (double) (f.applyAsInt((int) (v % Integer.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(DoubleUnaryOperator f, IntUnaryOperator i1) {
        return v -> (int) ((f.applyAsDouble(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(DoubleUnaryOperator f, IntFunction i1, Long i2) {
        return v -> (long) ((f.applyAsDouble(v)) % Long.MAX_VALUE);
    }

    public static IntFunction<Integer> adapt(DoubleUnaryOperator f, IntFunction i1, Integer i2) {
        return v -> (int) ((f.applyAsDouble(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(DoubleUnaryOperator f, IntFunction i1, Double i2) {
        return v -> (double) (f.applyAsDouble(v));
    }


    // to DoubleFunction<Double>
    public static LongUnaryOperator adapt(DoubleFunction<Double> f, Double i1, LongUnaryOperator i2) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static LongToDoubleFunction adapt(DoubleFunction<Double> f, Double i1, LongToDoubleFunction i2) {
        return f::apply;
    }

    public static LongToIntFunction adapt(DoubleFunction<Double> f, Double i1, LongToIntFunction i2) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(DoubleFunction<Double> f, Double i1, LongFunction i2, Double i3) {
        return f::apply;
    }

    public static LongFunction<Integer> adapt(DoubleFunction<Double> f, Double i1, LongFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntUnaryOperator adapt(DoubleFunction<Double> f, Double i1, IntUnaryOperator i2) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(DoubleFunction<Double> f, Double i1, IntFunction i2, Long i3) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static IntFunction<Integer> adapt(DoubleFunction<Double> f, Double i1, IntFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(DoubleFunction<Double> f, Double i1, IntFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static DoubleUnaryOperator adapt(DoubleFunction<Double> f, Double i1, DoubleUnaryOperator i2) {
        return f::apply;
    }


    // LongFunction<Long>

    public static LongFunction<Long> adapt(LongFunction<String> f, String i1, LongFunction i2, Long i3) {
        return v -> Long.parseLong(f.apply(v));
    }

    public static LongFunction<Long> adapt(LongFunction<Double> f, Double i1, LongFunction i2, Long i3) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static LongFunction<Long> adapt(IntFunction<Integer> f, Integer i1, LongFunction i2, Long i3) {
        return v -> (long) (f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Long> adapt(IntFunction<Double> f, Double i1, LongFunction i2, Long i3) {
        return v -> (long) ((f.apply((int) (v % Integer.MAX_VALUE))) % Long.MAX_VALUE);
    }

    public static LongFunction<Long> adapt(IntUnaryOperator f, LongFunction i1, Long i2) {
        return v -> (long) (f.applyAsInt((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Long> adapt(DoubleUnaryOperator f, LongFunction i1, Long i2) {
        return v -> (long) ((f.applyAsDouble(v)) % Long.MAX_VALUE);
    }

    public static LongFunction<Long> adapt(DoubleFunction<Double> f, Double i1, LongFunction i2, Long i3) {
        return v -> (long) ((f.apply(v)) % Long.MAX_VALUE);
    }

    public static LongFunction<Object> adapt(LongUnaryOperator f, LongFunction i1, Object i2) {
        return f::applyAsLong;
    }

    public static LongUnaryOperator adapt(LongFunction<Long> f, Long i1, LongUnaryOperator i2) {
        return f::apply;
    }

    public static LongToDoubleFunction adapt(LongFunction<Long> f, Long i1, LongToDoubleFunction i2) {
        return f::apply;
    }

    public static LongToIntFunction adapt(LongFunction<Long> f, Long i1, LongToIntFunction i2) {
        return v -> (int) (f.apply(v) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(LongFunction<Long> f, Long i1, LongFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static LongFunction<Integer> adapt(LongFunction<Long> f, Long i1, LongFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntUnaryOperator adapt(LongFunction<Long> f, Long i1, IntUnaryOperator i2) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(LongFunction<Long> f, Long i1, IntFunction i2, Long i3) {
        return f::apply;
    }

    public static IntFunction<Integer> adapt(LongFunction<Long> f, Long i1, IntFunction i2, Integer i3) {
        return v -> (int) ((f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(LongFunction<Long> f, Long i1, IntFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static DoubleUnaryOperator adapt(LongFunction<Long> f, Long i1, DoubleUnaryOperator i2) {
        return v -> (double) (f.apply((long) (v % Long.MAX_VALUE)));
    }

    public static LongUnaryOperator adapt(LongFunction<Integer> f, Integer i1, LongUnaryOperator i2) {
        return f::apply;
    }

    public static LongToDoubleFunction adapt(LongFunction<Integer> f, Integer i1, LongToDoubleFunction i2) {
        return f::apply;
    }

    public static LongToIntFunction adapt(LongFunction<Integer> f, Integer i1, LongToIntFunction i2) {
        return f::apply;
    }

    public static LongFunction<Double> adapt(LongFunction<Integer> f, Integer i1, LongFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static IntUnaryOperator adapt(LongFunction<Integer> f, Integer i1, IntUnaryOperator i2) {
        return f::apply;
    }

    public static IntFunction<Long> adapt(LongFunction<Integer> f, Integer i1, IntFunction i2, Long i3) {
        return v -> (long) (f.apply(v));
    }

    public static IntFunction<Integer> adapt(LongFunction<Integer> f, Integer i1, IntFunction i2, Integer i3) {
        return f::apply;
    }

    public static IntFunction<Double> adapt(LongFunction<Integer> f, Integer i1, IntFunction i2, Double i3) {
        return v -> (double) (f.apply(v));
    }

    public static DoubleUnaryOperator adapt(LongFunction<Integer> f, Integer i1, DoubleUnaryOperator i2) {
        return v -> (double) (f.apply((long) (v % Long.MAX_VALUE)));
    }

    public static LongFunction<Long> adapt(LongFunction<Integer> f, Integer i1, LongFunction i2, Long i3) {
        return v -> (long) (f.apply(v));
    }

    // to IntFunction<String>

    public static LongUnaryOperator adapt(IntFunction<String> f, String i1, LongUnaryOperator i2) {
        return v -> Long.parseLong(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToDoubleFunction adapt(IntFunction<String> f, String i1, LongToDoubleFunction i2) {
        return v -> Double.parseDouble(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongToIntFunction adapt(IntFunction<String> f, String i1, LongToIntFunction i2) {
        return v -> Integer.parseInt(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Double> adapt(IntFunction<String> f, String i1, LongFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static LongFunction<Integer> adapt(IntFunction<String> f, String i1, LongFunction i2, Integer i3) {
        return v -> Integer.parseInt(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    public static IntUnaryOperator adapt(IntFunction<String> f, String i1, IntUnaryOperator i2) {
        return v -> Integer.parseInt(f.apply(v));
    }

    public static IntFunction<Long> adapt(IntFunction<String> f, String i1, IntFunction i2, Long i3) {
        return v -> Long.parseLong(f.apply(v));
    }

    public static IntFunction<Integer> adapt(IntFunction<String> f, String i1, IntFunction i2, Integer i3) {
        return v -> Integer.parseInt(f.apply(v));
    }

    public static IntFunction<Double> adapt(IntFunction<String> f, String i1, IntFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static DoubleUnaryOperator adapt(IntFunction<String> f, String i1, DoubleUnaryOperator i2) {
        return v -> Double.parseDouble(f.apply((int) v % Integer.MAX_VALUE));
    }

    public static LongFunction<Long> adapt(IntFunction<String> f, String i1, LongFunction i2, Long i3) {
        return v -> Long.parseLong(f.apply((int) (v % Integer.MAX_VALUE)));
    }

    // to DoubleFunction<String>

    public static LongUnaryOperator adapt(DoubleFunction<String> f, String i1, LongUnaryOperator i2) {
        return v -> (long) (Double.parseDouble(f.apply(v)) % Long.MAX_VALUE);
    }

    public static LongToDoubleFunction adapt(DoubleFunction<String> f, String i1, LongToDoubleFunction i2) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static LongToIntFunction adapt(DoubleFunction<String> f, String i1, LongToIntFunction i2) {
        return v -> (int) (Double.parseDouble(f.apply(v)) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(DoubleFunction<String> f, String i1, LongFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static LongFunction<Integer> adapt(DoubleFunction<String> f, String i1, LongFunction i2, Integer i3) {
        return v -> (int) (Double.parseDouble(f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntUnaryOperator adapt(DoubleFunction<String> f, String i1, IntUnaryOperator i2) {
        return v -> (int) (Double.parseDouble(f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(DoubleFunction<String> f, String i1, IntFunction i2, Long i3) {
        return v -> (long) (Double.parseDouble(f.apply(v)) % Long.MAX_VALUE);
    }

    public static IntFunction<Integer> adapt(DoubleFunction<String> f, String i1, IntFunction i2, Integer i3) {
        return v -> (int) (Double.parseDouble(f.apply(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(DoubleFunction<String> f, String i1, IntFunction i2, Double i3) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static DoubleUnaryOperator adapt(DoubleFunction<String> f, String i1, DoubleUnaryOperator i2) {
        return v -> Double.parseDouble(f.apply(v));
    }

    public static LongFunction<Long> adapt(DoubleFunction<String> f, String i1, LongFunction i2, Long i3) {
        return v -> (long) (Double.parseDouble(f.apply(v)) % Long.MAX_VALUE);
    }

    public static LongFunction<Object> adapt(LongToDoubleFunction f, LongFunction i1, Object i2) {
        return f::applyAsDouble;
    }

    public static LongFunction<Object> adapt(LongToIntFunction f, LongFunction i1, Object i2) {
        return f::applyAsInt;
    }

    public static LongFunction<Object> adapt(DoubleUnaryOperator f, LongFunction i1, Object i2) {
        return f::applyAsDouble;
    }

    public static LongFunction<Object> adapt(IntUnaryOperator f, LongFunction i1, Object i2) {
        return v -> f.applyAsInt((int) (v % Integer.MAX_VALUE));
    }

    public static LongFunction<Object> adapt(IntFunction<Integer> f, Integer i1, LongFunction i2, Object i3) {
        return v -> f.apply((int) (v % Integer.MAX_VALUE));
    }

    public static LongFunction<Object> adapt(IntFunction<Double> f, Double i1, LongFunction i2, Object i3) {
        return v -> f.apply((int) (v % Integer.MAX_VALUE));
    }

    public static LongFunction<Object> adapt(IntFunction<String> f, String i1, LongFunction i2, Object i3) {
        return v -> f.apply((int) (v % Integer.MAX_VALUE));
    }

    public static LongFunction<Object> adapt(DoubleFunction<Double> f, Double i1, LongFunction i2, Object i3) {
        return f::apply;
    }

    public static LongFunction<Object> adapt(DoubleFunction<String> f, String i1, LongFunction i2, Object i3) {
        return f::apply;
    }


    public static LongFunction<Object> adapt(DoubleToLongFunction f, LongFunction i1, Object i2) {
        return f::applyAsLong;
    }

    public static LongUnaryOperator adapt(DoubleToLongFunction f, LongUnaryOperator i1) {
        return v -> f.applyAsLong(v);
    }

    public static LongToDoubleFunction adapt(DoubleToLongFunction f, LongToDoubleFunction i1) {
        return v -> (double) f.applyAsLong(v);
    }

    public static LongToIntFunction adapt(DoubleToLongFunction f, LongToIntFunction i1) {
        return v -> (int) ((f.applyAsLong(v)) % Integer.MAX_VALUE);
    }

    public static LongFunction<Double> adapt(DoubleToLongFunction f, LongFunction i1, Double i2) {
        return v -> (double) f.applyAsLong(v);
    }

    public static LongFunction<Integer> adapt(DoubleToLongFunction f, LongFunction i1, Integer i2) {
        return v -> (int) ((f.applyAsLong(v)) % Integer.MAX_VALUE);
    }

    public static IntUnaryOperator adapt(DoubleToLongFunction f, IntUnaryOperator i1) {
        return v -> (int) ((f.applyAsLong(v)) % Integer.MAX_VALUE);
    }

    public static IntFunction<Long> adapt(DoubleToLongFunction f, IntFunction i1, Long i2) {
        return v -> (long) f.applyAsLong(v);
    }

    public static IntFunction<Integer> adapt(DoubleToLongFunction f, IntFunction i1, Integer i2) {
        return v->(int) ((int)f.applyAsLong(v)%Integer.MAX_VALUE);
    }

    public static IntFunction<Double> adapt(DoubleToLongFunction f, IntFunction i1, Double i2) {
        return v->(double) (f.applyAsLong(v));
    }

    public static DoubleUnaryOperator adapt(DoubleToLongFunction f, DoubleUnaryOperator i1) {
        return v->(double)(f.applyAsLong(v));
    }

    public static LongFunction<Long> adapt(DoubleToLongFunction f, LongFunction i1, Long i2) {
        return v->(long) (f.applyAsLong(v));
    }

    public static LongFunction<Object> adapt(Function<Object,Class> f, Object i1, Class i2, LongFunction i3, Object i4) {
        return f::apply;
    }

    public static LongFunction<CharBuffer> adapt(LongFunction<String> f, String i1, LongFunction i2, CharBuffer i3) {
        return l -> CharBuffer.wrap(f.apply(l));
    }

    public static LongFunction<String> adapt(LongFunction<CharBuffer> f, CharBuffer i1, LongFunction i2, String i3) {
        return l -> f.apply(l).toString();
    }

    public static LongToIntFunction adapt(IntToDoubleFunction f, LongToIntFunction i1) {
        return l -> (int) f.applyAsDouble(((int)l));
    }

    public static LongToIntFunction adapt(IntToLongFunction f, LongToIntFunction i1) {
        return l -> (int)(f.applyAsLong((int) l));
    }


}
