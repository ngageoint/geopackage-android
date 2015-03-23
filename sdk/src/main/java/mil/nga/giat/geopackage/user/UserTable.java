package mil.nga.giat.geopackage.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;

/**
 * Abstract user table
 * 
 * @param <TColumn>
 * 
 * @author osbornb
 */
public abstract class UserTable<TColumn extends UserColumn> {

	/**
	 * Table name
	 */
	private final String tableName;

	/**
	 * Array of column names
	 */
	private final String[] columnNames;

	/**
	 * List of columns
	 */
	private final List<TColumn> columns;

	/**
	 * Mapping between column names and their index
	 */
	private final Map<String, Integer> nameToIndex = new HashMap<String, Integer>();

	/**
	 * Primary key column index
	 */
	private final int pkIndex;

	/**
	 * Unique constraints
	 */
	private final List<UserUniqueConstraint<TColumn>> uniqueConstraints = new ArrayList<UserUniqueConstraint<TColumn>>();

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param columns
	 */
	protected UserTable(String tableName, List<TColumn> columns) {
		this.tableName = tableName;
		this.columns = columns;

		Integer pk = null;

		Set<Integer> indices = new HashSet<Integer>();

		// Build the column name array for queries, find the primary key and
		// geometry
		this.columnNames = new String[columns.size()];
		for (TColumn column : columns) {

			int index = column.getIndex();

			if (column.isPrimaryKey()) {
				if (pk != null) {
					throw new GeoPackageException(
							"More than one primary key column was found for table '"
									+ tableName + "'. Index " + pk + " and "
									+ index);
				}
				pk = index;
			}

			// Check for duplicate indices
			if (indices.contains(index)) {
				throw new GeoPackageException("Duplicate index: " + index
						+ ", Table Name: " + tableName);
			}
			indices.add(index);

			columnNames[index] = column.getName();
			nameToIndex.put(column.getName(), index);
		}

		if (pk == null) {
			throw new GeoPackageException(
					"No primary key column was found for table '" + tableName
							+ "'");
		}
		pkIndex = pk;

		// Verify the columns have ordered indices without gaps
		for (int i = 0; i < columns.size(); i++) {
			if (!indices.contains(i)) {
				throw new GeoPackageException("No column found at index: " + i
						+ ", Table Name: " + tableName);
			}
		}

		// Sort the columns by index
		Collections.sort(columns);
	}

	/**
	 * Check for duplicate column names
	 * 
	 * @param tableName
	 * @param index
	 * @param previousIndex
	 * @param column
	 */
	protected void duplicateCheck(int index, Integer previousIndex,
			String column) {
		if (previousIndex != null) {
			throw new GeoPackageException("More than one " + column
					+ " column was found for table '" + tableName + "'. Index "
					+ previousIndex + " and " + index);

		}
	}

	/**
	 * Check for the expected data type
	 * 
	 * @param expected
	 * @param column
	 */
	protected void typeCheck(GeoPackageDataType expected, TColumn column) {

		GeoPackageDataType actual = column.getDataType();
		if (actual == null || !actual.equals(expected)) {
			throw new GeoPackageException("Unexpected " + column.getName()
					+ " column data type was found for table '" + tableName
					+ "', expected: " + expected.name() + ", actual: "
					+ (actual != null ? actual.name() : "null"));
		}
	}

	/**
	 * Check for missing columns
	 * 
	 * @param tableName
	 * @param index
	 * @param column
	 */
	protected void missingCheck(Integer index, String column) {
		if (index == null) {
			throw new GeoPackageException("No " + column
					+ " column was found for table '" + tableName + "'");
		}
	}

	/**
	 * Get the column index of the column name
	 * 
	 * @param columnName
	 * @return
	 */
	public int getColumnIndex(String columnName) {
		Integer index = nameToIndex.get(columnName);
		if (index == null) {
			throw new GeoPackageException("Column does not exist in table '"
					+ tableName + "', column: " + columnName);
		}
		return index;
	}

	/**
	 * Get the array of column names
	 * 
	 * @return
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

	/**
	 * Get the column name at the index
	 * 
	 * @param index
	 * @return
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}

	/**
	 * Get the list of columns
	 * 
	 * @return
	 */
	public List<TColumn> getColumns() {
		return columns;
	}

	/**
	 * Get the column at the index
	 * 
	 * @param index
	 * @return
	 */
	public TColumn getColumn(int index) {
		return columns.get(index);
	}

	/**
	 * Get the column of the column name
	 * 
	 * @param columnName
	 * @return
	 */
	public TColumn getColumn(String columnName) {
		return getColumn(getColumnIndex(columnName));
	}

	/**
	 * Get the column count
	 * 
	 * @return
	 */
	public int columnCount() {
		return columns.size();
	}

	/**
	 * Get the table name
	 * 
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Get the primary key column index
	 * 
	 * @return
	 */
	public int getPkColumnIndex() {
		return pkIndex;
	}

	/**
	 * Get the primary key column
	 * 
	 * @return
	 */
	public TColumn getPkColumn() {
		return columns.get(pkIndex);
	}

	/**
	 * Add unique constraint
	 * 
	 * @param uniqueConstraint
	 */
	public void addUniqueConstraint(
			UserUniqueConstraint<TColumn> uniqueConstraint) {
		uniqueConstraints.add(uniqueConstraint);
	}

	/**
	 * Get the unique constraints
	 * 
	 * @return
	 */
	public List<UserUniqueConstraint<TColumn>> getUniqueConstraints() {
		return uniqueConstraints;
	}

}
