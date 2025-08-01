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
 * A class with methods that implement the {@link SpzWriter} interface
 */
class DefaultSpzWriter
{
    /**
     * Write the given {@link GaussianCloud} into the given output stream, using
     * SPZ version 2.
     * 
     * A reference to this method can be used as an {@link SpzWriter}.
     * 
     * @param gaussianCloud The {@link GaussianCloud}
     * @param spzOutputStream The output stream
     * @throws IOException If an IO error occurs
     */
    static void writeV2(GaussianCloud gaussianCloud,
        OutputStream spzOutputStream) throws IOException
    {
        GZIPOutputStream spOutputStream = new GZIPOutputStream(spzOutputStream);
        DataOutputStream dataOutput = new DataOutputStream(spOutputStream);
        int fractionalBits = 12;

        int version = 2;
        byte[] headerBytes =
            createHeader(gaussianCloud, version, fractionalBits);
        dataOutput.write(headerBytes);

        RawGaussianCloud r = convertV2(gaussianCloud, fractionalBits);

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
     * Converts the given {@link GaussianCloud} into a {@link RawGaussianCloud}
     * for SPZ version 2.
     * 
     * @param g The {@link GaussianCloud}
     * @param fractionalBits The fractional bits
     * @return The {@link RawGaussianCloud}
     */
    private static RawGaussianCloud convertV2(GaussianCloud g,
        int fractionalBits)
    {
        int numPoints = g.getNumPoints();
        int shDegree = g.getShDegree();
        boolean antialiased = g.isAntialiased();

        int positionBytes = 3;
        int rotationBytes = 3; // For version 2
        RawGaussianCloud r = new RawGaussianCloud(numPoints, positionBytes,
            rotationBytes, shDegree, fractionalBits, antialiased);

        FloatBuffer positions = g.getPositions();
        FloatBuffer scales = g.getScales();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();
        FloatBuffer sh = g.getSh();

        RawGaussianClouds.convertPositions(positions, r.positions,
            fractionalBits);
        RawGaussianClouds.convertScales(scales, r.scales);
        RawGaussianClouds.convertRotationsV2(rotations, r.rotations);
        RawGaussianClouds.convertAlphas(alphas, r.alphas);
        RawGaussianClouds.convertColors(colors, r.colors);
        RawGaussianClouds.convertShs(sh, r.sh, numPoints, shDegree);

        return r;
    }

    
    /**
     * Write the given {@link GaussianCloud} into the given output stream, using
     * SPZ version 3.
     * 
     * A reference to this method can be used as an {@link SpzWriter}.
     * 
     * @param gaussianCloud The {@link GaussianCloud}
     * @param spzOutputStream The output stream
     * @throws IOException If an IO error occurs
     */
    static void writeV3(GaussianCloud gaussianCloud,
        OutputStream spzOutputStream) throws IOException
    {
        GZIPOutputStream spOutputStream = new GZIPOutputStream(spzOutputStream);
        DataOutputStream dataOutput = new DataOutputStream(spOutputStream);
        int fractionalBits = 12;

        int version = 3;
        byte[] headerBytes =
            createHeader(gaussianCloud, version, fractionalBits);
        dataOutput.write(headerBytes);

        RawGaussianCloud r = convertV3(gaussianCloud, fractionalBits);

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
     * Converts the given {@link GaussianCloud} into a {@link RawGaussianCloud}
     * for SPZ version 3.
     * 
     * @param g The {@link GaussianCloud}
     * @param fractionalBits The fractional bits
     * @return The {@link RawGaussianCloud}
     */
    private static RawGaussianCloud convertV3(GaussianCloud g,
        int fractionalBits)
    {
        int numPoints = g.getNumPoints();
        int shDegree = g.getShDegree();
        boolean antialiased = g.isAntialiased();

        int positionBytes = 3;
        int rotationBytes = 4; // For version 3
        RawGaussianCloud r = new RawGaussianCloud(numPoints, positionBytes,
            rotationBytes, shDegree, fractionalBits, antialiased);

        FloatBuffer positions = g.getPositions();
        FloatBuffer scales = g.getScales();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();
        FloatBuffer sh = g.getSh();

        RawGaussianClouds.convertPositions(positions, r.positions,
            fractionalBits);
        RawGaussianClouds.convertScales(scales, r.scales);
        RawGaussianClouds.convertRotationsV3(rotations, r.rotations);
        RawGaussianClouds.convertAlphas(alphas, r.alphas);
        RawGaussianClouds.convertColors(colors, r.colors);
        RawGaussianClouds.convertShs(sh, r.sh, numPoints, shDegree);

        return r;
    }
    
    /**
     * Create a byte array containing the SPZ header for the specified input
     * 
     * @param gaussianCloud The {@link GaussianCloud}
     * @param version The version
     * @param fractionalBits The fractional bits
     * @return The header
     */
    private static byte[] createHeader(GaussianCloud gaussianCloud, int version,
        int fractionalBits)
    {
        byte flags = 0;
        if (gaussianCloud.isAntialiased())
        {
            flags |= 1;
        }

        byte headerBytes[] = new byte[16];
        ByteBuffer headerBuffer = Buffers.wrap(headerBytes);
        headerBuffer.putInt(0, SpzUtils.MAGIC);
        headerBuffer.putInt(4, version);
        headerBuffer.putInt(8, gaussianCloud.getNumPoints());
        headerBuffer.put(12, (byte) gaussianCloud.getShDegree());
        headerBuffer.put(13, (byte) fractionalBits);
        headerBuffer.put(14, flags);
        return headerBytes;
    }

    
    /**
     * Private constructor to prevent instantiation
     */
    private DefaultSpzWriter()
    {
        // Private constructor to prevent instantiation
    }

}