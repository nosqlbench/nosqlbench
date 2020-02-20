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

package org.apache.commons.numbers.complex.streams;

import org.apache.commons.numbers.complex.Complex;

/**
 * Static implementations of common {@link Complex} utilities functions.
 */
public class ComplexUtils {

    /**
     * Utility class.
     */
    private ComplexUtils() {}

    /**
     * Creates a complex number from the given polar representation.
     * <p>
     * If either {@code r} or {@code theta} is NaN, or {@code theta} is
     * infinite, {@link Complex#NAN} is returned.
     * <p>
     * If {@code r} is infinite and {@code theta} is finite, infinite or NaN
     * values may be returned in parts of the result, following the rules for
     * double arithmetic.
     *
     * Examples:
     * <pre>
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
    public static Complex polar2Complex(double r, double theta) {
        if (r < 0) {
            throw new NegativeModulusException(r);
        }
        return Complex.ofCartesian(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * Creates {@code Complex[]} array given {@code double[]} arrays of r and
     * theta.
     *
     * @param r {@code double[]} of moduli
     * @param theta {@code double[]} of arguments
     * @return {@code Complex[]}
     */
    public static Complex[] polar2Complex(double[] r, double[] theta) {
        final int length = r.length;
        final Complex[] c = new Complex[length];
        for (int x = 0; x < length; x++) {
            if (r[x] < 0) {
                throw new NegativeModulusException(r[x]);
            }
            c[x] = Complex.ofCartesian(r[x] * Math.cos(theta[x]), r[x] * Math.sin(theta[x]));
        }
        return c;
    }

    /**
     * Creates {@code Complex[][]} array given {@code double[][]} arrays of r
     * and theta.
     *
     * @param r {@code double[]} of moduli
     * @param theta {@code double[]} of arguments
     * @return {@code Complex[][]}
     */
    public static Complex[][] polar2Complex(double[][] r, double[][] theta) {
        final int length = r.length;
        final Complex[][] c = new Complex[length][];
        for (int x = 0; x < length; x++) {
            c[x] = polar2Complex(r[x], theta[x]);
        }
        return c;
    }

    /**
     * Creates {@code Complex[][][]} array given {@code double[][][]} arrays of
     * r and theta.
     *
     * @param r array of moduli
     * @param theta array of arguments
     * @return {@code Complex}
     */
    public static Complex[][][] polar2Complex(double[][][] r, double[][][] theta) {
        final int length = r.length;
        final Complex[][][] c = new Complex[length][][];
        for (int x = 0; x < length; x++) {
            c[x] = polar2Complex(r[x], theta[x]);
        }
        return c;
    }

    /**
     * Returns double from array {@code real[]} at entry {@code index} as a
     * {@code Complex}.
     *
     * @param real array of real numbers
     * @param index location in the array
     * @return {@code Complex}.
     */
    public static Complex extractComplexFromRealArray(double[] real, int index) {
        return Complex.ofReal(real[index]);
    }

    /**
     * Returns float from array {@code real[]} at entry {@code index} as a
     * {@code Complex}.
     *
     * @param real array of real numbers
     * @param index location in the array
     * @return {@code Complex} array
     */
    public static Complex extractComplexFromRealArray(float[] real, int index) {
        return Complex.ofReal(real[index]);
    }

    /**
     * Returns double from array {@code imaginary[]} at entry {@code index} as a
     * {@code Complex}.
     *
     * @param imaginary array of imaginary numbers
     * @param index location in the array
     * @return {@code Complex} array
     */
    public static Complex extractComplexFromImaginaryArray(double[] imaginary, int index) {
        return Complex.ofCartesian(0, imaginary[index]);
    }

    /**
     * Returns float from array {@code imaginary[]} at entry {@code index} as a
     * {@code Complex}.
     *
     * @param imaginary array of imaginary numbers
     * @param index location in the array
     * @return {@code Complex} array
     */
    public static Complex extractComplexFromImaginaryArray(float[] imaginary, int index) {
        return Complex.ofCartesian(0, imaginary[index]);
    }

    /**
     * Returns real component of Complex from array {@code Complex[]} at entry
     * {@code index} as a {@code double}.
     *
     * @param complex array of complex numbers
     * @param index location in the array
     * @return {@code double}.
     */
    public static double extractRealFromComplexArray(Complex[] complex, int index) {
        return complex[index].getReal();
    }

    /**
     * Returns real component of array {@code Complex[]} at entry {@code index}
     * as a {@code float}.
     *
     * @param complex array of complex numbers
     * @param index location in the array
     * @return {@code float}.
     */
    public static float extractRealFloatFromComplexArray(Complex[] complex, int index) {
        return (float) complex[index].getReal();
    }

    /**
     * Returns imaginary component of Complex from array {@code Complex[]} at
     * entry {@code index} as a {@code double}.
     *
     * @param complex array of complex numbers
     * @param index location in the array
     * @return {@code double}.
     */
    public static double extractImaginaryFromComplexArray(Complex[] complex, int index) {
        return complex[index].getImaginary();
    }

    /**
     * Returns imaginary component of array {@code Complex[]} at entry
     * {@code index} as a {@code float}.
     *
     * @param complex array of complex numbers
     * @param index location in the array
     * @return {@code float}.
     */
    public static float extractImaginaryFloatFromComplexArray(Complex[] complex, int index) {
        return (float) complex[index].getImaginary();
    }

