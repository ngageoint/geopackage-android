package mil.nga.giat.geopackage.metadata;

import mil.nga.giat.geopackage.metadata.reference.MetadataReference;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Contains metadata in MIME encodings structured in accordance with any
 * authoritative metadata specification
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_metadata", daoClass = MetadataDao.class)
public class Metadata {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_metadata";

	/**
	 * id field name
	 */
	public static final String COLUMN_ID = "id";

	/**
	 * mdScope field name
	 */
	public static final String COLUMN_MD_SCOPE = "md_scope";

	/**
	 * mdStandardUri field name
	 */
	public static final String COLUMN_MD_STANDARD_URI = "md_standard_uri";

	/**
	 * mimeType field name
	 */
	public static final String COLUMN_MIME_TYPE = "mime_type";

	/**
	 * metadata field name
	 */
	public static final String COLUMN_METADATA = "metadata";

	/**
	 * Metadata primary key
	 */
	@DatabaseField(columnName = COLUMN_ID, id = true, canBeNull = false)
	private long id;

	/**
	 * Case sensitive name of the data scope to which this metadata applies; see
	 * Metadata Scopes below
	 */
	@DatabaseField(columnName = COLUMN_MD_SCOPE, canBeNull = false)
	private String mdScope;

	/**
	 * URI reference to the metadata structure definition authority
	 */
	@DatabaseField(columnName = COLUMN_MD_STANDARD_URI, canBeNull = false)
	private String mdStandardUri;

	/**
	 * MIME encoding of metadata
	 */
	@DatabaseField(columnName = COLUMN_MIME_TYPE, canBeNull = false)
	private String mimeType;

	/**
	 * metadata
	 */
	@DatabaseField(columnName = COLUMN_METADATA, canBeNull = false)
	private String metadata;

	/**
	 * Metadata References
	 */
	@ForeignCollectionField(eager = false)
	private ForeignCollection<MetadataReference> metadataReferences;

	/**
	 * Default Constructor
	 */
	public Metadata() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MetadataScopeType getMetadataScope() {
		return MetadataScopeType.fromName(mdScope);
	}

	public void setMetadataScope(MetadataScopeType metadataScope) {
		this.mdScope = metadataScope.getName();
	}

	public String getMdStandardUri() {
		return mdStandardUri;
	}

	public void setMdStandardUri(String mdStandardUri) {
		this.mdStandardUri = mdStandardUri;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public ForeignCollection<MetadataReference> getMetadataReferences() {
		return metadataReferences;
	}

}
