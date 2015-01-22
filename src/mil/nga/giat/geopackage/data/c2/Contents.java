package mil.nga.giat.geopackage.data.c2;

import java.util.Date;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Contents object. Provides identifying and descriptive information that an
 * application can display to a user in a menu of geospatial data that is
 * available for access and/or update.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_contents", daoClass = ContentsDao.class)
public class Contents {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_contents";

	/**
	 * tableName field name
	 */
	public static final String COLUMN_TABLE_NAME = "table_name";

	/**
	 * id field name, tableName
	 */
	public static final String COLUMN_ID = COLUMN_TABLE_NAME;

	/**
	 * dataType field name
	 */
	public static final String COLUMN_DATA_TYPE = "data_type";

	/**
	 * identifier field name
	 */
	public static final String COLUMN_IDENTIFIER = "identifier";

	/**
	 * description field name
	 */
	public static final String COLUMN_DESCRIPTION = "description";

	/**
	 * lastChange field name
	 */
	public static final String COLUMN_LAST_CHANGE = "last_change";

	/**
	 * minX field name
	 */
	public static final String COLUMN_MIN_X = "min_x";

	/**
	 * minY field name
	 */
	public static final String COLUMN_MIN_Y = "min_y";

	/**
	 * maxX field name
	 */
	public static final String COLUMN_MAX_X = "max_x";

	/**
	 * maxY field name
	 */
	public static final String COLUMN_MAX_Y = "max_y";

	/**
	 * srsId field name
	 */
	public static final String COLUMN_SRS_ID = SpatialReferenceSystem.COLUMN_SRS_ID;

	/**
	 * The name of the tiles, or feature table
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, id = true, canBeNull = false)
	private String tableName;

	/**
	 * Type of data stored in the table:. “features” per clause Features,
	 * “tiles” per clause Tiles, or an implementer-defined value for other data
	 * tables per clause in an Extended GeoPackage.
	 */
	@DatabaseField(columnName = COLUMN_DATA_TYPE, canBeNull = false)
	private String dataType;

	/**
	 * A human-readable identifier (e.g. short name) for the table_name content
	 */
	@DatabaseField(columnName = COLUMN_IDENTIFIER, unique = true)
	private String identifier;

	/**
	 * A human-readable description for the table_name content
	 */
	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	private String description;

	/**
	 * timestamp value in ISO 8601 format as defined by the strftime function
	 * %Y-%m-%dT%H:%M:%fZ format string applied to the current time
	 */
	@DatabaseField(columnName = COLUMN_LAST_CHANGE, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date lastChange;

	/**
	 * Bounding box minimum easting or longitude for all content in table_name
	 */
	@DatabaseField(columnName = COLUMN_MIN_X)
	private Double minX;

	/**
	 * Bounding box minimum northing or latitude for all content in table_name
	 */
	@DatabaseField(columnName = COLUMN_MIN_Y)
	private Double minY;

	/**
	 * Bounding box maximum easting or longitude for all content in table_name
	 */
	@DatabaseField(columnName = COLUMN_MAX_X)
	private Double maxX;

	/**
	 * Bounding box maximum northing or latitude for all content in table_name
	 */
	@DatabaseField(columnName = COLUMN_MAX_Y)
	private Double maxY;

	/**
	 * Spatial Reference System ID
	 */
	@DatabaseField(columnName = COLUMN_SRS_ID, foreign = true, foreignAutoRefresh = true)
	private SpatialReferenceSystem srs;

	/**
	 * Unique identifier for each Spatial Reference System within a GeoPackage
	 */
	@DatabaseField(columnName = COLUMN_SRS_ID)
	private int srsId;

	/**
	 * Geometry Columns
	 */
	@ForeignCollectionField(eager = false)
	private ForeignCollection<GeometryColumns> geometryColumns;

	/**
	 * Default Constructor
	 */
	public Contents() {

	}

	public String getId() {
		return tableName;
	}

	public void setId(String id) {
		this.tableName = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ContentsDataType getDataType() {
		return ContentsDataType.fromName(dataType);
	}

	public void setDataType(ContentsDataType dataType) {
		this.dataType = dataType.getName();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getLastChange() {
		return lastChange;
	}

	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}

	public Double getMinX() {
		return minX;
	}

	public void setMinX(Double minX) {
		this.minX = minX;
	}

	public Double getMinY() {
		return minY;
	}

	public void setMinY(Double minY) {
		this.minY = minY;
	}

	public Double getMaxX() {
		return maxX;
	}

	public void setMaxX(Double maxX) {
		this.maxX = maxX;
	}

	public Double getMaxY() {
		return maxY;
	}

	public void setMaxY(Double maxY) {
		this.maxY = maxY;
	}

	public SpatialReferenceSystem getSrs() {
		return srs;
	}

	public void setSrs(SpatialReferenceSystem srs) {
		this.srs = srs;
		if(srs != null){
			srsId = srs.getId();
		}
	}

	public int getSrsId() {
		return srsId;
	}

	public ForeignCollection<GeometryColumns> getGeometryColumns() {
		return geometryColumns;
	}

	public void setGeometryColumns(
			ForeignCollection<GeometryColumns> geometryColumns) {
		this.geometryColumns = geometryColumns;
	}

}
