package mil.nga.giat.geopackage.data.c3;

public class GeometryColumnsKey {

	private String tableName;

	private String columnName;

	public GeometryColumnsKey(String tableName, String columnName) {
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

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeometryColumnsKey other = (GeometryColumnsKey) obj;
		if (!columnName.equals(other.columnName))
			return false;
		if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

}
