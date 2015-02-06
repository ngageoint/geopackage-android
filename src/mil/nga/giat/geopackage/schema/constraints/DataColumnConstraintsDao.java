package mil.nga.giat.geopackage.schema.constraints;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.schema.columns.DataColumns;
import mil.nga.giat.geopackage.schema.columns.DataColumnsDao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Data Column Constraints Data Access Object
 * 
 * @author osbornb
 */
public class DataColumnConstraintsDao extends
		BaseDaoImpl<DataColumnConstraints, Void> {

	/**
	 * Data Columns DAO
	 */
	private DataColumnsDao dataColumnsDao;

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public DataColumnConstraintsDao(ConnectionSource connectionSource,
			Class<DataColumnConstraints> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * Delete the Data Columns Constraints, cascading
	 * 
	 * @param constraints
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(DataColumnConstraints constraints)
			throws SQLException {
		int count = 0;

		if (constraints != null) {

			// Check if the last remaining constraint with the constraint name
			// is being deleted
			List<DataColumnConstraints> remainingConstraints = queryByConstraintName(constraints
					.getConstraintName());
			if (remainingConstraints.size() == 1) {

				DataColumnConstraints remainingConstraint = remainingConstraints
						.get(0);

				// Compare the name, type, and value
				if (remainingConstraint.getConstraintName().equals(
						constraints.getConstraintName())
						&& remainingConstraint.getConstraintType().equals(
								constraints.getConstraintType())
						&& (remainingConstraint.getValue() == null ? constraints
								.getValue() == null : remainingConstraint
								.getValue().equals(constraints.getValue()))) {

					// Delete Data Columns
					DataColumnsDao dao = getDataColumnsDao();
					List<DataColumns> dataColumnsCollection = dao
							.queryByConstraintName(constraints
									.getConstraintName());
					if (!dataColumnsCollection.isEmpty()) {
						dao.delete(dataColumnsCollection);
					}
				}
			}

			// Delete
			count = delete(constraints);
		}
		return count;
	}

	/**
	 * Delete the collection of Data Column Constraints, cascading
	 * 
	 * @param constraintsCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(
			Collection<DataColumnConstraints> constraintsCollection)
			throws SQLException {
		int count = 0;
		if (constraintsCollection != null) {
			for (DataColumnConstraints constraints : constraintsCollection) {
				count += deleteCascade(constraints);
			}
		}
		return count;
	}

	/**
	 * Delete the Data Column Constraints matching the prepared query, cascading
	 * 
	 * @param preparedDelete
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(PreparedQuery<DataColumnConstraints> preparedDelete)
			throws SQLException {
		int count = 0;
		if (preparedDelete != null) {
			List<DataColumnConstraints> constraintsList = query(preparedDelete);
			count = deleteCascade(constraintsList);
		}
		return count;
	}

	/**
	 * Get or create a Data Columns DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private DataColumnsDao getDataColumnsDao() throws SQLException {
		if (dataColumnsDao == null) {
			dataColumnsDao = DaoManager.createDao(connectionSource,
					DataColumns.class);
		}
		return dataColumnsDao;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Update using the unique columns
	 */
	@Override
	public int update(DataColumnConstraints dataColumnConstraints)
			throws SQLException {

		UpdateBuilder<DataColumnConstraints, Void> ub = updateBuilder();
		ub.updateColumnValue(DataColumnConstraints.COLUMN_MIN,
				dataColumnConstraints.getMin());
		ub.updateColumnValue(DataColumnConstraints.COLUMN_MIN_IS_INCLUSIVE,
				dataColumnConstraints.getMinIsInclusive());
		ub.updateColumnValue(DataColumnConstraints.COLUMN_MAX,
				dataColumnConstraints.getMax());
		ub.updateColumnValue(DataColumnConstraints.COLUMN_MAX_IS_INCLUSIVE,
				dataColumnConstraints.getMaxIsInclusive());
		ub.updateColumnValue(DataColumnConstraints.COLUMN_DESCRIPTION,
				dataColumnConstraints.getDescription());

		setUniqueWhere(ub.where(), dataColumnConstraints.getConstraintName(),
				dataColumnConstraints.getConstraintType(),
				dataColumnConstraints.getValue());

		PreparedUpdate<DataColumnConstraints> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Delete using the unique columns
	 */
	@Override
	public int delete(DataColumnConstraints dataColumnConstraints)
			throws SQLException {

		DeleteBuilder<DataColumnConstraints, Void> db = deleteBuilder();

		setUniqueWhere(db.where(), dataColumnConstraints.getConstraintName(),
				dataColumnConstraints.getConstraintType(),
				dataColumnConstraints.getValue());

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Query by the constraint name
	 * 
	 * @param constraintName
	 * @return
	 * @throws SQLException
	 */
	public List<DataColumnConstraints> queryByConstraintName(
			String constraintName) throws SQLException {
		return queryForEq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
				constraintName);
	}

	/**
	 * Query by the unique column values
	 * 
	 * @param constraintName
	 * @param constraintType
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	public DataColumnConstraints queryByUnique(String constraintName,
			DataColumnConstraintType constraintType, String value)
			throws SQLException {

		DataColumnConstraints constraint = null;

		QueryBuilder<DataColumnConstraints, Void> qb = queryBuilder();
		setUniqueWhere(qb.where(), constraintName, constraintType, value);
		List<DataColumnConstraints> constraints = qb.query();
		if (!constraints.isEmpty()) {

			if (constraints.size() > 1) {
				throw new GeoPackageException("More than one "
						+ DataColumnConstraints.class.getSimpleName()
						+ " was found for unique constraint. Name: "
						+ constraintName + ", Type: " + constraintType
						+ ", Value: " + value);
			}

			constraint = constraints.get(0);
		}

		return constraint;
	}

	/**
	 * Set the unique column criteria in the where clause
	 * 
	 * @param where
	 * @param constraintName
	 * @param constraintType
	 * @param value
	 * @throws SQLException
	 */
	private void setUniqueWhere(Where<DataColumnConstraints, Void> where,
			String constraintName, DataColumnConstraintType constraintType,
			String value) throws SQLException {

		where.eq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME, constraintName)
				.and()
				.eq(DataColumnConstraints.COLUMN_CONSTRAINT_TYPE,
						constraintType.getValue());
		if (value == null) {
			where.and().isNull(DataColumnConstraints.COLUMN_VALUE);
		} else {
			where.and().eq(DataColumnConstraints.COLUMN_VALUE, value);
		}

	}

}
