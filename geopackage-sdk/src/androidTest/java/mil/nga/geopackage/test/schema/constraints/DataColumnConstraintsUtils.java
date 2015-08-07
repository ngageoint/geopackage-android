package mil.nga.geopackage.test.schema.constraints;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintType;
import mil.nga.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.test.TestUtils;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Tile Data Column Constraints Utility test methods
 * 
 * @author osbornb
 */
public class DataColumnConstraintsUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();
		if (dao.isTableExists()) {

			List<DataColumnConstraints> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of data column constraints rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();

				// Verify non nulls
				for (DataColumnConstraints result : results) {
					TestCase.assertNotNull(result.getConstraintName());
					TestCase.assertNotNull(result.getConstraintType());

					for (DataColumns column : result.getColumns(dataColumnsDao)) {
						TestCase.assertEquals(result.getConstraintName(),
								column.getConstraintName());
					}
				}

				// Choose random data column constraints
				int random = (int) (Math.random() * results.size());
				DataColumnConstraints dataColumnConstraints = results
						.get(random);

				// Query by constraint name
				List<DataColumnConstraints> queryDataColumnConstraintsList = dao
						.queryByConstraintName(dataColumnConstraints
								.getConstraintName());
				TestCase.assertNotNull(queryDataColumnConstraintsList);
				TestCase.assertTrue(queryDataColumnConstraintsList.size() > 0);
				for (DataColumnConstraints queryDataColumns : queryDataColumnConstraintsList) {
					TestCase.assertEquals(
							dataColumnConstraints.getConstraintName(),
							queryDataColumns.getConstraintName());
				}

				// Query for equal
				queryDataColumnConstraintsList = dao.queryForEq(
						DataColumnConstraints.COLUMN_CONSTRAINT_TYPE,
						dataColumnConstraints.getConstraintType().name()
								.toLowerCase());
				TestCase.assertNotNull(queryDataColumnConstraintsList);
				TestCase.assertTrue(queryDataColumnConstraintsList.size() >= 1);
				boolean found = false;
				for (DataColumnConstraints queryDataColumnConstraintsValue : queryDataColumnConstraintsList) {
					TestCase.assertEquals(
							dataColumnConstraints.getConstraintType(),
							queryDataColumnConstraintsValue.getConstraintType());
					if (!found) {
						TestCase.assertFalse(found);
						found = dataColumnConstraints.getConstraintName()
								.equals(queryDataColumnConstraintsValue
										.getConstraintName())
								&& (dataColumnConstraints.getValue() == null ? queryDataColumnConstraintsValue
										.getValue() == null
										: dataColumnConstraints
												.getValue()
												.equals(queryDataColumnConstraintsValue
														.getValue()));
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
						dataColumnConstraints.getConstraintName());
				fieldValues.put(DataColumnConstraints.COLUMN_CONSTRAINT_TYPE,
						dataColumnConstraints.getConstraintType().getValue());

				queryDataColumnConstraintsList = dao
						.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryDataColumnConstraintsList);
				TestCase.assertTrue(queryDataColumnConstraintsList.size() >= 1);
				found = false;
				for (DataColumnConstraints queryDataColumnConstraintsValue : queryDataColumnConstraintsList) {
					TestCase.assertEquals(
							dataColumnConstraints.getConstraintName(),
							queryDataColumnConstraintsValue.getConstraintName());
					TestCase.assertEquals(
							dataColumnConstraints.getConstraintType(),
							queryDataColumnConstraintsValue.getConstraintType());
					if (!found) {
						TestCase.assertFalse(found);
						found = dataColumnConstraints.getValue() == null ? queryDataColumnConstraintsValue
								.getValue() == null : dataColumnConstraints
								.getValue().equals(
										queryDataColumnConstraintsValue
												.getValue());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<DataColumnConstraints, Void> qb = dao
						.queryBuilder();
				qb.where().eq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
						dataColumnConstraints.getConstraintName());
				PreparedQuery<DataColumnConstraints> query = qb.prepare();
				queryDataColumnConstraintsList = dao.query(query);

				found = false;
				for (DataColumnConstraints queryDataColumnConstraintsValue : queryDataColumnConstraintsList) {
					TestCase.assertEquals(
							dataColumnConstraints.getConstraintName(),
							queryDataColumnConstraintsValue.getConstraintName());
					if (dataColumnConstraints.getConstraintType()
							.equals(queryDataColumnConstraintsValue
									.getConstraintType())
							&& (dataColumnConstraints.getValue() == null ? queryDataColumnConstraintsValue
									.getValue() == null : dataColumnConstraints
									.getValue().equals(
											queryDataColumnConstraintsValue
													.getValue()))) {
						TestCase.assertFalse(found);
						found = true;
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

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();
		if (dao.isTableExists()) {
			List<DataColumnConstraints> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Get a range constraint
				DataColumnConstraints dataColumnConstraints = dao
						.queryByConstraintName(
								TestUtils.SAMPLE_RANGE_CONSTRAINT).get(0);

				// Update
				BigDecimal updatedMax = new BigDecimal(99.1234);
				dataColumnConstraints.setMax(updatedMax);
				dao.update(dataColumnConstraints);

				// Verify update
				dao = geoPackage.getDataColumnConstraintsDao();
				DataColumnConstraints updatedDataColumns = dao
						.queryByConstraintName(
								TestUtils.SAMPLE_RANGE_CONSTRAINT).get(0);
				TestCase.assertEquals(updatedMax.doubleValue(),
						updatedDataColumns.getMax().doubleValue(), .00001);

				// Prepared update
				UpdateBuilder<DataColumnConstraints, Void> ub = dao
						.updateBuilder();
				ub.updateColumnValue(
						DataColumnConstraints.COLUMN_MIN_IS_INCLUSIVE, false);
				ub.updateColumnValue(
						DataColumnConstraints.COLUMN_MAX_IS_INCLUSIVE, false);
				ub.where().eq(DataColumnConstraints.COLUMN_CONSTRAINT_TYPE,
						DataColumnConstraintType.RANGE.getValue());
				PreparedUpdate<DataColumnConstraints> update = ub.prepare();
				int updated = dao.update(update);
				TestCase.assertTrue(updated > 0);

				for (DataColumnConstraints updatedQueryDataColumnConstraints : dao
						.queryByConstraintName(TestUtils.SAMPLE_RANGE_CONSTRAINT)) {
					TestCase.assertEquals(false,
							updatedQueryDataColumnConstraints
									.getMinIsInclusive().booleanValue());
					TestCase.assertEquals(false,
							updatedQueryDataColumnConstraints
									.getMaxIsInclusive().booleanValue());
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

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();

		if (dao.isTableExists()) {

			// Get current count
			long count = dao.countOf();

			// Create new data column constraints
			String name = "test_create_constraint";
			DataColumnConstraintType type = DataColumnConstraintType.RANGE;
			BigDecimal min = new BigDecimal(50);
			boolean minInclusive = false;
			BigDecimal max = new BigDecimal(50000000);
			boolean maxInclusive = true;
			String description = "test create description";

			DataColumnConstraints dataColumnConstraints = new DataColumnConstraints();
			dataColumnConstraints.setConstraintName(name);
			dataColumnConstraints.setConstraintType(type);
			dataColumnConstraints.setMin(min);
			dataColumnConstraints.setMinIsInclusive(minInclusive);
			dataColumnConstraints.setMax(max);
			dataColumnConstraints.setMaxIsInclusive(maxInclusive);
			dataColumnConstraints.setDescription(description);
			dao.create(dataColumnConstraints);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved data column constraints
			DataColumnConstraints queryDataColumnConstraints = dao
					.queryByUnique(name, type, null);
			TestCase.assertEquals(name,
					queryDataColumnConstraints.getConstraintName());
			TestCase.assertEquals(type,
					queryDataColumnConstraints.getConstraintType());
			TestCase.assertNull(queryDataColumnConstraints.getValue());
			TestCase.assertEquals(min, queryDataColumnConstraints.getMin());
			TestCase.assertEquals(minInclusive, queryDataColumnConstraints
					.getMinIsInclusive().booleanValue());
			TestCase.assertEquals(max, queryDataColumnConstraints.getMax());
			TestCase.assertEquals(maxInclusive, queryDataColumnConstraints
					.getMaxIsInclusive().booleanValue());
			TestCase.assertEquals(description,
					queryDataColumnConstraints.getDescription());

		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();
		DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
		if (dao.isTableExists()) {
			List<DataColumnConstraints> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random data column constraints
				int random = (int) (Math.random() * results.size());
				DataColumnConstraints dataColumnConstraints = results
						.get(random);

				int remainingConstraints = dao.queryByConstraintName(
						dataColumnConstraints.getConstraintName()).size();
				int dataColumns = dataColumnsDao.queryByConstraintName(
						dataColumnConstraints.getConstraintName()).size();

				// Delete the data column constraints
				dao.deleteCascade(dataColumnConstraints);

				// Verify deleted
				DataColumnConstraints queryDataColumns = dao.queryByUnique(
						dataColumnConstraints.getConstraintName(),
						dataColumnConstraints.getConstraintType(),
						dataColumnConstraints.getValue());
				TestCase.assertNull(queryDataColumns);

				// Verify cascade delete
				int afterRemainingConstraints = dao.queryByConstraintName(
						dataColumnConstraints.getConstraintName()).size();
				int afterDataColumns = dataColumnsDao.queryByConstraintName(
						dataColumnConstraints.getConstraintName()).size();

				TestCase.assertEquals(remainingConstraints - 1,
						afterRemainingConstraints);
				if (remainingConstraints == 1) {
					TestCase.assertEquals(0, afterDataColumns);
				} else {
					TestCase.assertEquals(dataColumns, afterDataColumns);
				}

				// Prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random data column constraints
					random = (int) (Math.random() * results.size());
					dataColumnConstraints = results.get(random);

					// Find which data column constraints to delete
					QueryBuilder<DataColumnConstraints, Void> qb = dao
							.queryBuilder();
					qb.where().eq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
							dataColumnConstraints.getConstraintName());
					PreparedQuery<DataColumnConstraints> query = qb.prepare();
					List<DataColumnConstraints> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<DataColumnConstraints, Void> db = dao
							.deleteBuilder();
					db.where().eq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
							dataColumnConstraints.getConstraintName());
					PreparedDelete<DataColumnConstraints> deleteQuery = db
							.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
