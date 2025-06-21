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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.zip.GZIPOutputStream;

/**
 * Default implementation of an {@link SpzWriter}
 */
class DefaultSpzWriter implements SpzWriter
{
    @Override
    public void write(GaussianCloud gaussianCloud, OutputStream spzOutputStream)
        throws IOException
    {
        GZIPOutputStream spOutputStream = new GZIPOutputStream(spzOutputStream);
        DataOutputStream dataOutput = new DataOutputStream(spOutputStream);
        int fractionalBits = 12;

        byte flags = 0;
        if (gaussianCloud.isAntialiased())
        {
            flags |= 1;
        }

        byte headerBytes[] = new byte[16];
        ByteBuffer headerBuffer = Buffers.wrap(headerBytes);
        headerBuffer.putInt(0, SpzUtils.MAGIC);
        int version = 2;
        headerBuffer.putInt(4, version);
        headerBuffer.putInt(8, gaussianCloud.getNumPoints());
        headerBuffer.put(12, (byte) gaussianCloud.getShDegree());
        headerBuffer.put(13, (byte) fractionalBits);
        headerBuffer.put(14, flags);
        dataOutput.write(headerBytes);

        RawGaussianCloudV2 r = convert(gaussianCloud, fractionalBits);

        dataOutput.write(r.positions);
        dataOutput.write(r.alphas);
        dataOutput.write(r.colors);
        dataOutput.write(r.scales);
        dataOutput.write(r.rotations);
        dataOutput.write(r.sh);

        dataOutput.flush();
        spOutputStream.finish();
        spOutputStream.flush();
    }

    /**
     * Converts the given {@link GaussianCloud} into a
     * {@link RawGaussianCloudV2}.
     * 
     * This is ... "heavily inspired" by the "packGaussians" function from
     * https://github.com/nianticlabs/spz, because many details are not
     * specified for the SPZ format itself.
     * 
     * @param g The {@link GaussianCloud}
     * @param fractionalBits The fractional bits
     * @return The {@link RawGaussianCloudV2}
     */
    private static RawGaussianCloudV2 convert(GaussianCloud g,
        int fractionalBits)
    {
        int numPoints = g.getNumPoints();
        int shDegree = g.getShDegree();
        boolean antialiased = g.isAntialiased();

        RawGaussianCloudV2 r = new RawGaussianCloudV2(numPoints, shDegree,
            fractionalBits, antialiased);

        FloatBuffer positions = g.getPositions();
        FloatBuffer scales = g.getScales();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();
        FloatBuffer sh = g.getSh();

        float scale = (1 << fractionalBits);
        for (int i = 0; i < numPoints * 3; i++)
        {
            int packed = Math.round(positions.get(i) * scale);
            r.positions[i * 3 + 0] = (byte) (packed & 0xFF);
            r.positions[i * 3 + 1] = (byte) ((packed >> 8) & 0xFF);
            r.positions[i * 3 + 2] = (byte) ((packed >> 16) & 0xFF);
        }

        for (int i = 0; i < numPoints * 3; i++)
        {
            byte s = SpzUtils.toByte((scales.get(i) + 10.0f) * 16.0f);
            r.scales[i] = s;
        }

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

            r.rotations[i * 3 + 0] = SpzUtils.toByte(x);
            r.rotations[i * 3 + 1] = SpzUtils.toByte(y);
            r.rotations[i * 3 + 2] = SpzUtils.toByte(z);
        }

        for (int i = 0; i < numPoints; i++)
        {
            float a = alphas.get(i);
            r.alphas[i] = SpzUtils.toByte(SpzUtils.sigmoid(a) * 255.0f);
        }

        float colorScale = 0.15f;
        for (int i = 0; i < numPoints * 3; i++)
        {
            float c = colors.get(i);
            r.colors[i] =
                SpzUtils.toByte(c * (colorScale * 255.0f) + (0.5f * 255.0f));
        }

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

                r.sh[i0] = SpzUtils.quantize(sh.get(i0), bucketSize);
                r.sh[i1] = SpzUtils.quantize(sh.get(i1), bucketSize);
                r.sh[i2] = SpzUtils.quantize(sh.get(i2), bucketSize);
            }
        }

        return r;
    }

}