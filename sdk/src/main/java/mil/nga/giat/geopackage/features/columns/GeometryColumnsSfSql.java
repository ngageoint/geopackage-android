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
 * SF/SQL Geometry Columns object. Identifies the geometry columns in tables
 * that contain user data representing features.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "geometry_columns", daoClass = GeometryColumnsSfSqlDao.class)
public class GeometryColumnsSfSql {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "geometry_columns";

	/**
	 * fTableName field name
	 */
	public static final String COLUMN_F_TABLE_NAME = "f_table_name";

	/**
	 * fGeometryColumn field name
	 */
	public static final String COLUMN_F_GEOMETRY_COLUMN = "f_geometry_column";

	/**
	 * id 1 field name, fTableName
	 */
	public static final String COLUMN_ID_1 = COLUMN_F_TABLE_NAME;

	/**
	 * id 2 field name, fGeometryColumn
	 */
	public static final String COLUMN_ID_2 = COLUMN_F_GEOMETRY_COLUMN;

	/**
	 * geometryType field name
	 */
	public static final String COLUMN_GEOMETRY_TYPE = "geometry_type";

	/**
	 * coordDimension field name
	 */
	public static final String COLUMN_COORD_DIMENSION = "coord_dimension";

	/**
	 * srid field name
	 */
	public static final String COLUMN_SRID = "srid";

	/**
	 * Foreign key to Contents by table name
	 */
	@DatabaseField(columnName = COLUMN_F_TABLE_NAME, canBeNull = false, unique = true, foreign = true, foreignAutoRefresh = true)
	private Contents contents;

	/**
	 * Name of the table containing the geometry column
	 */
	@DatabaseField(columnName = COLUMN_F_TABLE_NAME, id = true, canBeNull = false, uniqueCombo = true)
	private String fTableName;

	/**
	 * Name of a column in the feature table that is a Geometry Column
	 */
	@DatabaseField(columnName = COLUMN_F_GEOMETRY_COLUMN, canBeNull = false, uniqueCombo = true)
	private String fGeometryColumn;

	/**
	 * Geometry Type Code (Core) or Geometry Type Codes (Extension) in Geometry
	 * Types (Normative)
	 */
	@DatabaseField(columnName = COLUMN_GEOMETRY_TYPE, canBeNull = false)
	private int geometryType;

	/**
	 * Coord Dimension from z and m values
	 */
	@DatabaseField(columnName = COLUMN_COORD_DIMENSION, canBeNull = false)
	private byte coordDimension;

	/**
	 * Spatial Reference System ID: gpkg_spatial_ref_sys.srs_id
	 */
	@DatabaseField(columnName = COLUMN_SRID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private SpatialReferenceSystem srs;

	/**
	 * Unique identifier for each Spatial Reference System within a GeoPackage
	 */
	@DatabaseField(columnName = COLUMN_SRID, canBeNull = false)
	private long srid;

	/**
	 * Default Constructor
	 */
	public GeometryColumnsSfSql() {

	}

	/**
	 * Get the id
	 * 
	 * @return
	 */
	public TableColumnKey getId() {
		return new TableColumnKey(fTableName, fGeometryColumn);
	}

	/**
	 * Set the id
	 * 
	 * @param id
	 */
	public void setId(TableColumnKey id) {
		fTableName = id.getTableName();
		fGeometryColumn = id.getColumnName();
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
						+ GeometryColumnsSfSql.class.getSimpleName()
						+ " must have a data type of "
						+ ContentsDataType.FEATURES.getName());
			}
			fTableName = contents.getId();
		}
	}

	public String getFTableName() {
		return fTableName;
	}

	public String getFGeometryColumn() {
		return fGeometryColumn;
	}

	public void setFGeometryColumn(String fGeometryColumn) {
		this.fGeometryColumn = fGeometryColumn;
	}

	public GeometryType getGeometryType() {
		return GeometryType.fromCode(geometryType);
	}

	public void setGeometryType(GeometryType geometryType) {
		this.geometryType = geometryType.getCode();
	}

	public int getGeometryTypeCode() {
		return geometryType;
	}

	public void setCoordDimension(byte coordDimension) {
		validateCoordDimension(COLUMN_COORD_DIMENSION, coordDimension);
		this.coordDimension = coordDimension;
	}

	public byte getCoordDimension() {
		return coordDimension;
	}

	public SpatialReferenceSystem getSrs() {
		return srs;
	}

	public void setSrs(SpatialReferenceSystem srs) {
		this.srs = srs;
		if (srs != null) {
			srid = srs.getId();
		}
	}

	public long getSrid() {
		return srid;
	}

	/**
	 * Validate the coord dimension, between 2 and 5 per the view
	 * 
	 * @param column
	 * @param value
	 */
	private void validateCoordDimension(String column, byte value) {
		if (value < 2 || value > 5) {
			throw new GeoPackageException(column
					+ " value must be between 2 and 5");
		}
	}

}
