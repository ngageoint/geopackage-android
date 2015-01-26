package mil.nga.giat.geopackage.test.data.c4;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureCursor;
import mil.nga.giat.geopackage.data.c4.FeatureDao;
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryCollection;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.GeoPackageLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPoint;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPolygon;
import mil.nga.giat.geopackage.geom.GeoPackagePoint;
import mil.nga.giat.geopackage.geom.GeoPackagePolygon;
import mil.nga.giat.geopackage.geom.GeometryType;

/**
 * Features Utility test methods
 * 
 * @author osbornb
 */
public class FeatureUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			// Test the get feature DAO methods
			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getContents());
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getTableName());
			TestCase.assertNotNull(dao);

			TestCase.assertNotNull(dao.getDb());
			TestCase.assertEquals(geometryColumns.getId(), dao
					.getGeometryColumns().getId());
			TestCase.assertEquals(geometryColumns.getTableName(),
					dao.getTableName());
			TestCase.assertEquals(geometryColumns.getColumnName(),
					dao.getGeometryColumnName());

			String[] columns = dao.getColumns();
			int geomIndex = dao.getGeometryColumnIndex();
			TestCase.assertTrue(geomIndex >= 0 && geomIndex < columns.length);
			TestCase.assertEquals(geometryColumns.getColumnName(),
					columns[geomIndex]);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			int manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geoPackageGeometryData = cursor
						.getGeometry();
				if (cursor.getBlob(dao.getGeometryColumnIndex()) != null) {
					TestCase.assertNotNull(geoPackageGeometryData);
					GeoPackageGeometry geometry = geoPackageGeometryData
							.getGeometry();
					GeometryType geometryType = geometryColumns
							.getGeometryType();

					validateGeometry(geometryType, geometry);
				}
				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);

			cursor = (FeatureCursor) dao.getDb().query(dao.getTableName(),
					null, null, null, null, null, null);
			count = cursor.getCount();
			manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geometry = cursor.getGeometry();
				if (cursor.getBlob(dao.getGeometryColumnIndex()) != null) {
					TestCase.assertNotNull(geometry);
				}
				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);
			// TODO
		}

	}

	/**
	 * Validate the geometry
	 * 
	 * @param geometryType
	 * @param geometry
	 */
	private static void validateGeometry(GeometryType geometryType,
			GeoPackageGeometry geometry) {

		switch (geometryType) {
		case POINT:
			TestCase.assertTrue(geometry instanceof GeoPackagePoint);
			GeoPackagePoint point = (GeoPackagePoint) geometry;
			validatePoint(point, point);
			break;
		case LINESTRING:
			TestCase.assertTrue(geometry instanceof GeoPackageLineString);
			GeoPackageLineString lineString = (GeoPackageLineString) geometry;
			validateLineString(lineString, lineString);
			break;
		case POLYGON:
			TestCase.assertTrue(geometry instanceof GeoPackagePolygon);
			GeoPackagePolygon polygon = (GeoPackagePolygon) geometry;
			validatePolygon(polygon, polygon);
			break;
		case MULTIPOINT:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiPoint);
			GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) geometry;
			validateMultiPoint(multiPoint, multiPoint);
			break;
		case MULTILINESTRING:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiLineString);
			GeoPackageMultiLineString multiLineString = (GeoPackageMultiLineString) geometry;
			validateMultiLineString(multiLineString, multiLineString);
			break;
		case MULTIPOLYGON:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiPolygon);
			GeoPackageMultiPolygon multiPolygon = (GeoPackageMultiPolygon) geometry;
			validateMultiPolygon(multiPolygon, multiPolygon);
			break;
		case GEOMETRYCOLLECTION:
			TestCase.assertTrue(geometry instanceof GeoPackageGeometryCollection);
			GeoPackageGeometryCollection<GeoPackageGeometry> geometryCollection = (GeoPackageGeometryCollection<GeoPackageGeometry>) geometry;
			validateGeometryCollection(geometryCollection, geometryCollection);
			break;
		default:

		}
	}

	/**
	 * Validate Z and M values
	 * 
	 * @param topGeometry
	 * @param geometry
	 */
	private static void validateZAndM(GeoPackageGeometry topGeometry,
			GeoPackageGeometry geometry) {
		TestCase.assertEquals(topGeometry.hasZ(), geometry.hasZ());
		TestCase.assertEquals(topGeometry.hasM(), geometry.hasM());
	}

	/**
	 * Validate Point
	 * 
	 * @param topGeometry
	 * @param point
	 */
	private static void validatePoint(GeoPackageGeometry topGeometry,
			GeoPackagePoint point) {

		TestCase.assertEquals(GeometryType.POINT, point.getGeometryType());

		validateZAndM(topGeometry, point);

		if (topGeometry.hasZ()) {
			TestCase.assertNotNull(point.getZ());
		} else {
			TestCase.assertNull(point.getZ());
		}

		if (topGeometry.hasM()) {
			TestCase.assertNotNull(point.getM());
		} else {
			TestCase.assertNull(point.getM());
		}
	}

	/**
	 * Validate Line String
	 * 
	 * @param topGeometry
	 * @param lineString
	 */
	private static void validateLineString(GeoPackageGeometry topGeometry,
			GeoPackageLineString lineString) {

		TestCase.assertEquals(GeometryType.LINESTRING,
				lineString.getGeometryType());

		validateZAndM(topGeometry, lineString);

		for (GeoPackagePoint point : lineString.getPoints()) {
			validatePoint(topGeometry, point);
		}

	}

	/**
	 * Validate Polygon
	 * 
	 * @param topGeometry
	 * @param polygon
	 */
	private static void validatePolygon(GeoPackageGeometry topGeometry,
			GeoPackagePolygon polygon) {

		TestCase.assertEquals(GeometryType.POLYGON, polygon.getGeometryType());

		validateZAndM(topGeometry, polygon);

		for (GeoPackageLineString ring : polygon.getRings()) {
			validateLineString(topGeometry, ring);
		}

	}

	/**
	 * Validate Multi Point
	 * 
	 * @param topGeometry
	 * @param multiPoint
	 */
	private static void validateMultiPoint(GeoPackageGeometry topGeometry,
			GeoPackageMultiPoint multiPoint) {

		TestCase.assertEquals(GeometryType.MULTIPOINT,
				multiPoint.getGeometryType());

		validateZAndM(topGeometry, multiPoint);

		for (GeoPackagePoint point : multiPoint.get()) {
			validatePoint(topGeometry, point);
		}

	}

	/**
	 * Validate Multi Line String
	 * 
	 * @param topGeometry
	 * @param multiLineString
	 */
	private static void validateMultiLineString(GeoPackageGeometry topGeometry,
			GeoPackageMultiLineString multiLineString) {

		TestCase.assertEquals(GeometryType.MULTILINESTRING,
				multiLineString.getGeometryType());

		validateZAndM(topGeometry, multiLineString);

		for (GeoPackageLineString lineString : multiLineString.get()) {
			validateLineString(topGeometry, lineString);
		}

	}

	/**
	 * Validate Multi Polygon
	 * 
	 * @param topGeometry
	 * @param multiPolygon
	 */
	private static void validateMultiPolygon(GeoPackageGeometry topGeometry,
			GeoPackageMultiPolygon multiPolygon) {

		TestCase.assertEquals(GeometryType.MULTIPOLYGON,
				multiPolygon.getGeometryType());

		validateZAndM(topGeometry, multiPolygon);

		for (GeoPackagePolygon polygon : multiPolygon.get()) {
			validatePolygon(topGeometry, polygon);
		}

	}

	/**
	 * Validate Geometry Collection
	 * 
	 * @param topGeometry
	 * @param geometryCollection
	 */
	private static void validateGeometryCollection(
			GeoPackageGeometry topGeometry,
			GeoPackageGeometryCollection<GeoPackageGeometry> geometryCollection) {

		validateZAndM(topGeometry, geometryCollection);

		for (GeoPackageGeometry geometry : geometryCollection.get()) {
			validateGeometry(geometry.getGeometryType(), geometry);
		}

	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

}
