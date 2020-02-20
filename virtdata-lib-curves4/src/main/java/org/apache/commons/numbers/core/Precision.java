/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.numbers.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilities for comparing numbers.
 */
public class Precision {
    /**
     * <p>
     * Largest double-precision floating-point number such that
     * {@code 1 + EPSILON} is numerically equal to 1. This value is an upper
     * bound on the relative error due to rounding real numbers to double
     * precision floating-point numbers.
     * </p>
     * <p>
     * In IEEE 754 arithmetic, this is 2<sup>-53</sup>.
     * </p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/Machine_epsilon">Machine epsilon</a>
     */
    public static final double EPSILON;

    /**
     * Safe minimum, such that {@code 1 / SAFE_MIN} does not overflow.
     * In IEEE 754 arithmetic, this is also the smallest normalized
     * number 2<sup>-1022</sup>.
     */
    public static final double SAFE_MIN;

    /** Exponent offset in IEEE754 representation. */
    private static final long EXPONENT_OFFSET = 1023l;

    /** Offset to order signed double numbers lexicographically. */
    private static final long SGN_MASK = 0x8000000000000000L;
    /** Offset to order signed double numbers lexicographically. */
    private static final int SGN_MASK_FLOAT = 0x80000000;
    /** Positive zero. */
    private static final double POSITIVE_ZERO = 0d;
    /** Positive zero bits. */
    private static final long POSITIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(+0.0);
    /** Negative zero bits. */
    private static final long NEGATIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(-0.0);
    /** Positive zero bits. */
    private static final int POSITIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(+0.0f);
    /** Negative zero bits. */
    private static final int NEGATIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(-0.0f);

    static {
        /*
         *  This was previously expressed as = 0x1.0p-53;
         *  However, OpenJDK (Sparc Solaris) cannot handle such small
         *  constants: MATH-721
         */
        EPSILON = Double.longBitsToDouble((EXPONENT_OFFSET - 53l) << 52);

        /*
         * This was previously expressed as = 0x1.0p-1022;
         * However, OpenJDK (Sparc Solaris) cannot handle such small
         * constants: MATH-721
         */
        SAFE_MIN = Double.longBitsToDouble((EXPONENT_OFFSET - 1022l) << 52);
    }

    /**
     * Private constructor.
     */
    private Precision() {}

    /**
     * Compares two numbers given some amount of allowed error.
     * The returned value is
     * <ul>
     *  <li>
     *   0 if  {@link #equals(double,double,double) equals(x, y, eps)},
     *  </li>
     *  <li>
     *   negative if !{@link #equals(double,double,double) equals(x, y, eps)} and {@code x < y},
     *  </li>
     *  <li>
     *   positive if !{@link #equals(double,double,double) equals(x, y, eps)} and {@code x > y} or
     *   either argument is {@code NaN}.
     *  </li>
     * </ul>
     *
     * @param x First value.
     * @param y Second value.
     * @param eps Allowed error when checking for equality.
     * @return 0 if the value are considered equal, -1 if the first is smaller than
     * the second, 1 is the first is larger than the second.
     */
    public static int compareTo(double x, double y, double eps) {
        if (equals(x, y, eps)) {
            return 0;
        } else if (x < y) {
            return -1;
        }
        return 1;
    }

    /**
     * Compares two numbers given some amount of allowed error.
     * Two float numbers are considered equal if there are {@code (maxUlps - 1)}
     * (or fewer) floating point numbers between them, i.e. two adjacent floating
     * point numbers are considered equal.
     * Adapted from
     * <a href="http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
     * Bruce Dawson</a>. Returns {@code false} if either of the arguments is NaN.
     * The returned value is
     * <ul>
     *  <li>
     *   zero if {@link #equals(double,double,int) equals(x, y, maxUlps)},
     *  </li>
     *  <li>
     *   negative if !{@link #equals(double,double,int) equals(x, y, maxUlps)} and {@code x < y},
     *  </li>
     *  <li>
     *   positive if !{@link #equals(double,double,int) equals(x, y, maxUlps)} and {@code x > y}
     *       or either argument is {@code NaN}.
     *  </li>
     * </ul>
     *
     * @param x First value.
     * @param y Second value.
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between {@code x} and {@code y}.
     * @return 0 if the value are considered equal, -1 if the first is smaller than
     * the second, 1 is the first is larger than the second.
     */
    public static int compareTo(final double x, final double y, final int maxUlps) {
        if (equals(x, y, maxUlps)) {
            return 0;
        } else if (x < y) {
            return -1;
        }
        return 1;
    }

    /**
     * Returns true iff they are equal as defined by
     * {@link #equals(float,float,int) equals(x, y, 1)}.
     *
     * @param x first value
     * @param y second value
     * @return {@code true} if the values are equal.
     */
    public static boolean equals(float x, float y) {
        return equals(x, y, 1);
    }

    /**
     * Returns true if both arguments are NaN or they are
     * equal as defined by {@link #equals(float,float) equals(x, y, 1)}.
     *
     * @param x first value
     * @param y second value
     * @return {@code true} if the values are equal or both are NaN.
     */
    public static boolean equalsIncludingNaN(float x, float y) {
        return (x != x || y != y) ? !(x != x ^ y != y) : equals(x, y, 1);
    }

