package mil.nga.giat.geopackage.test.data.c4;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureColumn;
import mil.nga.giat.geopackage.data.c4.FeatureCursor;
import mil.nga.giat.geopackage.data.c4.FeatureDao;
import mil.nga.giat.geopackage.data.c4.FeatureRow;
import mil.nga.giat.geopackage.data.c4.FeatureTable;
import mil.nga.giat.geopackage.data.c4.FeatureValue;
import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryCollection;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryType;
import mil.nga.giat.geopackage.geom.GeoPackageLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPoint;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPolygon;
import mil.nga.giat.geopackage.geom.GeoPackagePoint;
import mil.nga.giat.geopackage.geom.GeoPackagePolygon;
import mil.nga.giat.geopackage.geom.wkb.WkbGeometryReader;
import mil.nga.giat.geopackage.test.geom.GeoPackageGeometryDataUtils;
import mil.nga.giat.geopackage.util.ByteReader;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

/**
 * Features Utility test methods
 * 
 * @author osbornb
 */
public class FeatureUtils {

	private static final double POINT_UPDATED_X = 45.11111;
	private static final double POINT_UPDATED_Y = 89.99999;
	private static final double POINT_UPDATED_Z = 10.55555;
	private static final double POINT_UPDATED_M = 2.87878;

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

			FeatureTable featureTable = dao.getTable();
			String[] columns = featureTable.getColumnNames();
			int geomIndex = featureTable.getGeometryColumnIndex();
			TestCase.assertTrue(geomIndex >= 0 && geomIndex < columns.length);
			TestCase.assertEquals(geometryColumns.getColumnName(),
					columns[geomIndex]);

			// Query for all
			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			int manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geoPackageGeometryData = cursor
						.getGeometry();
				if (cursor.getBlob(featureTable.getGeometryColumnIndex()) != null) {
					TestCase.assertNotNull(geoPackageGeometryData);
					GeoPackageGeometry geometry = geoPackageGeometryData
							.getGeometry();
					GeoPackageGeometryType geometryType = geometryColumns
							.getGeometryType();
					validateGeometry(geometryType, geometry);

					byte[] wkbBytes = geoPackageGeometryData.getWkbBytes();
					int byteLenth = wkbBytes.length;
					TestCase.assertTrue(byteLenth > 0);
					ByteReader wkbReader = new ByteReader(wkbBytes);
					wkbReader.setByteOrder(geoPackageGeometryData
							.getByteOrder());
					GeoPackageGeometry geometryFromBytes = WkbGeometryReader
							.readGeometry(wkbReader);
					TestCase.assertNotNull(geometryFromBytes);
					TestCase.assertEquals(geometry.getGeometryType(),
							geometryFromBytes.getGeometryType());
					validateGeometry(geometryType, geometryFromBytes);

					ByteBuffer wkbByteBuffer = geoPackageGeometryData
							.getWkbByteBuffer();
					TestCase.assertEquals(byteLenth, wkbByteBuffer.remaining());
					byte[] wkbBytes2 = new byte[wkbByteBuffer.remaining()];
					wkbByteBuffer.get(wkbBytes2);
					ByteReader wkbReader2 = new ByteReader(wkbBytes2);
					wkbReader2.setByteOrder(geoPackageGeometryData
							.getByteOrder());
					GeoPackageGeometry geometryFromBytes2 = WkbGeometryReader
							.readGeometry(wkbReader2);
					TestCase.assertNotNull(geometryFromBytes2);
					TestCase.assertEquals(geometry.getGeometryType(),
							geometryFromBytes2.getGeometryType());
					validateGeometry(geometryType, geometryFromBytes2);
				}

