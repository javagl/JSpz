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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.zip.GZIPInputStream;

/**
 * Default implementation of an {@link SpzReader}
 */
class DefaultSpzReader implements SpzReader
{
    /**
     * The magic SPZ header
     */
    private static final int MAGIC = 0x5053474e;

    /**
     * Internal, intermediate representation of the SPZ data, as it is read from
     * the file.
     */
    @SuppressWarnings("javadoc")
    private static class RawGaussianCloudV2
    {
        final int numPoints;
        final int shDegree;
        final int fractionalBits;
        final boolean antialiased;

        final byte positions[];
        final byte scales[];
        final byte rotations[];
        final byte alphas[];
        final byte colors[];
        final byte sh[];

        RawGaussianCloudV2(int numPoints, int shDegree, int fractionalBits,
            boolean antialiased)
        {
            this.numPoints = numPoints;
            this.shDegree = shDegree;
            this.fractionalBits = fractionalBits;
            this.antialiased = antialiased;

            int shDim = SpzUtils.dimensionsForDegree(shDegree);
            this.positions = new byte[numPoints * 3 * 3];
            this.scales = new byte[numPoints * 3];
            this.rotations = new byte[numPoints * 3];
            this.alphas = new byte[numPoints];
            this.colors = new byte[numPoints * 3];
            this.sh = new byte[numPoints * shDim * 3];
        }

    }

    @Override
    public GaussianCloud read(InputStream spzInputStream) throws IOException
    {
        InputStream spInputStream = new GZIPInputStream(spzInputStream);
        DataInput dataInput = new DataInputStream(spInputStream);

        byte headerBytes[] = new byte[16];
        dataInput.readFully(headerBytes);

        ByteBuffer headerBuffer = Buffers.wrap(headerBytes);
        int magic = headerBuffer.getInt(0);
        if (magic != MAGIC)
        {
            throw new IOException(
                "Expected magic to be " + Integer.toHexString(MAGIC)
                    + ", but it is " + Integer.toHexString(magic));
        }
        int version = headerBuffer.getInt(4);
        if (version != 2)
        {
            throw new IOException(
                "Expected version to be 2, but it is " + version);
        }
        int numPoints = headerBuffer.getInt(8);
        byte shDegree = headerBuffer.get(12);
        byte fractionalBits = headerBuffer.get(13);
        byte flags = headerBuffer.get(14);

        boolean antialiased = (flags & 1) != 0;

        RawGaussianCloudV2 r = new RawGaussianCloudV2(numPoints, shDegree,
            fractionalBits, antialiased);

        // Yeah. The order is mentioned in the README, but
        // does not match the order of the sections.
        dataInput.readFully(r.positions);
        dataInput.readFully(r.alphas);
        dataInput.readFully(r.colors);
        dataInput.readFully(r.scales);
        dataInput.readFully(r.rotations);
        dataInput.readFully(r.sh);

        GaussianCloud g = convert(r);
        return g;
    }

    /**
     * Converts the given {@link RawGaussianCloudV2} into a
     * {@link GaussianCloud}.
     * 
     * This is ... "heavily inspired" by the "unpackGaussians" function from
     * https://github.com/nianticlabs/spz, because many details are not
     * specified for the SPZ format itself.
     * 
     * @param raw The {@link RawGaussianCloudV2}
     * @return The {@link GaussianCloud}
     */
    private static GaussianCloud convert(RawGaussianCloudV2 raw)
    {
        int numPoints = raw.numPoints;
        int shDegree = raw.shDegree;
        boolean antialiased = raw.antialiased;

        GaussianCloud result =
            new DefaultGaussianCloud(numPoints, shDegree, antialiased);

        FloatBuffer positions = result.getPositions();
        FloatBuffer scales = result.getScales();
        FloatBuffer rotations = result.getRotations();
        FloatBuffer alphas = result.getAlphas();
        FloatBuffer colors = result.getColors();
        FloatBuffer sh = result.getSh();

        float scale = 1.0f / (1 << raw.fractionalBits);
        for (int i = 0; i < numPoints * 3; i++)
        {
            int p0 = Byte.toUnsignedInt(raw.positions[i * 3 + 0]);
            int p1 = Byte.toUnsignedInt(raw.positions[i * 3 + 1]);
            int p2 = Byte.toUnsignedInt(raw.positions[i * 3 + 2]);
            int p = 0;
            p |= p0;
            p |= p1 << 8;
            p |= p2 << 16;
            p |= ((p & 0x800000) != 0) ? 0xff000000 : 0;
            positions.put(i, p * scale);
        }

        for (int i = 0; i < numPoints * 3; i++)
        {
            int s = Byte.toUnsignedInt(raw.scales[i]);
            scales.put(i, s / 16.0f - 10.0f);
        }

        float invRotation = 1.0f / 127.5f;
        for (int i = 0; i < numPoints; i++)
        {
            int r0 = Byte.toUnsignedInt(raw.rotations[i * 3 + 0]);
            int r1 = Byte.toUnsignedInt(raw.rotations[i * 3 + 1]);
            int r2 = Byte.toUnsignedInt(raw.rotations[i * 3 + 2]);
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

        float invByte = 1.0f / 255.0f;
        for (int i = 0; i < numPoints; i++)
        {
            int a = Byte.toUnsignedInt(raw.alphas[i]);
            alphas.put(i, SpzUtils.invSigmoid(a * invByte));
        }

        float invColorScale = 1.0f / 0.15f;
        for (int i = 0; i < numPoints * 3; i++)
        {
            int c = Byte.toUnsignedInt(raw.colors[i]);
            float cf = ((c * invByte) - 0.5f) * invColorScale;
            colors.put(i, cf);
        }

        float invHalfByte = 1.0f / 128.0f;
        for (int i = 0; i < raw.sh.length; i++)
        {
            int sr = Byte.toUnsignedInt(raw.sh[i]);
            float sf = (sr - 128.0f) * invHalfByte;
            sh.put(i, sf);
        }

        return result;
    }

}