package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryData;
import mil.nga.giat.geopackage.user.UserCursor;
import android.database.Cursor;

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
	 * @param dao
	 * @param cursor
	 */
	public FeatureCursor(FeatureTable table, Cursor cursor) {
		super(table, cursor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FeatureRow getRow(int[] columnTypes, Object[] values) {
		FeatureRow row = new FeatureRow(getTable(), columnTypes, values);
		return row;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Handles geometries
	 */
	@Override
	protected Object getValue(FeatureColumn column) {
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

		byte[] geometryBytes = getBlob(getTable().getGeometryColumnIndex());

		GeoPackageGeometryData geometry = null;
		if (geometryBytes != null) {
			geometry = new GeoPackageGeometryData(geometryBytes);
		}

		return geometry;
	}

}
