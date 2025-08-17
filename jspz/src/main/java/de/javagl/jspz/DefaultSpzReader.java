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
    @Override
    public GaussianCloud read(InputStream spzInputStream) throws IOException
    {
        InputStream spInputStream = new GZIPInputStream(spzInputStream);
        DataInput dataInput = new DataInputStream(spInputStream);

        byte headerBytes[] = new byte[16];
        dataInput.readFully(headerBytes);

        ByteBuffer headerBuffer = Buffers.wrap(headerBytes);
        int magic = headerBuffer.getInt(0);
        if (magic != SpzUtils.MAGIC)
        {
            throw new IOException(
                "Expected magic to be " + Integer.toHexString(SpzUtils.MAGIC)
                    + ", but it is " + Integer.toHexString(magic));
        }
        int version = headerBuffer.getInt(4);
        if (version != 2 && version != 3)
        {
            throw new IOException(
                "Expected version to be 2 or 3, but it is " + version);
        }
        int numPoints = headerBuffer.getInt(8);
        byte shDegree = headerBuffer.get(12);
        byte fractionalBits = headerBuffer.get(13);
        byte flags = headerBuffer.get(14);
        boolean antialiased = (flags & 1) != 0;

        int positionBytes = 3;
        int rotationBytes = (version == 3) ? 4 : 3; // Yes!
        RawGaussianCloud r = new RawGaussianCloud(numPoints, positionBytes,
            rotationBytes, shDegree, fractionalBits, antialiased);

        // Yeah. The order is mentioned in the README, but
        // does not match the order of the sections.
        dataInput.readFully(r.positions);
        dataInput.readFully(r.alphas);
        dataInput.readFully(r.colors);
        dataInput.readFully(r.scales);
        dataInput.readFully(r.rotations);
        dataInput.readFully(r.sh);

        if (version == 2)
        {
            GaussianCloud g = convertV2(r);
            return g;
        }
        GaussianCloud g = convertV3(r);
        return g;
    }

    /**
     * Converts the given {@link RawGaussianCloud} storing data in SPZ version 2
     * into a {@link GaussianCloud}.
     * 
     * @param raw The {@link RawGaussianCloud}
     * @return The {@link GaussianCloud}
     */
    private static GaussianCloud convertV2(RawGaussianCloud raw)
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

        RawGaussianClouds.convertPositions(raw.positions, positions,
            raw.fractionalBits);
        RawGaussianClouds.convertScales(raw.scales, scales);
        RawGaussianClouds.convertRotationsV2(raw.rotations, rotations);
        RawGaussianClouds.convertAlphas(raw.alphas, alphas);
        RawGaussianClouds.convertColors(raw.colors, colors);
        RawGaussianClouds.convertShs(raw.sh, sh);

        return result;
    }

    /**
     * Converts the given {@link RawGaussianCloud} storing data in SPZ version 3
     * into a {@link GaussianCloud}.
     * 
     * @param raw The {@link RawGaussianCloud}
     * @return The {@link GaussianCloud}
     */
    private static GaussianCloud convertV3(RawGaussianCloud raw)
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

        RawGaussianClouds.convertPositions(raw.positions, positions,
            raw.fractionalBits);
        RawGaussianClouds.convertScales(raw.scales, scales);
        RawGaussianClouds.convertRotationsV3(raw.rotations, rotations);
        RawGaussianClouds.convertAlphas(raw.alphas, alphas);
        RawGaussianClouds.convertColors(raw.colors, colors);
        RawGaussianClouds.convertShs(raw.sh, sh);

        return result;
    }
}