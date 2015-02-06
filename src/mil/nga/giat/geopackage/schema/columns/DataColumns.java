package mil.nga.giat.geopackage.schema.columns;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.schema.TableColumnKey;
import mil.nga.giat.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.giat.geopackage.schema.constraints.DataColumnConstraintsDao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Stores minimal application schema identifying, descriptive and MIME type
 * information about columns in user vector feature and tile matrix data tables
 * that supplements the data available from the SQLite sqlite_master table and
 * pragma table_info(table_name) SQL function. The gpkg_data_columns data CAN be
 * used to provide more specific column data types and value ranges and
 * application specific structural and semantic information to enable more
 * informative user menu displays and more effective user decisions on the
 * suitability of GeoPackage contents for specific purposes.
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_data_columns", daoClass = DataColumnsDao.class)
public class DataColumns {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_data_columns";

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
	 * name field name
	 */
	public static final String COLUMN_NAME = "name";

	/**
	 * title field name
	 */
	public static final String COLUMN_TITLE = "title";

	/**
	 * description field name
	 */
	public static final String COLUMN_DESCRIPTION = "description";

	/**
	 * mimeType field name
	 */
	public static final String COLUMN_MIME_TYPE = "mime_type";

	/**
	 * constraintName field name
	 */
	public static final String COLUMN_CONSTRAINT_NAME = "constraint_name";

	/**
	 * Foreign key to Contents by table name
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, canBeNull = false, unique = true, foreign = true, foreignAutoRefresh = true)
	private Contents contents;

	/**
	 * Name of the tiles or feature table
	 */
	@DatabaseField(columnName = COLUMN_TABLE_NAME, id = true, canBeNull = false, uniqueCombo = true)
	private String tableName;

	/**
	 * Name of the table column
	 */
	@DatabaseField(columnName = COLUMN_COLUMN_NAME, canBeNull = false, uniqueCombo = true)
	private String columnName;

	/**
	 * A human-readable identifier (e.g. short name) for the column_name content
	 */
	@DatabaseField(columnName = COLUMN_NAME)
	private String name;

	/**
	 * A human-readable formal title for the column_name content
	 */
	@DatabaseField(columnName = COLUMN_TITLE)
	private String title;

	/**
	 * A human-readable description for the table_name content
	 */
	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	private String description;

	/**
	 * MIME type of column_name if BLOB type, or NULL for other types
	 */
	@DatabaseField(columnName = COLUMN_MIME_TYPE)
	private String mimeType;

	/**
	 * Case sensitive column value constraint name specified by reference to
	 * gpkg_data_column_constraints.constraint name
	 */
	@DatabaseField(columnName = COLUMN_CONSTRAINT_NAME)
	private String constraintName;

	/**
	 * Default Constructor
	 */
	public DataColumns() {

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
			// Verify the Contents have a data type
			if (contents.getDataType() == null) {
				throw new GeoPackageException("The "
						+ Contents.class.getSimpleName() + " of a "
						+ DataColumns.class.getSimpleName()
						+ " must have a data type");
			}
			tableName = contents.getId();
		} else {
			tableName = null;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraint(DataColumnConstraints constraint) {
		String name = null;
		if (constraint != null) {
			name = constraint.getConstraintName();
		}
		setConstraintName(name);
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public List<DataColumnConstraints> getConstraints(
			DataColumnConstraintsDao dao) throws SQLException {
		List<DataColumnConstraints> constraints = null;
		if (constraintName != null) {
			constraints = dao.queryByConstraintName(constraintName);
		}
		return constraints;
	}

}
