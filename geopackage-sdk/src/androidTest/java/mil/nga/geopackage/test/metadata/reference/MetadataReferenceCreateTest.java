package mil.nga.geopackage.test.metadata.reference;

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
	public void testRead() throws SQLException {

		MetadataReferenceUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_METADATA_REFERENCE_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		MetadataReferenceUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		MetadataReferenceUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		MetadataReferenceUtils.testDelete(geoPackage);

	}

}
