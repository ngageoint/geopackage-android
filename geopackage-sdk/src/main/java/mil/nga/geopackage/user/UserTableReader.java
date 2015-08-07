package mil.nga.geopackage.user;

/**
 * Reads the metadata from an existing user table
 *
 * @author osbornb
 */
public abstract class UserTableReader<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserCursor<TColumn, TTable, TRow>>
        extends UserCoreTableReader<TColumn, TTable, TRow, TResult> {

	/**
	 * Constructor
	 *
	 * @param tableName
	 */
	protected UserTableReader(String tableName) {
		super(tableName);
	}

}
