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

import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.SpzReader;
import de.javagl.jspz.SpzReaders;
import de.javagl.jspz.SpzWriter;
import de.javagl.jspz.SpzWriters;

/**
 * A basic example for how to use JSpz
 */
public class JSpzBasicWriting
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
        SpzReader spzReader = SpzReaders.createDefault();

        // Read a GaussianCloud object from the input stream
        System.out.println("Reading...");
        GaussianCloud g = spzReader.read(spzInputStream);

        // Create an output stream for the SPZ data
        OutputStream spzOutputStream =
            new FileOutputStream(new File("./data/unitCube_out.spz"));
        
        // Create a default SPZ writer
        SpzWriter spzWriter = SpzWriters.createDefaultV2();

        // Write the GaussianCloud to the output stream
        System.out.println("Writing...");
        spzWriter.write(g, spzOutputStream);

        // Verify the result
        System.out.println("Verifying...");
        InputStream spzResultInputStream =
            new FileInputStream(new File("./data/unitCube_out.spz"));
        GaussianCloud gResult = spzReader.read(spzResultInputStream);
        boolean equal = GaussianCloudUtils.equal(g, gResult);
        System.out.println("Equal? " + equal);
    }
}
