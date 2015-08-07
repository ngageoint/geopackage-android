package mil.nga.geopackage.test.metadata.reference;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Metadata Reference from an imported database
 * 
 * @author osbornb
 */
public class MetadataReferenceImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public MetadataReferenceImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		MetadataReferenceUtils.testRead(geoPackage, null);

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
