package mil.nga.giat.geopackage.data.c3;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDataType;
import mil.nga.giat.geopackage.util.GeoPackageException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Geometry Columns object. Identifies the geometry columns in tables that
 * contain user data representing features.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_geometry_columns", daoClass = GeometryColumnsDao.class)
public class GeometryColumns {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_geometry_columns";

	/**
	 * tableName field name
	 */
	public static final String COLUMN_TABLE_NAME = Contents.COLUMN_TABLE_NAME;

	/**
	 * columnName field name, tableName
	 */
	public static final String COLUMN_COLUMN_NAME = "column_name";

	/**
	 * geometryTypeName field name
	 */
	public static final String COLUMN_GEOMETRY_TYPE_NAME = "geometry_type_name";

	/**
	 * srsId field name
	 */
	public static final String COLUMN_SRS_ID = SpatialReferenceSystem.COLUMN_SRS_ID;

	/**
	 * z field name
	 */
	public static final String COLUMN_Z = "z";

	/**
	 * m field name
	 */
	public static final String COLUMN_M = "m";

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
	private int srsId;

	/**
	 * 0: z values prohibited; 1: z values mandatory; 2: z values optional
	 */
	@DatabaseField(columnName = COLUMN_Z, canBeNull = false)
	private Integer z;

	/**
	 * 0: m values prohibited; 1: m values mandatory; 2: m values optional
	 */
	@DatabaseField(columnName = COLUMN_M, canBeNull = false)
	private Integer m;

	/**
	 * Default Constructor
	 */
	public GeometryColumns() {

	}

	public GeometryColumnsKey getId() {
		return new GeometryColumnsKey(tableName, columnName);
	}

	public void setId(GeometryColumnsKey id) {
		tableName = id.getTableName();
		columnName = id.getColumnName();
	}

	public Contents getContents() {
		return contents;
	}

	public void setContents(Contents contents) {
		this.contents = contents;
		if (contents != null) {
			// Verify the Contents have a features data type (Spec Requirement 23)
			ContentsDataType dataType = contents.getDataType();
			if (dataType == null || dataType != ContentsDataType.FEATURES) {
				throw new GeoPackageException("The "
						+ Contents.class.getSimpleName() + " of a "
						+ GeometryColumns.class.getSimpleName()
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

	public String getGeometryTypeName() {
		return geometryTypeName;
	}

	public void setGeometryTypeName(String geometryTypeName) {
		this.geometryTypeName = geometryTypeName;
	}

	public SpatialReferenceSystem getSrs() {
		return srs;
	}

	public void setSrs(SpatialReferenceSystem srs) {
		this.srs = srs;
		if (srs != null) {
			srsId = srs.getId();
		}
	}

	public int getSrsId() {
		return srsId;
	}

	public Integer getZ() {
		return z;
	}

	public void setZ(Integer z) {
		this.z = z;
	}

	public Integer getM() {
		return m;
	}

	public void setM(Integer m) {
		this.m = m;
	}

}
