package mil.nga.giat.geopackage.schema.constraints;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.schema.columns.DataColumns;
import mil.nga.giat.geopackage.schema.columns.DataColumnsDao;

import com.j256.ormlite.field.DatabaseField;
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
	 * Specified case sensitive value for enum or glob or NULL for range
	 * constraint_type
	 */
	@DatabaseField(columnName = COLUMN_VALUE, uniqueCombo = true)
	private String value;

	/**
	 * Minimum value for ‘range’ or NULL for ‘enum’ or ‘glob’ constraint_type
	 */
	@DatabaseField(columnName = COLUMN_MIN)
	private BigDecimal min;

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
	private BigDecimal max;

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

	public DataColumnConstraintType getConstraintType() {
		return DataColumnConstraintType.fromValue(constraintType);
	}

	public void setConstraintType(String constraintType) {
		DataColumnConstraintType type = DataColumnConstraintType
				.fromValue(constraintType);
		setConstraintType(type);
	}

	public void setConstraintType(DataColumnConstraintType constraintType) {
		this.constraintType = constraintType.getValue();
		switch (constraintType) {
		case RANGE:
			setValue(null);
			break;
		case ENUM:
		case GLOB:
			setMin(null);
			setMax(null);
			setMinIsInclusive(null);
			setMaxIsInclusive(null);
			break;
		default:

		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (constraintType != null && value != null
				&& getConstraintType().equals(DataColumnConstraintType.RANGE)) {
			throw new GeoPackageException("The value must be null for "
					+ DataColumnConstraintType.RANGE + " constraints");
		}
		this.value = value;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		validateRangeValue(COLUMN_MIN, min);
		this.min = min;
	}

	public Boolean getMinIsInclusive() {
		return minIsInclusive;
	}

	public void setMinIsInclusive(Boolean minIsInclusive) {
		validateRangeValue(COLUMN_MIN_IS_INCLUSIVE, minIsInclusive);
		this.minIsInclusive = minIsInclusive;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		validateRangeValue(COLUMN_MAX, max);
		this.max = max;
	}

	public Boolean getMaxIsInclusive() {
		return maxIsInclusive;
	}

	public void setMaxIsInclusive(Boolean maxIsInclusive) {
		validateRangeValue(COLUMN_MAX_IS_INCLUSIVE, maxIsInclusive);
		this.maxIsInclusive = maxIsInclusive;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DataColumns> getColumns(DataColumnsDao dao) throws SQLException {
		List<DataColumns> columns = null;
		if (constraintName != null) {
			columns = dao.queryByConstraintName(constraintName);
		}
		return columns;
	}

	/**
	 * Validate the constraint type when a range value is set
	 * 
	 * @param column
	 * @param value
	 */
	private void validateRangeValue(String column, Object value) {
		if (constraintType != null && value != null
				&& !getConstraintType().equals(DataColumnConstraintType.RANGE)) {
			throw new GeoPackageException("The " + column
					+ " must be null for " + DataColumnConstraintType.ENUM
					+ " and " + DataColumnConstraintType.GLOB + " constraints");
		}
	}

}
