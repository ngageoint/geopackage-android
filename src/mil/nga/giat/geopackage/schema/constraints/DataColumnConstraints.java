package mil.nga.giat.geopackage.schema.constraints;

import mil.nga.giat.geopackage.schema.columns.DataColumns;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Contains data to specify restrictions on basic data type column values
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_data_column_constraints", daoClass = DataColumnConstraintsDao.class)
public class DataColumnConstraints {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "gpkg_data_column_constraints";

	/**
	 * constraintName field name
	 */
	public static final String COLUMN_CONSTRAINT_NAME = "constraint_name";

	/**
	 * constraintType field name
	 */
	public static final String COLUMN_CONSTRAINT_TYPE = "constraint_type";

	/**
	 * value field name
	 */
	public static final String COLUMN_VALUE = "value";

	/**
	 * min field name
	 */
	public static final String COLUMN_MIN = "min";

	/**
	 * minIsInclusive field name
	 */
	public static final String COLUMN_MIN_IS_INCLUSIVE = "minIsInclusive";

	/**
	 * max field name
	 */
	public static final String COLUMN_MAX = "max";

	/**
	 * maxIsInclusive field name
	 */
	public static final String COLUMN_MAX_IS_INCLUSIVE = "maxIsInclusive";

	/**
	 * description field name
	 */
	public static final String COLUMN_DESCRIPTION = "description";

	/**
	 * Case sensitive name of constraint
	 */
	@DatabaseField(columnName = COLUMN_CONSTRAINT_NAME, canBeNull = false, uniqueCombo = true)
	private String constraintName;

	/**
	 * Lowercase type name of constraint: range | enum | glob
	 */
	@DatabaseField(columnName = COLUMN_CONSTRAINT_TYPE, canBeNull = false, uniqueCombo = true)
	private String constraintType;

	/**
	 * Data Columns
	 */
	@ForeignCollectionField(eager = false)
	private ForeignCollection<DataColumns> dataColumns;

	/**
	 * Specified case sensitive value for enum or glob or NULL for range
	 * constraint_type
	 */
	@DatabaseField(columnName = COLUMN_VALUE, uniqueCombo = true)
	private String value;

	/**
	 * Minimum value for ‘range’ or NULL for ‘enum’ or ‘glob’ constraint_type
	 */
	@DatabaseField(columnName = COLUMN_MIN)
	private Number min;

	/**
	 * 0 (false) if min value is exclusive, or 1 (true) if min value is
	 * inclusive
	 */
	@DatabaseField(columnName = COLUMN_MIN_IS_INCLUSIVE)
	private Boolean minIsInclusive;

	/**
	 * Maximum value for ‘range’ or NULL for ‘enum’ or ‘glob’ constraint_type
	 */
	@DatabaseField(columnName = COLUMN_MAX)
	private Number max;

	/**
	 * 0 (false) if max value is exclusive, or 1 (true) if max value is
	 * inclusive
	 */
	@DatabaseField(columnName = COLUMN_MAX_IS_INCLUSIVE)
	private Boolean maxIsInclusive;

	/**
	 * For ranges and globs, describes the constraint; for enums, describes the
	 * enum value.
	 */
	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	private String description;

	/**
	 * Default Constructor
	 */
	public DataColumnConstraints() {

	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getConstraintType() {
		return constraintType;
	}

	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	public ForeignCollection<DataColumns> getDataColumns() {
		return dataColumns;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Number getMin() {
		return min;
	}

	public void setMin(Number min) {
		this.min = min;
	}

	public Boolean getMinIsInclusive() {
		return minIsInclusive;
	}

	public void setMinIsInclusive(Boolean minIsInclusive) {
		this.minIsInclusive = minIsInclusive;
	}

	public Number getMax() {
		return max;
	}

	public void setMax(Number max) {
		this.max = max;
	}

	public Boolean getMaxIsInclusive() {
		return maxIsInclusive;
	}

	public void setMaxIsInclusive(Boolean maxIsInclusive) {
		this.maxIsInclusive = maxIsInclusive;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
