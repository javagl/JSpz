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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Utility methods related to buffers
 */
class Buffers
{
    /**
     * Wrap the given data into a <i>little-endian</i> buffer
     * 
     * @param data The data
     * @return The buffer
     */
    static ByteBuffer wrap(byte data[])
    {
        ByteBuffer buffer =
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    /**
     * Allocate a <i>little-endian</i> float buffer with the given size
     * 
     * @param size The size
     * @return The buffer
     */
    public static FloatBuffer allocateFloat(int size)
    {
        ByteBuffer buffer = ByteBuffer.allocate(size * Float.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN);
        return buffer.asFloatBuffer();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private Buffers()
    {
        // Private constructor to prevent instantiation
    }

}
