package mil.nga.giat.geopackage.test.data.c3;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.test.TestUtils;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Geometry Columns from a created database (C.3. gpkg_geometry_columns)
 * 
 * @author osbornb
 */
public class GeometryColumnsCreateTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * GeoPackage activity
	 */
	private Activity activity = null;

	/**
	 * GeoPackage
	 */
	private GeoPackage geoPackage = null;

	/**
	 * Constructor
	 */
	public GeometryColumnsCreateTest() {
		super(GeoPackageActivity.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Set the activity
		activity = getActivity();

		// Create the database
		geoPackage = TestUtils.setUpCreate(activity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Tear down the create database
		TestUtils.tearDownCreate(activity, geoPackage);

		super.tearDown();
	}

	/**
	 * Test reading
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testRead() throws GeoPackageException, SQLException {

		GeometryColumnsUtils.testRead(geoPackage, 0 /*TODO*/);

	}

	/**
	 * Test updating
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testUpdate() throws GeoPackageException, SQLException {

		GeometryColumnsUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testCreate() throws GeoPackageException, SQLException {

		GeometryColumnsUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testDelete() throws GeoPackageException, SQLException {

		GeometryColumnsUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testDeleteCascade() throws GeoPackageException, SQLException {

		GeometryColumnsUtils.testDeleteCascade(geoPackage);

	}

}
