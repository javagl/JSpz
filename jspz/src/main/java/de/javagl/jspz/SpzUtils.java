/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 *
 * Parts of this class are taken from https://github.com/nianticlabs/spz,
 * published under the MIT license. Original copyright statement:
 *
 * Copyright (c) 2024 Niantic Labs
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jspz;

/**
 * Utility methods for this package
 */
class SpzUtils
{
    /**
     * The magic SPZ header
     */
    static final int MAGIC = 0x5053474e;

    /**
     * An inverse sigmoid function that is used for converting the raw alpha
     * values (normalized to the range [0...1]) into another value in [-Inf,
     * Inf] for whatever reason.
     * 
     * Taken from https://github.com/nianticlabs/spz, because the details are
     * not specified.
     * 
     * @param x The argument
     * @return The result
     */
    static float invSigmoid(float x)
    {
        float f = (float) Math.log(x / (1.0 - x));
        // TODO This does not make sense, but neither does dealing with 
        // infinity. See https://github.com/nianticlabs/spz/issues/22
        return Math.max(-20.0f, Math.min(20.0f, f));
    }

    /**
     * The sigmoid function that is used for converting the alpha value from the
     * range [-Inf, Inf] to a value in [0, 1].
     * 
     * See {@link #invSigmoid(float)}
     * 
     * @param x The argument
     * @return The result
     */
    static float sigmoid(float x)
    {
        return 1.0f / (1.0f + (float) Math.exp(-x));
    }

    /**
     * Converts the given value into a byte, by rounding it and clamping it to
     * the range [0, 255].
     * 
     * @param x The input value
     * @return The result
     */
    static byte toByte(float x)
    {
        return (byte) (Math.min(255, Math.max(0, Math.round(x))));
    }

    /**
     * Returns the number of dimensions for the given spherical harmonics
     * degree.
     * 
     * @param degree The degree
     * @return The dimensions
     */
    static int dimensionsForDegree(int degree)
    {
        return (degree + 1) * (degree + 1) - 1;
    }

    /**
     * Quantize the given value for spherical harmonics quantization.
     * 
     * Taken from https://github.com/nianticlabs/spz, because the details are
     * not specified.
     * 
     * @param x The input value
     * @param bucketSize The bucket size
     * @return The result
     */
    static byte quantize(float x, int bucketSize)
    {
        int q = Math.round(x * 128.0f) + 128;
        q = (q + bucketSize / 2) / bucketSize * bucketSize;
        return toByte(q);
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SpzUtils()
    {
        // Private constructor to prevent instantiation
    }
}