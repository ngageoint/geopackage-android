package mil.nga.geopackage.test.schema.columns;

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
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintType;
import mil.nga.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.wkb.geom.GeometryType;

/**
 * Tile Data Columns Utility test methods
 *
 * @author osbornb
 */
public class DataColumnsUtils {

	/**
	 * Test read
	 *
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		DataColumnsDao dao = geoPackage.getDataColumnsDao();
		if (dao.isTableExists()) {

			List<DataColumns> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals("Unexpected number of data columns rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				DataColumnConstraintsDao dataColumnConstraintsDao = geoPackage
						.getDataColumnConstraintsDao();

				// Verify non nulls
				for (DataColumns result : results) {
					TestCase.assertNotNull(result.getTableName());
					TestCase.assertNotNull(result.getColumnName());
					TestCase.assertNotNull(result.getId());

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());

					for (DataColumnConstraints constraints : result
							.getConstraints(dataColumnConstraintsDao)) {
						TestCase.assertEquals(result.getConstraintName(),
								constraints.getConstraintName());
					}
				}

				// Choose random data columns
				int random = (int) (Math.random() * results.size());
				DataColumns dataColumns = results.get(random);

				// Query by id
				DataColumns queryDataColumns = dao.queryForId(dataColumns
						.getId());
				TestCase.assertNotNull(queryDataColumns);
				TestCase.assertEquals(dataColumns.getId(),
						queryDataColumns.getId());

                // Query by id shortcut method
                DataColumns queryDataColumns2 = dao.getDataColumn(dataColumns.getTableName(),
                        dataColumns.getColumnName());
                TestCase.assertNotNull(queryDataColumns2);
                TestCase.assertEquals(dataColumns.getId(),
                        queryDataColumns2.getId());

				// Query for equal
				List<DataColumns> queryDataColumnsList = dao.queryForEq(
						DataColumns.COLUMN_COLUMN_NAME,
						dataColumns.getColumnName());
				TestCase.assertNotNull(queryDataColumnsList);
				TestCase.assertTrue(queryDataColumnsList.size() >= 1);
				boolean found = false;
				for (DataColumns queryDataColumnsValue : queryDataColumnsList) {
					TestCase.assertEquals(dataColumns.getColumnName(),
							queryDataColumnsValue.getColumnName());
					if (!found) {
						found = dataColumns.getId().equals(
								queryDataColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(DataColumns.COLUMN_CONSTRAINT_NAME,
						dataColumns.getConstraintName());
				fieldValues.put(DataColumns.COLUMN_MIME_TYPE,
						dataColumns.getMimeType());

				queryDataColumnsList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryDataColumnsList);
				TestCase.assertTrue(queryDataColumnsList.size() >= 1);
				found = false;
				for (DataColumns queryDataColumnsValue : queryDataColumnsList) {
					TestCase.assertEquals(dataColumns.getConstraintName(),
							queryDataColumnsValue.getConstraintName());
					TestCase.assertEquals(dataColumns.getMimeType(),
							queryDataColumnsValue.getMimeType());
					if (!found) {
						found = dataColumns.getId().equals(
								queryDataColumnsValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<DataColumns, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().eq(DataColumns.COLUMN_CONSTRAINT_NAME,
						dataColumns.getConstraintName());
				PreparedQuery<DataColumns> query = qb.prepare();
				queryDataColumnsList = dao.query(query);

				found = false;
				for (DataColumns queryDataColumnsValue : queryDataColumnsList) {
					if (dataColumns.getId().equals(
							queryDataColumnsValue.getId())) {
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

		DataColumnsDao dao = geoPackage.getDataColumnsDao();
		if (dao.isTableExists()) {
			List<DataColumns> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random data contents
				int random = (int) (Math.random() * results.size());
				DataColumns dataColumns = results.get(random);

				// Update
				String updatedDescription = "UPDATED_DESCRIPTION";
				dataColumns.setDescription(updatedDescription);
				dao.update(dataColumns);

				// Verify update
				dao = geoPackage.getDataColumnsDao();
				DataColumns updatedDataColumns = dao.queryForId(dataColumns
						.getId());
				TestCase.assertEquals(updatedDescription,
						updatedDataColumns.getDescription());

				// Find expected results for prepared update
				String updatedConstraintName = TestUtils.SAMPLE_GLOB_CONSTRAINT;
				QueryBuilder<DataColumns, TableColumnKey> qb = dao
						.queryBuilder();
				qb.where().ne(DataColumns.COLUMN_CONSTRAINT_NAME,
						updatedConstraintName);
				PreparedQuery<DataColumns> preparedQuery = qb.prepare();
				List<DataColumns> queryDataColumns = dao.query(preparedQuery);

				// Prepared update
				UpdateBuilder<DataColumns, TableColumnKey> ub = dao
						.updateBuilder();
				ub.updateColumnValue(DataColumns.COLUMN_CONSTRAINT_NAME,
						updatedConstraintName);
				ub.where().ne(DataColumns.COLUMN_CONSTRAINT_NAME,
						updatedConstraintName);
				PreparedUpdate<DataColumns> update = ub.prepare();
				int updated = dao.update(update);
				TestCase.assertEquals(queryDataColumns.size(), updated);

				for (DataColumns updatedQueryDataColumns : queryDataColumns) {
					DataColumns reloadedDataColumns = dao
							.queryForId(updatedQueryDataColumns.getId());
					TestCase.assertNotNull(reloadedDataColumns);
					TestCase.assertEquals(updatedConstraintName,
							reloadedDataColumns.getConstraintName());
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
		DataColumnsDao dao = geoPackage.getDataColumnsDao();
		DataColumnConstraintsDao dataColumnConstraintsDao = geoPackage
				.getDataColumnConstraintsDao();

		if (dao.isTableExists()) {

			// Get current count
			long count = dao.countOf();

			// Retrieve a random srs
			List<SpatialReferenceSystem> results = srsDao.queryForAll();
			SpatialReferenceSystem srs = null;
			if (!results.isEmpty()) {
				int random = (int) (Math.random() * results.size());
				srs = results.get(random);
			}

			// Create a new contents
			Contents tileContents = new Contents();
			tileContents.setTableName("tile_contents");
			tileContents.setDataType(ContentsDataType.TILES);
			tileContents.setIdentifier("tile_contents");
			tileContents.setDescription("");
			// tileContents.setLastChange(new Date());
			tileContents.setMinX(-180.0);
			tileContents.setMinY(-90.0);
			tileContents.setMaxX(180.0);
			tileContents.setMaxY(90.0);
			tileContents.setSrs(srs);

			// Create the user tile table
			geoPackage.createTileTable(TestUtils.buildTileTable(tileContents
					.getTableName()));

			contentsDao.create(tileContents);

			// Create new data column
			String columnName = TileTable.COLUMN_TILE_DATA;
			String name = columnName + " NAME";
			String title = columnName + " TITLE";
			String description = columnName + " DESCRIPTION";
			String mimeType = "image/" + TestConstants.TILE_FILE_NAME_EXTENSION;

			DataColumns dataColumns = new DataColumns();
			dataColumns.setContents(tileContents);
			dataColumns.setColumnName(columnName);
			dataColumns.setName(name);
			dataColumns.setTitle(title);
			dataColumns.setDescription(description);
			dataColumns.setMimeType(mimeType);
			dao.create(dataColumns);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved data contents
			DataColumns queryDataColumns = dao.queryForId(dataColumns.getId());
			TestCase.assertEquals(tileContents.getId(),
					queryDataColumns.getTableName());
			TestCase.assertEquals(columnName, queryDataColumns.getColumnName());
			TestCase.assertEquals(name, queryDataColumns.getName());
			TestCase.assertEquals(title, queryDataColumns.getTitle());
			TestCase.assertEquals(description,
					queryDataColumns.getDescription());
			TestCase.assertEquals(mimeType, queryDataColumns.getMimeType());
			TestCase.assertEquals(tileContents.getId(), queryDataColumns
					.getContents().getId());

			// Get current count
			count = dao.countOf();

			// Create a new contents
			Contents featureContents = new Contents();
			featureContents.setTableName("feature_contents");
			featureContents.setDataType(ContentsDataType.FEATURES);
			featureContents.setIdentifier("feature_contents");
			featureContents.setDescription("");
			// featureContents.setLastChange(new Date());
			featureContents.setMinX(-180.0);
			featureContents.setMinY(-90.0);
			featureContents.setMaxX(180.0);
			featureContents.setMaxY(90.0);
			featureContents.setSrs(srs);

			// Create the feature table
			geoPackage.createFeatureTable(TestUtils.buildFeatureTable(
					featureContents.getTableName(), "geom",
					GeometryType.GEOMETRY));

			contentsDao.create(featureContents);

			// Create constraints
			String constraintName = "TestConstraintName";
			DataColumnConstraints sampleEnum1 = new DataColumnConstraints();
			sampleEnum1.setConstraintName(constraintName);
			sampleEnum1.setConstraintType(DataColumnConstraintType.ENUM);
			sampleEnum1.setValue("ONE");
			dataColumnConstraintsDao.create(sampleEnum1);

			DataColumnConstraints sampleEnum2 = new DataColumnConstraints();
			sampleEnum2.setConstraintName(constraintName);
			sampleEnum2.setConstraintType(DataColumnConstraintType.ENUM);
			sampleEnum2.setValue("TWO");
			dataColumnConstraintsDao.create(sampleEnum2);

			// Create new data column
			columnName = TestUtils.TEST_INTEGER_COLUMN;
			name = columnName + " NAME";
			title = columnName + " TITLE";
			description = columnName + " DESCRIPTION";

			dataColumns = new DataColumns();
			dataColumns.setContents(tileContents);
			dataColumns.setColumnName(columnName);
			dataColumns.setName(name);
			dataColumns.setTitle(title);
			dataColumns.setDescription(description);
			dataColumns.setConstraint(dataColumnConstraintsDao
					.queryByConstraintName(constraintName).get(0));
			dao.create(dataColumns);

			// Verify count
			newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved matrix tile
			queryDataColumns = dao.queryForId(dataColumns.getId());
			TestCase.assertEquals(tileContents.getId(),
					queryDataColumns.getTableName());
			TestCase.assertEquals(columnName, queryDataColumns.getColumnName());
			TestCase.assertEquals(name, queryDataColumns.getName());
			TestCase.assertEquals(title, queryDataColumns.getTitle());
			TestCase.assertEquals(description,
					queryDataColumns.getDescription());
			TestCase.assertNull(queryDataColumns.getMimeType());
			TestCase.assertEquals(tileContents.getId(), queryDataColumns
					.getContents().getId());
			List<DataColumnConstraints> constraints = queryDataColumns
					.getConstraints(dataColumnConstraintsDao);
			TestCase.assertTrue(constraints.size() > 1);
			for (DataColumnConstraints constraint : constraints) {
				TestCase.assertEquals(constraintName,
						constraint.getConstraintName());
				TestCase.assertEquals(DataColumnConstraintType.ENUM,
						constraint.getConstraintType());
			}
		}
	}

	/**
	 * Test delete
	 *
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		DataColumnsDao dao = geoPackage.getDataColumnsDao();
		if (dao.isTableExists()) {
			List<DataColumns> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random data columns
				int random = (int) (Math.random() * results.size());
				DataColumns dataColumns = results.get(random);

				// Delete the data columns
				dao.delete(dataColumns);

				// Verify deleted
				DataColumns queryDataColumns = dao.queryForId(dataColumns
						.getId());
				TestCase.assertNull(queryDataColumns);

				// Prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random data columns
					random = (int) (Math.random() * results.size());
					dataColumns = results.get(random);

					// Find which data columns to delete
					QueryBuilder<DataColumns, TableColumnKey> qb = dao
							.queryBuilder();
					qb.where().eq(DataColumns.COLUMN_TABLE_NAME,
							dataColumns.getTableName());
					PreparedQuery<DataColumns> query = qb.prepare();
					List<DataColumns> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<DataColumns, TableColumnKey> db = dao
							.deleteBuilder();
					db.where().eq(DataColumns.COLUMN_TABLE_NAME,
							dataColumns.getTableName());
					PreparedDelete<DataColumns> deleteQuery = db.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
