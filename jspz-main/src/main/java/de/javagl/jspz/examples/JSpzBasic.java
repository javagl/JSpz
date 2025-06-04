/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Locale;

import de.javagl.jspz.CoordinateSystem;
import de.javagl.jspz.CoordinateSystems;
import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.SpzReader;
import de.javagl.jspz.SpzReaders;

/**
 * A basic example for how to use JSpz
 */
public class JSpzBasic
{
    /**
     * The entry point of the application
     * 
     * @param args Not use
     * @throws IOException If an IO error occurs
     */
    public static void main(String[] args) throws IOException
    {
        // Create an input stream with the SPZ data
        String fileName = "./data/hornedlizard.spz";
        InputStream spzInputStream = new FileInputStream(new File(fileName));

        // Create a default SPZ reader
        SpzReader spzReader = SpzReaders.createDefaultV2();

        // Read a GaussianCloud object from the input stream
        GaussianCloud g = spzReader.read(spzInputStream);

        // Optionally do some coordinate system conversions
        CoordinateSystems.convertCoordinates(g, CoordinateSystem.UNSPECIFIED,
            CoordinateSystem.LUF);

        // Print some info...
        int n = 10;
        for (int i = 0; i < n; i++)
        {
            printSplat(g, i);
        }
    }

    /**
     * Print basic, unspecified information about the splat at the given index
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     */
    private static void printSplat(GaussianCloud g, int index)
    {
        FloatBuffer positions = g.getPositions();
        FloatBuffer scales = g.getScales();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();

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
        sb.append("  color   : " + cs);

        System.out.println(sb.toString());
    }

}
