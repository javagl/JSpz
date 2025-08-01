/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Locale;

import de.javagl.jspz.GaussianCloud;

/**
 * Utility methods for {@link GaussianCloud} objects.
 * 
 * These are only used in the examples, and not part of the public API.
 */
public class GaussianCloudUtils
{
    /**
     * Print basic, unspecified information about the splat at the given index
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     */
    static void printSplat(GaussianCloud g, int index)
    {
        FloatBuffer positions = g.getPositions();
        FloatBuffer scales = g.getScales();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();
        FloatBuffer sh = g.getSh();

        float px = positions.get(index * 3 + 0);
        float py = positions.get(index * 3 + 1);
        float pz = positions.get(index * 3 + 2);

        float sx = scales.get(index * 3 + 0);
        float sy = scales.get(index * 3 + 1);
        float sz = scales.get(index * 3 + 2);

        float rx = rotations.get(index * 4 + 0);
        float ry = rotations.get(index * 4 + 1);
        float rz = rotations.get(index * 4 + 2);
        float rw = rotations.get(index * 4 + 3);

        float a = alphas.get(index);

        float cx = colors.get(index * 3 + 0);
        float cy = colors.get(index * 3 + 1);
        float cz = colors.get(index * 3 + 2);

        String ps = String.format(Locale.ENGLISH, "%f, %f, %f", px, py, pz);
        String ss = String.format(Locale.ENGLISH, "%f, %f, %f", sx, sy, sz);
        String rs =
            String.format(Locale.ENGLISH, "%f, %f, %f, %f", rx, ry, rz, rw);
        String as = String.format(Locale.ENGLISH, "%f", a);
        String cs = String.format(Locale.ENGLISH, "%f, %f, %f", cx, cy, cz);

        StringBuilder sb = new StringBuilder();
        sb.append("Splat " + index + ": ").append("\n");
        sb.append("  position: " + ps).append("\n");
        sb.append("  scale   : " + ss).append("\n");
        sb.append("  rotation: " + rs).append("\n");
        sb.append("  alpha   : " + as).append("\n");
        sb.append("  color   : " + cs).append("\n");

        int degree = g.getShDegree();
        int dimensions = dimensionsForDegree(degree);
        for (int d = 0; d < dimensions; d++)
        {
            float shx = sh.get((index * dimensions + d) * 3 + 0);
            float shy = sh.get((index * dimensions + d) * 3 + 1);
            float shz = sh.get((index * dimensions + d) * 3 + 2);
            String shs =
                String.format(Locale.ENGLISH, "%f, %f, %f", shx, shy, shz);
            String ds = String.format("%2d", d);
            sb.append("  sh " + ds + "   : " + shs).append("\n");
        }
        System.out.println(sb.toString());
    }

    /**
     * Returns whether the given {@link GaussianCloud} objects are equal
     * 
     * @param g0 The first {@link GaussianCloud}
     * @param g1 The second {@link GaussianCloud}
     * @return The result
     */
    static boolean equal(GaussianCloud g0, GaussianCloud g1)
    {
        FloatBuffer positions0 = g0.getPositions();
        FloatBuffer scales0 = g0.getScales();
        FloatBuffer rotations0 = g0.getRotations();
        FloatBuffer alphas0 = g0.getAlphas();
        FloatBuffer colors0 = g0.getColors();
        FloatBuffer sh0 = g0.getSh();

        FloatBuffer positions1 = g1.getPositions();
        FloatBuffer scales1 = g1.getScales();
        FloatBuffer rotations1 = g1.getRotations();
        FloatBuffer alphas1 = g1.getAlphas();
        FloatBuffer colors1 = g1.getColors();
        FloatBuffer sh1 = g1.getSh();

        if (!equal(positions0, positions1))
        {
            return false;
        }
        if (!equal(scales0, scales1))
        {
            return false;
        }
        if (!equal(rotations0, rotations1))
        {
            return false;
        }
        if (!equal(alphas0, alphas1))
        {
            return false;
        }
        if (!equal(colors0, colors1))
        {
            return false;
        }
        if (!equal(sh0, sh1))
        {
            return false;
        }
        return true;
    }

    /**
     * Returns the whether the given buffers are equal, based on their remaining
     * data.
     * 
     * @param b0 The first buffer
     * @param b1 The second buffer
     * @return The result
     */
    private static boolean equal(FloatBuffer b0, FloatBuffer b1)
    {
        float a0[] = new float[b0.remaining()];
        b0.slice().get(a0);
        float a1[] = new float[b1.remaining()];
        b1.slice().get(a1);
        return Arrays.equals(a0, a1);
    }

