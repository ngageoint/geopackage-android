package mil.nga.giat.geopackage.data.c4;

import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
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

		byte[] geometryBytes = getBlob(dao.getGeometryColumnIndex());

		GeoPackageGeometryData geometry = null;
		if (geometryBytes != null) {
			geometry = new GeoPackageGeometryData(geometryBytes);
		}

		return geometry;
	}

}
