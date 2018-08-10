package mil.nga.geopackage.test.core.contents;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Contents from a created database
 * 
 * @author osbornb
 */
public class ContentsCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ContentsCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		ContentsUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_CONTENTS_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		ContentsUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		ContentsUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		ContentsUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDeleteCascade() throws SQLException {

		ContentsUtils.testDeleteCascade(geoPackage);

	}

}
