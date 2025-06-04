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
 * Default implementation of a {@link GaussianCloud}
 */
class DefaultGaussianCloud implements GaussianCloud
{

    /**
     * The number of points
     */
    private final int numPoints;

    /**
     * The spherical harmonics degree
     */
    private final int shDegree;

    /**
     * Whether the cloud is antialiased
     */
    private final boolean antialiased;

    /**
     * The positions
     */
    private final FloatBuffer positions;

    /**
     * The scales
     */
    private final FloatBuffer scales;

    /**
     * The rotations
     */
    private final FloatBuffer rotations;

    /**
     * The alphas
     */
    private final FloatBuffer alphas;

    /**
     * The colors
     */
    private final FloatBuffer colors;

    /**
     * The spherical harmonics
     */
    private final FloatBuffer sh;

    /**
     * Default constructor
     * 
     * @param numPoints The number of points
     * @param shDegree The spherical harmonics degree
     * @param antialiased Whether the cloud is antialiased
     */
    DefaultGaussianCloud(int numPoints, int shDegree, boolean antialiased)
    {
        this.numPoints = numPoints;
        this.shDegree = shDegree;
        this.antialiased = antialiased;

        int shDim = SpzUtils.dimensionsForDegree(shDegree);
        this.positions = Buffers.allocateFloat(numPoints * 3);
        this.scales = Buffers.allocateFloat(numPoints * 3);
        this.rotations = Buffers.allocateFloat(numPoints * 4);
        this.alphas = Buffers.allocateFloat(numPoints);
        this.colors = Buffers.allocateFloat(numPoints * 3);
        this.sh = Buffers.allocateFloat(numPoints * shDim * 3);
    }

    @Override
    public int getNumPoints()
    {
        return numPoints;
    }

    @Override
    public int getShDegree()
    {
        return shDegree;
    }

    @Override
    public boolean isAntialiased()
    {
        return antialiased;
    }

    @Override
    public FloatBuffer getPositions()
    {
        return positions.slice();
    }

    @Override
    public FloatBuffer getScales()
    {
        return scales.slice();
    }

    @Override
    public FloatBuffer getRotations()
    {
        return rotations.slice();
    }

    @Override
    public FloatBuffer getAlphas()
    {
        return alphas.slice();
    }

    @Override
    public FloatBuffer getColors()
    {
        return colors.slice();
    }

    @Override
    public FloatBuffer getSh()
    {
        return sh.slice();
    }

}