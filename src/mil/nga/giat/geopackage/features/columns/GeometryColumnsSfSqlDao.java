package mil.nga.giat.geopackage.features.columns;

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
 * SF/SQL Geometry Columns Data Access Object
 * 
 * @author osbornb
 */
public class GeometryColumnsSfSqlDao extends
		BaseDaoImpl<GeometryColumnsSfSql, TableColumnKey> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public GeometryColumnsSfSqlDao(ConnectionSource connectionSource,
			Class<GeometryColumnsSfSql> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsSfSql queryForId(TableColumnKey key)
			throws SQLException {
		GeometryColumnsSfSql geometryColumns = null;
		if (key != null) {
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(GeometryColumnsSfSql.COLUMN_F_TABLE_NAME,
					key.getTableName());
			fieldValues.put(GeometryColumnsSfSql.COLUMN_F_GEOMETRY_COLUMN,
					key.getColumnName());
			List<GeometryColumnsSfSql> results = queryForFieldValues(fieldValues);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					throw new SQLException("More than one "
							+ GeometryColumnsSfSql.class.getSimpleName()
							+ " returned for key. Table Name: "
							+ key.getTableName() + ", Column Name: "
							+ key.getColumnName());
				}
				geometryColumns = results.get(0);
			}
		}
		return geometryColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableColumnKey extractId(GeometryColumnsSfSql data)
			throws SQLException {
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
	public GeometryColumnsSfSql queryForSameId(GeometryColumnsSfSql data)
			throws SQLException {
		return queryForId(data.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateId(GeometryColumnsSfSql data, TableColumnKey newId)
			throws SQLException {
		int count = 0;
		GeometryColumnsSfSql readData = queryForId(data.getId());
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
			GeometryColumnsSfSql geometryColumns = queryForId(id);
			if (geometryColumns != null) {
				count = delete(geometryColumns);
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
	public int update(GeometryColumnsSfSql geometryColumns) throws SQLException {

		UpdateBuilder<GeometryColumnsSfSql, TableColumnKey> ub = updateBuilder();
		ub.updateColumnValue(GeometryColumnsSfSql.COLUMN_GEOMETRY_TYPE,
				geometryColumns.getGeometryTypeCode());
		ub.updateColumnValue(GeometryColumnsSfSql.COLUMN_COORD_DIMENSION,
				geometryColumns.getCoordDimension());
		ub.updateColumnValue(GeometryColumnsSfSql.COLUMN_SRID,
				geometryColumns.getSrid());

		ub.where()
				.eq(GeometryColumnsSfSql.COLUMN_F_TABLE_NAME,
						geometryColumns.getFTableName())
				.and()
				.eq(GeometryColumnsSfSql.COLUMN_F_GEOMETRY_COLUMN,
						geometryColumns.getFGeometryColumn());

		PreparedUpdate<GeometryColumnsSfSql> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

}
