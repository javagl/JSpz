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

/**
 * Internal, intermediate representation of SPZ data version 2, only used for
 * serialization and deserialization.
 */
@SuppressWarnings("javadoc")
class RawGaussianCloudV2
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