				FeatureRow featureRow = cursor.getRow();
				validateFeatureRow(columns, featureRow);

				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);
			cursor.close();

			// Manually query for all and compare
			cursor = (FeatureCursor) dao.getDb().query(dao.getTableName(),
					null, null, null, null, null, null);
			count = cursor.getCount();
			manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geometry = cursor.getGeometry();
				if (cursor.getBlob(featureTable.getGeometryColumnIndex()) != null) {
					TestCase.assertNotNull(geometry);
				}
				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);

			TestCase.assertTrue("No features to test", count > 0);

			// Choose random feature
			int random = (int) (Math.random() * count);
			cursor.moveToPosition(random);
			FeatureRow featureRow = cursor.getRow();

			cursor.close();

			// Query by id
			FeatureRow queryFeatureRow = dao.queryForIdRow(featureRow.getId());
			TestCase.assertNotNull(queryFeatureRow);
			TestCase.assertEquals(featureRow.getId(), queryFeatureRow.getId());

			// Find two non id non geom columns
			FeatureColumn column1 = null;
			FeatureColumn column2 = null;
			for (FeatureColumn column : featureRow.getTable().getColumns()) {
				if (!column.isPrimaryKey() && !column.isGeometry()) {
					if (column1 == null) {
						column1 = column;
					} else {
						column2 = column;
						break;
					}
				}
			}

			// Query for equal
			if (column1 != null) {

				Object column1Value = featureRow.getValue(column1.getName());
				Class<?> column1ClassType = column1.getDataType()
						.getClassType();
				boolean column1Decimal = column1ClassType == Double.class
						|| column1ClassType == Float.class;
				FeatureValue column1FeatureValue;
				if (column1Decimal) {
					column1FeatureValue = new FeatureValue(column1Value,
							.000001);
				} else {
					column1FeatureValue = new FeatureValue(column1Value);
				}
				cursor = dao.queryForFeatureEq(column1.getName(),
						column1FeatureValue);
				TestCase.assertTrue(cursor.getCount() > 0);
				boolean found = false;
				while (cursor.moveToNext()) {
					queryFeatureRow = cursor.getRow();
					TestCase.assertEquals(column1Value,
							queryFeatureRow.getValue(column1.getName()));
					if (!found) {
						found = featureRow.getId() == queryFeatureRow.getId();
					}
				}
				TestCase.assertTrue(found);
				cursor.close();

				// Query for field values
				Map<String, FeatureValue> fieldValues = new HashMap<String, FeatureValue>();
				fieldValues.put(column1.getName(), column1FeatureValue);
				Object column2Value = null;
				FeatureValue column2FeatureValue;
				if (column2 != null) {
					column2Value = featureRow.getValue(column2.getName());
					Class<?> column2ClassType = column2.getDataType()
							.getClassType();
					boolean column2Decimal = column2ClassType == Double.class
							|| column2ClassType == Float.class;
					if (column2Decimal) {
						column2FeatureValue = new FeatureValue(column2Value,
								.000001);
					} else {
						column2FeatureValue = new FeatureValue(column2Value);
					}
					fieldValues.put(column2.getName(), column2FeatureValue);
				}
				cursor = dao.queryForFieldFeatureValues(fieldValues);
				TestCase.assertTrue(cursor.getCount() > 0);
				found = false;
				while (cursor.moveToNext()) {
					queryFeatureRow = cursor.getRow();
					TestCase.assertEquals(column1Value,
							queryFeatureRow.getValue(column1.getName()));
					if (column2 != null) {
						TestCase.assertEquals(column2Value,
								queryFeatureRow.getValue(column2.getName()));
					}
					if (!found) {
						found = featureRow.getId() == queryFeatureRow.getId();
					}
				}
				TestCase.assertTrue(found);
				cursor.close();
			}
		}

	}

	/**
	 * Validate a feature row
	 * 
	 * @param columns
	 * @param featureRow
	 */
	private static void validateFeatureRow(String[] columns,
			FeatureRow featureRow) {
		TestCase.assertEquals(columns.length, featureRow.columnCount());

		for (int i = 0; i < featureRow.columnCount(); i++) {
			FeatureColumn column = featureRow.getTable().getColumns().get(i);
			TestCase.assertEquals(i, column.getIndex());
			TestCase.assertEquals(columns[i], featureRow.getColumnName(i));
			TestCase.assertEquals(i, featureRow.getColumnIndex(columns[i]));
			int rowType = featureRow.getRowColumnType(i);
			Object value = featureRow.getValue(i);

			switch (rowType) {

			case Cursor.FIELD_TYPE_INTEGER:
				validateIntegerValue(value, column.getDataType());
				break;

			case Cursor.FIELD_TYPE_FLOAT:
				validateFloatValue(value, column.getDataType());
				break;

			case Cursor.FIELD_TYPE_STRING:
				TestCase.assertTrue(value instanceof String);
				break;

			case Cursor.FIELD_TYPE_BLOB:
				if (featureRow.getGeometryColumnIndex() == i) {
					TestCase.assertTrue(value instanceof GeoPackageGeometryData);
				} else {
					TestCase.assertTrue(value instanceof byte[]);
				}
				break;

			case Cursor.FIELD_TYPE_NULL:
				TestCase.assertNull(value);
				break;

			}
		}
	}

	/**
	 * Validate the integer value with the data type
	 * 
	 * @param value
	 * @param dataType
	 * @return
	 */
	public static void validateIntegerValue(Object value,
			GeoPackageDataType dataType) {

		switch (dataType) {

		case BOOLEAN:
			TestCase.assertTrue(value instanceof Boolean);
			break;
		case TINYINT:
			TestCase.assertTrue(value instanceof Byte);
			break;
		case SMALLINT:
			TestCase.assertTrue(value instanceof Short);
			break;
		case MEDIUMINT:
			TestCase.assertTrue(value instanceof Integer);
			break;
		case INT:
		case INTEGER:
			TestCase.assertTrue(value instanceof Long);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not an integer type");
		}
	}

	/**
	 * Validate the float value with the data type
	 * 
	 * @param value
	 * @param dataType
	 * @return
	 */
	public static void validateFloatValue(Object value,
			GeoPackageDataType dataType) {

		switch (dataType) {

		case FLOAT:
			TestCase.assertTrue(value instanceof Float);
			break;
		case DOUBLE:
		case REAL:
			TestCase.assertTrue(value instanceof Double);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not a float type");
		}
	}

	/**
	 * Validate the geometry
	 * 
	 * @param geometryType
	 * @param geometry
	 */
	private static void validateGeometry(GeoPackageGeometryType geometryType,
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
			GeoPackageGeometryCollection<?> geometryCollection = (GeoPackageGeometryCollection<?>) geometry;
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

		TestCase.assertEquals(GeoPackageGeometryType.POINT,
				point.getGeometryType());

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

		TestCase.assertEquals(GeoPackageGeometryType.LINESTRING,
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

		TestCase.assertEquals(GeoPackageGeometryType.POLYGON,
				polygon.getGeometryType());

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

		TestCase.assertEquals(GeoPackageGeometryType.MULTIPOINT,
				multiPoint.getGeometryType());

		validateZAndM(topGeometry, multiPoint);

		for (GeoPackagePoint point : multiPoint.getPoints()) {
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

		TestCase.assertEquals(GeoPackageGeometryType.MULTILINESTRING,
				multiLineString.getGeometryType());

		validateZAndM(topGeometry, multiLineString);

		for (GeoPackageLineString lineString : multiLineString.getLineStrings()) {
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

		TestCase.assertEquals(GeoPackageGeometryType.MULTIPOLYGON,
				multiPolygon.getGeometryType());

		validateZAndM(topGeometry, multiPolygon);

		for (GeoPackagePolygon polygon : multiPolygon.getPolygons()) {
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
			GeoPackageGeometryCollection<?> geometryCollection) {

		validateZAndM(topGeometry, geometryCollection);

		for (GeoPackageGeometry geometry : geometryCollection.getGeometries()) {
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

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			if (count > 0) {

				// // Choose random feature
				// int random = (int) (Math.random() * count);
				// cursor.moveToPosition(random);
				cursor.moveToFirst();

				GeoPackageGeometryData geometryData = cursor.getGeometry();
				while (geometryData == null && cursor.moveToNext()) {
					geometryData = cursor.getGeometry();
				}
				if (geometryData != null) {
					String UPDATED_STRING = "updated string";

					GeoPackageGeometry geometry = geometryData.getGeometry();
					FeatureRow originalRow = cursor.getRow();
					FeatureRow featureRow = cursor.getRow();

					try {
						featureRow.setValue(featureRow.getPkColumnIndex(), 9);
						TestCase.fail("Updated the primary key value");
					} catch (GeoPackageException e) {
						// expected
					}

					for (FeatureColumn featureColumn : dao.getTable()
							.getColumns()) {
						if (!featureColumn.isPrimaryKey()) {

							if (featureColumn.isGeometry()) {

								boolean updateGeometry = true;

								switch (geometry.getGeometryType()) {

								case POINT:
									GeoPackagePoint point = (GeoPackagePoint) geometry;
									updatePoint(point);
									break;
								case MULTIPOINT:
									GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) geometry;
									if (multiPoint.numPoints() > 1) {
										multiPoint.getPoints().remove(0);
									}
									for (GeoPackagePoint multiPointPoint : multiPoint
											.getPoints()) {
										updatePoint(multiPointPoint);
									}
									break;

								default:
									updateGeometry = false;
								}
								if (updateGeometry) {
									featureRow.setValue(
											featureColumn.getIndex(),
											geometryData);
								}

							} else {
								switch (featureRow
										.getRowColumnType(featureColumn
												.getIndex())) {

								case Cursor.FIELD_TYPE_STRING:
									featureRow.setValue(
											featureColumn.getIndex(),
											UPDATED_STRING);
									break;

								default:
								}
							}

						}
					}

					cursor.close();
					try {
						TestCase.assertEquals(1, dao.update(featureRow));
					} catch (SQLiteException e) {
						if (isFutureSQLiteException(e)) {
							continue;
						} else {
							throw e;
						}
					}

					long id = featureRow.getId();
					FeatureRow readRow = dao.queryForIdRow(id);
					TestCase.assertNotNull(readRow);
					TestCase.assertEquals(originalRow.getId(), readRow.getId());
					GeoPackageGeometryData readGeometryData = readRow
							.getGeometry();
					GeoPackageGeometry readGeometry = readGeometryData
							.getGeometry();

					for (String readColumnName : readRow.getColumnNames()) {

						FeatureColumn readFeatureColumn = readRow
								.getColumn(readColumnName);
						if (!readFeatureColumn.isPrimaryKey()
								&& !readFeatureColumn.isGeometry()) {
							switch (readRow.getRowColumnType(readColumnName)) {
							case Cursor.FIELD_TYPE_STRING:
								TestCase.assertEquals(UPDATED_STRING, readRow
										.getValue(readFeatureColumn.getIndex()));
								break;

							default:
							}
						}

					}

					switch (geometry.getGeometryType()) {

					case POINT:
						GeoPackagePoint point = (GeoPackagePoint) readGeometry;
						validateUpdatedPoint(point);
						break;

					case MULTIPOINT:
						GeoPackageMultiPoint originalMultiPoint = (GeoPackageMultiPoint) geometry;
						GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) readGeometry;
						TestCase.assertEquals(originalMultiPoint.numPoints(),
								multiPoint.numPoints());
						for (GeoPackagePoint multiPointPoint : multiPoint
								.getPoints()) {
							validateUpdatedPoint(multiPointPoint);
						}
						break;

					default:
						geometry.getGeometryType();
					}

					// Compare the modified geometry with the updated geometry
					// from the database
					GeoPackageGeometryDataUtils.compareGeometries(geometry,
							readGeometry);

					// Compare the geo package headers since nothing in the
					// header was changed
					GeoPackageGeometryDataUtils.compareByteArrays(
							geometryData.getHeaderBytes(),
							readGeometryData.getHeaderBytes());

				}

			}
			cursor.close();
		}

	}

	/**
	 * Update a point
	 * 
	 * @param point
	 */
	private static void updatePoint(GeoPackagePoint point) {
		point.setX(POINT_UPDATED_X);
		point.setY(POINT_UPDATED_Y);
		if (point.hasZ()) {
			point.setZ(POINT_UPDATED_Z);
		}
		if (point.hasM()) {
			point.setM(POINT_UPDATED_M);
		}
	}

	/**
	 * Validate an updated point
	 * 
	 * @param point
	 */
	private static void validateUpdatedPoint(GeoPackagePoint point) {
		TestCase.assertEquals(POINT_UPDATED_X, point.getX());
		TestCase.assertEquals(POINT_UPDATED_Y, point.getY());
		if (point.hasZ()) {
			TestCase.assertEquals(POINT_UPDATED_Z, point.getZ());
		}
		if (point.hasM()) {
			TestCase.assertEquals(POINT_UPDATED_M, point.getM());
		}
	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			if (count > 0) {

				// Choose random feature
				int random = (int) (Math.random() * count);
				cursor.moveToPosition(random);

				FeatureRow featureRow = cursor.getRow();
				cursor.close();

				// Create new row from existing
				long id = featureRow.getId();
				featureRow.resetId();
				long newRowId;
				try {
					newRowId = dao.create(featureRow);
				} catch (SQLiteException e) {
					if (isFutureSQLiteException(e)) {
						continue;
					} else {
						throw e;
					}
				}
				TestCase.assertEquals(newRowId, featureRow.getId());

				// Verify original still exists and new was created
				featureRow = dao.queryForIdRow(id);
				TestCase.assertNotNull(featureRow);
				FeatureRow queryFeatureRow = dao.queryForIdRow(newRowId);
				TestCase.assertNotNull(queryFeatureRow);
				cursor = dao.queryForAll();
				TestCase.assertEquals(count + 1, cursor.getCount());
				cursor.close();

				// Create new row with copied values from another
				FeatureRow newRow = dao.newRow();
				for (FeatureColumn column : dao.getTable().getColumns()) {

					if (column.isPrimaryKey()) {
						try {
							newRow.setValue(column.getName(), 10);
							TestCase.fail("Set primary key on new row");
						} catch (GeoPackageException e) {
							// Expected
						}
					} else {
						newRow.setValue(column.getName(),
								featureRow.getValue(column.getName()));
					}
				}

				long newRowId2;
				try {
					newRowId2 = dao.create(newRow);
				} catch (SQLiteException e) {
					if (isFutureSQLiteException(e)) {
						continue;
					} else {
						throw e;
					}
				}
				TestCase.assertEquals(newRowId2, newRow.getId());

				// Verify new was created
				FeatureRow queryFeatureRow2 = dao.queryForIdRow(newRowId2);
				TestCase.assertNotNull(queryFeatureRow2);
				cursor = dao.queryForAll();
				TestCase.assertEquals(count + 2, cursor.getCount());
				cursor.close();
			}
			cursor.close();
		}

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			if (count > 0) {

				// Choose random feature
				int random = (int) (Math.random() * count);
				cursor.moveToPosition(random);

				FeatureRow featureRow = cursor.getRow();
				cursor.close();

				// Delete row
				try {
					TestCase.assertEquals(1, dao.delete(featureRow));
				} catch (SQLiteException e) {
					if (isFutureSQLiteException(e)) {
						continue;
					} else {
						throw e;
					}
				}

				// Verify deleted
				FeatureRow queryFeatureRow = dao.queryForIdRow(featureRow
						.getId());
				TestCase.assertNull(queryFeatureRow);
				cursor = dao.queryForAll();
				TestCase.assertEquals(count - 1, cursor.getCount());
				cursor.close();
			}
			cursor.close();
		}

	}

	/**
	 * Determine if the exception is caused from a missing function or module in
	 * SQLite versions 4.2.0 and later. Lollipop uses version 3.8.4.3 so these
	 * are not supported in Android.
	 * 
	 * @param e
	 * @return
	 */
	private static boolean isFutureSQLiteException(SQLiteException e) {
		String message = e.getMessage();
		return message.contains("no such function: ST_IsEmpty")
				|| message.contains("no such module: rtree");
	}

}
