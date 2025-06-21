/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 * 
 * This class is ported from https://github.com/nianticlabs/spz,
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

import java.nio.FloatBuffer;

/**
 * Methods for changing the {@link CoordinateSystem} of a {@link GaussianCloud}.
 */
public class CoordinateSystems
{
    // NOTE: This class is nearly entirely ported from the original
    // implementation, because the coordinate systems in SPZ are
    // not specified (https://github.com/nianticlabs/spz/issues/14)

    /**
     * Convert the coordinate system of the given {@link GaussianCloud}, in
     * place.
     * 
     * @param g The {@link GaussianCloud}
     * @param from The {@link CoordinateSystem} to convert from
     * @param to The {@link CoordinateSystem} to convert to
     */
    public static void convertCoordinates(GaussianCloud g,
        CoordinateSystem from, CoordinateSystem to)
    {
        CoordinateConverter c = coordinateConverter(from, to);
        int n = g.getNumPoints();
        FloatBuffer positions = g.getPositions();
        for (int i = 0; i < n; i++)
        {
            float v0 = positions.get(i * 3 + 0);
            float v1 = positions.get(i * 3 + 1);
            float v2 = positions.get(i * 3 + 2);
            positions.put(i * 3 + 0, v0 * c.flipP[0]);
            positions.put(i * 3 + 1, v1 * c.flipP[1]);
            positions.put(i * 3 + 2, v2 * c.flipP[2]);
        }

        FloatBuffer rotations = g.getRotations();
        for (int i = 0; i < n; i++)
        {

            float v0 = rotations.get(i * 3 + 0);
            float v1 = rotations.get(i * 3 + 1);
            float v2 = rotations.get(i * 3 + 2);
            rotations.put(i * 3 + 0, v0 * c.flipQ[0]);
            rotations.put(i * 3 + 1, v1 * c.flipQ[1]);
            rotations.put(i * 3 + 2, v2 * c.flipQ[2]);
        }
        FloatBuffer sh = g.getSh();
        int numCoeffs = sh.capacity() / 3;
        int numCoeffsPerPoint = numCoeffs / n;
        int idx = 0;
        for (int i = 0; i < numCoeffs; i += numCoeffsPerPoint)
        {
            for (int j = 0; j < numCoeffsPerPoint; ++j, idx += 3)
            {
                float flip = c.flipSh[j];
                float v0 = sh.get(idx + 0);
                float v1 = sh.get(idx + 1);
                float v2 = sh.get(idx + 2);
                sh.put(idx + 0, v0 * flip);
                sh.put(idx + 1, v1 * flip);
                sh.put(idx + 2, v2 * flip);
            }
        }
    }

    // Ported from the original implementation, which is oh so clever...

    @SuppressWarnings("javadoc")
    private static class CoordinateConverter
    {
        float flipP[];
        float flipQ[];
        float flipSh[];

        CoordinateConverter(float flipP[], float flipQ[], float flipSh[])
        {
            this.flipP = flipP;
            this.flipQ = flipQ;
            this.flipSh = flipSh;
        }
    }

    @SuppressWarnings("javadoc")
    private static boolean[] axesMatch(CoordinateSystem a, CoordinateSystem b)
    {
        int aNum = a.getN();
        int bNum = b.getN();
        if (aNum < 0 || bNum < 0)
        {
            return new boolean[]
            { true, true, true };
        }
        return new boolean[]
        { ((aNum >> 0) & 1) == ((bNum >> 0) & 1),
            ((aNum >> 1) & 1) == ((bNum >> 1) & 1),
            ((aNum >> 2) & 1) == ((bNum >> 2) & 1) };
    }

    @SuppressWarnings("javadoc")
    static CoordinateConverter coordinateConverter(CoordinateSystem from,
        CoordinateSystem to)
    {
        boolean match[] = axesMatch(from, to);
        float x = match[0] ? 1.0f : -1.0f;
        float y = match[1] ? 1.0f : -1.0f;
        float z = match[2] ? 1.0f : -1.0f;
        return new CoordinateConverter(new float[]
        { x, y, z }, new float[]
        { y * z, x * z, x * y }, new float[]
        { y, z, x, x * y, y * z, 1.0f, x * z, 1.0f, y, x * y * z, y, z, x, z,
            x });
    }

    /**
     * Private constructor to prevent instantiation
     */
    private CoordinateSystems()
    {
        // Private constructor to prevent instantiation
    }

}
