package mil.nga.giat.geopackage.user;

import java.util.ArrayList;
import java.util.List;

/**
 * User table unique constraint for one or more columns
 * 
 * @param <TColumn>
 * 
 * @author osbornb
 */
public class UserUniqueConstraint<TColumn extends UserColumn> {

	/**
	 * Columns included in the unique constraint
	 */
	private final List<TColumn> columns = new ArrayList<TColumn>();

	/**
	 * Constructor
	 */
	public UserUniqueConstraint() {

	}

	/**
	 * Constructor
	 * 
	 * @param columns
	 */
	public UserUniqueConstraint(TColumn... columns) {
		for (TColumn column : columns) {
			add(column);
		}
	}

	/**
	 * Add a column
	 * 
	 * @param column
	 */
	public void add(TColumn column) {
		columns.add(column);
	}

	/**
	 * Get the columns
	 * 
	 * @return
	 */
	public List<TColumn> getColumns() {
		return columns;
	}

}
