package mil.nga.giat.geopackage.schema.constraints;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.geopackage.schema.columns.DataColumns;
import mil.nga.giat.geopackage.schema.columns.DataColumnsDao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
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

			// Delete Data Columns
			DataColumnsDao dao = getDataColumnsDao();
			List<DataColumns> dataColumnsCollection = dao
					.queryForConstraintName(constraints.getConstraintName());
			if (!dataColumnsCollection.isEmpty()) {
				dao.delete(dataColumnsCollection);
			}

			// Delete
			count = super.delete(constraints);
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
			List<DataColumnConstraints> constraintsList = super
					.query(preparedDelete);
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
	 * Query for the constraint name
	 * 
	 * @param constraintName
	 * @return
	 * @throws SQLException
	 */
	public List<DataColumnConstraints> queryForConstraintName(
			String constraintName) throws SQLException {
		return queryForEq(DataColumnConstraints.COLUMN_CONSTRAINT_NAME,
				constraintName);
	}

}
