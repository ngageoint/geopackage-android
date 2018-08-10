package mil.nga.geopackage.test.metadata.reference;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Metadata Reference from a created database
 * 
 * @author osbornb
 */
public class MetadataReferenceCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public MetadataReferenceCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		MetadataReferenceUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_METADATA_REFERENCE_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		MetadataReferenceUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		MetadataReferenceUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		MetadataReferenceUtils.testDelete(geoPackage);

	}

}
