package mil.nga.giat.geopackage.metadata.reference;

import java.util.Date;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.metadata.Metadata;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Links metadata in the gpkg_metadata table to data in the feature, and tiles
 * tables
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_metadata_reference", daoClass = MetadataReferenceDao.class)
public class MetadataReference {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_metadata_reference";

	/**
	 * referenceScope field name
	 */
	public static final String COLUMN_REFERENCE_SCOPE = "reference_scope";

	/**
	 * tableName field name
	 */
	public static final String COLUMN_TABLE_NAME = "table_name";

	/**
	 * columnName field name
	 */
	public static final String COLUMN_COLUMN_NAME = "column_name";

	/**
	 * rowIdValue field name
	 */
	public static final String COLUMN_ROW_ID_VALUE = "row_id_value";

	/**
	 * timestamp field name
	 */
	public static final String COLUMN_TIMESTAMP = "timestamp";

	/**
	 * mdFileId field name
	 */
	public static final String COLUMN_FILE_ID = "md_file_id";

	/**
	 * mdParentId field name
	 */
	public static final String COLUMN_PARENT_ID = "md_parent_id";

	/**
	 * Lowercase metadata reference scope; one of ‘geopackage’,
	 * ‘table’,‘column’, ’row’, ’row/col’
	 */
	@DatabaseField(columnName = COLUMN_REFERENCE_SCOPE, canBeNull = false)
	private String referenceScope;

	/**
	 * Name of the table to which this metadata reference applies, or NULL for
	 * reference_scope of ‘geopackage’.
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME)
	private String tableName;

	/**
	 * Name of the column to which this metadata reference applies; NULL for
	 * reference_scope of ‘geopackage’,‘table’ or ‘row’, or the name of a column
	 * in the table_name table for reference_scope of ‘column’ or ‘row/col’
	 */
	@DatabaseField(columnName = COLUMN_COLUMN_NAME)
	private String columnName;

	/**
	 * NULL for reference_scope of ‘geopackage’, ‘table’ or ‘column’, or the
	 * rowed of a row record in the table_name table for reference_scope of
	 * ‘row’ or ‘row/col’
	 */
	@DatabaseField(columnName = COLUMN_ROW_ID_VALUE)
	private Long rowIdValue;

	/**
	 * timestamp value in ISO 8601 format as defined by the strftime function
	 * '%Y-%m-%dT%H:%M:%fZ' format string applied to the current time
	 */
	@DatabaseField(columnName = COLUMN_TIMESTAMP, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", canBeNull = false)
	private Date timestamp;

	/**
	 * Metadata
	 */
	@DatabaseField(columnName = COLUMN_FILE_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Metadata metadata;

	/**
	 * gpkg_metadata table id column value for the metadata to which this
	 * gpkg_metadata_reference applies
	 */
	@DatabaseField(columnName = COLUMN_FILE_ID, canBeNull = false)
	private long fileId;

	/**
	 * Parent Metadata
	 */
	@DatabaseField(columnName = COLUMN_PARENT_ID, foreign = true, foreignAutoRefresh = true)
	private Metadata parentMetadata;

	/**
	 * gpkg_metadata table id column value for the hierarchical parent
	 * gpkg_metadata for the gpkg_metadata to which this gpkg_metadata_reference
	 * applies, or NULL if md_file_id forms the root of a metadata hierarchy
	 */
	@DatabaseField(columnName = COLUMN_PARENT_ID)
	private Long parentId;

	/**
	 * Default Constructor
	 */
	public MetadataReference() {

	}

	public ReferenceScopeType getReferenceScope() {
		return ReferenceScopeType.fromValue(referenceScope);
	}

	public void setReferenceScope(ReferenceScopeType referenceScope) {
		this.referenceScope = referenceScope.getValue();
		switch (referenceScope) {
		case GEOPACKAGE:
			setTableName(null);
			setColumnName(null);
			setRowIdValue(null);
			break;
		case TABLE:
			setColumnName(null);
			setRowIdValue(null);
			break;
		case ROW:
			setColumnName(null);
			break;
		case COLUMN:
			setRowIdValue(null);
			break;
		case ROW_COL:
			break;
		default:

		}
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		if (referenceScope != null && tableName != null
				&& getReferenceScope().equals(ReferenceScopeType.GEOPACKAGE)) {
			throw new GeoPackageException("The table name must be null for "
					+ ReferenceScopeType.GEOPACKAGE + " reference scope");
		}
		this.tableName = tableName;

	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		if (referenceScope != null && columnName != null) {
			ReferenceScopeType scopeType = getReferenceScope();
			if (scopeType.equals(ReferenceScopeType.GEOPACKAGE)
					|| scopeType.equals(ReferenceScopeType.TABLE)
					|| scopeType.equals(ReferenceScopeType.ROW)) {
				throw new GeoPackageException(
						"The column name must be null for " + scopeType
								+ " reference scope");
			}
		}
		this.columnName = columnName;
	}

	public Long getRowIdValue() {
		return rowIdValue;
	}

	public void setRowIdValue(Long rowIdValue) {
		if (referenceScope != null && rowIdValue != null) {
			ReferenceScopeType scopeType = getReferenceScope();
			if (scopeType.equals(ReferenceScopeType.GEOPACKAGE)
					|| scopeType.equals(ReferenceScopeType.TABLE)
					|| scopeType.equals(ReferenceScopeType.COLUMN)) {
				throw new GeoPackageException(
						"The row id value must be null for " + scopeType
								+ " reference scope");
			}
		}
		this.rowIdValue = rowIdValue;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
		fileId = metadata != null ? metadata.getId() : -1;
	}

	public long getFileId() {
		return fileId;
	}

	public Metadata getParentMetadata() {
		return parentMetadata;
	}

	public void setParentMetadata(Metadata parentMetadata) {
		this.parentMetadata = parentMetadata;
		parentId = parentMetadata != null ? parentMetadata.getId() : -1;
	}

	public Long getParentId() {
		return parentId;
	}

}
