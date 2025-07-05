/*
 * www.javagl.de - JSpz
 *
 * Copyright 2025 Marco Hutter - http://www.javagl.de
 */
package de.javagl.jspz.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.io.GltfAssetWriter;
import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jspz.CoordinateSystem;
import de.javagl.jspz.CoordinateSystems;
import de.javagl.jspz.GaussianCloud;
import de.javagl.jspz.SpzReader;
import de.javagl.jspz.SpzReaders;
import de.javagl.jspz.SpzWriter;
import de.javagl.jspz.SpzWriters;

/**
 * A example that converts an SPZ file into a tileset.
 * 
 * NOTE: Some aspects of the coordinate system conventions are not yet clear,
 * and the bounding volume of the tileset may not match the actual primitive.
 * This is tracked in https://github.com/CesiumGS/cesium/issues/12682
 */
public class SpzToTileset
{
    /**
     * The entry point
     * 
     * @param args Not used
     * @throws IOException If an IO error occurs
     */
    public static void main(String[] args) throws IOException
    {
        // Adjust this as necessary:
        // String spzFileName = "./data/unitCube.spz";
        String spzFileName = "./data/box-100-200-300-700-600-500.spz";
        String outputDirectory = "./data/";

        createTileset(spzFileName, outputDirectory);
    }