    /**
     * Returns a Complex object from interleaved {@code double[]} array at entry
     * {@code index}.
     *
     * @param d array of interleaved complex numbers alternating real and imaginary values
     * @param index location in the array This is the location by complex number, e.g. index number 5 in the array will return {@code Complex.ofCartesian(d[10], d[11])}
     * @return {@code Complex}.
     */
    public static Complex extractComplexFromInterleavedArray(double[] d, int index) {
        return Complex.ofCartesian(d[index * 2], d[index * 2 + 1]);
    }

    /**
     * Returns a Complex object from interleaved {@code float[]} array at entry
     * {@code index}.
     *
     * @param f float array of interleaved complex numbers alternating real and imaginary values
     * @param index location in the array This is the location by complex number, e.g. index number 5 in the {@code float[]} array will return new {@code Complex(d[10], d[11])}
     * @return {@code Complex}.
     */
    public static Complex extractComplexFromInterleavedArray(float[] f, int index) {
        return Complex.ofCartesian(f[index * 2], f[index * 2 + 1]);
    }

    /**
     * Returns values of Complex object from array {@code Complex[]} at entry
     * {@code index} as a size 2 {@code double} of the form {real, imag}.
     *
     * @param complex array of complex numbers
     * @param index location in the array
     * @return size 2 array.
     */
    public static double[] extractInterleavedFromComplexArray(Complex[] complex, int index) {
        return new double[] { complex[index].getReal(), complex[index].getImaginary() };
    }

    /**
     * Returns Complex object from array {@code Complex[]} at entry
     * {@code index} as a size 2 {@code float} of the form {real, imag}.
     *
     * @param complex {@code Complex} array
     * @param index location in the array
     * @return size 2 {@code float[]}.
     */
    public static float[] extractInterleavedFloatFromComplexArray(Complex[] complex, int index) {
        return new float[] { (float) complex[index].getReal(), (float) complex[index].getImaginary() };
    }

    /**
     * Converts a {@code double[]} array to a {@code Complex[]} array.
     *
     * @param real array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] real2Complex(double[] real) {
        int index = 0;
        final Complex[] c = new Complex[real.length];
        for (double d : real) {
            c[index] = Complex.ofReal(d);
            index++;
        }
        return c;
    }

    /**
     * Converts a {@code float[]} array to a {@code Complex[]} array.
     *
     * @param real array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] real2Complex(float[] real) {
        int index = 0;
        final Complex[] c = new Complex[real.length];
        for (float d : real) {
            c[index] = Complex.ofReal(d);
            index++;
        }
        return c;
    }

    /**
     * Converts a 2D real {@code double[][]} array to a 2D {@code Complex[][]}
     * array.
     *
     * @param d 2D array
     * @return 2D {@code Complex} array
     */
    public static Complex[][] real2Complex(double[][] d) {
        final int w = d.length;
        final Complex[][] c = new Complex[w][];
        for (int n = 0; n < w; n++) {
            c[n] = ComplexUtils.real2Complex(d[n]);
        }
        return c;
    }

    /**
     * Converts a 2D real {@code float[][]} array to a 2D {@code Complex[][]}
     * array.
     *
     * @param d 2D array
     * @return 2D {@code Complex} array
     */
    public static Complex[][] real2Complex(float[][] d) {
        final int w = d.length;
        final Complex[][] c = new Complex[w][];
        for (int n = 0; n < w; n++) {
            c[n] = ComplexUtils.real2Complex(d[n]);
        }
        return c;
    }

    /**
     * Converts a 3D real {@code double[][][]} array to a {@code Complex [][][]}
     * array.
     *
     * @param d 3D complex interleaved array
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] real2Complex(double[][][] d) {
        final int w = d.length;
        final Complex[][][] c = new Complex[w][][];
        for (int x = 0; x < w; x++) {
            c[x] = ComplexUtils.real2Complex(d[x]);
        }
        return c;
    }

    /**
     * Converts a 3D real {@code float[][][]} array to a {@code Complex [][][]}
     * array.
     *
     * @param d 3D complex interleaved array
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] real2Complex(float[][][] d) {
        final int w = d.length;
        final Complex[][][] c = new Complex[w][][];
        for (int x = 0; x < w; x++) {
            c[x] = ComplexUtils.real2Complex(d[x]);
        }
        return c;
    }

    /**
     * Converts a 4D real {@code double[][][][]} array to a {@code Complex [][][][]}
     * array.
     *
     * @param d 4D complex interleaved array
     * @return 4D {@code Complex} array
     */
    public static Complex[][][][] real2Complex(double[][][][] d) {
        final int w = d.length;
        final Complex[][][][] c = new Complex[w][][][];
        for (int x = 0; x < w; x++) {
            c[x] = ComplexUtils.real2Complex(d[x]);
        }
        return c;
    }

    /**
     * Converts real component of {@code Complex[]} array to a {@code double[]}
     * array.
     *
     * @param c {@code Complex} array
     * @return array of the real component
     */
    public static double[] complex2Real(Complex[] c) {
        int index = 0;
        final double[] d = new double[c.length];
        for (Complex cc : c) {
            d[index] = cc.getReal();
            index++;
        }
        return d;
    }

