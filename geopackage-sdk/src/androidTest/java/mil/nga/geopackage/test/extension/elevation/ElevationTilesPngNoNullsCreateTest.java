package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.test.CreateElevationTilesGeoPackageTestCase;

/**
 * Elevation Tiles Extensions Tests from a created GeoPackage with no null
 * extension values
 * 
 * @author osbornb
 */
public class ElevationTilesPngNoNullsCreateTest extends
		CreateElevationTilesGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ElevationTilesPngNoNullsCreateTest() {
		super(false);
	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR, allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	public void testElevationsBilinear() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BILINEAR, allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	public void testElevationsBicubic() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BICUBIC, allowNulls);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.NEAREST_NEIGHBOR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BILINEAR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BICUBIC,
				allowNulls);

	}

}
