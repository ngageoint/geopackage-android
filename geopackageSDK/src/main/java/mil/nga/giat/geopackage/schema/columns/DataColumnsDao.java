package mil.nga.giat.geopackage.schema.columns;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.schema.TableColumnKey;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Data Columns Data Access Object
 * 
 * @author osbornb
 */
public class DataColumnsDao extends BaseDaoImpl<DataColumns, TableColumnKey> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public DataColumnsDao(ConnectionSource connectionSource,
			Class<DataColumns> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumns queryForId(TableColumnKey key) throws SQLException {
		DataColumns dataColumns = null;
		if (key != null) {
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(DataColumns.COLUMN_TABLE_NAME, key.getTableName());
			fieldValues
					.put(DataColumns.COLUMN_COLUMN_NAME, key.getColumnName());
			List<DataColumns> results = queryForFieldValues(fieldValues);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					throw new SQLException("More than one "
							+ DataColumns.class.getSimpleName()
							+ " returned for key. Table Name: "
							+ key.getTableName() + ", Column Name: "
							+ key.getColumnName());
				}
				dataColumns = results.get(0);
			}
		}
		return dataColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableColumnKey extractId(DataColumns data) throws SQLException {
		return data.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean idExists(TableColumnKey id) throws SQLException {
		return queryForId(id) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumns queryForSameId(DataColumns data) throws SQLException {
		return queryForId(data.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateId(DataColumns data, TableColumnKey newId)
			throws SQLException {
		int count = 0;
		DataColumns readData = queryForId(data.getId());
		if (readData != null && newId != null) {
			readData.setId(newId);
			count = update(readData);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteById(TableColumnKey id) throws SQLException {
		int count = 0;
		if (id != null) {
			DataColumns dataColumns = queryForId(id);
			if (dataColumns != null) {
				count = delete(dataColumns);
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteIds(Collection<TableColumnKey> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (TableColumnKey id : idCollection) {
				count += deleteById(id);
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Update using the complex key
	 */
	@Override
	public int update(DataColumns dataColumns) throws SQLException {

		UpdateBuilder<DataColumns, TableColumnKey> ub = updateBuilder();
		ub.updateColumnValue(DataColumns.COLUMN_NAME, dataColumns.getName());
		ub.updateColumnValue(DataColumns.COLUMN_TITLE, dataColumns.getTitle());
		ub.updateColumnValue(DataColumns.COLUMN_DESCRIPTION,
				dataColumns.getDescription());
		ub.updateColumnValue(DataColumns.COLUMN_MIME_TYPE,
				dataColumns.getMimeType());
		ub.updateColumnValue(DataColumns.COLUMN_CONSTRAINT_NAME,
				dataColumns.getConstraintName());

		ub.where()
				.eq(DataColumns.COLUMN_TABLE_NAME, dataColumns.getTableName())
				.and()
				.eq(DataColumns.COLUMN_COLUMN_NAME, dataColumns.getColumnName());

		PreparedUpdate<DataColumns> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

	/**
	 * Query by the constraint name
	 * 
	 * @param constraintName
	 * @return
	 * @throws SQLException
	 */
	public List<DataColumns> queryByConstraintName(String constraintName)
			throws SQLException {
		return queryForEq(DataColumns.COLUMN_CONSTRAINT_NAME, constraintName);
	}

}
