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

package org.apache.commons.numbers.complex;

import org.apache.commons.numbers.core.Precision;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a Complex number, i.e. a number which has both a
 * real and imaginary part.
 * <p>
 * Implementations of arithmetic operations handle {@code NaN} and
 * infinite values according to the rules for {@link Double}, i.e.
 * {@link #equals} is an equivalence relation for all instances that have
 * a {@code NaN} in either real or imaginary part, e.g. the following are
 * considered equal:
 * <ul>
 *  <li>{@code 1 + NaNi}</li>
 *  <li>{@code NaN + i}</li>
 *  <li>{@code NaN + NaNi}</li>
 * </ul><p>
 * Note that this contradicts the IEEE-754 standard for floating
 * point numbers (according to which the test {@code x == x} must fail if
 * {@code x} is {@code NaN}). The method
 * {@link Precision#equals(double,double,int)
 * equals for primitive double} in class {@code Precision} conforms with
 * IEEE-754 while this class conforms with the standard behavior for Java
 * object types.</p>
 *
 */
public final class Complex implements Serializable  {
    /** The square root of -1, a.k.a. "i". */
    public static final Complex I = new Complex(0, 1);
    /** A complex number representing "+INF + INF i" */
    public static final Complex INF = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    /** A complex number representing one. */
    public static final Complex ONE = new Complex(1, 0);
    /** A complex number representing zero. */
    public static final Complex ZERO = new Complex(0, 0);
    /** A complex number representing "NaN + NaN i" */
    private static final Complex NAN = new Complex(Double.NaN, Double.NaN);

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180201L;

    /** {@link #toString() String representation}. */
    private static final String FORMAT_START = "(";
    /** {@link #toString() String representation}. */
    private static final String FORMAT_END = ")";
    /** {@link #toString() String representation}. */
    private static final String FORMAT_SEP = ",";

    /** The imaginary part. */
    private final double imaginary;
    /** The real part. */
    private final double real;

    /**
     * Private default constructor.
     *
     * @param real Real part.
     * @param imaginary Imaginary part.
     */
    private Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
    * Create a complex number given the real and imaginary parts.
    *
    * @param real Real part.
    * @param imaginary Imaginary part.
    * @return {@code Complex} object
    */
    public static Complex ofCartesian(double real, double imaginary) {
        return new Complex(real, imaginary);
    }

    /**
    * Create a complex number given the real part.
    *
    * @param real Real part.
    * @return {@code Complex} object
    */
    public static Complex ofReal(double real) {
        return new Complex(real, 0);
    }

     /**
     * Creates a Complex from its polar representation.
     *
     * If {@code r} is infinite and {@code theta} is finite, infinite or NaN
     * values may be returned in parts of the result, following the rules for
     * double arithmetic.
     *
     * <pre>
     * Examples:
     * {@code
     * polar2Complex(INFINITY, \(\pi\)) = INFINITY + INFINITY i
     * polar2Complex(INFINITY, 0) = INFINITY + NaN i
     * polar2Complex(INFINITY, \(-\frac{\pi}{4}\)) = INFINITY - INFINITY i
     * polar2Complex(INFINITY, \(5\frac{\pi}{4}\)) = -INFINITY - INFINITY i }
     * </pre>
     *
     * @param r the modulus of the complex number to create
     * @param theta the argument of the complex number to create
     * @return {@code Complex}
     */
    public static Complex ofPolar(double r, double theta) {
        checkNotNegative(r);
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * For a real constructor argument x, returns a new Complex object c
     * where {@code c = cos(x) + i sin (x)}
     *
     * @param x {@code double} to build the cis number
     * @return {@code Complex}
     */
    public static Complex ofCis(double x) {
        return new Complex(Math.cos(x), Math.sin(x));
    }

    /**
     * Parses a string that would be produced by {@link #toString()}
     * and instantiates the corresponding object.
     *
     * @param s String representation.
     * @return an instance.
     * @throws IllegalArgumentException if the string does not
     * conform to the specification.
     */
    public static Complex parse(String s) {
        final int len = s.length();
        final int startParen = s.indexOf(FORMAT_START);
        if (startParen != 0) {
            throw new ComplexParsingException("Expected start string: " + FORMAT_START);
        }
        final int endParen = s.indexOf(FORMAT_END);
        if (endParen != len - 1) {
            throw new ComplexParsingException("Expected end string: " + FORMAT_END);
        }
        final String[] elements = s.substring(1, s.length() - 1).split(FORMAT_SEP);
        if (elements.length != 2) {
            throw new ComplexParsingException("Incorrect number of parts: Expected 2 but was " +
                                              elements.length +
                                              " (separator is '" + FORMAT_SEP + "')");
        }

        final double re;
        try {
            re = Double.parseDouble(elements[0]);
        } catch (NumberFormatException ex) {
            throw new ComplexParsingException("Could not parse real part" + elements[0]);
        }
        final double im;
        try {
            im = Double.parseDouble(elements[1]);
        } catch (NumberFormatException ex) {
            throw new ComplexParsingException("Could not parse imaginary part" + elements[1]);
        }

        return ofCartesian(re, im);
    }

    /**
     * Returns true if either real or imaginary component of the Complex
     * is NaN
     *
     * @return {@code boolean}
     */
    public boolean isNaN() {
        if (Double.isNaN(real) ||
            Double.isNaN(imaginary)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if either real or imaginary component of the Complex
     * is Infinite
     *
     * @return {@code boolean}
     */
    public boolean isInfinite() {
        if (Double.isInfinite(real) ||
            Double.isInfinite(imaginary)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns projection of this complex number onto the Riemann sphere,
     * i.e. all infinities (including those with an NaN component)
     * project onto real infinity, as described in the
     * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/functions/cproj.html">
     * IEEE and ISO C standards</a>.
     * <p>
     *
     *
     * @return {@code Complex} projected onto the Riemann sphere.
     */
    public Complex proj() {
        if (Double.isInfinite(real) ||
            Double.isInfinite(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else {
            return this;
        }
    }

     /**
     * Return the absolute value of this complex number.
     * This code follows the <a href="http://www.iso-9899.info/wiki/The_Standard">ISO C Standard</a>, Annex G,
     * in calculating the returned value (i.e. the hypot(x,y) method)
     * and in handling of NaNs.
     *
     * @return the absolute value.
     */
    public double abs() {
        if (Math.abs(real) < Math.abs(imaginary)) {
            final double q = real / imaginary;
            return Math.abs(imaginary) * Math.sqrt(1 + q * q);
        } else {
            if (real == 0) {
                return Math.abs(imaginary);
            }
            final double q = imaginary / real;
            return Math.abs(real) * Math.sqrt(1 + q * q);
        }
    }

    /**
     * Returns a {@code Complex} whose value is
     * {@code (this + addend)}.
     * Uses the definitional formula
     * <p>
     *   {@code (a + bi) + (c + di) = (a+c) + (b+d)i}
     * </p>
     *
     * @param  addend Value to be added to this {@code Complex}.
     * @return {@code this + addend}.
     */
    public Complex add(Complex addend) {
        return new Complex(real + addend.real,
                           imaginary + addend.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this + addend)},
     * with {@code addend} interpreted as a real number.
     *
     * @param addend Value to be added to this {@code Complex}.
     * @return {@code this + addend}.
     * @see #add(Complex)
     */
    public Complex add(double addend) {
        return new Complex(real + addend, imaginary);
    }

     /**
     * Returns the conjugate of this complex number.
     * The conjugate of {@code a + bi} is {@code a - bi}.
     *
     * @return the conjugate of this complex object.
     */
    public Complex conjugate() {
        return new Complex(real, -imaginary);
    }

     /**
     * Returns the conjugate of this complex number.
     * C++11 grammar.
     * @return the conjugate of this complex object.
     */
    public Complex conj() {
        return conjugate();
    }


    /**
     * Returns a {@code Complex} whose value is
     * {@code (this / divisor)}.
     * Implements the definitional formula
     * <pre>
     *  <code>
     *    a + bi          ac + bd + (bc - ad)i
     *    ----------- = -------------------------
     *    c + di         c<sup>2</sup> + d<sup>2</sup>
     *  </code>
     * </pre>
     *
     *
     * Recalculates to recover infinities as specified in C.99
     * standard G.5.1. Method is fully in accordance with
     * C++11 standards for complex numbers.
     *
     * @param divisor Value by which this {@code Complex} is to be divided.
     * @return {@code this / divisor}.
     */
    public Complex divide(Complex divisor) {

        double a = real;
        double b = imaginary;
        double c = divisor.getReal();
        double d = divisor.getImaginary();
        int ilogbw = 0;
        double logbw = Math.log(Math.max(Math.abs(c), Math.abs(d))) / Math.log(2);
        if (!Double.isInfinite(logbw)) {
            ilogbw = (int)logbw;
            c = Math.scalb(c, -ilogbw);
            d = Math.scalb(d, -ilogbw);
        }
        double denom = c*c + d*d;
        double x = Math.scalb( (a*c + b*d) / denom, -ilogbw);
        double y = Math.scalb( (b*c - a*d) / denom, -ilogbw);
        if (Double.isNaN(x) && Double.isNaN(y)) {
            if ((denom == 0.0) &&
                    (!Double.isNaN(a) || !Double.isNaN(b))) {
                x = Math.copySign(Double.POSITIVE_INFINITY, c) * a;
                y = Math.copySign(Double.POSITIVE_INFINITY, c) * b;
            } else if ((Double.isInfinite(a) && Double.isInfinite(b)) &&
                    !Double.isInfinite(c) && !Double.isInfinite(d)) {
                a = Math.copySign(Double.isInfinite(a) ? 1.0 : 0.0, a);
                b = Math.copySign(Double.isInfinite(b) ? 1.0 : 0.0, b);
                x = Double.POSITIVE_INFINITY * (a*c + b*d);
                y = Double.POSITIVE_INFINITY * (b*c - a*d);
            } else if (Double.isInfinite(logbw) &&
                    !Double.isInfinite(a) && !Double.isInfinite(b)) {
                c = Math.copySign(Double.isInfinite(c) ? 1.0 : 0.0, c);
                d = Math.copySign(Double.isInfinite(d) ? 1.0 : 0.0, d);
                x = 0.0 * (a*c + b*d);
                y = 0.0 * (b*c - a*d);
            }
        }
        return new Complex(x, y);


    }

    /**
     * Returns a {@code Complex} whose value is {@code (this / divisor)},
     * with {@code divisor} interpreted as a real number.
     *
     * @param  divisor Value by which this {@code Complex} is to be divided.
     * @return {@code this / divisor}.
     * @see #divide(Complex)
     */
    public Complex divide(double divisor) {
        return divide(new Complex(divisor, 0));
    }

    /**
     * Returns the multiplicative inverse of this instance.
     *
     * @return {@code 1 / this}.
     * @see #divide(Complex)
     */
    public Complex reciprocal() {
        if (Math.abs(real) < Math.abs(imaginary)) {
            final double q = real / imaginary;
            final double scale = 1. / (real * q + imaginary);
            double scaleQ = 0;
            if (q != 0 &&
                scale != 0) {
                scaleQ = scale * q;
            }
            return new Complex(scaleQ, -scale);
        } else {
            final double q = imaginary / real;
            final double scale = 1. / (imaginary * q + real);
            double scaleQ = 0;
            if (q != 0 &&
                scale != 0) {
                scaleQ = scale * q;
            }
            return new Complex(scale, -scaleQ);
        }
    }

    /**
     * Test for equality with another object.
     * If both the real and imaginary parts of two complex numbers
     * are exactly the same, and neither is {@code Double.NaN}, the two
     * Complex objects are considered to be equal.
     * The behavior is the same as for JDK's {@link Double#equals(Object)
     * Double}:
     * <ul>
     *  <li>All {@code NaN} values are considered to be equal,
     *   i.e, if either (or both) real and imaginary parts of the complex
     *   number are equal to {@code Double.NaN}, the complex number is equal
     *   to {@code NaN}.
     *  </li>
     *  <li>
     *   Instances constructed with different representations of zero (i.e.
     *   either "0" or "-0") are <em>not</em> considered to be equal.
     *  </li>
     * </ul>
     *
     * @param other Object to test for equality with this instance.
     * @return {@code true} if the objects are equal, {@code false} if object
     * is {@code null}, not an instance of {@code Complex}, or not equal to
     * this instance.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Complex){
            Complex c = (Complex) other;
            return equals(real, c.real) &&
                equals(imaginary, c.imaginary);
        }
        return false;
    }

    /**
     * Test for the floating-point equality between Complex objects.
     * It returns {@code true} if both arguments are equal or within the
     * range of allowed error (inclusive).
     *
     * @param x First value (cannot be {@code null}).
     * @param y Second value (cannot be {@code null}).
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     * values between the real (resp. imaginary) parts of {@code x} and
     * {@code y}.
     * @return {@code true} if there are fewer than {@code maxUlps} floating
     * point values between the real (resp. imaginary) parts of {@code x}
     * and {@code y}.
     *
     * @see Precision#equals(double,double,int)
     */
    public static boolean equals(Complex x,
                                 Complex y,
                                 int maxUlps) {
        return Precision.equals(x.real, y.real, maxUlps) &&
            Precision.equals(x.imaginary, y.imaginary, maxUlps);
    }

    /**
     * Returns {@code true} iff the values are equal as defined by
     * {@link #equals(Complex,Complex,int) equals(x, y, 1)}.
     *
     * @param x First value (cannot be {@code null}).
     * @param y Second value (cannot be {@code null}).
     * @return {@code true} if the values are equal.
     */
    public static boolean equals(Complex x,
                                 Complex y) {
        return equals(x, y, 1);
    }

    /**
     * Returns {@code true} if, both for the real part and for the imaginary
     * part, there is no double value strictly between the arguments or the
     * difference between them is within the range of allowed error
     * (inclusive).  Returns {@code false} if either of the arguments is NaN.
     *
     * @param x First value (cannot be {@code null}).
     * @param y Second value (cannot be {@code null}).
     * @param eps Amount of allowed absolute error.
     * @return {@code true} if the values are two adjacent floating point
     * numbers or they are within range of each other.
     *
     * @see Precision#equals(double,double,double)
     */
    public static boolean equals(Complex x,
                                 Complex y,
                                 double eps) {
        return Precision.equals(x.real, y.real, eps) &&
            Precision.equals(x.imaginary, y.imaginary, eps);
    }

    /**
     * Returns {@code true} if, both for the real part and for the imaginary
     * part, there is no double value strictly between the arguments or the
     * relative difference between them is smaller or equal to the given
     * tolerance. Returns {@code false} if either of the arguments is NaN.
     *
     * @param x First value (cannot be {@code null}).
     * @param y Second value (cannot be {@code null}).
     * @param eps Amount of allowed relative error.
     * @return {@code true} if the values are two adjacent floating point
     * numbers or they are within range of each other.
     *
     * @see Precision#equalsWithRelativeTolerance(double,double,double)
     */
    public static boolean equalsWithRelativeTolerance(Complex x, Complex y,
                                                      double eps) {
        return Precision.equalsWithRelativeTolerance(x.real, y.real, eps) &&
            Precision.equalsWithRelativeTolerance(x.imaginary, y.imaginary, eps);
    }

    /**
     * Get a hash code for the complex number.
     * Any {@code Double.NaN} value in real or imaginary part produces
     * the same hash code {@code 7}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        if (Double.isNaN(real) ||
            Double.isNaN(imaginary)) {
            return 7;
        }
        return 37 * (17 * hash(imaginary) + hash(real));
    }

    /**
     * @param d Value.
     * @return a hash code for the given value.
     */
    private int hash(double d) {
        final long v = Double.doubleToLongBits(d);
        return (int) (v ^ (v >>> 32));
        //return new Double(d).hashCode();
    }

    /**
     * Access the imaginary part.
     *
     * @return the imaginary part.
     */
    public double getImaginary() {
        return imaginary;
    }
    /**
     * Access the imaginary part (C++ grammar)
     *
     * @return the imaginary part.
     */
    public double imag() {
        return imaginary;
    }

    /**
     * Access the real part.
     *
     * @return the real part.
     */
    public double getReal() {
        return real;
    }

     /**
     * Access the real part (C++ grammar)
     *
     * @return the real part.
     */
    public double real() {
        return real;
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}.
     * Implements the definitional formula:
     *
     *   {@code (a + bi)(c + di) = (ac - bd) + (ad + bc)i}
     *
     * Recalculates to recover infinities as specified in C.99
     * standard G.5.1. Method is fully in accordance with
     * C++11 standards for complex numbers.
     *
     * @param  factor value to be multiplied by this {@code Complex}.
     * @return {@code this * factor}.
     */
    public Complex multiply(Complex factor) {
        double a = real;
        double b = imaginary;
        double c = factor.getReal();
        double d = factor.getImaginary();
        final double ac = a*c;
        final double bd = b*d;
        final double ad = a*d;
        final double bc = b*c;
        double x = ac - bd;
        double y = ad + bc;
        if (Double.isNaN(a) && Double.isNaN(b)) {
            boolean recalc = false;
            if (Double.isInfinite(a) || Double.isInfinite(b)) {
                a = Math.copySign(Double.isInfinite(a) ? 1.0 : 0.0, a);
                b = Math.copySign(Double.isInfinite(a) ? 1.0 : 0.0, a);
                if (Double.isNaN(c)) {
                    c = Math.copySign(0.0,  c);
                }
                if (Double.isNaN(d)) {
                    d = Math.copySign(0.0,  d);
                }
                recalc = true;
            }
            if (Double.isInfinite(c) || Double.isInfinite(d)) {
                c = Math.copySign(Double.isInfinite(c) ? 1.0 : 0.0, c);
                d = Math.copySign(Double.isInfinite(d) ? 1.0 : 0.0, d);
                if (Double.isNaN(a)) {
                    a = Math.copySign(0.0,  a);
                }
                if (Double.isNaN(b)) {
                    b = Math.copySign(0.0,  b);
                }
                recalc = true;
            }
            if (!recalc && (Double.isInfinite(ac) || Double.isInfinite(bd) ||
                    Double.isInfinite(ad) || Double.isInfinite(bc))) {
                if (Double.isNaN(a)) {
                    a = Math.copySign(0.0,  a);
                }
                if (Double.isNaN(b)) {
                    b = Math.copySign(0.0,  b);
                }
                if (Double.isNaN(c)) {
                    c = Math.copySign(0.0,  c);
                }
                if (Double.isNaN(d)) {
                    d = Math.copySign(0.0,  d);
                }
                recalc = true;
            }
            if (recalc) {
                x = Double.POSITIVE_INFINITY * (a*c - b*d);
                y = Double.POSITIVE_INFINITY * (a*d + b*c);
            }
        }
        return new Complex(x, y);
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}, with {@code factor}
     * interpreted as a integer number.
     *
     * @param  factor value to be multiplied by this {@code Complex}.
     * @return {@code this * factor}.
     * @see #multiply(Complex)
     */
    public Complex multiply(final int factor) {
        return new Complex(real * factor, imaginary * factor);
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}, with {@code factor}
     * interpreted as a real number.
     *
     * @param  factor value to be multiplied by this {@code Complex}.
     * @return {@code this * factor}.
     * @see #multiply(Complex)
     */
    public Complex multiply(double factor) {
        return new Complex(real * factor, imaginary * factor);
    }

    /**
     * Returns a {@code Complex} whose value is {@code (-this)}.
     *
     * @return {@code -this}.
     */
    public Complex negate() {
        return new Complex(-real, -imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is
     * {@code (this - subtrahend)}.
     * Uses the definitional formula
     * <p>
     *  {@code (a + bi) - (c + di) = (a-c) + (b-d)i}
     * </p>
     *
     * @param  subtrahend value to be subtracted from this {@code Complex}.
     * @return {@code this - subtrahend}.
     */
    public Complex subtract(Complex subtrahend) {
        return new Complex(real - subtrahend.real,
                           imaginary - subtrahend.imaginary);
    }

    /**
     * Returns a {@code Complex} whose value is
     * {@code (this - subtrahend)}.
     *
     * @param  subtrahend value to be subtracted from this {@code Complex}.
     * @return {@code this - subtrahend}.
     * @see #subtract(Complex)
     */
    public Complex subtract(double subtrahend) {
        return new Complex(real - subtrahend, imaginary);
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseCosine.html" TARGET="_top">
     * inverse cosine</a> of this complex number.
     * Implements the formula:
     * <p>
     *  {@code acos(z) = -i (log(z + i (sqrt(1 - z<sup>2</sup>))))}
     * </p>
     *
     * @return the inverse cosine of this complex number.
     */
    public Complex acos() {
        if (real == 0 &&
            Double.isNaN(imaginary)) {
            return new Complex(Math.PI * 0.5, Double.NaN);
        } else if (neitherInfiniteNorZeroNorNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Math.PI * 0.5, Double.NEGATIVE_INFINITY);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   imaginary == 1) {
            return new Complex(Math.PI, Double.NEGATIVE_INFINITY);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == 1) {
            return new Complex(0, Double.NEGATIVE_INFINITY);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Math.PI * 0.75, Double.NEGATIVE_INFINITY);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Math.PI * 0.25, Double.NEGATIVE_INFINITY);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.NaN , Double.POSITIVE_INFINITY);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.NaN, Double.NEGATIVE_INFINITY);
        } else if (Double.isNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.NaN, Double.NEGATIVE_INFINITY);
        }
        return add(sqrt1z().multiply(I)).log().multiply(I.negate());
    }
    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseSine.html" TARGET="_top">
     * inverse sine</a> of this complex number.
     * <p>
     *  {@code asin(z) = -i (log(sqrt(1 - z<sup>2</sup>) + iz))}
     * </p><p>
     * @return the inverse sine of this complex number
     */
    public Complex asin() {
        return sqrt1z().add(multiply(I)).log().multiply(I.negate());
    }
    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseTangent.html" TARGET="_top">
     * inverse tangent</a> of this complex number.
     * Implements the formula:
     * <p>
     * {@code atan(z) = (i/2) log((i + z)/(i - z))}
     * </p><p>
     * @return the inverse tangent of this complex number
     */
    public Complex atan() {
        return add(I).divide(I.subtract(this)).log().multiply(I.multiply(0.5));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicSine.html" TARGET="_top">
     * inverse hyperbolic sine</a> of this complex number.
     * Implements the formula:
     * <p>
     * {@code asinh(z) = log(z+sqrt(z^2+1))}
     * </p><p>
     * @return the inverse hyperbolic cosine of this complex number
     */
    public Complex asinh(){
        if (neitherInfiniteNorZeroNorNaN(real) &&
            imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Math.PI * 0.5);
        } else if (real == Double.POSITIVE_INFINITY &&
                   !Double.isInfinite(imaginary) && !Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Math.PI * 0.25);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY,  Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.NaN, 0);
        } else if (Double.isNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        }
        return square().add(ONE).sqrt().add(this).log();
    }

   /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicTangent.html" TARGET="_top">
     * inverse hyperbolic tangent</a> of this complex number.
     * Implements the formula:
     * <p>
     * {@code atanh(z) = log((1+z)/(1-z))/2}
     * </p><p>
     * @return the inverse hyperbolic cosine of this complex number
     */
    public Complex atanh(){
        if (real == 0 &&
            Double.isNaN(imaginary)) {
            return new Complex(0, Double.NaN);
        } else if (neitherInfiniteNorZeroNorNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else if (neitherInfiniteNorZeroNorNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(0, Math.PI * 0.5);
        } else if (real == Double.POSITIVE_INFINITY &&
                   neitherInfiniteNorZeroNorNaN(imaginary)) {
            return new Complex(0, Math.PI * 0.5);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(0, Math.PI * 0.5);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(0, Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(0, Math.PI * 0.5);
        }
        return add(ONE).divide(ONE.subtract(this)).log().multiply(0.5);
    }
   /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/InverseHyperbolicCosine.html" TARGET="_top">
     * inverse hyperbolic cosine</a> of this complex number.
     * Implements the formula:
     * <p>
     * {@code acosh(z) = log(z+sqrt(z^2-1))}
     * </p><p>
     * @return the inverse hyperbolic cosine of this complex number
     */
    public Complex acosh() {
        return square().subtract(ONE).sqrt().add(this).log();
    }

    /**
     * Compute the square of this complex number.
     *
     * @return square of this complex number
     */
    public Complex square() {
        return multiply(this);
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/Cosine.html" TARGET="_top">
     * cosine</a> of this complex number.
     * Implements the formula:
     * <p>
     *  {@code cos(a + bi) = cos(a)cosh(b) - sin(a)sinh(b)i}
     * </p><p>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos},
     * {@link Math#cosh} and {@link Math#sinh}.
     * </p><p>
     *
     * @return the cosine of this complex number.
     */
    public Complex cos() {
        return new Complex(Math.cos(real) * Math.cosh(imaginary),
                           -Math.sin(real) * Math.sinh(imaginary));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/HyperbolicCosine.html" TARGET="_top">
     * hyperbolic cosine</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   cosh(a + bi) = cosh(a)cos(b) + sinh(a)sin(b)i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos},
     * {@link Math#cosh} and {@link Math#sinh}.
     * <p>
     *
     * @return the hyperbolic cosine of this complex number.
     */
    public Complex cosh() {
        if (real == 0 &&
            imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.NaN, 0);
        } else if (real == 0 &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.NaN, 0);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == 0) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.NaN, 0);
        }

        return new Complex(Math.cosh(real) * Math.cos(imaginary),
                           Math.sinh(real) * Math.sin(imaginary));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/ExponentialFunction.html" TARGET="_top">
     * exponential function</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   exp(a + bi) = exp(a)cos(b) + exp(a)sin(b)i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#exp}, {@link Math#cos}, and
     * {@link Math#sin}.
     *
     * @return <code><i>e</i><sup>this</sup></code>.
     */
    public Complex exp() {
        if (real == Double.POSITIVE_INFINITY &&
            imaginary == 0) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return Complex.ZERO;
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return Complex.ZERO;
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.NaN, 0);
        }
        double expReal = Math.exp(real);
        return new Complex(expReal *  Math.cos(imaginary),
                           expReal * Math.sin(imaginary));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/NaturalLogarithm.html" TARGET="_top">
     * natural logarithm</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   log(a + bi) = ln(|a + bi|) + arg(a + bi)i
     *  </code>
     * </pre>
     * where ln on the right hand side is {@link Math#log},
     * {@code |a + bi|} is the modulus, {@link Complex#abs},  and
     * {@code arg(a + bi) = }{@link Math#atan2}(b, a).
     *
     * @return the value <code>ln &nbsp; this</code>, the natural logarithm
     * of {@code this}.
     */
    public Complex log() {
        if (real == Double.POSITIVE_INFINITY &&
            imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Math.PI * 0.25);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        }
        return new Complex(Math.log(abs()),
                           Math.atan2(imaginary, real));
    }

    /**
     * Compute the base 10 or
     * <a href="http://mathworld.wolfram.com/CommonLogarithm.html" TARGET="_top">
     * common logarithm</a> of this complex number.
     *
     *  @return the base 10 logarithm of <code>this</code>.
    */
    public Complex log10() {
        return new Complex(Math.log(abs()) / Math.log(10),
                           Math.atan2(imaginary, real));
    }

    /**
     * Returns of value of this complex number raised to the power of {@code x}.
     * Implements the formula:
     * <pre>
     *  <code>
     *   y<sup>x</sup> = exp(x&middot;log(y))
     *  </code>
     * </pre>
     * where {@code exp} and {@code log} are {@link #exp} and
     * {@link #log}, respectively.
     *
     * @param  x exponent to which this {@code Complex} is to be raised.
     * @return <code> this<sup>x</sup></code>.
     */
    public Complex pow(Complex x) {
        if (real == 0 &&
            imaginary == 0) {
            if (x.real > 0 &&
                x.imaginary == 0) {
                // 0 raised to positive number is 0
                return ZERO;
            } else {
                // 0 raised to anything else is NaN
                return NAN;
            }
        }
        return log().multiply(x).exp();
    }

    /**
     * Returns of value of this complex number raised to the power of {@code x}.
     *
     * @param  x exponent to which this {@code Complex} is to be raised.
     * @return <code>this<sup>x</sup></code>.
     * @see #pow(Complex)
     */
     public Complex pow(double x) {
        if (real == 0 &&
            imaginary == 0) {
            if (x > 0) {
                // 0 raised to positive number is 0
                return ZERO;
            } else {
                // 0 raised to anything else is NaN
                return NAN;
            }
        }
        return log().multiply(x).exp();
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/Sine.html" TARGET="_top">
     * sine</a>
     * of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   sin(a + bi) = sin(a)cosh(b) - cos(a)sinh(b)i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos},
     * {@link Math#cosh} and {@link Math#sinh}.
     *
     * @return the sine of this complex number.
     */
    public Complex sin() {
        return new Complex(Math.sin(real) * Math.cosh(imaginary),
                           Math.cos(real) * Math.sinh(imaginary));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/HyperbolicSine.html" TARGET="_top">
     * hyperbolic sine</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   sinh(a + bi) = sinh(a)cos(b)) + cosh(a)sin(b)i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos},
     * {@link Math#cosh} and {@link Math#sinh}.
     *
     * @return the hyperbolic sine of {@code this}.
     */
    public Complex sinh() {
        if (real == 0 &&
            imaginary == 0) {
            return Complex.ZERO;
        } else if (real == 0 &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(0, Double.NaN);
        } else if (real == 0 &&
                   Double.isNaN(imaginary)) {
            return new Complex(0, Double.NaN);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == 0) {
            return new Complex(Double.POSITIVE_INFINITY, 0);
        } else if (real == Double.POSITIVE_INFINITY &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        } else if (Double.isNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.NaN, 0);
        }
        return new Complex(Math.sinh(real) * Math.cos(imaginary),
                           Math.cosh(real) * Math.sin(imaginary));
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/SquareRoot.html" TARGET="_top">
     * square root</a> of this complex number.
     * Implements the following algorithm to compute {@code sqrt(a + bi)}:
     * <ol><li>Let {@code t = sqrt((|a| + |a + bi|) / 2)}</li>
     * <li><pre>if {@code  a &#8805; 0} return {@code t + (b/2t)i}
     *  else return {@code |b|/2t + sign(b)t i }</pre></li>
     * </ol>
     * where <ul>
     * <li>{@code |a| = }{@link Math#abs}(a)</li>
     * <li>{@code |a + bi| = }{@link Complex#abs}(a + bi)</li>
     * <li>{@code sign(b) =  }{@link Math#copySign(double,double) copySign(1d, b)}
     * </ul>
     *
     * @return the square root of {@code this}.
     */
    public Complex sqrt() {
        if (real == 0 &&
            imaginary == 0) {
            return ZERO;
        } else if (neitherInfiniteNorZeroNorNaN(real) &&
                   imaginary == Double.POSITIVE_INFINITY) {
            return new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   neitherInfiniteNorZeroNorNaN(imaginary)) {
            return new Complex(0, Double.NaN);
        } else if (real == Double.NEGATIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.NaN, Double.POSITIVE_INFINITY);
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        }

        final double t = Math.sqrt((Math.abs(real) + abs()) / 2);
        if (real >= 0) {
            return new Complex(t, imaginary / (2 * t));
        } else {
            return new Complex(Math.abs(imaginary) / (2 * t),
                               Math.copySign(1d, imaginary) * t);
        }
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/SquareRoot.html" TARGET="_top">
     * square root</a> of <code>1 - this<sup>2</sup></code> for this complex
     * number.
     * Computes the result directly as
     * {@code sqrt(ONE.subtract(z.multiply(z)))}.
     *
     * @return the square root of <code>1 - this<sup>2</sup></code>.
     */
    private Complex sqrt1z() {
        return ONE.subtract(square()).sqrt();
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/Tangent.html" TARGET="_top">
     * tangent</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   tan(a + bi) = sin(2a)/(cos(2a)+cosh(2b)) + [sinh(2b)/(cos(2a)+cosh(2b))]i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos}, {@link Math#cosh} and
     * {@link Math#sinh}.
     *
     * @return the tangent of {@code this}.
     */
    public Complex tan() {
        if (imaginary > 20) {
            return ONE;
        }
        if (imaginary < -20) {
            return new Complex(0, -1);
        }

        final double real2 = 2 * real;
        final double imaginary2 = 2 * imaginary;
        final double d = Math.cos(real2) + Math.cosh(imaginary2);

        return new Complex(Math.sin(real2) / d,
                           Math.sinh(imaginary2) / d);
    }

    /**
     * Compute the
     * <a href="http://mathworld.wolfram.com/HyperbolicTangent.html" TARGET="_top">
     * hyperbolic tangent</a> of this complex number.
     * Implements the formula:
     * <pre>
     *  <code>
     *   tan(a + bi) = sinh(2a)/(cosh(2a)+cos(2b)) + [sin(2b)/(cosh(2a)+cos(2b))]i
     *  </code>
     * </pre>
     * where the (real) functions on the right-hand side are
     * {@link Math#sin}, {@link Math#cos}, {@link Math#cosh} and
     * {@link Math#sinh}.
     *
     * @return the hyperbolic tangent of {@code this}.
     */
    public Complex tanh() {
        if (real == Double.POSITIVE_INFINITY &&
            imaginary == Double.POSITIVE_INFINITY) {
            return ONE;
        } else if (real == Double.POSITIVE_INFINITY &&
                   Double.isNaN(imaginary)) {
            return ONE;
        } else if (Double.isNaN(real) &&
                   imaginary == 0) {
            return new Complex(Double.NaN, 0);
        }
        final double real2 = 2 * real;
        final double imaginary2 = 2 * imaginary;
        final double d = Math.cosh(real2) + Math.cos(imaginary2);

        return new Complex(Math.sinh(real2) / d,
                           Math.sin(imaginary2) / d);
    }

   /**
     * Compute the argument of this complex number.
     * The argument is the angle phi between the positive real axis and
     * the point representing this number in the complex plane.
     * The value returned is between -PI (not inclusive)
     * and PI (inclusive), with negative values returned for numbers with
     * negative imaginary parts.
     * <p>
     * If either real or imaginary part (or both) is NaN, NaN is returned.
     * Infinite parts are handled as {@code Math.atan2} handles them,
     * essentially treating finite parts as zero in the presence of an
     * infinite coordinate and returning a multiple of pi/4 depending on
     * the signs of the infinite parts.
     * See the javadoc for {@code Math.atan2} for full details.
     *
     * @return the argument of {@code this}.
     */
    public double getArgument() {
        return Math.atan2(imaginary, real);
    }

    /**
     * Compute the argument of this complex number.
     * C++11 syntax
     *
     * @return the argument of {@code this}.
     */
    public double arg() {
        return getArgument();
    }

    /**
     * Computes the n-th roots of this complex number.
     * The nth roots are defined by the formula:
     * <pre>
     *  <code>
     *   z<sub>k</sub> = abs<sup>1/n</sup> (cos(phi + 2&pi;k/n) + i (sin(phi + 2&pi;k/n))
     *  </code>
     * </pre>
     * for <i>{@code k=0, 1, ..., n-1}</i>, where {@code abs} and {@code phi}
     * are respectively the {@link #abs() modulus} and
     * {@link #getArgument() argument} of this complex number.
     * <p>
     * If one or both parts of this complex number is NaN, a list with just
     * one element, {@code NaN + NaN i} is returned.
     * if neither part is NaN, but at least one part is infinite, the result
     * is a one-element list containing {@link #INF}.
     *
     * @param n Degree of root.
     * @return a List of all {@code n}-th roots of {@code this}.
     */
    public List<Complex> nthRoot(int n) {
        if (n == 0) {
            throw new IllegalArgumentException("cannot compute zeroth root");
        }

        final List<Complex> result = new ArrayList<Complex>();

        // nth root of abs -- faster / more accurate to use a solver here?
        final double nthRootOfAbs = Math.pow(abs(), 1d / n);

        // Compute nth roots of complex number with k = 0, 1, ... n-1
        final double nthPhi = getArgument() / n;
        final double slice = 2 * Math.PI / n;
        double innerPart = nthPhi;
        for (int k = 0; k < Math.abs(n) ; k++) {
            // inner part
            final double realPart = nthRootOfAbs *  Math.cos(innerPart);
            final double imaginaryPart = nthRootOfAbs *  Math.sin(innerPart);
            result.add(new Complex(realPart, imaginaryPart));
            innerPart += slice;
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append(FORMAT_START)
            .append(real).append(FORMAT_SEP)
            .append(imaginary)
            .append(FORMAT_END);

        return s.toString();
    }

    /**
     * Check that the argument is positive and throw a RuntimeException
     * if it is not.
     * @param arg {@code double} to check
     */
    private static void checkNotNegative(double arg) {
        if (arg <= 0) {
            throw new IllegalArgumentException("Complex: Non-positive argument");
        }
    }

    /**
     * Returns {@code true} if the values are equal according to semantics of
     * {@link Double#equals(Object)}.
     *
     * @param x Value
     * @param y Value
     * @return {@code Double.valueof(x).equals(Double.valueOf(y))}
     */
    private static boolean equals(double x, double y) {
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(y);
    }

    /**
     * Check that a value meets all the following conditions:
     * <ul>
     *  <li>it is not {@code NaN},</li>
     *  <li>it is not infinite,</li>
     *  <li>it is not zero,</li>
     * </ul>
     *
     * @param d Value.
     * @return {@code true} if {@code d} meets all the conditions and
     * {@code false} otherwise.
     */
    private static boolean neitherInfiniteNorZeroNorNaN(double d) {
        return !Double.isNaN(d) &&
            !Double.isInfinite(d) &&
            d != 0;
    }

    /** See {@link #parse(String)}. */
    private static class ComplexParsingException extends IllegalArgumentException {
        /** Serializable version identifier. */
        private static final long serialVersionUID = 20180430L;

        /**
         * @param msg Error message.
         */
        ComplexParsingException(String msg) {
            super(msg);
        }
    }
}