    /**
     * Returns true if the arguments are equal or within the range of allowed
     * error (inclusive).  Returns {@code false} if either of the arguments
     * is NaN.
     *
     * @param x first value
     * @param y second value
     * @param eps the amount of absolute error to allow.
     * @return {@code true} if the values are equal or within range of each other.
     */
    public static boolean equals(float x, float y, float eps) {
        return equals(x, y, 1) || Math.abs(y - x) <= eps;
    }

    /**
     * Returns true if the arguments are both NaN, are equal, or are within the range
     * of allowed error (inclusive).
     *
     * @param x first value
     * @param y second value
     * @param eps the amount of absolute error to allow.
     * @return {@code true} if the values are equal or within range of each other,
     * or both are NaN.
     */
    public static boolean equalsIncludingNaN(float x, float y, float eps) {
        return equalsIncludingNaN(x, y) || (Math.abs(y - x) <= eps);
    }

    /**
     * Returns true if the arguments are equal or within the range of allowed
     * error (inclusive).
     * Two float numbers are considered equal if there are {@code (maxUlps - 1)}
     * (or fewer) floating point numbers between them, i.e. two adjacent floating
     * point numbers are considered equal.
     * Adapted from <a
     * href="http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
     * Bruce Dawson</a>.  Returns {@code false} if either of the arguments is NaN.
     *
     * @param x first value
     * @param y second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between {@code x} and {@code y}.
     * @return {@code true} if there are fewer than {@code maxUlps} floating
     * point values between {@code x} and {@code y}.
     */
    public static boolean equals(final float x, final float y, final int maxUlps) {

        final int xInt = Float.floatToRawIntBits(x);
        final int yInt = Float.floatToRawIntBits(y);

        final boolean isEqual;
        if (((xInt ^ yInt) & SGN_MASK_FLOAT) == 0) {
            // number have same sign, there is no risk of overflow
            isEqual = Math.abs(xInt - yInt) <= maxUlps;
        } else {
            // number have opposite signs, take care of overflow
            final int deltaPlus;
            final int deltaMinus;
            if (xInt < yInt) {
                deltaPlus  = yInt - POSITIVE_ZERO_FLOAT_BITS;
                deltaMinus = xInt - NEGATIVE_ZERO_FLOAT_BITS;
            } else {
                deltaPlus  = xInt - POSITIVE_ZERO_FLOAT_BITS;
                deltaMinus = yInt - NEGATIVE_ZERO_FLOAT_BITS;
            }

            if (deltaPlus > maxUlps) {
                isEqual = false;
            } else {
                isEqual = deltaMinus <= (maxUlps - deltaPlus);
            }

        }

        return isEqual && !Float.isNaN(x) && !Float.isNaN(y);

    }

    /**
     * Returns true if the arguments are both NaN or if they are equal as defined
     * by {@link #equals(float,float,int) equals(x, y, maxUlps)}.
     *
     * @param x first value
     * @param y second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between {@code x} and {@code y}.
     * @return {@code true} if both arguments are NaN or if there are less than
     * {@code maxUlps} floating point values between {@code x} and {@code y}.
     */
    public static boolean equalsIncludingNaN(float x, float y, int maxUlps) {
        return (x != x || y != y) ? !(x != x ^ y != y) : equals(x, y, maxUlps);
    }

    /**
     * Returns true iff they are equal as defined by
     * {@link #equals(double,double,int) equals(x, y, 1)}.
     *
     * @param x first value
     * @param y second value
     * @return {@code true} if the values are equal.
     */
    public static boolean equals(double x, double y) {
        return equals(x, y, 1);
    }

    /**
     * Returns true if the arguments are both NaN or they are
     * equal as defined by {@link #equals(double,double) equals(x, y, 1)}.
     *
     * @param x first value
     * @param y second value
     * @return {@code true} if the values are equal or both are NaN.
     */
    public static boolean equalsIncludingNaN(double x, double y) {
        return (x != x || y != y) ? !(x != x ^ y != y) : equals(x, y, 1);
    }

    /**
     * Returns {@code true} if there is no double value strictly between the
     * arguments or the difference between them is within the range of allowed
     * error (inclusive). Returns {@code false} if either of the arguments
     * is NaN.
     *
     * @param x First value.
     * @param y Second value.
     * @param eps Amount of allowed absolute error.
     * @return {@code true} if the values are two adjacent floating point
     * numbers or they are within range of each other.
     */
    public static boolean equals(double x, double y, double eps) {
        return equals(x, y, 1) || Math.abs(y - x) <= eps;
    }

    /**
     * Returns {@code true} if there is no double value strictly between the
     * arguments or the relative difference between them is less than or equal
     * to the given tolerance. Returns {@code false} if either of the arguments
     * is NaN.
     *
     * @param x First value.
     * @param y Second value.
     * @param eps Amount of allowed relative error.
     * @return {@code true} if the values are two adjacent floating point
     * numbers or they are within range of each other.
     */
    public static boolean equalsWithRelativeTolerance(double x, double y, double eps) {
        if (equals(x, y, 1)) {
            return true;
        }

        final double absoluteMax = Math.max(Math.abs(x), Math.abs(y));
        final double relativeDifference = Math.abs((x - y) / absoluteMax);

        return relativeDifference <= eps;
    }