    /**
     * Converts real component of {@code Complex[]} array to a {@code float[]}
     * array.
     *
     * @param c {@code Complex} array
     * @return {@code float[]} array of the real component
     */
    public static float[] complex2RealFloat(Complex[] c) {
        int index = 0;
        final float[] f = new float[c.length];
        for (Complex cc : c) {
            f[index] = (float) cc.getReal();
            index++;
        }
        return f;
    }

    /**
     * Converts real component of a 2D {@code Complex[][]} array to a 2D
     * {@code double[][]} array.
     *
     * @param c 2D {@code Complex} array
     * @return {@code double[][]} of real component
     */
    public static double[][] complex2Real(Complex[][] c) {
        final int length = c.length;
        double[][] d = new double[length][];
        for (int n = 0; n < length; n++) {
            d[n] = complex2Real(c[n]);
        }
        return d;
    }

    /**
     * Converts real component of a 2D {@code Complex[][]} array to a 2D
     * {@code float[][]} array.
     *
     * @param c 2D {@code Complex} array
     * @return {@code float[][]} of real component
     */
    public static float[][] complex2RealFloat(Complex[][] c) {
        final int length = c.length;
        float[][] f = new float[length][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2RealFloat(c[n]);
        }
        return f;
    }

    /**
     * Converts real component of a 3D {@code Complex[][][]} array to a 3D
     * {@code double[][][]} array.
     *
     * @param c 3D complex interleaved array
     * @return array of real component
     */
    public static double[][][] complex2Real(Complex[][][] c) {
        final int length = c.length;
        double[][][] d = new double[length][][];
        for (int n = 0; n < length; n++) {
            d[n] = complex2Real(c[n]);
        }
        return d;
    }

    /**
     * Converts real component of a 3D {@code Complex[][][]} array to a 3D
     * {@code float[][][]} array.
     *
     * @param c 3D {@code Complex} array
     * @return {@code float[][][]} of real component
     */
    public static float[][][] complex2RealFloat(Complex[][][] c) {
        final int length = c.length;
        float[][][] f = new float[length][][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2RealFloat(c[n]);
        }
        return f;
    }

    /**
     * Converts real component of a 4D {@code Complex[][][][]} array to a 4D
     * {@code double[][][][]} array.
     *
     * @param c 4D complex interleaved array
     * @return array of real component
     */
    public static double[][][][] complex2Real(Complex[][][][] c) {
        final int length = c.length;
        double[][][][] d = new double[length][][][];
        for (int n = 0; n < length; n++) {
            d[n] = complex2Real(c[n]);
        }
        return d;
    }

    /**
     * Converts real component of a 4D {@code Complex[][][][]} array to a 4D
     * {@code float[][][][]} array.
     *
     * @param c 4D {@code Complex} array
     * @return {@code float[][][][]} of real component
     */
    public static float[][][][] complex2RealFloat(Complex[][][][] c) {
        final int length = c.length;
        float[][][][] f = new float[length][][][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2RealFloat(c[n]);
        }
        return f;
    }

    /**
     * Converts a {@code double[]} array to an imaginary {@code Complex[]}
     * array.
     *
     * @param imaginary array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] imaginary2Complex(double[] imaginary) {
        int index = 0;
        final Complex[] c = new Complex[imaginary.length];
        for (double d : imaginary) {
            c[index] = Complex.ofCartesian(0, d);
            index++;
        }
        return c;
    }

    /**
     * Converts a {@code float[]} array to an imaginary {@code Complex[]} array.
     *
     * @param imaginary array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] imaginary2Complex(float[] imaginary) {
        int index = 0;
        final Complex[] c = new Complex[imaginary.length];
        for (float d : imaginary) {
            c[index] = Complex.ofCartesian(0, d);
            index++;
        }
        return c;
    }

    /**
     * Converts a 2D imaginary array {@code double[][]} to a 2D
     * {@code Complex[][]} array.
     *
     * @param i 2D array
     * @return 2D {@code Complex} array
     */
    public static Complex[][] imaginary2Complex(double[][] i) {
        int w = i.length;
        Complex[][] c = new Complex[w][];
        for (int n = 0; n < w; n++) {
            c[n] = ComplexUtils.imaginary2Complex(i[n]);
        }
        return c;
    }

    /**
     * Converts a 3D imaginary array {@code double[][][]} to a {@code Complex[]}
     * array.
     *
     * @param i 3D complex imaginary array
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] imaginary2Complex(double[][][] i) {
        int w = i.length;
        Complex[][][] c = new Complex[w][][];
        for (int n = 0; n < w; n++) {
            c[n] = ComplexUtils.imaginary2Complex(i[n]);
        }
        return c;
    }

    /**
     * Converts a 4D imaginary array {@code double[][][][]} to a 4D {@code Complex[][][][]}
     * array.
     *
     * @param i 4D complex imaginary array
     * @return 4D {@code Complex} array
     */
    public static Complex[][][][] imaginary2Complex(double[][][][] i) {
        int w = i.length;
        Complex[][][][] c = new Complex[w][][][];
        for (int n = 0; n < w; n++) {
            c[n] = ComplexUtils.imaginary2Complex(i[n]);
        }
        return c;
    }

    /**
     * Converts imaginary part of a {@code Complex[]} array to a
     * {@code double[]} array.
     *
     * @param c {@code Complex} array.
     * @return array of the imaginary component
     */
    public static double[] complex2Imaginary(Complex[] c) {
        int index = 0;
        final double[] i = new double[c.length];
        for (Complex cc : c) {
            i[index] = cc.getImaginary();
            index++;
        }
        return i;
    }

