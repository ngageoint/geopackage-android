package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.db.GeoPackageDatabaseUtils;
import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryData;
import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Feature Cursor to wrap a database cursor for feature queries
 * 
 * @author osbornb
 */
public class FeatureCursor extends CursorWrapper {

	/**
	 * Feature DAO
	 */
	private final FeatureDao dao;

	/**
	 * Constructor
	 * 
	 * @param dao
	 * @param cursor
	 */
	public FeatureCursor(FeatureDao dao, Cursor cursor) {
		super(cursor);
		this.dao = dao;
	}

	/**
	 * Get the geometry
	 * 
	 * @return
	 */
	public GeoPackageGeometryData getGeometry() {

		byte[] geometryBytes = getBlob(dao.getTable().getGeometryColumnIndex());

		GeoPackageGeometryData geometry = null;
		if (geometryBytes != null) {
			geometry = new GeoPackageGeometryData(geometryBytes);
		}

		return geometry;
	}

	/**
	 * Get the feature row at the current cursor position
	 * 
	 * @return
	 */
	public FeatureRow getRow() {

		FeatureTable table = dao.getTable();
		int[] columnTypes = new int[table.columnCount()];
		Object[] values = new Object[table.columnCount()];

		for (FeatureColumn column : table.getColumns()) {

			int index = column.getIndex();

			columnTypes[index] = getType(index);

			if (column.isGeometry()) {
				values[index] = getGeometry();
			} else {
				values[index] = GeoPackageDatabaseUtils.getValue(this, index,
						column.getDataType());
			}

		}

		FeatureRow row = new FeatureRow(table, columnTypes, values);

		return row;
	}

}
