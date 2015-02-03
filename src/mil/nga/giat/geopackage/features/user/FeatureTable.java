package mil.nga.giat.geopackage.features.user;

import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.user.UserTable;

/**
 * Represents a user feature table
 * 
 * @author osbornb
 */
public class FeatureTable extends UserTable<FeatureColumn> {

	/**
	 * Geometry column index
	 */
	private final int geometryIndex;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param columns
	 */
	public FeatureTable(String tableName, List<FeatureColumn> columns) {
		super(tableName, columns);

		Integer geometry = null;

		// Find the geometry
		for (FeatureColumn column : columns) {

			if (column.isGeometry()) {
				if (geometry != null) {
					throw new GeoPackageException(
							"More than one geometry column was found for feature table '"
									+ tableName + "'. Index " + geometry
									+ " and " + column.getIndex());

				}
				geometry = column.getIndex();
			}

		}

		if (geometry == null) {
			throw new GeoPackageException(
					"No geometry column was found for feature table '"
							+ tableName + "'");
		}
		geometryIndex = geometry;

	}

	/**
	 * Get the geometry column index
	 * 
	 * @return
	 */
	public int getGeometryColumnIndex() {
		return geometryIndex;
	}

	/**
	 * Get the geometry feature column
	 * 
	 * @return
	 */
	public FeatureColumn getGeometryColumn() {
		return getColumn(geometryIndex);
	}

}