    /**
     * Returns true if the arguments are both NaN, are equal or are within the range
     * of allowed error (inclusive).
     *
     * @param x first value
     * @param y second value
     * @param eps the amount of absolute error to allow.
     * @return {@code true} if the values are equal or within range of each other,
     * or both are NaN.
     */
    public static boolean equalsIncludingNaN(double x, double y, double eps) {
        return equalsIncludingNaN(x, y) || (Math.abs(y - x) <= eps);
    }

    /**
     * Returns true if the arguments are equal or within the range of allowed
     * error (inclusive).
     * <p>
     * Two float numbers are considered equal if there are {@code (maxUlps - 1)}
     * (or fewer) floating point numbers between them, i.e. two adjacent
     * floating point numbers are considered equal.
     * </p>
     * <p>
     * Adapted from <a
     * href="http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
     * Bruce Dawson</a>. Returns {@code false} if either of the arguments is NaN.
     * </p>
     *
     * @param x first value
     * @param y second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between {@code x} and {@code y}.
     * @return {@code true} if there are fewer than {@code maxUlps} floating
     * point values between {@code x} and {@code y}.
     */
    public static boolean equals(final double x, final double y, final int maxUlps) {

        final long xInt = Double.doubleToRawLongBits(x);
        final long yInt = Double.doubleToRawLongBits(y);

        final boolean isEqual;
        if (((xInt ^ yInt) & SGN_MASK) == 0l) {
            // number have same sign, there is no risk of overflow
            isEqual = Math.abs(xInt - yInt) <= maxUlps;
        } else {
            // number have opposite signs, take care of overflow
            final long deltaPlus;
            final long deltaMinus;
            if (xInt < yInt) {
                deltaPlus  = yInt - POSITIVE_ZERO_DOUBLE_BITS;
                deltaMinus = xInt - NEGATIVE_ZERO_DOUBLE_BITS;
            } else {
                deltaPlus  = xInt - POSITIVE_ZERO_DOUBLE_BITS;
                deltaMinus = yInt - NEGATIVE_ZERO_DOUBLE_BITS;
            }

            if (deltaPlus > maxUlps) {
                isEqual = false;
            } else {
                isEqual = deltaMinus <= (maxUlps - deltaPlus);
            }

        }

        return isEqual && !Double.isNaN(x) && !Double.isNaN(y);

    }

    /**
     * Returns true if both arguments are NaN or if they are equal as defined
     * by {@link #equals(double,double,int) equals(x, y, maxUlps)}.
     *
     * @param x first value
     * @param y second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between {@code x} and {@code y}.
     * @return {@code true} if both arguments are NaN or if there are less than
     * {@code maxUlps} floating point values between {@code x} and {@code y}.
     */
    public static boolean equalsIncludingNaN(double x, double y, int maxUlps) {
        return (x != x || y != y) ? !(x != x ^ y != y) : equals(x, y, maxUlps);
    }

    /**
     * Rounds the given value to the specified number of decimal places.
     * The value is rounded using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x Value to round.
     * @param scale Number of digits to the right of the decimal point.
     * @return the rounded value.
     */
    public static double round(double x, int scale) {
        return round(x, scale, RoundingMode.HALF_UP);
    }

    /**
     * Rounds the given value to the specified number of decimal places.
     * The value is rounded using the given method which is any method defined
     * in {@link BigDecimal}.
     * If {@code x} is infinite or {@code NaN}, then the value of {@code x} is
     * returned unchanged, regardless of the other parameters.
     *
     * @param x Value to round.
     * @param scale Number of digits to the right of the decimal point.
     * @param roundingMethod Rounding method as defined in {@link BigDecimal}.
     * @return the rounded value.
     * @throws ArithmeticException if {@code roundingMethod} is
     * {@link RoundingMode#UNNECESSARY} and the specified scaling operation
     * would require rounding.
     */
    public static double round(double x,
                               int scale,
                               RoundingMode roundingMethod) {
        try {
            final double rounded = (new BigDecimal(Double.toString(x))
                   .setScale(scale, roundingMethod))
                   .doubleValue();
            // MATH-1089: negative values rounded to zero should result in negative zero
            return rounded == POSITIVE_ZERO ? POSITIVE_ZERO * x : rounded;
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        }
    }

    /**
     * Computes a number {@code delta} close to {@code originalDelta} with
     * the property that <pre><code>
     *   x + delta - x
     * </code></pre>
     * is exactly machine-representable.
     * This is useful when computing numerical derivatives, in order to reduce
     * roundoff errors.
     *
     * @param x Value.
     * @param originalDelta Offset value.
     * @return a number {@code delta} so that {@code x + delta} and {@code x}
     * differ by a representable floating number.
     */
    public static double representableDelta(double x,
                                            double originalDelta) {
        return x + originalDelta - x;
    }
}
