package mil.nga.giat.geopackage.extension;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Extensions Data Access Object
 * 
 * @author osbornb
 */
public class ExtensionsDao extends BaseDaoImpl<Extensions, Void> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public ExtensionsDao(ConnectionSource connectionSource,
			Class<Extensions> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Update using the unique columns
	 */
	@Override
	public int update(Extensions extensions) throws SQLException {

		UpdateBuilder<Extensions, Void> ub = updateBuilder();
		ub.updateColumnValue(Extensions.COLUMN_DEFINITION,
				extensions.getDefinition());
		ub.updateColumnValue(Extensions.COLUMN_SCOPE, extensions.getScope()
				.getValue());

		setUniqueWhere(ub.where(), extensions.getExtensionName(), true,
				extensions.getTableName(), true, extensions.getColumnName());

		PreparedUpdate<Extensions> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Delete using the unique columns
	 */
	@Override
	public int delete(Extensions extensions) throws SQLException {

		DeleteBuilder<Extensions, Void> db = deleteBuilder();

		setUniqueWhere(db.where(), extensions.getExtensionName(), true,
				extensions.getTableName(), true, extensions.getColumnName());

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Delete by extension name
	 * 
	 * @param extensionName
	 * @return
	 * @throws SQLException
	 */
	public int deleteByExtension(String extensionName) throws SQLException {

		DeleteBuilder<Extensions, Void> db = deleteBuilder();
		db.where().eq(Extensions.COLUMN_EXTENSION_NAME, extensionName);

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Delete by extension name and table name
	 * 
	 * @param extensionName
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public int deleteByExtension(String extensionName, String tableName)
			throws SQLException {

		DeleteBuilder<Extensions, Void> db = deleteBuilder();

		setUniqueWhere(db.where(), extensionName, true, tableName, false, null);

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Delete by extension name, table name, and column name
	 * 
	 * @param extensionName
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public int deleteByExtension(String extensionName, String tableName,
			String columnName) throws SQLException {

		DeleteBuilder<Extensions, Void> db = deleteBuilder();

		setUniqueWhere(db.where(), extensionName, true, tableName, true,
				columnName);

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Query by extension name
	 * 
	 * @param extensionName
	 * @return
	 * @throws SQLException
	 */
	public List<Extensions> queryByExtension(String extensionName)
			throws SQLException {

		QueryBuilder<Extensions, Void> qb = queryBuilder();

		setUniqueWhere(qb.where(), extensionName, false, null, false, null);

		List<Extensions> extensions = qb.query();

		return extensions;
	}

	/**
	 * Query by extension name and table name
	 * 
	 * @param extensionName
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public List<Extensions> queryByExtension(String extensionName,
			String tableName) throws SQLException {

		QueryBuilder<Extensions, Void> qb = queryBuilder();

		setUniqueWhere(qb.where(), extensionName, true, tableName, false, null);

		List<Extensions> extensions = qb.query();

		return extensions;
	}

	/**
	 * Query by extension name, table name, and column name
	 * 
	 * @param extensionName
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	public Extensions queryByExtension(String extensionName, String tableName,
			String columnName) throws SQLException {

		QueryBuilder<Extensions, Void> qb = queryBuilder();

		setUniqueWhere(qb.where(), extensionName, true, tableName, true,
				columnName);

		List<Extensions> extensions = qb.query();

		Extensions extension = null;
		if (extensions.size() > 1) {
			throw new GeoPackageException("More than one "
					+ Extensions.class.getSimpleName()
					+ " existed for unique combination of Extension Name: "
					+ extensionName + ", Table Name: " + tableName
					+ ", Column Name: " + columnName);
		} else if (extensions.size() == 1) {
			extension = extensions.get(0);
		}

		return extension;
	}

	/**
	 * Set the unique column criteria in the where clause
	 * 
	 * @param where
	 * @param extensionName
	 * @param queryTableName
	 * @param tableName
	 * @param queryColumnName
	 * @param columnName
	 * @throws SQLException
	 */
	private void setUniqueWhere(Where<Extensions, Void> where,
			String extensionName, boolean queryTableName, String tableName,
			boolean queryColumnName, String columnName) throws SQLException {

		where.eq(Extensions.COLUMN_EXTENSION_NAME, extensionName);

		if (queryTableName) {
			if (tableName == null) {
				where.and().isNull(Extensions.COLUMN_TABLE_NAME);
			} else {
				where.and().eq(Extensions.COLUMN_TABLE_NAME, tableName);
			}
		}

		if (queryColumnName) {
			if (columnName == null) {
				where.and().isNull(Extensions.COLUMN_COLUMN_NAME);
			} else {
				where.and().eq(Extensions.COLUMN_COLUMN_NAME, columnName);
			}
		}

	}

}
