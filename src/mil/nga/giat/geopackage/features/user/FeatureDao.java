package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.user.UserDao;
import android.database.sqlite.SQLiteDatabase;

/**
 * Feature DAO for reading feature user data tables
 * 
 * @author osbornb
 */
public class FeatureDao extends
		UserDao<FeatureRow, FeatureCursor, FeatureTable> {

	/**
	 * Geometry Columns
	 */
	private final GeometryColumns geometryColumns;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param geometryColumns
	 * @param table
	 */
	public FeatureDao(SQLiteDatabase db, GeometryColumns geometryColumns,
			FeatureTable table) {
		super(db, table);

		this.geometryColumns = geometryColumns;
		if (geometryColumns.getContents() == null) {
			throw new GeoPackageException(GeometryColumns.class.getSimpleName()
					+ " " + geometryColumns.getId() + " has null "
					+ Contents.class.getSimpleName());
		}
		if (geometryColumns.getSrs() == null) {
			throw new GeoPackageException(GeometryColumns.class.getSimpleName()
					+ " " + geometryColumns.getId() + " has null "
					+ SpatialReferenceSystem.class.getSimpleName());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureRow newRow() {
		return new FeatureRow(getTable());
	}

	/**
	 * Get the Geometry Columns
	 * 
	 * @return
	 */
	public GeometryColumns getGeometryColumns() {
		return geometryColumns;
	}

	/**
	 * The the Geometry Column name
	 * 
	 * @return
	 */
	public String getGeometryColumnName() {
		return geometryColumns.getColumnName();
	}

	/**
	 * Get the Geometry Type
	 * 
	 * @return
	 */
	public GeometryType getGeometryType() {
		return geometryColumns.getGeometryType();
	}

}
