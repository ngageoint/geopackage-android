package mil.nga.geopackage.features.user;

import android.database.Cursor;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserCursor;

/**
 * Feature Cursor to wrap a database cursor for feature queries
 *
 * @author osbornb
 */
public class FeatureCursor extends
		UserCursor<FeatureColumn, FeatureTable, FeatureRow> {

	/**
	 * Constructor
	 *
	 * @param table
	 * @param cursor
	 */
	public FeatureCursor(FeatureTable table, Cursor cursor) {
		super(table, cursor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public FeatureRow getRow(int[] columnTypes, Object[] values) {
		FeatureRow row = new FeatureRow(getTable(), columnTypes, values);
		return row;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Handles geometries
	 */
	@Override
    public Object getValue(FeatureColumn column) {
		Object value;
		if (column.isGeometry()) {
			value = getGeometry();
		} else {
			value = super.getValue(column);
		}
		return value;
	}

	/**
	 * Get the geometry
	 *
	 * @return
	 */
	public GeoPackageGeometryData getGeometry() {

		GeoPackageGeometryData geometry = null;

		int columnIndex = getTable().getGeometryColumnIndex();
		int type = getType(columnIndex);

		if(type != FIELD_TYPE_NULL) {
			byte[] geometryBytes = getBlob(columnIndex);

			if (geometryBytes != null) {
				geometry = new GeoPackageGeometryData(geometryBytes);
			}
		}

		return geometry;
	}

}
