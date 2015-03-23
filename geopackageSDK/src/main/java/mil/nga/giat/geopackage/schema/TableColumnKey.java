package mil.nga.giat.geopackage.schema;

/**
 * Table and column name complex primary key
 * 
 * @author osbornb
 */
public class TableColumnKey {

	/**
	 * Table name
	 */
	private String tableName;

	/**
	 * Column name
	 */
	private String columnName;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param columnName
	 */
	public TableColumnKey(String tableName, String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return tableName + ":" + columnName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableColumnKey other = (TableColumnKey) obj;
		if (!columnName.equals(other.columnName))
			return false;
		if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

}
