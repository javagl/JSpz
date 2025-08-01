/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
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

import java.nio.FloatBuffer;

/**
 * Methods related to raw Gaussian cloud representations.
 * 
 * This is ... "heavily inspired" by the "unpackGaussians" function from
 * https://github.com/nianticlabs/spz, because many details are not specified
 * for the SPZ format itself.
 */
class RawGaussianClouds
{
    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}
     * 
     * @param rawPositions The input data
     * @param positions The data for the Gaussian cloud
     * @param fractionalBits The number of fractional bits
     */
    static void convertPositions(byte rawPositions[], FloatBuffer positions,
        int fractionalBits)
    {
        int numPoints = positions.capacity() / 3;
        float scale = 1.0f / (1 << fractionalBits);
        for (int i = 0; i < numPoints * 3; i++)
        {
            int p0 = Byte.toUnsignedInt(rawPositions[i * 3 + 0]);
            int p1 = Byte.toUnsignedInt(rawPositions[i * 3 + 1]);
            int p2 = Byte.toUnsignedInt(rawPositions[i * 3 + 2]);
            int p = 0;
            p |= p0;
            p |= p1 << 8;
            p |= p2 << 16;
            p |= ((p & 0x800000) != 0) ? 0xff000000 : 0;
            positions.put(i, p * scale);
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}
     * 
     * @param rawScales The raw data
     * @param scales The data for the Gaussian cloud
     */
    static void convertScales(byte[] rawScales, FloatBuffer scales)
    {
        int numPoints = scales.capacity() / 3;
        for (int i = 0; i < numPoints * 3; i++)
        {
            int s = Byte.toUnsignedInt(rawScales[i]);
            scales.put(i, s / 16.0f - 10.0f);
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}, assuming that the input is encoded according to
     * SPZ version 2.
     * 
     * @param rawRotations The raw data
     * @param rotations The data for the Gaussian cloud
     */
    static void convertRotationsV2(byte[] rawRotations, FloatBuffer rotations)
    {
        int numPoints = rotations.capacity() / 4;
        float invRotation = 1.0f / 127.5f;
        for (int i = 0; i < numPoints; i++)
        {
            int r0 = Byte.toUnsignedInt(rawRotations[i * 3 + 0]);
            int r1 = Byte.toUnsignedInt(rawRotations[i * 3 + 1]);
            int r2 = Byte.toUnsignedInt(rawRotations[i * 3 + 2]);
            float q0 = r0 * invRotation - 1.0f;
            float q1 = r1 * invRotation - 1.0f;
            float q2 = r2 * invRotation - 1.0f;
            float sn = q0 * q0 + q1 * q1 + q2 * q2;
            float q3 = (float) Math.sqrt(Math.max(0, 1.0f - sn));
            rotations.put(i * 4 + 0, q0);
            rotations.put(i * 4 + 1, q1);
            rotations.put(i * 4 + 2, q2);
            rotations.put(i * 4 + 3, q3);
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}, assuming that the input is encoded according to
     * SPZ version 3.
     * 
     * @param rawRotations The raw data
     * @param rotations The data for the Gaussian cloud
     */
    static void convertRotationsV3(byte[] rawRotations, FloatBuffer rotations)
    {
        float oneOverSqrt2 = (float) (1.0 / Math.sqrt(2.0));
        int numPoints = rotations.capacity() / 4;
        for (int i = 0; i < numPoints; i++)
        {
            int r0 = Byte.toUnsignedInt(rawRotations[i * 4 + 0]);
            int r1 = Byte.toUnsignedInt(rawRotations[i * 4 + 1]);
            int r2 = Byte.toUnsignedInt(rawRotations[i * 4 + 2]);
            int r3 = Byte.toUnsignedInt(rawRotations[i * 4 + 3]);

            int components = r0 + (r1 << 8) + (r2 << 16) + (r3 << 24);
            int mask = (1 << 9) - 1;
            int indexOfLargest = components >>> 30;
            float sumSquares = 0;
            for (int j = 3; j >= 0; --j)
            {
                if (j != indexOfLargest)
                {
                    int magnitude = components & mask;
                    int signBit = (components >>> 9) & 0x1;
                    float r = oneOverSqrt2 * magnitude / mask;
                    if (signBit == 1)
                    {
                        r = -r;
                    }
                    rotations.put(i * 4 + j, r);
                    sumSquares += r * r;
                    components = components >>> 10;
                }
            }
            float rLargest = (float) Math.sqrt(1.0f - sumSquares);
            rotations.put(i * 4 + indexOfLargest, rLargest);
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}
     * 
     * @param rawAlphas The raw data
     * @param alphas The data for the Gaussian cloud
     */
    static void convertAlphas(byte[] rawAlphas, FloatBuffer alphas)
    {
        int numPoints = alphas.capacity();
        float invByte = 1.0f / 255.0f;
        for (int i = 0; i < numPoints; i++)
        {
            int a = Byte.toUnsignedInt(rawAlphas[i]);
            alphas.put(i, SpzUtils.invSigmoid(a * invByte));
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}
     * 
     * @param rawColors THe raw data
     * @param colors The data for the Gaussian cloud
     */
    static void convertColors(byte[] rawColors, FloatBuffer colors)
    {
        int numPoints = colors.capacity() / 3;
        float invColorScale = 1.0f / 0.15f;
        float invByte = 1.0f / 255.0f;
        for (int i = 0; i < numPoints * 3; i++)
        {
            int c = Byte.toUnsignedInt(rawColors[i]);
            float cf = ((c * invByte) - 0.5f) * invColorScale;
            colors.put(i, cf);
        }
    }

    /**
     * Convert the given raw data into the data that is stored in the actual
     * {@link GaussianCloud}
     * 
     * @param rawSh The raw data
     * @param sh The data for the Gaussian cloud
     */
    static void convertShs(byte[] rawSh, FloatBuffer sh)
    {
        float invHalfByte = 1.0f / 128.0f;
        for (int i = 0; i < rawSh.length; i++)
        {
            int sr = Byte.toUnsignedInt(rawSh[i]);
            float sf = (sr - 128.0f) * invHalfByte;
            sh.put(i, sf);
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in the file.
     * 
     * @param positions The data from the Gaussian cloud
     * @param rawPositions The raw data
     * @param fractionalBits The fractional bits
     */
    static void convertPositions(FloatBuffer positions, byte[] rawPositions,
        int fractionalBits)
    {
        int numPoints = positions.capacity() / 3;
        float scale = (1 << fractionalBits);
        for (int i = 0; i < numPoints * 3; i++)
        {
            int packed = Math.round(positions.get(i) * scale);
            rawPositions[i * 3 + 0] = (byte) (packed & 0xFF);
            rawPositions[i * 3 + 1] = (byte) ((packed >> 8) & 0xFF);
            rawPositions[i * 3 + 2] = (byte) ((packed >> 16) & 0xFF);
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in the file.
     * 
     * @param scales The data from the Gaussian cloud
     * @param rawScales The raw data
     */
    static void convertScales(FloatBuffer scales, byte[] rawScales)
    {
        int numPoints = scales.capacity() / 3;
        for (int i = 0; i < numPoints * 3; i++)
        {
            byte s = SpzUtils.toByte((scales.get(i) + 10.0f) * 16.0f);
            rawScales[i] = s;
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in a file with SPZ version 2.
     * 
     * @param rotations The data from the Gaussian cloud
     * @param rawRotations The raw data
     */
    static void convertRotationsV2(FloatBuffer rotations, byte[] rawRotations)
    {
        int numPoints = rotations.capacity() / 4;
        float quantization = 127.5f;
        for (int i = 0; i < numPoints; i++)
        {
            float x = rotations.get(i * 4 + 0);
            float y = rotations.get(i * 4 + 1);
            float z = rotations.get(i * 4 + 2);
            float w = rotations.get(i * 4 + 3);

            // Normalize
            float invLen =
                1.0f / (float) Math.sqrt(x * x + y * y + z * z + w * w);
            x *= invLen;
            y *= invLen;
            z *= invLen;

            // Quantize
            if (w < 0)
            {
                x *= -quantization;
                y *= -quantization;
                z *= -quantization;
            }
            else
            {
                x *= quantization;
                y *= quantization;
                z *= quantization;
            }
            x += quantization;
            y += quantization;
            z += quantization;

            rawRotations[i * 3 + 0] = SpzUtils.toByte(x);
            rawRotations[i * 3 + 1] = SpzUtils.toByte(y);
            rawRotations[i * 3 + 2] = SpzUtils.toByte(z);
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in the file.
     * 
     * @param alphas The data from the Gaussian cloud
     * @param rawAlphas The raw data
     */
    static void convertAlphas(FloatBuffer alphas, byte[] rawAlphas)
    {
        int numPoints = alphas.capacity();
        for (int i = 0; i < numPoints; i++)
        {
            float a = alphas.get(i);
            rawAlphas[i] = SpzUtils.toByte(SpzUtils.sigmoid(a) * 255.0f);
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in the file.
     * 
     * @param colors The data from the Gaussian cloud
     * @param rawColors The raw data
     */
    static void convertColors(FloatBuffer colors, byte[] rawColors)
    {
        int numPoints = colors.capacity() / 3;
        float colorScale = 0.15f;
        for (int i = 0; i < numPoints * 3; i++)
        {
            float c = colors.get(i);
            rawColors[i] =
                SpzUtils.toByte(c * (colorScale * 255.0f) + (0.5f * 255.0f));
        }
    }

    /**
     * Convert the given data from a {@link GaussianCloud} into the raw data
     * that is stored in the file.
     * 
     * @param sh The data from the Gaussian cloud
     * @param rawSh The raw data
     * @param numPoints The number of points
     * @param shDegree The spherical harmonics degree
     */
    static void convertShs(FloatBuffer sh, byte[] rawSh, int numPoints, int shDegree)
    {
        int shDim = SpzUtils.dimensionsForDegree(shDegree);
        for (int i = 0; i < numPoints; i++)
        {
            for (int j = 0; j < shDim; j++)
            {
                int bucketSize;
                if (j < 3) // dimensionsForDegree(1) = 3
                {
                    // According to the README.md of the SPZ repository at
                    // 9ba83ffedac9016bb76452598cb0dc676ad7e238, the
                    // spherical harmonics for degree 1 are quantized with
                    // 5 bits, and the remaining ones with 4 bits
                    int quantizationBitsDeg0 = 5;
                    bucketSize = 1 << (8 - quantizationBitsDeg0);
                }
                else
                {
                    int quantizationBitsDegN = 4;
                    bucketSize = 1 << (8 - quantizationBitsDegN);
                }
                int index = (i * shDim + j) * 3;
                int i0 = index + 0;
                int i1 = index + 1;
                int i2 = index + 2;

                rawSh[i0] = SpzUtils.quantize(sh.get(i0), bucketSize);
                rawSh[i1] = SpzUtils.quantize(sh.get(i1), bucketSize);
                rawSh[i2] = SpzUtils.quantize(sh.get(i2), bucketSize);
            }
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private RawGaussianClouds()
    {
        // Private constructor to prevent instantiation
    }

}