    /**
     * Create a tileset that contains a single glTF content that uses the
     * <code>KHR_spz_gaussian_splats_compression</code> extension to define the
     * Gaussian Splats from the specified file.
     * 
     * @param spzFileName The SPZ file name
     * @param outputDirectory The output directory
     * @throws IOException If an IO error occurs
     */
    private static void createTileset(String spzFileName,
        String outputDirectory) throws IOException
    {
        // Read the SPZ data and a GaussianCloud
        byte[] spzBytes = Files.readAllBytes(Paths.get(spzFileName));
        InputStream spzInputStream = new ByteArrayInputStream(spzBytes);
        SpzReader spzReader = SpzReaders.createDefaultV2();
        GaussianCloud g = spzReader.read(spzInputStream);

        byte[] gltfSpzBytes = spzBytes;
        // Convert the coordinate system for glTF
        
        // NOTE: See https://github.com/nianticlabs/spz/issues/42
        boolean CONVERT_COORDINATES = false;
        if (CONVERT_COORDINATES)
        {
            // This should (probably) happen here, based on the description
            // of the KHR_spz_gaussian_splats_compression README at
            // 068b74f3bc8f0a1bb13e2265409caddb76d31d12, but this is likely 
            // not correct. 
            CoordinateSystems.convertCoordinates(g, CoordinateSystem.RUB,
                CoordinateSystem.LUF);
            SpzWriter spzWriter = SpzWriters.createDefaultV2();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            spzWriter.write(g, baos);
            gltfSpzBytes = baos.toByteArray();
        }
        
        float box[] = computeBoundingBox(g);
        
        // Create a binary glTF asset from the SPZ data
        int numPoints = g.getNumPoints();
        int shDegree = g.getShDegree();
        GltfAssetV2 gltfAsset =
            createGltfAsset(numPoints, shDegree, box, gltfSpzBytes);
        // print(gltfAsset);

        // Create some dummy tileset JSON
        String contentUrl = "content.glb";
        float[] tilesetBox = createTilesetBoundingBoxFromGltf(box);
        String tilesetJson = createTilesetJson(contentUrl, tilesetBox);

        // Prepare the output directory
        Paths.get(outputDirectory).toFile().mkdirs();

        // Write the glTF to the output directory
        Path glbFilePath = Paths.get(outputDirectory, contentUrl);
        GltfAssetWriter w = new GltfAssetWriter();
        w.writeBinary(gltfAsset, glbFilePath.toFile());

        // Write the tileset JSON to the output directory
        Path tilesetJsonPath = Paths.get(outputDirectory, "tileset.json");
        Files.write(tilesetJsonPath, tilesetJson.getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Create a binary glTF asset that uses the
     * <code>KHR_spz_gaussian_splats_compression</code> extension to define
     * Gaussian Splats
     * 
     * @param numPoints The number of points
     * @param shDegree The shpherical harmonics degree
     * @param box The bounding box
     * @param spzBytes The SPZ data
     * @return The asset
     */
    private static GltfAssetV2 createGltfAsset(int numPoints, int shDegree,
        float box[], byte spzBytes[])
    {
        // Create the glTF
        GlTF gltf = new GlTF();

        // Add the asset
        Asset asset = new Asset();
        asset.setVersion("2.0");
        gltf.setAsset(asset);

        // Add the POSITION accessor
        Accessor position = new Accessor();
        position.setComponentType(GltfConstants.GL_FLOAT);
        position.setType("VEC3");
        position.setCount(numPoints);
        position.setMin(new Number[]
        { box[0], box[1], box[2] });
        position.setMax(new Number[]
        { box[3], box[3], box[5] });
        gltf.addAccessors(position);

        // Add the COLOR_0 accessor
        Accessor color = new Accessor();
        color.setComponentType(GltfConstants.GL_UNSIGNED_BYTE);
        color.setNormalized(true);
        color.setType("VEC4");
        color.setCount(numPoints);
        gltf.addAccessors(color);

        // Add the _ROTATION accessor
        Accessor rotation = new Accessor();
        rotation.setComponentType(GltfConstants.GL_FLOAT);
        rotation.setType("VEC4");
        rotation.setCount(numPoints);
        gltf.addAccessors(rotation);

        // Add the _SCALE accessor
        Accessor scale = new Accessor();
        scale.setComponentType(GltfConstants.GL_FLOAT);
        scale.setType("VEC3");
        scale.setCount(numPoints);
        gltf.addAccessors(scale);

        // Add the spherical harmonics accessors
        int numCoeffsPerDegree[] =
        { 3, 5, 7 };
        for (int d = 0; d < shDegree; d++)
        {
            int numCoeffs = numCoeffsPerDegree[d];
            for (int n = 0; n < numCoeffs; n++)
            {
                Accessor sh = new Accessor();
                sh.setComponentType(GltfConstants.GL_FLOAT);
                sh.setType("VEC3");
                sh.setCount(numPoints);
                gltf.addAccessors(sh);
            }
        }

        // Add the buffer
        Buffer buffer = new Buffer();
        buffer.setByteLength(spzBytes.length);
        gltf.addBuffers(buffer);

        // Add the buffer view
        BufferView bufferView = new BufferView();
        bufferView.setBuffer(0);
        bufferView.setByteLength(spzBytes.length);
        gltf.addBufferViews(bufferView);

        // Create the mesh primitive
        MeshPrimitive primitive = new MeshPrimitive();
        primitive.setMode(GltfConstants.GL_POINTS);

        // Add all accessors to the mesh primitive
        int a = 0;
        primitive.addAttributes("POSITION", a++);
        primitive.addAttributes("COLOR_0", a++);
        primitive.addAttributes("_ROTATION", a++);
        primitive.addAttributes("_SCALE", a++);

        for (int d = 0; d < shDegree; d++)
        {
            int numCoeffs = numCoeffsPerDegree[d];
            for (int n = 0; n < numCoeffs; n++)
            {
                String s = "_SH_DEGREE_" + (d + 1) + "_COEFF_" + n;
                primitive.addAttributes(s, a++);
            }
        }

        // Add the SPZ extension object to the primitive
        Map<Object, Object> extension = new LinkedHashMap<Object, Object>();
        extension.put("bufferView", 0);
        primitive.addExtensions("KHR_spz_gaussian_splats_compression",
            extension);

        // Add the mesh
        Mesh mesh = new Mesh();
        mesh.addPrimitives(primitive);
        gltf.addMeshes(mesh);

        // Add the node
        Node node = new Node();
        node.setMesh(0);

        // The node needs a matrix, as this currently
        // seems to be assumed by CesiumJS
        // @formatter:off
        node.setMatrix(new float[]
        { 
            1.0f, 0.0f, 0.0f, 0.0f, 
            0.0f, 1.0f, 0.0f, 0.0f, 
            0.0f, 0.0f, 1.0f, 0.0f, 
            0.0f, 0.0f, 0.0f, 1.0f 
        });
        // @formatter:on
        gltf.addNodes(node);

        // Add the scene
        Scene scene = new Scene();
        scene.addNodes(0);
        gltf.addScenes(scene);
        gltf.setScene(0);

        // Add information about the used/required extension
        gltf.addExtensionsUsed("KHR_spz_gaussian_splats_compression");
        gltf.addExtensionsRequired("KHR_spz_gaussian_splats_compression");

        // Build the actual asset
        ByteBuffer binaryData = ByteBuffer.wrap(spzBytes);
        GltfAssetV2 gltfAsset = new GltfAssetV2(gltf, binaryData);
        return gltfAsset;
    }

    /**
     * Compute the bounding box of the given {@link GaussianCloud}
     * 
     * @param g The {@link GaussianCloud}
     * @return The bounding box
     */
    private static float[] computeBoundingBox(GaussianCloud g)
    {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        int n = g.getNumPoints();
        FloatBuffer positions = g.getPositions();
        for (int i = 0; i < n; i++)
        {
            float x = positions.get(i * 3 + 0);
            float y = positions.get(i * 3 + 1);
            float z = positions.get(i * 3 + 2);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        float box[] = new float[]
        { minX, minY, minZ, maxX, maxY, maxZ };
        return box;
    }

    /**
     * Creates a dummy tileset JSON string that refers to a content with the
     * given URL, and that uses the given bounding box.
     * 
     * @param contentUrl The content URL
     * @param box The bounding box
     * @return The string
     */
    private static String createTilesetJson(String contentUrl, float box[])
    {
        String boxString = Arrays.toString(box);

        // This is creating a "dummy" tileset JSON string, because
        // J3DTiles is not public yet. The obscure structure with
        // the additional child node is to work around what might
        // be a bug in CesiumJS, but everything is in flux here.
        // @formatter:off
        String tilesetJsonString = "" +
            "{" + "\n" +
            "  \"asset\": {" + "\n" +
            "    \"version\": \"1.1\"" + "\n" +
            "  }," + "\n" +
            "  \"extensions\": {" + "\n" +
            "    \"3DTILES_content_gltf\": {" + "\n" +
            "      \"extensionsRequired\": [\"KHR_spz_gaussian_splats_compression\"]," + "\n" +
            "      \"extensionsUsed\": [\"KHR_spz_gaussian_splats_compression\"]" + "\n" +
            "    }" + "\n" +
            "  }," + "\n" +
            "  \"extensionsUsed\": [\"3DTILES_content_gltf\"]," + "\n" +
            "  \"geometricError\": 65536," + "\n" +
            "  \"root\": {" + "\n" +
            "    \"boundingVolume\": {" + "\n" +
            "      \"box\": " + boxString + "\n" +
            "    }," + "\n" +
            "    \"geometricError\": 32768," + "\n" +
            "    \"refine\": \"REPLACE\"," + "\n" +
            "    \"children\": [" + "\n" +
            "      {" + "\n" +
            "        \"boundingVolume\": {" + "\n" +
            "          \"box\": " + boxString + "\n" +
            "        }," + "\n" +
            "        \"content\": {" + "\n" +
            "          \"uri\": \"" + contentUrl + "\"" + "\n" +
            "        }," + "\n" +
            "        \"geometricError\": 0," + "\n" +
            "        \"refine\": \"REPLACE\"" + "\n" +
            "      }" + "\n" +
            "    ]" + "\n" +
            "  }" + "\n" +
            "}" + "\n";
        // @formatter:on
        return tilesetJsonString;
    }

    /**
     * Creates a bounding box for a tileset- or tile bounding volume from the
     * bounding volume of a glTF asset
     *
     * This is the center- and half-axis representation of the
     * `boundingVolume.box` that is described at
     * https://github.com/CesiumGS/3d-tiles/tree/main/specification#box,
     * computed from the minimum- and maximum point of a box.
     *
     * @param box The input bounding box
     * @return The tileset boundingVolume .box
     */
    private static float[] createTilesetBoundingBoxFromGltf(float box[])
    {
        float minX = box[0];
        float minY = box[1];
        float minZ = box[2];
        float maxX = box[3];
        float maxY = box[4];
        float maxZ = box[5];
        
        // Take into account the y-up-to-z-up transform:
        float tMinX = minX;
        float tMinY = -minZ;
        float tMinZ = minY;
        float tMaxX = maxX;
        float tMaxY = -maxZ;
        float tMaxZ = maxY;
        return createTilesetBoundingBox(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);
    }

    /**
     * Creates a bounding box, as stored in a tileset JSON, from the given
     * minimum and maximum point of the box
     * 
     * @param minX The minimum x
     * @param minY The minimum y
     * @param minZ The minimum z
     * @param maxX The maximum x
     * @param maxY The maximum y
     * @param maxZ The maximum z
     * @return The box
     */
    private static float[] createTilesetBoundingBox(float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ)
    {
        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;

        float cx = minX + dx * 0.5f;
        float cy = minY + dy * 0.5f;
        float cz = minZ + dz * 0.5f;

        float hxx = dx * 0.5f;
        float hxy = 0.0f;
        float hxz = 0.0f;

        float hyx = 0.0f;
        float hyy = dy * 0.5f;
        float hyz = 0.0f;

        float hzx = 0.0f;
        float hzy = 0.0f;
        float hzz = dz * 0.5f;

        float box[] =
        { cx, cy, cz, hxx, hxy, hxz, hyx, hyy, hyz, hzx, hzy, hzz, };
        return box;
    }

    /**
     * Print the JSON part of the given asset to the console.
     * 
     * Only intended for debugging.
     * 
     * 
     * @param gltfAsset The asset
     * @throws IOException If something goes wrong
     */
    static void print(GltfAssetV2 gltfAsset) throws IOException
    {
        GlTF gltf = gltfAsset.getGltf();
        GltfWriter gltfWriter = new GltfWriter();
        gltfWriter.setIndenting(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gltfWriter.write(gltf, baos);
        System.out.println(new String(baos.toByteArray()));
    }

}
