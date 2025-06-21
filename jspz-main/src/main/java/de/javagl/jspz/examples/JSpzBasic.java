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
        String fileName = "./data/unitCube.spz";
        InputStream spzInputStream = new FileInputStream(new File(fileName));

        // Create a default SPZ reader
        SpzReader spzReader = SpzReaders.createDefaultV2();

        // Read a GaussianCloud object from the input stream
        GaussianCloud g = spzReader.read(spzInputStream);

        // Optionally do some coordinate system conversions
        CoordinateSystems.convertCoordinates(g, CoordinateSystem.UNSPECIFIED,
            CoordinateSystem.LUF);

        // Print some info...
        int n = Math.min(10, g.getNumPoints());
        for (int i = 0; i < n; i++)
        {
            GaussianCloudUtils.printSplat(g, i);
        }
    }

}
