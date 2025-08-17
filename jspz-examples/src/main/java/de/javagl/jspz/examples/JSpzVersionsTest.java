/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.SpzReader;
import de.javagl.jspz.SpzReaders;
import de.javagl.jspz.SpzWriter;
import de.javagl.jspz.SpzWriters;

/**
 * A basic test for the handling of different SPZ versions
 */
public class JSpzVersionsTest
{
    /**
     * The entry point of the application
     * 
     * @param args Not use
     * @throws IOException If an IO error occurs
     */
    public static void main(String[] args) throws IOException
    {
        GaussianCloud g = JSpzBasicCreation.createExampleGaussianCloud();

        GaussianCloud gResultV2 = roundtripV2(g);
        GaussianCloud gResultV3 = roundtripV3(g);

        float epsilon = 0.05f;
        verifyEqual(g, gResultV2, epsilon);
        verifyEqual(g, gResultV3, epsilon);
    }

    /**
     * Write the given {@link GaussianCloud} as SPZ version 2, read it, and
     * return the result.
     * 
     * @param g The {@link GaussianCloud}
     * @return The result
     * @throws IOException If an IO error occurs
     */
    private static GaussianCloud roundtripV2(GaussianCloud g) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SpzWriter spzWriter = SpzWriters.createDefaultV2();
        spzWriter.write(g, baos);

        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        SpzReader spzReader = SpzReaders.createDefault();

        GaussianCloud gResult = spzReader.read(bais);
        return gResult;
    }

    /**
     * Write the given {@link GaussianCloud} as SPZ version 3, read it, and
     * return the result.
     * 
     * @param g The {@link GaussianCloud}
     * @return The result
     * @throws IOException If an IO error occurs
     */
    private static GaussianCloud roundtripV3(GaussianCloud g) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SpzWriter spzWriter = SpzWriters.createDefaultV3();
        spzWriter.write(g, baos);

        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        SpzReader spzReader = SpzReaders.createDefault();

        GaussianCloud gResult = spzReader.read(bais);
        return gResult;
    }

    /**
     * Verify that the given {@link GaussianCloud} objects are epsilon-equal,
     * printing the comparison results to the console
     * 
     * @param g0 The first {@link GaussianCloud}
     * @param g1 The second {@link GaussianCloud}
     * @param epsilon The epsilon
     */
    private static void verifyEqual(GaussianCloud g0, GaussianCloud g1,
        float epsilon)
    {
        int n = g0.getNumPoints();
        boolean allEqual = true;
        for (int i = 0; i < n; i++)
        {
            boolean equal =
                GaussianCloudUtils.equalsEpsilon(g0, i, g1, i, epsilon);
            if (!equal)
            {
                System.out.println("Guassian cloud 0 at index " + i);
                GaussianCloudUtils.printSplat(g0, i);
                System.out.println("Guassian cloud 1 at index " + i);
                GaussianCloudUtils.printSplat(g1, i);
            }
            allEqual &= equal;
        }
        System.out.println("Equal? " + allEqual);
    }
}