    /**
     * Converts imaginary component of a {@code Complex[]} array to a
     * {@code float[]} array.
     *
     * @param c {@code Complex} array.
     * @return {@code float[]} array of the imaginary component
     */
    public static float[] complex2ImaginaryFloat(Complex[] c) {
        int index = 0;
        final float[] f = new float[c.length];
        for (Complex cc : c) {
            f[index] = (float) cc.getImaginary();
            index++;
        }
        return f;
    }

    /**
     * Converts imaginary component of a 2D {@code Complex[][]} array to a 2D
     * {@code double[][]} array.
     *
     * @param c 2D {@code Complex} array
     * @return {@code double[][]} of imaginary component
     */
    public static double[][] complex2Imaginary(Complex[][] c) {
        final int length = c.length;
        double[][] i = new double[length][];
        for (int n = 0; n < length; n++) {
            i[n] = complex2Imaginary(c[n]);
        }
        return i;
    }

    /**
     * Converts imaginary component of a 2D {@code Complex[][]} array to a 2D
     * {@code float[][]} array.
     *
     * @param c 2D {@code Complex} array
     * @return {@code float[][]} of imaginary component
     */
    public static float[][] complex2ImaginaryFloat(Complex[][] c) {
        final int length = c.length;
        float[][] f = new float[length][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2ImaginaryFloat(c[n]);
        }
        return f;
    }

    /**
     * Converts imaginary component of a 3D {@code Complex[][][]} array to a 3D
     * {@code double[][][]} array.
     *
     * @param c 3D complex interleaved array
     * @return 3D {@code Complex} array
     */
    public static double[][][] complex2Imaginary(Complex[][][] c) {
        final int length = c.length;
        double[][][] i = new double[length][][];
        for (int n = 0; n < length; n++) {
            i[n] = complex2Imaginary(c[n]);
        }
        return i;
    }

    /**
     * Converts imaginary component of a 3D {@code Complex[][][]} array to a 3D
     * {@code float[][][]} array.
     *
     * @param c 3D {@code Complex} array
     * @return {@code float[][][]} of imaginary component
     */
    public static float[][][] complex2ImaginaryFloat(Complex[][][] c) {
        final int length = c.length;
        float[][][] f = new float[length][][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2ImaginaryFloat(c[n]);
        }
        return f;
    }

    /**
     * Converts imaginary component of a 4D {@code Complex[][][][]} array to a 4D
     * {@code double[][][][]} array.
     *
     * @param c 4D complex interleaved array
     * @return 4D {@code Complex} array
     */
    public static double[][][][] complex2Imaginary(Complex[][][][] c) {
        final int length = c.length;
        double[][][][] i = new double[length][][][];
        for (int n = 0; n < length; n++) {
            i[n] = complex2Imaginary(c[n]);
        }
        return i;
    }

    /**
     * Converts imaginary component of a 4D {@code Complex[][][][]} array to a 4D
     * {@code float[][][][]} array.
     *
     * @param c 4D {@code Complex} array
     * @return {@code float[][][][]} of imaginary component
     */
    public static float[][][][] complex2ImaginaryFloat(Complex[][][][] c) {
        final int length = c.length;
        float[][][][] f = new float[length][][][];
        for (int n = 0; n < length; n++) {
            f[n] = complex2ImaginaryFloat(c[n]);
        }
        return f;
    }

    // INTERLEAVED METHODS

    /**
     * Converts a complex interleaved {@code double[]} array to a
     * {@code Complex[]} array
     *
     * @param interleaved array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] interleaved2Complex(double[] interleaved) {
        final int length = interleaved.length / 2;
        final Complex[] c = new Complex[length];
        for (int n = 0; n < length; n++) {
            c[n] = Complex.ofCartesian(interleaved[n * 2], interleaved[n * 2 + 1]);
        }
        return c;
    }

    /**
     * Converts a complex interleaved {@code float[]} array to a
     * {@code Complex[]} array
     *
     * @param interleaved float[] array of numbers to be converted to their {@code Complex} equivalent
     * @return {@code Complex} array
     */
    public static Complex[] interleaved2Complex(float[] interleaved) {
        final int length = interleaved.length / 2;
        final Complex[] c = new Complex[length];
        for (int n = 0; n < length; n++) {
            c[n] = Complex.ofCartesian(interleaved[n * 2], interleaved[n * 2 + 1]);
        }
        return c;
    }

    /**
     * Converts a {@code Complex[]} array to an interleaved complex
     * {@code double[]} array
     *
     * @param c Complex array
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[] complex2Interleaved(Complex[] c) {
        int index = 0;
        final double[] i = new double[c.length * 2];
        for (Complex cc : c) {
            int real = index * 2;
            int imag = index * 2 + 1;
            i[real] = cc.getReal();
            i[imag] = cc.getImaginary();
            index++;
        }
        return i;
    }

    /**
     * Converts a {@code Complex[]} array to an interleaved complex
     * {@code float[]} array
     *
     * @param c Complex array
     * @return complex interleaved {@code float[]} alternating real and
     *         imaginary values
     */
    public static float[] complex2InterleavedFloat(Complex[] c) {
        int index = 0;
        final float[] f = new float[c.length * 2];
        for (Complex cc : c) {
            int real = index * 2;
            int imag = index * 2 + 1;
            f[real] = (float) cc.getReal();
            f[imag] = (float) cc.getImaginary();
            index++;
        }
        return f;
    }

