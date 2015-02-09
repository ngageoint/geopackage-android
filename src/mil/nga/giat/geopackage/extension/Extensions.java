package mil.nga.giat.geopackage.extension;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Indicates that a particular extension applies to a GeoPackage, a table in a
 * GeoPackage or a column of a table in a GeoPackage. An application that access
 * a GeoPackage can query the gpkg_extensions table instead of the contents of
 * all the user data tables to determine if it has the required capabilities to
 * read or write to tables with extensions, and to “fail fast” and return an
 * error message if it does not.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_extensions", daoClass = ExtensionsDao.class)
public class Extensions {

	/**
	 * Divider between extension name parts
	 */
	public static final String EXTENSION_NAME_DIVIDER = "_";

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_extensions";

	/**
	 * tableName field name
	 */
	public static final String COLUMN_TABLE_NAME = "table_name";

	/**
	 * columnName field name
	 */
	public static final String COLUMN_COLUMN_NAME = "column_name";

	/**
	 * extensionName field name
	 */
	public static final String COLUMN_EXTENSION_NAME = "extension_name";

	/**
	 * definition field name
	 */
	public static final String COLUMN_DEFINITION = "definition";

	/**
	 * scope field name
	 */
	public static final String COLUMN_SCOPE = "scope";

	/**
	 * Name of the table that requires the extension. When NULL, the extension
	 * is required for the entire GeoPackage. SHALL NOT be NULL when the
	 * column_name is not NULL.
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, uniqueCombo = true)
	private String tableName;

	/**
	 * Name of the column that requires the extension. When NULL, the extension
	 * is required for the entire table.
	 */
	@DatabaseField(columnName = COLUMN_COLUMN_NAME, uniqueCombo = true)
	private String columnName;

	/**
	 * The case sensitive name of the extension that is required, in the form
	 * <author>_<extension_name>.
	 */
	@DatabaseField(columnName = COLUMN_EXTENSION_NAME, canBeNull = false, uniqueCombo = true)
	private String extensionName;

	/**
	 * Definition of the extension in the form specfied by the template in
	 * GeoPackage Extension Template (Normative) or reference thereto.
	 */
	@DatabaseField(columnName = COLUMN_DEFINITION, canBeNull = false)
	private String definition;

	/**
	 * Indicates scope of extension effects on readers / writers: read-write or
	 * write-only in lowercase.
	 */
	@DatabaseField(columnName = COLUMN_SCOPE, canBeNull = false)
	private String scope;

	/**
	 * Default Constructor
	 */
	public Extensions() {

	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
		if (tableName == null) {
			columnName = null;
		}
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	/**
	 * Set the extension name by combining the required parts
	 * 
	 * @param author
	 * @param extensionName
	 */
	public void setExtensionName(String author, String extensionName) {
		setExtensionName(author + EXTENSION_NAME_DIVIDER + extensionName);
	}

	/**
	 * Get the author from the beginning of the extension name
	 * 
	 * @return
	 */
	public String getAuthor() {
		String author = null;
		if (extensionName != null) {
			author = extensionName.substring(0,
					extensionName.indexOf(EXTENSION_NAME_DIVIDER));
		}
		return author;
	}

	/**
	 * Get the extension name with the author prefix removed
	 * 
	 * @return
	 */
	public String getExtensionNameNoAuthor() {
		String value = null;
		if (extensionName != null) {
			value = extensionName.substring(
					extensionName.indexOf(EXTENSION_NAME_DIVIDER) + 1,
					extensionName.length());
		}
		return value;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public ExtensionScopeType getScope() {
		return ExtensionScopeType.fromValue(scope);
	}

	public void setScope(ExtensionScopeType scope) {
		this.scope = scope.getValue();
	}

}
