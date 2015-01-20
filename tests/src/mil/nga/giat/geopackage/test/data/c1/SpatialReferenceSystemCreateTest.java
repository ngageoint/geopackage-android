package mil.nga.giat.geopackage.test.data.c1;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.test.TestUtils;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Spatial Reference System from a created database (C.1.
 * gpkg_spatial_ref_sys)
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemCreateTest extends
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
	public SpatialReferenceSystemCreateTest() {
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

		SpatialReferenceSystemUtils.testRead(geoPackage, 2);

	}

	/**
	 * Test reading using the SQL/MM view
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testSqlMmRead() throws GeoPackageException, SQLException {

		SpatialReferenceSystemUtils.testSqlMmRead(geoPackage, 2);

	}

	/**
	 * Test reading using the SF/SQL view
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testSfSqlRead() throws GeoPackageException, SQLException {

		SpatialReferenceSystemUtils.testSfSqlRead(geoPackage, 2);

	}

}