    /**
     * Converts a 2D {@code Complex[][]} array to an interleaved complex
     * {@code double[][]} array.
     *
     * @param c 2D Complex array
     * @param interleavedDim Depth level of the array to interleave
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][] complex2Interleaved(Complex[][] c, int interleavedDim) {
        if (interleavedDim > 1 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = c.length;
        final int h = c[0].length;
        double[][] i;
        if (interleavedDim == 0) {
            i = new double[2 * w][h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    i[x * 2][y] = c[x][y].getReal();
                    i[x * 2 + 1][y] = c[x][y].getImaginary();
                }
            }
        } else {
            i = new double[w][2 * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    i[x][y * 2] = c[x][y].getReal();
                    i[x][y * 2 + 1] = c[x][y].getImaginary();
                }
            }
        }
        return i;
    }

    /**
     * Converts a 2D {@code Complex[][]} array to an interleaved complex
     * {@code double[][]} array. The second d level of the array is assumed
     * to be interleaved.
     *
     * @param c 2D Complex array
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][] complex2Interleaved(Complex[][] c) {
        return complex2Interleaved(c, 1);
    }

    /**
     * Converts a 3D {@code Complex[][][]} array to an interleaved complex
     * {@code double[][][]} array.
     *
     * @param c 3D Complex array
     * @param interleavedDim Depth level of the array to interleave
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][][] complex2Interleaved(Complex[][][] c, int interleavedDim) {
        if (interleavedDim > 2 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        int w = c.length;
        int h = c[0].length;
        int d = c[0][0].length;
        double[][][] i;
        if (interleavedDim == 0) {
            i = new double[2 * w][h][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x * 2][y][z] = c[x][y][z].getReal();
                        i[x * 2 + 1][y][z] = c[x][y][z].getImaginary();
                    }
                }
            }
        } else if (interleavedDim == 1) {
            i = new double[w][2 * h][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x][y * 2][z] = c[x][y][z].getReal();
                        i[x][y * 2 + 1][z] = c[x][y][z].getImaginary();
                    }
                }
            }
        } else {
            i = new double[w][h][2 * d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x][y][z * 2] = c[x][y][z].getReal();
                        i[x][y][z * 2 + 1] = c[x][y][z].getImaginary();
                    }
                }
            }
        }
        return i;
    }

    /**
     * Converts a 4D {@code Complex[][][][]} array to an interleaved complex
     * {@code double[][][][]} array.
     *
     * @param c 4D Complex array
     * @param interleavedDim Depth level of the array to interleave
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][][][] complex2Interleaved(Complex[][][][] c, int interleavedDim) {
        if (interleavedDim > 3 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        int w = c.length;
        int h = c[0].length;
        int d = c[0][0].length;
        int v = c[0][0][0].length;
        double[][][][] i;
        if (interleavedDim == 0) {
            i = new double[2 * w][h][d][v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                            i[x * 2][y][z][t] = c[x][y][z][t].getReal();
                            i[x * 2 + 1][y][z][t] = c[x][y][z][t].getImaginary();
                        }
                    }
                }
            }
        } else if (interleavedDim == 1) {
            i = new double[w][2 * h][d][v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                            i[x][y * 2][z][t] = c[x][y][z][t].getReal();
                            i[x][y * 2 + 1][z][t] = c[x][y][z][t].getImaginary();
                        }
                    }
                }
            }
        } else if (interleavedDim == 2) {
            i = new double[w][h][2 * d][v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                        i[x][y][z * 2][t] = c[x][y][z][t].getReal();
                        i[x][y][z * 2 + 1][t] = c[x][y][z][t].getImaginary();
                        }
                    }
                }
            }
        } else {
            i = new double[w][h][d][2 * v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                        i[x][y][z][t * 2] = c[x][y][z][t].getReal();
                        i[x][y][z][t * 2 + 1] = c[x][y][z][t].getImaginary();
                        }
                    }
                }
            }
        }
        return i;
    }

    /**
     * Converts a 3D {@code Complex[][][]} array to an interleaved complex
     * {@code double[][][]} array. The third level of the array is
     * interleaved.
     *
     * @param c 3D Complex array
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][][] complex2Interleaved(Complex[][][] c) {
        return complex2Interleaved(c, 2);
    }

    /**
     * Converts a 4D {@code Complex[][][][]} array to an interleaved complex
     * {@code double[][][][]} array. The fourth level of the array is
     * interleaved.
     *
     * @param c 4D Complex array
     * @return complex interleaved array alternating real and
     *         imaginary values
     */
    public static double[][][][] complex2Interleaved(Complex[][][][] c) {
        return complex2Interleaved(c, 3);
    }

