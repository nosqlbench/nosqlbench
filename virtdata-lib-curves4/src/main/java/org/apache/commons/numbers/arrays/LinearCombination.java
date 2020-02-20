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
package org.apache.commons.numbers.arrays;

/**
 * Computes linear combinations accurately.
 * This method computes the sum of the products
 * <code>a<sub>i</sub> b<sub>i</sub></code> to high accuracy.
 * It does so by using specific multiplication and addition algorithms to
 * preserve accuracy and reduce cancellation effects.
 *
 * It is based on the 2005 paper
 * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
 * Accurate Sum and Dot Product</a> by Takeshi Ogita, Siegfried M. Rump,
 * and Shin'ichi Oishi published in <em>SIAM J. Sci. Comput</em>.
 */
public class LinearCombination {
    /*
     * Caveat:
     *
     * The code below is split in many additions/subtractions that may
     * appear redundant. However, they should NOT be simplified, as they
     * do use IEEE754 floating point arithmetic rounding properties.
     * The variables naming conventions are that xyzHigh contains the most significant
     * bits of xyz and xyzLow contains its least significant bits. So theoretically
     * xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
     * be represented in only one double precision number so we preserve two numbers
     * to hold it as long as we can, combining the high and low order bits together
     * only at the end, after cancellation may have occurred on high order bits
     */

    /**
     * @param a Factors.
     * @param b Factors.
     * @return \( \sum_i a_i b_i \).
     * @throws IllegalArgumentException if the sizes of the arrays are different.
     */
    public static double value(double[] a,
                               double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " != " + b.length);
        }

        final int len = a.length;

        if (len == 1) {
            // Revert to scalar multiplication.
            return a[0] * b[0];
        }

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai    = a[i];
            final double aHigh = highPart(ai);
            final double aLow  = ai - aHigh;

            final double bi    = b[i];
            final double bHigh = highPart(bi);
            final double bLow  = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = prodLow(aLow, bLow, prodHigh[i], aHigh, bHigh);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

    /**
     * @param a1 First factor of the first term.
     * @param b1 Second factor of the first term.
     * @param a2 First factor of the second term.
     * @param b2 Second factor of the second term.
     * @return \( a_1 b_1 + a_2 b_2 \)
     *
     * @see #value(double, double, double, double, double, double)
     * @see #value(double, double, double, double, double, double, double, double)
     * @see #value(double[], double[])
     */
    public static double value(double a1, double b1,
                               double a2, double b2) {
        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = highPart(a1);
        final double a1Low      = a1 - a1High;
        final double b1High     = highPart(b1);
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = prodLow(a1Low, b1Low, prod1High, a1High, b1High);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = highPart(a2);
        final double a2Low      = a2 - a2High;
        final double b2High     = highPart(b2);
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = prodLow(a2Low, b2Low, prod2High, a2High, b2High);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // final rounding, s12 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s12High + (prod1Low + prod2Low + s12Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2;
        }

        return result;
    }

    /**
     * @param a1 First factor of the first term.
     * @param b1 Second factor of the first term.
     * @param a2 First factor of the second term.
     * @param b2 Second factor of the second term.
     * @param a3 First factor of the third term.
     * @param b3 Second factor of the third term.
     * @return \( a_1 b_1 + a_2 b_2 + a_3 b_3 \)
     *
     * @see #value(double, double, double, double)
     * @see #value(double, double, double, double, double, double, double, double)
     * @see #value(double[], double[])
     */
    public static double value(double a1, double b1,
                               double a2, double b2,
                               double a3, double b3) {
        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = highPart(a1);
        final double a1Low      = a1 - a1High;
        final double b1High     = highPart(b1);
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = prodLow(a1Low, b1Low, prod1High, a1High, b1High);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = highPart(a2);
        final double a2Low      = a2 - a2High;
        final double b2High     = highPart(b2);
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = prodLow(a2Low, b2Low, prod2High, a2High, b2High);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High     = highPart(a3);
        final double a3Low      = a3 - a3High;
        final double b3High     = highPart(b3);
        final double b3Low      = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High  = a3 * b3;
        final double prod3Low   = prodLow(a3Low, b3Low, prod3High, a3High, b3High);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High   = s12High + prod3High;
        final double s123Prime  = s123High - prod3High;
        final double s123Low    = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // final rounding, s123 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s123High + (prod1Low + prod2Low + prod3Low + s12Low + s123Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2 + a3 * b3;
        }

        return result;
    }

    /**
     * @param a1 First factor of the first term.
     * @param b1 Second factor of the first term.
     * @param a2 First factor of the second term.
     * @param b2 Second factor of the second term.
     * @param a3 First factor of the third term.
     * @param b3 Second factor of the third term.
     * @param a4 First factor of the fourth term.
     * @param b4 Second factor of the fourth term.
     * @return \( a_1 b_1 + a_2 b_2 + a_3 b_3 + a_4 b_4 \)
     *
     * @see #value(double, double, double, double)
     * @see #value(double, double, double, double, double, double)
     * @see #value(double[], double[])
     */
    public static double value(double a1, double b1,
                               double a2, double b2,
                               double a3, double b3,
                               double a4, double b4) {
        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = highPart(a1);
        final double a1Low      = a1 - a1High;
        final double b1High     = highPart(b1);
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = prodLow(a1Low, b1Low, prod1High, a1High, b1High);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = highPart(a2);
        final double a2Low      = a2 - a2High;
        final double b2High     = highPart(b2);
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = prodLow(a2Low, b2Low, prod2High, a2High, b2High);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High     = highPart(a3);
        final double a3Low      = a3 - a3High;
        final double b3High     = highPart(b3);
        final double b3Low      = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High  = a3 * b3;
        final double prod3Low   = prodLow(a3Low, b3Low, prod3High, a3High, b3High);

        // split a4 and b4 as one 26 bits number and one 27 bits number
        final double a4High     = highPart(a4);
        final double a4Low      = a4 - a4High;
        final double b4High     = highPart(b4);
        final double b4Low      = b4 - b4High;

        // accurate multiplication a4 * b4
        final double prod4High  = a4 * b4;
        final double prod4Low   = prodLow(a4Low, b4Low, prod4High, a4High, b4High);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High   = s12High + prod3High;
        final double s123Prime  = s123High - prod3High;
        final double s123Low    = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4
        final double s1234High  = s123High + prod4High;
        final double s1234Prime = s1234High - prod4High;
        final double s1234Low   = (prod4High - (s1234High - s1234Prime)) + (s123High - s1234Prime);

        // final rounding, s1234 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s1234High + (prod1Low + prod2Low + prod3Low + prod4Low + s12Low + s123Low + s1234Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4;
        }

        return result;
    }

    /**
     * @param value Value.
     * @return the high part of the value.
     */
    private static double highPart(double value) {
        return Double.longBitsToDouble(Double.doubleToRawLongBits(value) & ((-1L) << 27));
    }

    /**
     * @param aLow Low part of first factor.
     * @param bLow Low part of second factor.
     * @param prodHigh Product of the factors.
     * @param aHigh High part of first factor.
     * @param bHigh High part of second factor.
     * @return <code>aLow * bLow - (((prodHigh - aHigh * bHigh) - aLow * bHigh) - aHigh * bLow)</code>
     */
    private static double prodLow(double aLow,
                                  double bLow,
                                  double prodHigh,
                                  double aHigh,
                                  double bHigh) {
        return aLow * bLow - (((prodHigh - aHigh * bHigh) - aLow * bHigh) - aHigh * bLow);
    }
}
