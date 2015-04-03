package mil.nga.giat.geopackage.user;

import mil.nga.giat.geopackage.db.CursorDatabaseUtils;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Abstract User Cursor
 * 
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * 
 * @author osbornb
 */
public abstract class UserCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>>
		extends CursorWrapper {

	/**
	 * Table
	 */
	private final TTable table;

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param cursor
	 */
	protected UserCursor(TTable table, Cursor cursor) {
		super(cursor);
		this.table = table;
	}

	/**
	 * Get a row using the column types and values
	 * 
	 * @param columnTypes
	 * @param values
	 * @return
	 */
	protected abstract TRow getRow(int[] columnTypes, Object[] values);

	/**
	 * Get the value for the column
	 * 
	 * @param column
	 * @return
	 */
	protected Object getValue(TColumn column) {
		Object value = CursorDatabaseUtils.getValue(this,
                column.getIndex(), column.getDataType());
		return value;
	}

	/**
	 * Get the table
	 * 
	 * @return
	 */
	protected TTable getTable() {
		return table;
	}

	/**
	 * Get the row at the current cursor position
	 * 
	 * @return
	 */
	public TRow getRow() {

		int[] columnTypes = new int[table.columnCount()];
		Object[] values = new Object[table.columnCount()];

		for (TColumn column : table.getColumns()) {

			int index = column.getIndex();

			columnTypes[index] = getType(index);

			values[index] = getValue(column);

		}

		TRow row = getRow(columnTypes, values);

		return row;
	}

}