    /**
     * Converts a 2D {@code Complex[][]} array to an interleaved complex
     * {@code float[][]} array.
     *
     * @param c 2D Complex array
     * @param interleavedDim Depth level of the array to interleave
     * @return complex interleaved {@code float[][]} alternating real and
     *         imaginary values
     */
    public static float[][] complex2InterleavedFloat(Complex[][] c, int interleavedDim) {
        if (interleavedDim > 1 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = c.length;
        final int h = c[0].length;
        float[][] i;
        if (interleavedDim == 0) {
            i = new float[2 * w][h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    i[x * 2][y] = (float) c[x][y].getReal();
                    i[x * 2 + 1][y] = (float) c[x][y].getImaginary();
                }
            }
        } else {
            i = new float[w][2 * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    i[x][y * 2] = (float) c[x][y].getReal();
                    i[x][y * 2 + 1] = (float) c[x][y].getImaginary();
                }
            }
        }
        return i;
    }

    /**
     * Converts a 2D {@code Complex[][]} array to an interleaved complex
     * {@code float[][]} array. The second d level of the array is assumed
     * to be interleaved.
     *
     * @param c 2D Complex array
     *
     * @return complex interleaved {@code float[][]} alternating real and
     *         imaginary values
     */
    public static float[][] complex2InterleavedFloat(Complex[][] c) {
        return complex2InterleavedFloat(c, 1);
    }

    /**
     * Converts a 3D {@code Complex[][][]} array to an interleaved complex
     * {@code float[][][]} array.
     *
     * @param c 3D Complex array
     * @param interleavedDim Depth level of the array to interleave
     * @return complex interleaved {@code float[][][]} alternating real and
     *         imaginary values
     */
    public static float[][][] complex2InterleavedFloat(Complex[][][] c, int interleavedDim) {
        if (interleavedDim > 2 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = c.length;
        final int h = c[0].length;
        final int d = c[0][0].length;
        float[][][] i;
        if (interleavedDim == 0) {
            i = new float[2 * w][h][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x * 2][y][z] = (float) c[x][y][z].getReal();
                        i[x * 2 + 1][y][z] = (float) c[x][y][z].getImaginary();
                    }
                }
            }
        } else if (interleavedDim == 1) {
            i = new float[w][2 * h][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x][y * 2][z] = (float) c[x][y][z].getReal();
                        i[x][y * 2 + 1][z] = (float) c[x][y][z].getImaginary();
                    }
                }
            }
        } else {
            i = new float[w][h][2 * d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        i[x][y][z * 2] = (float) c[x][y][z].getReal();
                        i[x][y][z * 2 + 1] = (float) c[x][y][z].getImaginary();
                    }
                }
            }
        }
        return i;
    }

    /**
     * Converts a 3D {@code Complex[][][]} array to an interleaved complex
     * {@code float[][][]} array. The third d level of the array is
     * interleaved.
     *
     * @param c 2D Complex array
     *
     * @return complex interleaved {@code float[][][]} alternating real and
     *         imaginary values
     */
    public static float[][][] complex2InterleavedFloat(Complex[][][] c) {
        return complex2InterleavedFloat(c, 2);
    }

    /**
     * Converts a 2D interleaved complex {@code double[][]} array to a
     * {@code Complex[][]} array.
     *
     * @param i 2D complex interleaved array
     * @param interleavedDim Depth level of the array to interleave
     * @return 2D {@code Complex} array
     */
    public static Complex[][] interleaved2Complex(double[][] i, int interleavedDim) {
        if (interleavedDim > 1 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = i.length;
        final int h = i[0].length;
        Complex[][] c;
        if (interleavedDim == 0) {
            c = new Complex[w / 2][h];
            for (int x = 0; x < w / 2; x++) {
                for (int y = 0; y < h; y++) {
                    c[x][y] = Complex.ofCartesian(i[x * 2][y], i[x * 2 + 1][y]);
                }
            }
        } else {
            c = new Complex[w][h / 2];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h / 2; y++) {
                    c[x][y] = Complex.ofCartesian(i[x][y * 2], i[x][y * 2 + 1]);
                }
            }
        }
        return c;
    }

    /**
     * Converts a 2D interleaved complex {@code double[][]} array to a
     * {@code Complex[][]} array. The second d level of the array is assumed
     * to be interleaved.
     *
     * @param d 2D complex interleaved array
     * @return 2D {@code Complex} array
     */
    public static Complex[][] interleaved2Complex(double[][] d) {
        return interleaved2Complex(d, 1);
    }

    /**
     * Converts a 3D interleaved complex {@code double[][][]} array to a
     * {@code Complex[][][]} array.
     *
     * @param i 3D complex interleaved array
     * @param interleavedDim Depth level of the array to interleave
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] interleaved2Complex(double[][][] i, int interleavedDim) {
        if (interleavedDim > 2 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = i.length;
        final int h = i[0].length;
        final int d = i[0][0].length;
        Complex[][][] c;
        if (interleavedDim == 0) {
            c = new Complex[w / 2][h][d];
            for (int x = 0; x < w / 2; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x * 2][y][z], i[x * 2 + 1][y][z]);
                    }
                }
            }
        } else if (interleavedDim == 1) {
            c = new Complex[w][h / 2][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h / 2; y++) {
                    for (int z = 0; z < d; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x][y * 2][z], i[x][y * 2 + 1][z]);
                    }
                }
            }
        } else {
            c = new Complex[w][h][d / 2];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d / 2; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x][y][z * 2], i[x][y][z * 2 + 1]);
                    }
                }
            }
        }
        return c;
    }

    /**
     * Converts a 4D interleaved complex {@code double[][][][]} array to a
     * {@code Complex[][][][]} array.
     *
     * @param i 4D complex interleaved array
     * @param interleavedDim Depth level of the array to interleave
     * @return 4D {@code Complex} array
     */
    public static Complex[][][][] interleaved2Complex(double[][][][] i, int interleavedDim) {
        if (interleavedDim > 3 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = i.length;
        final int h = i[0].length;
        final int d = i[0][0].length;
        final int v = i[0][0][0].length;
        Complex[][][][] c;
        if (interleavedDim == 0) {
            c = new Complex[w / 2][h][d][v];
            for (int x = 0; x < w / 2; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                            c[x][y][z][t] = Complex.ofCartesian(i[x * 2][y][z][t], i[x * 2 + 1][y][z][t]);
                        }
                    }
                }
            }
        } else if (interleavedDim == 1) {
            c = new Complex[w][h / 2][d][v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h / 2; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v; t++) {
                            c[x][y][z][t] = Complex.ofCartesian(i[x][y * 2][z][t], i[x][y * 2 + 1][z][t]);
                        }
                    }
                }
            }
        } else if (interleavedDim == 2) {
            c = new Complex[w][h][d / 2][v];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d / 2; z++) {
                        for (int t = 0; t < v; t++) {
                            c[x][y][z][t] = Complex.ofCartesian(i[x][y][z * 2][t], i[x][y][z * 2 + 1][t]);
                        }
                    }
                }
            }
        } else {
            c = new Complex[w][h][d][v / 2];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        for (int t = 0; t < v / 2; t++) {
                            c[x][y][z][t] = Complex.ofCartesian(i[x][y][z][t * 2], i[x][y][z][t * 2 + 1]);
                        }
                    }
                }
            }
        }
        return c;
    }

    /**
     * Converts a 3D interleaved complex {@code double[][][]} array to a
     * {@code Complex[][][]} array. The third d level is assumed to be
     * interleaved.
     *
     * @param d 3D complex interleaved array
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] interleaved2Complex(double[][][] d) {
        return interleaved2Complex(d, 2);
    }

    /**
     * Converts a 2D interleaved complex {@code float[][]} array to a
     * {@code Complex[][]} array.
     *
     * @param i 2D complex interleaved float array
     * @param interleavedDim Depth level of the array to interleave
     * @return 2D {@code Complex} array
     */
    public static Complex[][] interleaved2Complex(float[][] i, int interleavedDim) {
        if (interleavedDim > 1 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = i.length;
        final int h = i[0].length;
        Complex[][] c;
        if (interleavedDim == 0) {
            c = new Complex[w / 2][h];
            for (int x = 0; x < w / 2; x++) {
                for (int y = 0; y < h; y++) {
                    c[x][y] = Complex.ofCartesian(i[x * 2][y], i[x * 2 + 1][y]);
                }
            }
        } else {
            c = new Complex[w][h / 2];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h / 2; y++) {
                    c[x][y] = Complex.ofCartesian(i[x][y * 2], i[x][y * 2 + 1]);
                }
            }
        }
        return c;
    }

    /**
     * Converts a 2D interleaved complex {@code float[][]} array to a
     * {@code Complex[][]} array. The second d level of the array is assumed
     * to be interleaved.
     *
     * @param d 2D complex interleaved float array
     * @return 2D {@code Complex} array
     */
    public static Complex[][] interleaved2Complex(float[][] d) {
        return interleaved2Complex(d, 1);
    }

    /**
     * Converts a 3D interleaved complex {@code float[][][]} array to a
     * {@code Complex[][][]} array.
     *
     * @param i 3D complex interleaved float array
     * @param interleavedDim Depth level of the array to interleave
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] interleaved2Complex(float[][][] i, int interleavedDim) {
        if (interleavedDim > 2 || interleavedDim < 0) {
            throw new IndexOutOfRangeException(interleavedDim);
        }
        final int w = i.length;
        final int h = i[0].length;
        final int d = i[0][0].length;
        Complex[][][] c;
        if (interleavedDim == 0) {
            c = new Complex[w / 2][h][d];
            for (int x = 0; x < w/2; x ++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x * 2][y][z], i[x * 2 + 1][y][z]);
                    }
                }
            }
        } else if (interleavedDim == 1) {
            c = new Complex[w][h / 2][d];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h/2; y ++) {
                    for (int z = 0; z < d; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x][y * 2][z], i[x][y * 2 + 1][z]);
                    }
                }
            }
        } else {
            c = new Complex[w][h][d / 2];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < d/2; z++) {
                        c[x][y][z] = Complex.ofCartesian(i[x][y][z * 2], i[x][y][z * 2 + 1]);
                    }
                }
            }
        }
        return c;
    }

    /**
     * Converts a 3D interleaved complex {@code float[][][]} array to a
     * {@code Complex[]} array. The third level of the array is assumed to
     * be interleaved.
     *
     * @param d 3D complex interleaved float array
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] interleaved2Complex(float[][][] d) {
        return interleaved2Complex(d, 2);
    }

    // SPLIT METHODS

    /**
     * Converts a split complex array {@code double[] r, double[] i} to a
     * {@code Complex[]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return {@code Complex} array
     */
    public static Complex[] split2Complex(double[] real, double[] imag) {
        final int length = real.length;
        final Complex[] c = new Complex[length];
        for (int n = 0; n < length; n++) {
            c[n] = Complex.ofCartesian(real[n], imag[n]);
        }
        return c;
    }

    /**
     * Converts a 2D split complex array {@code double[][] r, double[][] i} to a
     * 2D {@code Complex[][]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return 2D {@code Complex} array
     */
    public static Complex[][] split2Complex(double[][] real, double[][] imag) {
        final int length = real.length;
        Complex[][] c = new Complex[length][];
        for (int x = 0; x < length; x++) {
            c[x] = split2Complex(real[x], imag[x]);
        }
        return c;
    }

    /**
     * Converts a 3D split complex array {@code double[][][] r, double[][][] i}
     * to a 3D {@code Complex[][][]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] split2Complex(double[][][] real, double[][][] imag) {
        final int length = real.length;
        Complex[][][] c = new Complex[length][][];
        for (int x = 0; x < length; x++) {
            c[x] = split2Complex(real[x], imag[x]);
        }
        return c;
    }

    /**
     * Converts a 4D split complex array {@code double[][][][] r, double[][][][] i}
     * to a 4D {@code Complex[][][][]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return 4D {@code Complex} array
     */
    public static Complex[][][][] split2Complex(double[][][][] real, double[][][][] imag) {
        final int length = real.length;
        Complex[][][][] c = new Complex[length][][][];
        for (int x = 0; x < length; x++) {
            c[x] = split2Complex(real[x], imag[x]);
        }
        return c;
    }

    /**
     * Converts a split complex array {@code float[] r, float[] i} to a
     * {@code Complex[]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return {@code Complex} array
     */
    public static Complex[] split2Complex(float[] real, float[] imag) {
        final int length = real.length;
        final Complex[] c = new Complex[length];
        for (int n = 0; n < length; n++) {
            c[n] = Complex.ofCartesian(real[n], imag[n]);
        }
        return c;
    }

    /**
     * Converts a 2D split complex array {@code float[][] r, float[][] i} to a
     * 2D {@code Complex[][]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return 2D {@code Complex} array
     */
    public static Complex[][] split2Complex(float[][] real, float[][] imag) {
        final int length = real.length;
        Complex[][] c = new Complex[length][];
        for (int x = 0; x < length; x++) {
            c[x] = split2Complex(real[x], imag[x]);
        }
        return c;
    }

    /**
     * Converts a 3D split complex array {@code float[][][] r, float[][][] i} to
     * a 3D {@code Complex[][][]} array.
     *
     * @param real real component
     * @param imag imaginary component
     * @return 3D {@code Complex} array
     */
    public static Complex[][][] split2Complex(float[][][] real, float[][][] imag) {
        final int length = real.length;
        Complex[][][] c = new Complex[length][][];
        for (int x = 0; x < length; x++) {
            c[x] = split2Complex(real[x], imag[x]);
        }
        return c;
    }

    // MISC

    /**
     * Initializes a {@code Complex[]} array to zero, to avoid
     * NullPointerExceptions.
     *
     * @param c Complex array
     * @return c
     */
    public static Complex[] initialize(Complex[] c) {
        final int length = c.length;
        for (int x = 0; x < length; x++) {
            c[x] = Complex.ZERO;
        }
        return c;
    }

    /**
     * Initializes a {@code Complex[][]} array to zero, to avoid
     * NullPointerExceptions.
     *
     * @param c {@code Complex} array
     * @return c
     */
    public static Complex[][] initialize(Complex[][] c) {
        final int length = c.length;
        for (int x = 0; x < length; x++) {
            c[x] = initialize(c[x]);
        }
        return c;
    }

    /**
     * Initializes a {@code Complex[][][]} array to zero, to avoid
     * NullPointerExceptions.
     *
     * @param c {@code Complex} array
     * @return c
     */
    public static Complex[][][] initialize(Complex[][][] c) {
        final int length = c.length;
        for (int x = 0; x < length; x++) {
            c[x] = initialize(c[x]);
        }
        return c;
    }

    /**
     * Returns {@code double[]} containing absolute values (magnitudes) of a
     * {@code Complex[]} array.
     *
     * @param c {@code Complex} array
     * @return {@code double[]}
     */
    public static double[] abs(Complex[] c) {
        final int length = c.length;
        final double[] i = new double[length];
        for (int x = 0; x < length; x++) {
            i[x] = c[x].abs();
        }
        return i;
    }

    /**
     * Returns {@code double[]} containing arguments (phase angles) of a
     * {@code Complex[]} array.
     *
     * @param c {@code Complex} array
     * @return {@code double[]} array
     */
    public static double[] arg(Complex[] c) {
        final int length = c.length;
        final double[] i = new double[length];
        for (int x = 0; x < length; x++) {
            i[x] = c[x].getArgument();
        }
        return i;
    }

    /**
     * Exception to be throw when a negative value is passed as the modulus.
     */
    private static class NegativeModulusException extends IllegalArgumentException {
        /** Serializable version identifier. */
        private static final long serialVersionUID = 20181205L;

        /**
         * @param r Wrong modulus.
         */
        NegativeModulusException(double r) {
            super("Modulus is negative: " + r);
        }
    }

    /**
     * Exception to be throw when an out-of-range index value is passed.
     */
    private static class IndexOutOfRangeException extends IllegalArgumentException {
        /** Serializable version identifier. */
        private static final long serialVersionUID = 20181205L;

        /**
         * @param i Wrong index.
         */
        IndexOutOfRangeException(int i) {
            super("Out of range: " + i);
        }
    }
}
