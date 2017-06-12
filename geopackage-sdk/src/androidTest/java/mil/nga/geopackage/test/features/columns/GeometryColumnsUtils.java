package mil.nga.geopackage.test.features.columns;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.columns.GeometryColumnsSfSql;
import mil.nga.geopackage.features.columns.GeometryColumnsSfSqlDao;
import mil.nga.geopackage.features.columns.GeometryColumnsSqlMm;
import mil.nga.geopackage.features.columns.GeometryColumnsSqlMmDao;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.wkb.geom.GeometryType;

/**
 * Geometry Columns Utility test methods
 * 
 * @author osbornb
 */
public class GeometryColumnsUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

		if (dao.isTableExists()) {
			List<GeometryColumns> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of geometry columns rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (GeometryColumns result : results) {
					TestCase.assertNotNull(result.getTableName());
					TestCase.assertNotNull(result.getColumnName());
					TestCase.assertNotNull(result.getId());
					TestCase.assertNotNull(result.getId().getTableName());
					TestCase.assertNotNull(result.getId().getColumnName());
					TestCase.assertNotNull(result.getGeometryType());
					TestCase.assertNotNull(result.getGeometryTypeName());
					TestCase.assertNotNull(result.getSrsId());
					TestCase.assertNotNull(result.getZ());
					TestCase.assertNotNull(result.getM());

					SpatialReferenceSystem srs = result.getSrs();
					TestCase.assertNotNull(srs);
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());

				}

				// Choose random contents
				int random = (int) (Math.random() * results.size());
				GeometryColumns geometryColumns = results.get(random);

				// Query by id
				GeometryColumns queryGeometryColumns = dao
						.queryForId(geometryColumns.getId());
				TestCase.assertNotNull(queryGeometryColumns);
				TestCase.assertEquals(geometryColumns.getId(),
						queryGeometryColumns.getId());

				// Query for equal
				List<GeometryColumns> queryGeometryColumnsList = dao
						.queryForEq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
								geometryColumns.getGeometryType().getName());
				TestCase.assertNotNull(queryGeometryColumnsList);
				if(queryGeometryColumnsList.isEmpty()){
					queryGeometryColumnsList = dao
							.queryForEq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
									geometryColumns.getGeometryTypeName());
				}
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				boolean found = false;
				for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getGeometryType(),
							queryGeometryColumnsValue.getGeometryType());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(GeometryColumns.COLUMN_Z,
						geometryColumns.getZ());
				fieldValues.put(GeometryColumns.COLUMN_M,
						geometryColumns.getM());

				queryGeometryColumnsList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryGeometryColumnsList);
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				found = false;
				for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getZ(),
							queryGeometryColumnsValue.getZ());
					TestCase.assertEquals(geometryColumns.getM(),
							queryGeometryColumnsValue.getM());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<GeometryColumns, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().eq(GeometryColumns.COLUMN_COLUMN_NAME,
						geometryColumns.getColumnName());
				PreparedQuery<GeometryColumns> query = qb.prepare();
				queryGeometryColumnsList = dao.query(query);

				found = false;
				for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
					if (geometryColumns.getId().equals(
							queryGeometryColumnsValue.getId())) {
						found = true;
						break;
					}
				}
				TestCase.assertTrue(found);

			}
		}
	}

	/**
	 * Test SQL/MM read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testSqlMmRead(GeoPackage geoPackage,
			Integer expectedResults) throws SQLException {

		GeometryColumnsSqlMmDao dao = geoPackage.getGeometryColumnsSqlMmDao();

		if (dao.isTableExists()) {
			List<GeometryColumnsSqlMm> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of geometry columns rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (GeometryColumnsSqlMm result : results) {
					TestCase.assertNotNull(result.getTableName());
					TestCase.assertNotNull(result.getColumnName());
					TestCase.assertNotNull(result.getId());
					TestCase.assertNotNull(result.getId().getTableName());
					TestCase.assertNotNull(result.getId().getColumnName());
					TestCase.assertNotNull(result.getGeometryType());
					TestCase.assertNotNull(result.getGeometryTypeName());
					TestCase.assertTrue(result
							.getGeometryTypeName()
							.startsWith(
									GeometryColumnsSqlMm.COLUMN_GEOMETRY_TYPE_NAME_PREFIX));
					TestCase.assertNotNull(result.getSrsId());
					TestCase.assertNotNull(result.getSrsName());

					SpatialReferenceSystem srs = result.getSrs();
					TestCase.assertNotNull(srs);
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());

				}

				// Choose random contents
				int random = (int) (Math.random() * results.size());
				GeometryColumnsSqlMm geometryColumns = results.get(random);

				// Query by id
				GeometryColumnsSqlMm queryGeometryColumns = dao
						.queryForId(geometryColumns.getId());
				TestCase.assertNotNull(queryGeometryColumns);
				TestCase.assertEquals(geometryColumns.getId(),
						queryGeometryColumns.getId());

				// Query for equal
				List<GeometryColumnsSqlMm> queryGeometryColumnsList = dao
						.queryForEq(
								GeometryColumnsSqlMm.COLUMN_GEOMETRY_TYPE_NAME,
								GeometryColumnsSqlMm.COLUMN_GEOMETRY_TYPE_NAME_PREFIX
										+ geometryColumns.getGeometryType()
												.getName());
				TestCase.assertNotNull(queryGeometryColumnsList);
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				boolean found = false;
				for (GeometryColumnsSqlMm queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getGeometryType(),
							queryGeometryColumnsValue.getGeometryType());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(GeometryColumnsSqlMm.COLUMN_SRS_NAME,
						geometryColumns.getSrsName());
				fieldValues.put(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
						geometryColumns.getGeometryTypeName());

				queryGeometryColumnsList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryGeometryColumnsList);
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				found = false;
				for (GeometryColumnsSqlMm queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getSrsName(),
							queryGeometryColumnsValue.getSrsName());
					TestCase.assertEquals(
							geometryColumns.getGeometryTypeName(),
							queryGeometryColumnsValue.getGeometryTypeName());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<GeometryColumnsSqlMm, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().eq(GeometryColumnsSqlMm.COLUMN_COLUMN_NAME,
						geometryColumns.getColumnName());
				PreparedQuery<GeometryColumnsSqlMm> query = qb.prepare();
				queryGeometryColumnsList = dao.query(query);

				found = false;
				for (GeometryColumnsSqlMm queryGeometryColumnsValue : queryGeometryColumnsList) {
					if (geometryColumns.getId().equals(
							queryGeometryColumnsValue.getId())) {
						found = true;
						break;
					}
				}
				TestCase.assertTrue(found);

			}
		}
	}

	/**
	 * Test SF/SQL read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testSfSqlRead(GeoPackage geoPackage,
			Integer expectedResults) throws SQLException {

		GeometryColumnsSfSqlDao dao = geoPackage.getGeometryColumnsSfSqlDao();

		if (dao.isTableExists()) {
			List<GeometryColumnsSfSql> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of geometry columns rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (GeometryColumnsSfSql result : results) {
					TestCase.assertNotNull(result.getFTableName());
					TestCase.assertNotNull(result.getFGeometryColumn());
					TestCase.assertNotNull(result.getId());
					TestCase.assertNotNull(result.getId().getTableName());
					TestCase.assertNotNull(result.getId().getColumnName());
					TestCase.assertNotNull(result.getGeometryType());
					TestCase.assertNotNull(GeometryType.fromCode(result
							.getGeometryTypeCode()));
					TestCase.assertTrue(result.getCoordDimension() >= 2
							&& result.getCoordDimension() <= 5);
					TestCase.assertNotNull(result.getSrid());

					SpatialReferenceSystem srs = result.getSrs();
					TestCase.assertNotNull(srs);
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());

				}

				// Choose random contents
				int random = (int) (Math.random() * results.size());
				GeometryColumnsSfSql geometryColumns = results.get(random);

				// Query by id
				GeometryColumnsSfSql queryGeometryColumns = dao
						.queryForId(geometryColumns.getId());
				TestCase.assertNotNull(queryGeometryColumns);
				TestCase.assertEquals(geometryColumns.getId(),
						queryGeometryColumns.getId());

				// Query for equal
				List<GeometryColumnsSfSql> queryGeometryColumnsList = dao
						.queryForEq(GeometryColumnsSfSql.COLUMN_GEOMETRY_TYPE,
								geometryColumns.getGeometryType().getCode());
				TestCase.assertNotNull(queryGeometryColumnsList);
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				boolean found = false;
				for (GeometryColumnsSfSql queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getGeometryType(),
							queryGeometryColumnsValue.getGeometryType());
					TestCase.assertEquals(geometryColumns.getGeometryType()
							.getCode(), queryGeometryColumnsValue
							.getGeometryTypeCode());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(GeometryColumnsSfSql.COLUMN_GEOMETRY_TYPE,
						geometryColumns.getGeometryTypeCode());
				fieldValues.put(GeometryColumnsSfSql.COLUMN_SRID,
						geometryColumns.getSrid());

				queryGeometryColumnsList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryGeometryColumnsList);
				TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
				found = false;
				for (GeometryColumnsSfSql queryGeometryColumnsValue : queryGeometryColumnsList) {
					TestCase.assertEquals(geometryColumns.getGeometryType(),
							queryGeometryColumnsValue.getGeometryType());
					TestCase.assertEquals(
							geometryColumns.getGeometryTypeCode(),
							queryGeometryColumnsValue.getGeometryTypeCode());
					TestCase.assertEquals(geometryColumns.getSrid(),
							queryGeometryColumnsValue.getSrid());
					if (!found) {
						found = geometryColumns.getId().equals(
								queryGeometryColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<GeometryColumnsSfSql, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().eq(GeometryColumnsSfSql.COLUMN_F_GEOMETRY_COLUMN,
						geometryColumns.getFGeometryColumn());
				PreparedQuery<GeometryColumnsSfSql> query = qb.prepare();
				queryGeometryColumnsList = dao.query(query);

				found = false;
				for (GeometryColumnsSfSql queryGeometryColumnsValue : queryGeometryColumnsList) {
					if (geometryColumns.getId().equals(
							queryGeometryColumnsValue.getId())) {
						found = true;
						break;
					}
				}
				TestCase.assertTrue(found);

			}
		}
	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

		if (dao.isTableExists()) {
			List<GeometryColumns> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random geometry columns
				int random = (int) (Math.random() * results.size());
				GeometryColumns geometryColumns = results.get(random);

				// Update
				byte updatedM = 2;
				geometryColumns.setM(updatedM);
				dao.update(geometryColumns);

				// Verify update
				dao = geoPackage.getGeometryColumnsDao();
				GeometryColumns updatedGeometryColumns = dao
						.queryForId(geometryColumns.getId());
				TestCase.assertEquals(updatedM, updatedGeometryColumns.getM());

				// Find expected results for prepared update
				String updatedColumnName = "new_geom";
				QueryBuilder<GeometryColumns, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().eq(GeometryColumns.COLUMN_Z, 0).or()
						.eq(GeometryColumns.COLUMN_Z, 2);
				PreparedQuery<GeometryColumns> preparedQuery = qb.prepare();
				List<GeometryColumns> queryGeometryColumns = dao
						.query(preparedQuery);

				// Prepared update
				UpdateBuilder<GeometryColumns, TableColumnKey> ub = dao
						.updateBuilder();
				ub.updateColumnValue(GeometryColumns.COLUMN_COLUMN_NAME,
						updatedColumnName);
				ub.where().eq(GeometryColumns.COLUMN_Z, 0).or()
						.eq(GeometryColumns.COLUMN_Z, 2);
				PreparedUpdate<GeometryColumns> update = ub.prepare();
				int updated = dao.update(update);
				TestCase.assertEquals(queryGeometryColumns.size(), updated);

				for (GeometryColumns updatedQueryGeometryColumns : queryGeometryColumns) {
					updatedQueryGeometryColumns
							.setColumnName(updatedColumnName);
					GeometryColumns reloadedGeometryColumns = dao
							.queryForId(updatedQueryGeometryColumns.getId());
					TestCase.assertNotNull(reloadedGeometryColumns);
					TestCase.assertEquals(updatedColumnName,
							reloadedGeometryColumns.getColumnName());
				}

			}
		}

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		ContentsDao contentsDao = geoPackage.getContentsDao();
		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

		if (dao.isTableExists()) {

			// Get current count
			long count = dao.countOf();
			TestCase.assertEquals(count, dao.getFeatureTables().size());

			// Retrieve a random srs
			List<SpatialReferenceSystem> results = srsDao.queryForAll();
			SpatialReferenceSystem srs = null;
			if (!results.isEmpty()) {
				int random = (int) (Math.random() * results.size());
				srs = results.get(random);
			}

			// Create a new contents
			Contents contents = new Contents();
			contents.setTableName("test_contents");
			contents.setDataType(ContentsDataType.FEATURES);
			contents.setIdentifier("test_contents");
			contents.setDescription("");
			// contents.setLastChange(new Date());
			contents.setMinX(-180.0);
			contents.setMinY(-90.0);
			contents.setMaxX(180.0);
			contents.setMaxY(90.0);
			contents.setSrs(srs);

			// Create the feature table
			geoPackage.createFeatureTable(TestUtils.buildFeatureTable(
					contents.getTableName(), "geom", GeometryType.GEOMETRY));

			contentsDao.create(contents);

			String columnName = "TEST_COLUMN_NAME";
			GeometryType geometryType = GeometryType.POINT;
			byte z = 2;
			byte m = 2;

			// Create new geometry columns
			GeometryColumns geometryColumns = new GeometryColumns();
			geometryColumns.setContents(contents);
			geometryColumns.setColumnName(columnName);
			geometryColumns.setGeometryType(geometryType);
			geometryColumns.setSrs(contents.getSrs());
			geometryColumns.setZ(z);
			geometryColumns.setM(m);
			dao.create(geometryColumns);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);
			TestCase.assertEquals(newCount, dao.getFeatureTables().size());
			TestCase.assertTrue(dao.getFeatureTables().contains(
					contents.getTableName()));

			// Verify saved geometry columns
			GeometryColumns queryGeometryColumns = dao
					.queryForId(geometryColumns.getId());
			TestCase.assertEquals(contents.getId(),
					queryGeometryColumns.getTableName());
			TestCase.assertEquals(columnName,
					queryGeometryColumns.getColumnName());
			TestCase.assertEquals(geometryType,
					queryGeometryColumns.getGeometryType());
			TestCase.assertEquals(contents.getSrsId().longValue(),
					queryGeometryColumns.getSrsId());
			TestCase.assertEquals(z, queryGeometryColumns.getZ());
			TestCase.assertEquals(m, queryGeometryColumns.getM());
			TestCase.assertEquals(contents.getId(), queryGeometryColumns
					.getContents().getId());
			TestCase.assertEquals(contents.getSrsId().longValue(),
					queryGeometryColumns.getSrs().getId());
		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

		if (dao.isTableExists()) {
			List<GeometryColumns> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random geometry columns
				int random = (int) (Math.random() * results.size());
				GeometryColumns geometryColumns = results.get(random);

				// Delete the geometry columns
				dao.delete(geometryColumns);

				// Verify deleted
				GeometryColumns queryGeometryColumns = dao
						.queryForId(geometryColumns.getId());
				TestCase.assertNull(queryGeometryColumns);

				// Prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random geometry columns
					random = (int) (Math.random() * results.size());
					geometryColumns = results.get(random);

					// Find which geometry columns to delete
					QueryBuilder<GeometryColumns, TableColumnKey> qb = dao
							.queryBuilder();
					qb.where().eq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
							geometryColumns.getGeometryType().getName());
					PreparedQuery<GeometryColumns> query = qb.prepare();
					List<GeometryColumns> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<GeometryColumns, TableColumnKey> db = dao
							.deleteBuilder();
					db.where().eq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
							geometryColumns.getGeometryType().getName());
					PreparedDelete<GeometryColumns> deleteQuery = db.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
