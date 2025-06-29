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
     * Returns the whether the given {@link GaussianCloud} objects are equal
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