    /**
     * Returns whether the specified splats are epsilon-equal.
     * 
     * This involves special treatment for the scalar (rotation) component of
     * the quaternions: It will treat them as actual rotation angles, meaning
     * that values like 1.0 and -1.0 will be considered to be equal.
     * 
     * @param g0 The first {@link GaussianCloud}
     * @param index0 The first index
     * @param g1 The second {@link GaussianCloud}
     * @param index1 The second index
     * @param epsilon The epsilon
     * @return The result
     */
    static boolean equalsEpsilon(GaussianCloud g0, int index0, GaussianCloud g1,
        int index1, float epsilon)
    {
        int shDegree0 = g0.getShDegree();
        int shDegree1 = g1.getShDegree();
        if (shDegree0 != shDegree1)
        {
            return false;
        }

        float p0[] = getPosition(g0, index0);
        float p1[] = getPosition(g1, index1);
        if (!equalsEpsilon(p0, p1, epsilon))
        {
            return false;
        }

        float s0[] = getScale(g0, index0);
        float s1[] = getScale(g1, index1);
        if (!equalsEpsilon(s0, s1, epsilon))
        {
            return false;
        }

        float r0[] = getRotation(g0, index0);
        float r1[] = getRotation(g1, index1);
        if (!equalsEpsilon(r0[0], r1[0], epsilon))
        {
            return false;
        }
        if (!equalsEpsilon(r0[1], r1[1], epsilon))
        {
            return false;
        }
        if (!equalsEpsilon(r0[2], r1[2], epsilon))
        {
            return false;
        }
        // Special treatment for rotation component: The difference
        // modulo 1.0 is computed, and should be epsilon-equal to 0
        float dw = 1.0f - Math.abs(Math.abs(r0[3] - r1[3]) - 1.0f);
        if (!equalsEpsilon(dw, 0.0f, epsilon))
        {
            return false;
        }

        float a0 = getAlpha(g0, index0);
        float a1 = getAlpha(g1, index1);
        if (!equalsEpsilon(a0, a1, epsilon))
        {
            return false;
        }

        float c0[] = getColor(g0, index0);
        float c1[] = getColor(g1, index1);
        if (!equalsEpsilon(c0, c1, epsilon))
        {
            return false;
        }

        int shDimensions = dimensionsForDegree(shDegree0);
        for (int d = 0; d < shDimensions; d++)
        {
            float sh0[] = getSh(g0, index0, shDegree0, d);
            float sh1[] = getSh(g1, index1, shDegree1, d);
            if (!equalsEpsilon(sh0, sh1, epsilon))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Internal method to return the specified position as an array
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @return The result
     */
    private static float[] getPosition(GaussianCloud g, int index)
    {
        FloatBuffer positions = g.getPositions();
        float px = positions.get(index * 3 + 0);
        float py = positions.get(index * 3 + 1);
        float pz = positions.get(index * 3 + 2);
        return new float[]
        { px, py, pz };
    }

    /**
     * Internal method to return the specified scale as an array
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @return The result
     */
    private static float[] getScale(GaussianCloud g, int index)
    {
        FloatBuffer scales = g.getScales();
        float sx = scales.get(index * 3 + 0);
        float sy = scales.get(index * 3 + 1);
        float sz = scales.get(index * 3 + 2);
        return new float[]
        { sx, sy, sz };
    }

    /**
     * Internal method to return the specified rotation as an array
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @return The result
     */
    private static float[] getRotation(GaussianCloud g, int index)
    {
        FloatBuffer rotations = g.getRotations();
        float rx = rotations.get(index * 3 + 0);
        float ry = rotations.get(index * 4 + 1);
        float rz = rotations.get(index * 4 + 2);
        float rw = rotations.get(index * 4 + 2);
        return new float[]
        { rx, ry, rz, rw };
    }

    /**
     * Internal method to return the specified alpha value
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @return The result
     */
    private static float getAlpha(GaussianCloud g, int index)
    {
        FloatBuffer alphas = g.getAlphas();
        return alphas.get(index);
    }

    /**
     * Internal method to return the specified color as an array
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @return The result
     */
    private static float[] getColor(GaussianCloud g, int index)
    {
        FloatBuffer colors = g.getColors();
        float cx = colors.get(index * 3 + 0);
        float cy = colors.get(index * 3 + 1);
        float cz = colors.get(index * 3 + 2);
        return new float[]
        { cx, cy, cz };
    }

    /**
     * Internal method to return the specified spherical harmonic as an array
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @param shDegree The SH degree
     * @param shDimension The SH dimension
     * @return The result
     */
    private static float[] getSh(GaussianCloud g, int index, int shDegree,
        int shDimension)
    {
        FloatBuffer shs = g.getColors();
        int shDimensions = dimensionsForDegree(shDegree);
        int indexOffset = (index * shDimensions + shDimension) * 3;
        int i0 = indexOffset + 0;
        int i1 = indexOffset + 1;
        int i2 = indexOffset + 2;
        float shx = shs.get(i0);
        float shy = shs.get(i1);
        float shz = shs.get(i2);
        return new float[]
        { shx, shy, shz };
    }

    /**
     * Returns whether the given arrays are epsilon-equal
     * 
     * @param a The first array
     * @param b The second array
     * @param epsilon The relative epsilon
     * @return Whether they are equal
     */
    private static boolean equalsEpsilon(float a[], float b[], float epsilon)
    {
        if (a.length != b.length)
        {
            return false;
        }
        for (int i = 0; i < a.length; i++)
        {
            if (!equalsEpsilon(a[i], b[i], epsilon))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given values are epsilon-equal
     * 
     * @param a The first value
     * @param b The second value
     * @param epsilon The relative epsilon
     * @return Whether they are equal
     */
    private static boolean equalsEpsilon(float a, float b, float epsilon)
    {
        float d = Math.abs(a - b);
        if (d < epsilon)
        {
            return true;
        }
        float aa = Math.abs(a);
        float ab = Math.abs(b);
        if (aa < ab)
        {
            return d <= ab * epsilon;
        }
        return d <= aa * epsilon;
    }

    /**
     * Returns the number of dimensions for the given spherical harmonics
     * degree.
     * 
     * @param degree The degree
     * @return The dimensions
     */
    private static int dimensionsForDegree(int degree)
    {
        return (degree + 1) * (degree + 1) - 1;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private GaussianCloudUtils()
    {
        // Private constructor to prevent instantiation
    }

}
