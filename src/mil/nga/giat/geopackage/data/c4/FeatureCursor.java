package mil.nga.giat.geopackage.data.c4;

import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.util.GeoPackageDatabaseUtils;
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

		for (int i = 0; i < table.columnCount(); i++) {

			columnTypes[i] = getType(i);

			if (i == table.getGeometryColumnIndex()) {
				values[i] = getGeometry();
			} else {
				values[i] = GeoPackageDatabaseUtils.getValue(this, i);
			}

		}

		FeatureRow row = new FeatureRow(table, columnTypes, values);

		return row;
	}

}
