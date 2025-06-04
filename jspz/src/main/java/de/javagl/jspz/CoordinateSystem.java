/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 * 
 * This class is ported from https://github.com/nianticlabs/spz,
 * because https://github.com/nianticlabs/spz/issues/14 has not
 * yet been addressed. The class was originally published under 
 * the MIT license. Original copyright statement:
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
 * Coordinate systems, as defined in the SPZ library
 */
public enum CoordinateSystem
{
        /**
         * Unspecified
         */
        UNSPECIFIED(0),
        /**
         * Left Down Back
         */
        LDB(1),
        /**
         * Right Down Back
         */
        RDB(2),
        /**
         * Left Up Back
         */
        LUB(3),
        /**
         * Right Up Back, Three.js coordinate system
         */
        RUB(4),
        /**
         * Left Down Front
         */
        LDF(5),
        /**
         * Right Down Front, PLY coordinate system
         */
        RDF(6),
        /**
         * Left Up Front, GLB coordinate system
         */
        LUF(7),
        /**
         * Right Up Front, Unity coordinate system
         */
        RUF(8);

    /**
     * The numeric value of this coordinate system
     */
    private final int n;

    /**
     * Creates a new instance
     * 
     * @param n The numeric value of this coordinate system
     */
    private CoordinateSystem(int n)
    {
        this.n = n;
    }

    /**
     * Returns the numeric value of this coordinate system
     * 
     * @return The numeric value
     */
    int getN()
    {
        return n;
    }

}