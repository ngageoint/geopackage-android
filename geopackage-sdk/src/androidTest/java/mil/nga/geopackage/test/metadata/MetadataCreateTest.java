package mil.nga.geopackage.test.metadata;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Metadata from a created database
 * 
 * @author osbornb
 */
public class MetadataCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public MetadataCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		MetadataUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_METADATA_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		MetadataUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		MetadataUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		MetadataUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDeleteCascade() throws SQLException {

		MetadataUtils.testDeleteCascade(geoPackage);

	}

}
