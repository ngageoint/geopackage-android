package mil.nga.geopackage.test.extension.elevation;

import junit.framework.TestCase;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.elevation.ElevationTileResults;
import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.extension.elevation.ElevationTilesPng;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.ImportElevationTilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;

/**
 * Elevation Tiles Extensions Tests from an imported GeoPackage
 *
 * @author osbornb
 */
public class ElevationTilesPngImportTest extends
        ImportElevationTilesGeoPackageTestCase {

    private static final boolean PRINT = false;
    private static final boolean allowNulls = false;

    /**
     * Test the elevation extension with a newly created GeoPackage using the
     * Nearest Neighbor Algorithm
     */
    public void testElevationsNearestNeighbor() throws Exception {

        ElevationTilesPngTestUtils.testElevations(geoPackage, null,
                ElevationTilesAlgorithm.NEAREST_NEIGHBOR, allowNulls);

    }

    /**
     * Test the elevation extension with a newly created GeoPackage using the
     * Bilinear Algorithm
     */
    public void testElevationsBilinear() throws Exception {

        ElevationTilesPngTestUtils.testElevations(geoPackage, null,
                ElevationTilesAlgorithm.BILINEAR, allowNulls);

    }

    /**
     * Test the elevation extension with a newly created GeoPackage using the
     * Bicubic Algorithm
     */
    public void testElevationsBicubic() throws Exception {

        ElevationTilesPngTestUtils.testElevations(geoPackage, null,
                ElevationTilesAlgorithm.BICUBIC, allowNulls);

    }

    /**
     * Test a random bounding box using the Nearest Neighbor Algorithm
     */
    public void testRandomBoundingBoxNearestNeighbor() throws Exception {

        ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage, null,
                ElevationTilesAlgorithm.NEAREST_NEIGHBOR, true);

    }

    /**
     * Test a random bounding box using the Bilinear Algorithm
     */
    public void testRandomBoundingBoxBilinear() throws Exception {

        ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage, null,
                ElevationTilesAlgorithm.BILINEAR, true);

    }

    /**
     * Test a random bounding box using the Bicubic Algorithm
     */
    public void testRandomBoundingBoxBicubic() throws Exception {

        ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage, null,
                ElevationTilesAlgorithm.BICUBIC, true);

    }

    /**
     * Test a single hard coded location and optional print
     *
     * @throws Exception
     */
    public void testLocation() throws Exception {

        double latitude = 61.57941522271581;
        double longitude = -148.96174115565339;

        testLocation(latitude, longitude);
    }

    /**
     * Test 10 random locations and optionally print
     *
     * @throws Exception
     */
    public void testRandomLocations() throws Exception {

        BoundingBox projectedBoundingBox = null;

        List<String> elevationTables = ElevationTilesPng.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String elevationTable : elevationTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(elevationTable);

            BoundingBox boundingBox = tileMatrixSet.getBoundingBox();
            if (PRINT) {
                System.out.println("Min Latitude: "
                        + boundingBox.getMinLatitude());
                System.out.println("Max Latitude: "
                        + boundingBox.getMaxLatitude());
                System.out.println("Min Longitude: "
                        + boundingBox.getMinLongitude());
                System.out.println("Max Longitude: "
                        + boundingBox.getMaxLongitude());
                System.out.println();
            }
            SpatialReferenceSystemDao srsDao = geoPackage
                    .getSpatialReferenceSystemDao();
            long srsId = tileMatrixSet.getSrsId();
            SpatialReferenceSystem srs = srsDao.queryForId(srsId);
            Projection projection = ProjectionFactory.getProjection(srs);
            Projection requestProjection = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            ProjectionTransform elevationToRequest = projection
                    .getTransformation(requestProjection);
            projectedBoundingBox = elevationToRequest.transform(boundingBox);

        }
        if (PRINT) {
            System.out.println("Min Latitude: "
                    + projectedBoundingBox.getMinLatitude());
            System.out.println("Max Latitude: "
                    + projectedBoundingBox.getMaxLatitude());
            System.out.println("Min Longitude: "
                    + projectedBoundingBox.getMinLongitude());
            System.out.println("Max Longitude: "
                    + projectedBoundingBox.getMaxLongitude());
            System.out.println();
        }

        double latDistance = projectedBoundingBox.getMaxLatitude()
                - projectedBoundingBox.getMinLatitude();
        double lonDistance = projectedBoundingBox.getMaxLongitude()
                - projectedBoundingBox.getMinLongitude();

        for (int i = 0; i < 10; i++) {

            // Get a random coordinate
            double latitude = latDistance * .9 * Math.random()
                    + projectedBoundingBox.getMinLatitude()
                    + (.05 * latDistance);
            double longitude = lonDistance * .9 * Math.random()
                    + projectedBoundingBox.getMinLongitude()
                    + (.05 * lonDistance);
            testLocation(latitude, longitude);
            if (PRINT) {
                System.out.println();
            }
        }
    }

    /**
     * Test elevation requests within the bounds of the tiles and optionally
     * print
     *
     * @throws Exception
     */
    public void testBounds() throws Exception {

        long requestEpsg = ProjectionConstants.EPSG_WEB_MERCATOR;

        double widthPixelDistance = 1000;
        double heightPixelDistance = 1000;
        int width = 10;
        int height = 6;
        double minLongitude = -16586000;
        double maxLongitude = minLongitude + (width * widthPixelDistance);
        double minLatitude = 8760000;
        double maxLatitude = minLatitude + (height * heightPixelDistance);

        BoundingBox boundingBox = new BoundingBox(minLongitude, minLatitude,
                maxLongitude, maxLatitude);

        Projection projection = ProjectionFactory.getProjection(requestEpsg);
        Projection printProjection = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        ProjectionTransform wgs84Transform = projection
                .getTransformation(printProjection);

        if (PRINT) {
            System.out.println();
            System.out.println();
            System.out.println("Bounds Test");
            System.out.println();
            System.out.println("REQUEST");
            System.out.println();
            System.out.println("   Min Lat: " + boundingBox.getMinLatitude());
            System.out.println("   Max Lat: " + boundingBox.getMaxLatitude());
            System.out.println("   Min Lon: " + boundingBox.getMinLongitude());
            System.out.println("   Max Lon: " + boundingBox.getMaxLongitude());
            System.out.println("   Result Width: " + width);
            System.out.println("   Result Height: " + height);

            System.out.println();
            System.out.println();
            System.out.println("WGS84 REQUEST");
            System.out.println();
            BoundingBox wgs84BoundingBox = wgs84Transform
                    .transform(boundingBox);
            System.out.println("   Min Lat: "
                    + wgs84BoundingBox.getMinLatitude());
            System.out.println("   Max Lat: "
                    + wgs84BoundingBox.getMaxLatitude());
            System.out.println("   Min Lon: "
                    + wgs84BoundingBox.getMinLongitude());
            System.out.println("   Max Lon: "
                    + wgs84BoundingBox.getMaxLongitude());

            System.out.println();
            System.out.println();
            System.out.println("WGS84 LOCATIONS");

            for (double lat = maxLatitude - (heightPixelDistance * .5); lat >= minLatitude; lat -= heightPixelDistance) {
                System.out.println();
                for (double lon = minLongitude + (widthPixelDistance * .5); lon <= maxLongitude; lon += widthPixelDistance) {
                    double[] point = wgs84Transform.transform(lon, lat);
                    System.out.print("   (" + point[1] + "," + point[0] + ")");
                }
            }
        }

        for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
                .values()) {

            if (PRINT) {
                System.out.println();
                System.out.println();
                System.out.println(algorithm.name() + " SINGLE ELEVATIONS");
            }
            for (double lat = maxLatitude - (heightPixelDistance * .5); lat >= minLatitude; lat -= heightPixelDistance) {
                if (PRINT) {
                    System.out.println();
                }
                for (double lon = minLongitude + (widthPixelDistance * .5); lon <= maxLongitude; lon += widthPixelDistance) {
                    Double elevation = ElevationTilesPngTestUtils.getElevation(
                            geoPackage, algorithm, lat, lon, requestEpsg);
                    if (PRINT) {
                        System.out.print("   " + elevation);
                    }
                    if (!allowNulls) {
                        TestCase.assertNotNull(elevation);
                    }
                }
            }

            ElevationTileResults results = ElevationTilesPngTestUtils
                    .getElevations(geoPackage, algorithm, boundingBox, width,
                            height, requestEpsg);
            if (!allowNulls) {
                TestCase.assertNotNull(results);
            }
            if (results != null) {
                if (PRINT) {
                    System.out.println();
                    System.out.println();
                    System.out.println(algorithm.name());
                }
                Double[][] elevations = results.getElevations();
                TestCase.assertEquals(height, elevations.length);
                TestCase.assertEquals(width, elevations[0].length);
                for (int y = 0; y < elevations.length; y++) {
                    if (PRINT) {
                        System.out.println();
                    }
                    for (int x = 0; x < elevations[0].length; x++) {
                        Double elevation = elevations[y][x];
                        if (PRINT) {
                            System.out.print("   " + elevation);
                        }
                        if (!allowNulls) {
                            TestCase.assertNotNull(elevation);
                        }
                    }
                }
            }
        }

    }

    /**
     * Test a full bounding box around tiles and optionally print. Also test the
     * bounds of individual tiles.
     *
     * @throws Exception
     */
    public void testFullBoundingBox() throws Exception {

        int width = 10;
        int height = 6;

        List<String> elevationTables = ElevationTilesPng.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String elevationTable : elevationTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(elevationTable);

            long geoPackageEpsg = tileMatrixSet.getSrs().getOrganizationCoordsysId();

            Projection projection = ProjectionFactory.getProjection(geoPackageEpsg);
            Projection printProjection = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            ProjectionTransform wgs84Transform = projection
                    .getTransformation(printProjection);

            BoundingBox boundingBox = tileMatrixSet.getBoundingBox();

            double minLongitude = boundingBox.getMinLongitude();
            double maxLongitude = boundingBox.getMaxLongitude();
            double minLatitude = boundingBox.getMinLatitude();
            double maxLatitude = boundingBox.getMaxLatitude();

            double widthPixelDistance = (maxLongitude - minLongitude) / width;
            double heightPixelDistance = (maxLatitude - minLatitude) / height;

            if (PRINT) {
                System.out.println();
                System.out.println();
                System.out.println("Full Bounding Box Test");
                System.out.println();
                System.out.println("REQUEST");
                System.out.println();
                System.out.println("   Min Lat: "
                        + boundingBox.getMinLatitude());
                System.out.println("   Max Lat: "
                        + boundingBox.getMaxLatitude());
                System.out.println("   Min Lon: "
                        + boundingBox.getMinLongitude());
                System.out.println("   Max Lon: "
                        + boundingBox.getMaxLongitude());
                System.out.println("   Result Width: " + width);
                System.out.println("   Result Height: " + height);

                System.out.println();
                System.out.println();
                System.out.println("WGS84 REQUEST");
                System.out.println();
                BoundingBox wgs84BoundingBox = wgs84Transform
                        .transform(boundingBox);
                System.out.println("   Min Lat: "
                        + wgs84BoundingBox.getMinLatitude());
                System.out.println("   Max Lat: "
                        + wgs84BoundingBox.getMaxLatitude());
                System.out.println("   Min Lon: "
                        + wgs84BoundingBox.getMinLongitude());
                System.out.println("   Max Lon: "
                        + wgs84BoundingBox.getMaxLongitude());

                System.out.println();
                System.out.println();
                System.out.println("WGS84 LOCATIONS");
                for (double lat = maxLatitude; lat >= minLatitude; lat -= heightPixelDistance) {
                    System.out.println();
                    for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
                        double[] point = wgs84Transform.transform(lon, lat);
                        System.out.print("   (" + point[1] + "," + point[0]
                                + ")");
                    }
                    double[] point = wgs84Transform
                            .transform(maxLongitude, lat);
                    System.out.print("   (" + point[1] + "," + point[0] + ")");
                }
                System.out.println();
                for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
                    double[] point = wgs84Transform.transform(lon, minLatitude);
                    System.out.print("   (" + point[1] + "," + point[0] + ")");
                }
                double[] point = wgs84Transform.transform(maxLongitude,
                        minLatitude);
                System.out.print("   (" + point[1] + "," + point[0] + ")");
            }

            for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
                    .values()) {

                if (PRINT) {
                    System.out.println();
                    System.out.println();
                    System.out.println(algorithm.name()
                            + " SINGLE ELEVATIONS Full Bounding Box");
                }
                for (double lat = maxLatitude; lat >= minLatitude; lat -= heightPixelDistance) {
                    if (PRINT) {
                        System.out.println();
                    }
                    for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
                        Double elevation = ElevationTilesPngTestUtils
                                .getElevation(geoPackage, algorithm, lat, lon,
                                        geoPackageEpsg);
                        if (PRINT) {
                            System.out.print("   " + elevation);
                        }
                        if (algorithm == ElevationTilesAlgorithm.NEAREST_NEIGHBOR
                                || (lat < maxLatitude && lon > minLongitude
                                && lat > minLatitude && lon < maxLongitude)) {
                            if (!allowNulls) {
                                TestCase.assertNotNull(elevation);
                            }
                        }
                    }
                    Double elevation = ElevationTilesPngTestUtils.getElevation(
                            geoPackage, algorithm, lat, maxLongitude,
                            geoPackageEpsg);
                    if (PRINT) {
                        System.out.print("   " + elevation);
                    }
                    if (algorithm == ElevationTilesAlgorithm.NEAREST_NEIGHBOR) {
                        if (!allowNulls) {
                            TestCase.assertNotNull(elevation);
                        }
                    }
                }
                if (PRINT) {
                    System.out.println();
                }
                for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
                    Double elevation = ElevationTilesPngTestUtils.getElevation(
                            geoPackage, algorithm, minLatitude, lon,
                            geoPackageEpsg);
                    if (PRINT) {
                        System.out.print("   " + elevation);
                    }
                    if (algorithm == ElevationTilesAlgorithm.NEAREST_NEIGHBOR) {
                        if (!allowNulls) {
                            TestCase.assertNotNull(elevation);
                        }
                    }
                }
                Double elevation = ElevationTilesPngTestUtils.getElevation(
                        geoPackage, algorithm, minLatitude, maxLongitude,
                        geoPackageEpsg);
                if (PRINT) {
                    System.out.print("   " + elevation);
                }
                if (algorithm == ElevationTilesAlgorithm.NEAREST_NEIGHBOR) {
                    if (!allowNulls) {
                        TestCase.assertNotNull(elevation);
                    }
                }

                ElevationTileResults results = ElevationTilesPngTestUtils
                        .getElevations(geoPackage, algorithm, boundingBox,
                                width, height, geoPackageEpsg);
                if (PRINT) {
                    System.out.println();
                    System.out.println();
                    System.out.println(algorithm.name() + " Full Bounding Box");
                }
                Double[][] elevations = results.getElevations();
                for (int y = 0; y < elevations.length; y++) {
                    if (PRINT) {
                        System.out.println();
                    }
                    for (int x = 0; x < elevations[0].length; x++) {
                        elevation = elevations[y][x];
                        if (PRINT) {
                            System.out.print("   " + elevations[y][x]);
                        }
                        if (!allowNulls) {
                            TestCase.assertNotNull(elevation);
                        }
                    }
                }

                TileMatrix tileMatrix = results.getTileMatrix();
                for (int row = 0; row < tileMatrix.getMatrixHeight(); row++) {
                    for (int column = 0; column < tileMatrix.getMatrixWidth(); column++) {

                        BoundingBox boundingBox2 = TileBoundingBoxUtils
                                .getBoundingBox(boundingBox, tileMatrix,
                                        column, row);

                        double minLongitude2 = boundingBox2.getMinLongitude();
                        double maxLongitude2 = boundingBox2.getMaxLongitude();
                        double minLatitude2 = boundingBox2.getMinLatitude();
                        double maxLatitude2 = boundingBox2.getMaxLatitude();

                        if (PRINT) {
                            System.out.println();
                            System.out.println();
                            System.out.println(algorithm.name()
                                    + " SINGLE ELEVATIONS Tile row = " + row
                                    + ", column = " + column);
                        }

                        elevation = ElevationTilesPngTestUtils.getElevation(
                                geoPackage, algorithm, maxLatitude2,
                                minLongitude2, geoPackageEpsg);
                        double[] point = wgs84Transform.transform(
                                minLongitude2, maxLatitude2);
                        if (PRINT) {
                            System.out.print("   " + elevation + " ("
                                    + point[1] + "," + point[0] + ")");
                        }
                        if (algorithm != ElevationTilesAlgorithm.NEAREST_NEIGHBOR
                                && (row == 0 || column == 0)) {
                            TestCase.assertNull(elevation);
                        } else {
                            if (!allowNulls) {
                                TestCase.assertNotNull(elevation);
                            }
                        }

                        elevation = ElevationTilesPngTestUtils.getElevation(
                                geoPackage, algorithm, maxLatitude2,
                                maxLongitude2, geoPackageEpsg);
                        point = wgs84Transform.transform(maxLongitude2,
                                maxLatitude2);
                        if (PRINT) {
                            System.out.println("   " + elevation + " ("
                                    + point[1] + "," + point[0] + ")");
                        }
                        if (algorithm != ElevationTilesAlgorithm.NEAREST_NEIGHBOR
                                && (row == 0 || column == tileMatrix
                                .getMatrixWidth() - 1)) {
                            TestCase.assertNull(elevation);
                        } else {
                            if (!allowNulls) {
                                TestCase.assertNotNull(elevation);
                            }
                        }

                        elevation = ElevationTilesPngTestUtils.getElevation(
                                geoPackage, algorithm, minLatitude2,
                                minLongitude2, geoPackageEpsg);
                        point = wgs84Transform.transform(minLongitude2,
                                minLatitude2);
                        if (PRINT) {
                            System.out.print("   " + elevation + " ("
                                    + point[1] + "," + point[0] + ")");
                        }
                        if (algorithm != ElevationTilesAlgorithm.NEAREST_NEIGHBOR
                                && (row == tileMatrix.getMatrixHeight() - 1 || column == 0)) {
                            TestCase.assertNull(elevation);
                        } else {
                            if (!allowNulls) {
                                TestCase.assertNotNull(elevation);
                            }
                        }

                        elevation = ElevationTilesPngTestUtils.getElevation(
                                geoPackage, algorithm, minLatitude2,
                                maxLongitude2, geoPackageEpsg);
                        point = wgs84Transform.transform(maxLongitude2,
                                minLatitude2);
                        if (PRINT) {
                            System.out.println("   " + elevation + " ("
                                    + point[1] + "," + point[0] + ")");
                        }
                        if (algorithm != ElevationTilesAlgorithm.NEAREST_NEIGHBOR
                                && (row == tileMatrix.getMatrixHeight() - 1 || column == tileMatrix
                                .getMatrixWidth() - 1)) {
                            TestCase.assertNull(elevation);
                        } else {
                            if (!allowNulls) {
                                TestCase.assertNotNull(elevation);
                            }
                        }

                        results = ElevationTilesPngTestUtils.getElevations(
                                geoPackage, algorithm, boundingBox2, width,
                                height, geoPackageEpsg);
                        if (PRINT) {
                            System.out.println();
                            System.out.println();
                            System.out.println(algorithm.name()
                                    + " Tile row = " + row + ", column = "
                                    + column);
                        }
                        if (results == null) {
                            if (PRINT) {
                                System.out.println();
                                System.out.print("null results");
                            }
                        } else {
                            elevations = results.getElevations();
                            for (int y = 0; y < elevations.length; y++) {
                                if (PRINT) {
                                    System.out.println();
                                }
                                for (int x = 0; x < elevations[0].length; x++) {
                                    elevation = elevations[y][x];
                                    if (PRINT) {
                                        System.out.print("   "
                                                + elevations[y][x]);
                                    }
                                    if (!allowNulls) {
                                        TestCase.assertNotNull(elevation);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

    }

    /**
     * Test a single location
     *
     * @param latitude
     * @param longitude
     * @throws Exception
     */
    private void testLocation(double latitude, double longitude)
            throws Exception {

        if (PRINT) {
            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);
        }

        for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
                .values()) {
            Double elevation = ElevationTilesPngTestUtils.getElevation(geoPackage,
                    algorithm, latitude, longitude,
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            if (PRINT) {
                System.out.println(algorithm.name() + ": " + elevation);
            }
        }
    }

}
