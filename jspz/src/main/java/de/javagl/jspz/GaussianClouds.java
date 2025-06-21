/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz;

/**
 * Methods for {@link GaussianCloud} objects.
 */
public class GaussianClouds
{
    /**
     * Creates a new {@link GaussianCloud} instance with the given number of
     * points and spherical harmonics degree.
     * 
     * @param numPoints The number of points
     * @param shDegree The spherical harmonics degree
     * @return The {@link GaussianCloud}
     */
    public static GaussianCloud create(int numPoints, int shDegree)
    {
        boolean antialiased = false;
        return new DefaultGaussianCloud(numPoints, shDegree, antialiased);
    }

    /**
     * Private constructor to prevent instantiation
     */
    private GaussianClouds()
    {
        // Private constructor to prevent instantiation
    }

}
