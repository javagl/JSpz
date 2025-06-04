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
 * An interface describing a Gaussian (splat) cloud.
 * 
 * The methods in this interface that return buffers can be assumed to return
 * <i>slices</i> of the buffers that are stored internally. This means that
 * modifications to properties like the position or limit of the returned
 * buffers will not affect this instance. But modifications of the contents of
 * these buffers will affect this instance.
 */
public interface GaussianCloud
{

    /**
     * Returns the number of points
     * 
     * @return The number of points
     */
    int getNumPoints();

    /**
     * Returns the spherical harmonics degree
     * 
     * @return The degree
     */
    int getShDegree();

    /**
     * Returns whether this cloud was created with antialiasing
     * 
     * @return The flag
     */
    boolean isAntialiased();

    /**
     * Returns the positions.
     * 
     * @return The positions
     */
    FloatBuffer getPositions();

    /**
     * Returns the scales.
     * 
     * @return The scales
     */
    FloatBuffer getScales();

    /**
     * Returns the rotations.
     * 
     * @return The rotations
     */
    FloatBuffer getRotations();

    /**
     * Returns the alphas
     * 
     * @return The alphas
     */
    FloatBuffer getAlphas();

    /**
     * Returns the colors.
     * 
     * @return The colors
     */
    FloatBuffer getColors();

    /**
     * Returns the spherical harmonics.
     * 
     * @return The spherical harmonics.
     */
    FloatBuffer getSh();
}