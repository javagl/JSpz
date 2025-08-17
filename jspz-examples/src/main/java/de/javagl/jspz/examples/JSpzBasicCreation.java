/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.FloatBuffer;

import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.GaussianClouds;
import de.javagl.jspz.SpzWriter;
import de.javagl.jspz.SpzWriters;

/**
 * A basic example for how to use JSpz
 */
public class JSpzBasicCreation
{
    /**
     * The size of the cube
     */
    private static final float size = 10.0f;

    /**
     * A scale factor so that the edges look nice
     */
    private static final float edgeScale = 1.0f;

    /**
     * The base scale factor for all blobs
     */
    private static final float baseScale = 0.1f;

    /**
     * The alpha value for all blobs
     */
    private static final float alpha = 1.0f;

    /**
     * The entry point of the application
     * 
     * @param args Not use
     * @throws IOException If an IO error occurs
     */
    public static void main(String[] args) throws IOException
    {
        GaussianCloud g = createExampleGaussianCloud();
        writeV2(g);
        writeV3(g);
    }

    /**
     * Write the given {@link GaussianCloud} as SPZ version 2
     * 
     * @param g The {@link GaussianCloud}
     * @throws IOException If an IO error occurs
     */
    private static void writeV2(GaussianCloud g)
        throws IOException
    {
        // Create an output stream for the SPZ data
        OutputStream spzOutputStream =
            new FileOutputStream(new File("./data/unitCube-V2.spz"));

        // Create a default SPZ writer
        SpzWriter spzWriter = SpzWriters.createDefaultV2();

        // Write the GaussianCloud to the output stream
        spzWriter.write(g, spzOutputStream);
    }

    /**
     * Write the given {@link GaussianCloud} as SPZ version 3
     * 
     * @param g The {@link GaussianCloud}
     * @throws IOException If an IO error occurs
     */
    private static void writeV3(GaussianCloud g)
        throws IOException
    {
        // Create an output stream for the SPZ data
        OutputStream spzOutputStream =
            new FileOutputStream(new File("./data/unitCube-V3.spz"));

        // Create a default SPZ writer
        SpzWriter spzWriter = SpzWriters.createDefaultV3();

        // Write the GaussianCloud to the output stream
        spzWriter.write(g, spzOutputStream);
    }

    /**
     * Create an unspecified example {@link GaussianCloud}
     * 
     * @return The {@link GaussianCloud}
     */
    static GaussianCloud createExampleGaussianCloud()
    {
        // Create a Gaussian cloud and fill it with data
        GaussianCloud g = GaussianClouds.create(20, 0);

        for (int c = 0; c < 8; c++)
        {
            float x = (c & 1) == 0 ? 0.0f : 1.0f;
            float y = (c & 2) == 0 ? 0.0f : 1.0f;
            float z = (c & 4) == 0 ? 0.0f : 1.0f;

            set(g, c, x, y, z, 1.0f, 1.0f, 1.0f);
        }
        int c = 8;

        set(g, c++, 0.5f, 0.0f, 0.0f, size, 1.0f, 1.0f);
        set(g, c++, 0.5f, 1.0f, 0.0f, size, 1.0f, 1.0f);
        set(g, c++, 0.5f, 0.0f, 1.0f, size, 1.0f, 1.0f);
        set(g, c++, 0.5f, 1.0f, 1.0f, size, 1.0f, 1.0f);

        set(g, c++, 0.0f, 0.5f, 0.0f, 1.0f, size, 1.0f);
        set(g, c++, 0.0f, 0.5f, 1.0f, 1.0f, size, 1.0f);
        set(g, c++, 1.0f, 0.5f, 0.0f, 1.0f, size, 1.0f);
        set(g, c++, 1.0f, 0.5f, 1.0f, 1.0f, size, 1.0f);

        set(g, c++, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, size);
        set(g, c++, 0.0f, 1.0f, 0.5f, 1.0f, 1.0f, size);
        set(g, c++, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f, size);
        set(g, c++, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, size);
        return g;
    }

    /**
     * Set properties in the given {@link GaussianCloud} at the given index.
     * Some details are not specified.
     * 
     * @param g The {@link GaussianCloud}
     * @param index The index
     * @param npx The normalized x-coordinate
     * @param npy The normalized y-coordinate
     * @param npz The normalized z-coordinate
     * @param sx The scaling factor in x-direction
     * @param sy The scaling factor in y-direction
     * @param sz The scaling factor in z-direction
     */
    private static void set(GaussianCloud g, int index, float npx, float npy,
        float npz, float sx, float sy, float sz)
    {
        FloatBuffer positions = g.getPositions();
        FloatBuffer rotations = g.getRotations();
        FloatBuffer scales = g.getScales();
        FloatBuffer alphas = g.getAlphas();
        FloatBuffer colors = g.getColors();

        positions.put(index * 3 + 0, npx * size);
        positions.put(index * 3 + 1, npy * size);
        positions.put(index * 3 + 2, npz * size);

        scales.put(index * 3 + 0, sx * baseScale * edgeScale);
        scales.put(index * 3 + 1, sy * baseScale * edgeScale);
        scales.put(index * 3 + 2, sz * baseScale * edgeScale);

        rotations.put(index * 4 + 0, 0.0f);
        rotations.put(index * 4 + 1, 0.0f);
        rotations.put(index * 4 + 2, 0.0f);
        rotations.put(index * 4 + 3, 1.0f);

        alphas.put(index, alpha);

        colors.put(index * 3 + 0, -1.0f + npx * 2.0f);
        colors.put(index * 3 + 1, -1.0f + npy * 2.0f);
        colors.put(index * 3 + 2, -1.0f + npz * 2.0f);

    }

}
