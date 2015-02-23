package mil.nga.giat.geopackage.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.user.UserColumn;
import mil.nga.giat.geopackage.user.UserTable;
import mil.nga.giat.geopackage.user.UserUniqueConstraint;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Executes database scripts to create GeoPackage tables
 * 
 * @author osbornb
 */
public class GeoPackageTableCreator {

	/**
	 * Log tag
	 */
	private static final String TAG = GeoPackageTableCreator.class
			.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Asset manager
	 */
	private final AssetManager assetManager;

	/**
	 * SQLite database
	 */
	private final SQLiteDatabase db;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param db
	 */
	public GeoPackageTableCreator(Context context, SQLiteDatabase db) {
		this.context = context;
		this.assetManager = context.getAssets();
		this.db = db;
	}

	/**
	 * Create Spatial Reference System table and views
	 * 
	 * @return
	 */
	public int createSpatialReferenceSystem() {
		return createTable(context
				.getString(R.string.sql_spatial_reference_system));
	}

	/**
	 * Create Contents table
	 * 
	 * @return
	 */
	public int createContents() {
		return createTable(context.getString(R.string.sql_contents));
	}

	/**
	 * Create Geometry Columns table
	 * 
	 * @return executed statements
	 */
	public int createGeometryColumns() {
		return createTable(context.getString(R.string.sql_geometry_columns));
	}

	/**
	 * Create Tile Matrix Set table
	 * 
	 * @return executed statements
	 */
	public int createTileMatrixSet() {
		return createTable(context.getString(R.string.sql_tile_matrix_set));
	}

	/**
	 * Create Tile Matrix table
	 * 
	 * @return executed statements
	 */
	public int createTileMatrix() {
		return createTable(context.getString(R.string.sql_tile_matrix));
	}

	/**
	 * Create Data Columns table
	 * 
	 * @return executed statements
	 */
	public int createDataColumns() {
		return createTable(context.getString(R.string.sql_data_columns));
	}

	/**
	 * Create Data Column Constraints table
	 * 
	 * @return executed statements
	 */
	public int createDataColumnConstraints() {
		return createTable(context
				.getString(R.string.sql_data_column_constraints));
	}

	/**
	 * Create Metadata table
	 * 
	 * @return executed statements
	 */
	public int createMetadata() {
		return createTable(context.getString(R.string.sql_metadata));
	}

	/**
	 * Create Metadata Reference table
	 * 
	 * @return executed statements
	 */
	public int createMetadataReference() {
		return createTable(context.getString(R.string.sql_metadata_reference));
	}

	/**
	 * Create Extensions table
	 * 
	 * @return executed statements
	 */
	public int createExtensions() {
		return createTable(context.getString(R.string.sql_extensions));
	}

	/**
	 * Create a table using the table script
	 * 
	 * @param tableScript
	 * @return
	 */
	private int createTable(String tableScript) {
		int statements = 0;
		try {

			InputStream scriptStream = assetManager.open(context
					.getString(R.string.sql_directory)
					+ File.separatorChar
					+ tableScript);
			statements = runScript(scriptStream);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create table from file: " + tableScript, e);
		}
		return statements;
	}

	/**
	 * Run the script input stream
	 * 
	 * @param stream
	 * @return executed statements
	 */
	private int runScript(final InputStream stream) {
		int count = 0;

		// Use multiple newlines as the delimiter
		Scanner s = new java.util.Scanner(stream).useDelimiter(Pattern
				.compile("\\n\\s*\\n"));

		// Execute each statement
		while (s.hasNext()) {
			String statement = s.next().trim();
			db.execSQL(statement);
			count++;
		}
		return count;
	}

	/**
	 * Create the user defined table
	 * 
	 * @param table
	 * @param <TColumn>
	 */
	public <TColumn extends UserColumn> void createTable(
			UserTable<TColumn> table) {

		// Verify the table does not already exist
		if (GeoPackageDatabaseUtils.tableExists(db, table.getTableName())) {
			throw new GeoPackageException(
					"Table already exists and can not be created: "
							+ table.getTableName());
		}

		// Build the create table sql
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(table.getTableName()).append(" (");

		// Add each column to the sql
		List<? extends UserColumn> columns = table.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			UserColumn column = columns.get(i);
			if (i > 0) {
				sql.append(",");
			}
			sql.append("\n  ").append(column.getName()).append(" ")
					.append(column.getTypeName());
			if (column.getMax() != null) {
				sql.append("(").append(column.getMax()).append(")");
			}
			if (column.isNotNull()) {
				sql.append(" NOT NULL");
			}
			if (column.isPrimaryKey()) {
				sql.append(" PRIMARY KEY AUTOINCREMENT");
			}
		}

		// Add unique constraints
		List<UserUniqueConstraint<TColumn>> uniqueConstraints = table
				.getUniqueConstraints();
		for (int i = 0; i < uniqueConstraints.size(); i++) {
			UserUniqueConstraint<TColumn> uniqueConstraint = uniqueConstraints
					.get(i);
			sql.append(",\n  UNIQUE (");
			List<TColumn> uniqueColumns = uniqueConstraint.getColumns();
			for (int j = 0; j < uniqueColumns.size(); j++) {
				TColumn uniqueColumn = uniqueColumns.get(j);
				if (j > 0) {
					sql.append(", ");
				}
				sql.append(uniqueColumn.getName());
			}
			sql.append(")");
		}

		sql.append("\n);");

		// Create the table
		db.execSQL(sql.toString());
	}

}
