/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.javagl.jspz.CoordinateSystem;
import de.javagl.jspz.CoordinateSystems;
import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.SpzReader;
import de.javagl.jspz.SpzReaders;
import de.javagl.jspz.SpzWriter;
import de.javagl.jspz.SpzWriters;

/**
 * A basic example for how to use JSpz
 */
public class JSpzTransform
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
        System.out.println("Reading...");
        GaussianCloud g = spzReader.read(spzInputStream);

        // Do some coordinate system conversions
        CoordinateSystems.convertCoordinates(g, CoordinateSystem.RUB,
            CoordinateSystem.LUF);

        // Create an output stream for the SPZ data
        OutputStream spzOutputStream =
            new FileOutputStream(new File("./data/unitCube_OUT.spz"));

        // Create a default SPZ writer
        SpzWriter spzWriter = SpzWriters.createDefaultV2();

        // Write the GaussianCloud to the output stream
        System.out.println("Writing...");
        spzWriter.write(g, spzOutputStream);

        System.out.println("Done");
    }
}
