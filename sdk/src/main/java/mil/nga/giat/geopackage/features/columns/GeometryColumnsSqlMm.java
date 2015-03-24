package mil.nga.giat.geopackage.features.columns;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDataType;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.wkb.geom.GeometryType;
import mil.nga.giat.geopackage.schema.TableColumnKey;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * SQL/MM Geometry Columns object. Identifies the geometry columns in tables
 * that contain user data representing features.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "st_geometry_columns", daoClass = GeometryColumnsSqlMmDao.class)
public class GeometryColumnsSqlMm {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "st_geometry_columns";

	/**
	 * tableName field name
	 */
	public static final String COLUMN_TABLE_NAME = Contents.COLUMN_TABLE_NAME;

	/**
	 * columnName field name
	 */
	public static final String COLUMN_COLUMN_NAME = "column_name";

	/**
	 * id 1 field name, tableName
	 */
	public static final String COLUMN_ID_1 = COLUMN_TABLE_NAME;

	/**
	 * id 2 field name, columnName
	 */
	public static final String COLUMN_ID_2 = COLUMN_COLUMN_NAME;

	/**
	 * geometryTypeName field name
	 */
	public static final String COLUMN_GEOMETRY_TYPE_NAME = "geometry_type_name";

	/**
	 * The prefix added to geometry types
	 */
	public static final String COLUMN_GEOMETRY_TYPE_NAME_PREFIX = "ST_";

	/**
	 * srsId field name
	 */
	public static final String COLUMN_SRS_ID = SpatialReferenceSystem.COLUMN_SRS_ID;

	/**
	 * srsName field name
	 */
	public static final String COLUMN_SRS_NAME = SpatialReferenceSystem.COLUMN_SRS_NAME;

	/**
	 * Foreign key to Contents by table name
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, canBeNull = false, unique = true, foreign = true, foreignAutoRefresh = true)
	private Contents contents;

	/**
	 * Name of the table containing the geometry column
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, id = true, canBeNull = false, uniqueCombo = true)
	private String tableName;

	/**
	 * Name of a column in the feature table that is a Geometry Column
	 */
	@DatabaseField(columnName = COLUMN_COLUMN_NAME, canBeNull = false, uniqueCombo = true)
	private String columnName;

	/**
	 * Name from Geometry Type Codes (Core) or Geometry Type Codes (Extension)
	 * in Geometry Types (Normative)
	 */
	@DatabaseField(columnName = COLUMN_GEOMETRY_TYPE_NAME, canBeNull = false)
	private String geometryTypeName;

	/**
	 * Spatial Reference System ID: gpkg_spatial_ref_sys.srs_id
	 */
	@DatabaseField(columnName = COLUMN_SRS_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private SpatialReferenceSystem srs;

	/**
	 * Unique identifier for each Spatial Reference System within a GeoPackage
	 */
	@DatabaseField(columnName = COLUMN_SRS_ID, canBeNull = false)
	private long srsId;

	/**
	 * Human readable name of this SRS
	 */
	@DatabaseField(columnName = COLUMN_SRS_NAME, canBeNull = false)
	private String srsName;

	/**
	 * Default Constructor
	 */
	public GeometryColumnsSqlMm() {

	}

	/**
	 * Get the id
	 * 
	 * @return
	 */
	public TableColumnKey getId() {
		return new TableColumnKey(tableName, columnName);
	}

	/**
	 * Set the id
	 * 
	 * @param id
	 */
	public void setId(TableColumnKey id) {
		tableName = id.getTableName();
		columnName = id.getColumnName();
	}

	public Contents getContents() {
		return contents;
	}

	public void setContents(Contents contents) {
		this.contents = contents;
		if (contents != null) {
			// Verify the Contents have a features data type (Spec Requirement
			// 23)
			ContentsDataType dataType = contents.getDataType();
			if (dataType == null || dataType != ContentsDataType.FEATURES) {
				throw new GeoPackageException("The "
						+ Contents.class.getSimpleName() + " of a "
						+ GeometryColumnsSqlMm.class.getSimpleName()
						+ " must have a data type of "
						+ ContentsDataType.FEATURES.getName());
			}
			tableName = contents.getId();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public GeometryType getGeometryType() {
		return GeometryType.fromName(geometryTypeName.substring(
				COLUMN_GEOMETRY_TYPE_NAME_PREFIX.length(),
				geometryTypeName.length()));
	}

	public void setGeometryType(GeometryType geometryType) {
		this.geometryTypeName = COLUMN_GEOMETRY_TYPE_NAME_PREFIX
				+ geometryType.getName();
	}

	public String getGeometryTypeName() {
		return geometryTypeName;
	}

	public SpatialReferenceSystem getSrs() {
		return srs;
	}

	public void setSrs(SpatialReferenceSystem srs) {
		this.srs = srs;
		if (srs != null) {
			srsId = srs.getId();
			srsName = srs.getSrsName();
		}
	}

	public long getSrsId() {
		return srsId;
	}

	public String getSrsName() {
		return srsName;
	}

}
