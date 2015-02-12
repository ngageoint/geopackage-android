package mil.nga.giat.geopackage.factory;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSfSql;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSfSqlDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSqlMm;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSqlMmDao;
import mil.nga.giat.geopackage.db.GeoPackageTableCreator;
import mil.nga.giat.geopackage.extension.Extensions;
import mil.nga.giat.geopackage.extension.ExtensionsDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSfSql;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSfSqlDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSqlMm;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSqlMmDao;
import mil.nga.giat.geopackage.features.user.FeatureCursor;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureTable;
import mil.nga.giat.geopackage.features.user.FeatureTableReader;
import mil.nga.giat.geopackage.geom.unit.CoordinateConverter;
import mil.nga.giat.geopackage.geom.unit.DistanceConverter;
import mil.nga.giat.geopackage.metadata.Metadata;
import mil.nga.giat.geopackage.metadata.MetadataDao;
import mil.nga.giat.geopackage.metadata.reference.MetadataReference;
import mil.nga.giat.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.giat.geopackage.schema.columns.DataColumns;
import mil.nga.giat.geopackage.schema.columns.DataColumnsDao;
import mil.nga.giat.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.giat.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.user.TileCursor;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import mil.nga.giat.geopackage.tiles.user.TileTableReader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * A single GeoPackage database connection implementation
 * 
 * @author osbornb
 */
class GeoPackageImpl implements GeoPackage {

	/**
	 * SQLite database
	 */
	private final SQLiteDatabase database;

	/**
	 * Cursor factory
	 */
	private final GeoPackageCursorFactory cursorFactory;

	/**
	 * Connection source for creating data access objects
	 */
	private final ConnectionSource connectionSource;

	/**
	 * Table creator
	 */
	private final GeoPackageTableCreator tableCreator;

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param cursorFactory
	 * @param tableCreator
	 */
	GeoPackageImpl(SQLiteDatabase database,
			GeoPackageCursorFactory cursorFactory,
			GeoPackageTableCreator tableCreator) {
		this.database = database;
		this.cursorFactory = cursorFactory;
		this.connectionSource = new AndroidConnectionSource(database);
		this.tableCreator = tableCreator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		connectionSource.closeQuietly();
		database.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SQLiteDatabase getDatabase() {
		return database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getFeatureTables() {
		GeometryColumnsDao geometryColumnsDao = getGeometryColumnsDao();
		List<String> tableNames;
		try {
			tableNames = geometryColumnsDao.getFeatureTables();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve feature tables",
					e);
		}
		return tableNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTileTables() {
		TileMatrixSetDao tileMatrixSetDao = getTileMatrixSetDao();
		List<String> tableNames;
		try {
			tableNames = tileMatrixSetDao.getTileTables();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve tile tables", e);
		}
		return tableNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao() {
		return createDao(SpatialReferenceSystem.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpatialReferenceSystemSqlMmDao getSpatialReferenceSystemSqlMmDao() {

		SpatialReferenceSystemSqlMmDao dao = createDao(SpatialReferenceSystemSqlMm.class);
		verifyTableExists(dao);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpatialReferenceSystemSfSqlDao getSpatialReferenceSystemSfSqlDao() {

		SpatialReferenceSystemSfSqlDao dao = createDao(SpatialReferenceSystemSfSql.class);
		verifyTableExists(dao);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContentsDao getContentsDao() {
		ContentsDao dao = createDao(Contents.class);
		dao.setDatabase(database);
		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsDao getGeometryColumnsDao() {
		return createDao(GeometryColumns.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsSqlMmDao getGeometryColumnsSqlMmDao() {

		GeometryColumnsSqlMmDao dao = createDao(GeometryColumnsSqlMm.class);
		verifyTableExists(dao);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsSfSqlDao getGeometryColumnsSfSqlDao() {

		GeometryColumnsSfSqlDao dao = createDao(GeometryColumnsSfSql.class);
		verifyTableExists(dao);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createGeometryColumnsTable() {
		boolean created = false;
		GeometryColumnsDao dao = getGeometryColumnsDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createGeometryColumns() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ GeometryColumns.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(GeometryColumns geometryColumns) {

		if (geometryColumns == null) {
			throw new GeoPackageException("Non null "
					+ GeometryColumns.class.getSimpleName()
					+ " is required to create "
					+ FeatureDao.class.getSimpleName());
		}

		// Read the existing table and create the dao
		FeatureTableReader tableReader = new FeatureTableReader(geometryColumns);
		final FeatureTable featureTable = tableReader.readTable(database);
		FeatureDao dao = new FeatureDao(database, geometryColumns, featureTable);

		// Register the table to wrap cursors with the feature cursor
		cursorFactory.registerTable(geometryColumns.getTableName(),
				new GeoPackageCursorWrapper() {

					@Override
					public Cursor wrapCursor(Cursor cursor) {
						return new FeatureCursor(featureTable, cursor);
					}
				});

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(Contents contents) {

		if (contents == null) {
			throw new GeoPackageException("Non null "
					+ Contents.class.getSimpleName()
					+ " is required to create "
					+ FeatureDao.class.getSimpleName());
		}

		GeometryColumns geometryColumns = contents.getGeometryColumns();
		if (geometryColumns == null) {
			throw new GeoPackageException("No "
					+ GeometryColumns.class.getSimpleName() + " exists for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		return getFeatureDao(geometryColumns);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(String tableName) {
		GeometryColumnsDao dao = getGeometryColumnsDao();
		List<GeometryColumns> geometryColumnsList;
		try {
			geometryColumnsList = dao.queryForEq(
					GeometryColumns.COLUMN_TABLE_NAME, tableName);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve "
					+ FeatureDao.class.getSimpleName() + " for table name: "
					+ tableName + ". Exception retrieving "
					+ GeometryColumns.class.getSimpleName() + ".", e);
		}
		if (geometryColumnsList.isEmpty()) {
			throw new GeoPackageException(
					"No Feature Table exists for table name: " + tableName);
		} else if (geometryColumnsList.size() > 1) {
			// This shouldn't happen with the table name unique constraint on
			// geometry columns
			throw new GeoPackageException("Unexpected state. More than one "
					+ GeometryColumns.class.getSimpleName()
					+ " matched for table name: " + tableName + ", count: "
					+ geometryColumnsList.size());
		}
		return getFeatureDao(geometryColumnsList.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTable(FeatureTable table) {
		tableCreator.createTable(table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileMatrixSetDao getTileMatrixSetDao() {
		return createDao(TileMatrixSet.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createTileMatrixSetTable() {
		boolean created = false;
		TileMatrixSetDao dao = getTileMatrixSetDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createTileMatrixSet() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ TileMatrixSet.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileMatrixDao getTileMatrixDao() {
		return createDao(TileMatrix.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createTileMatrixTable() {
		boolean created = false;
		TileMatrixDao dao = getTileMatrixDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createTileMatrix() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ TileMatrix.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(TileMatrixSet tileMatrixSet) {
		return getTileDao(tileMatrixSet, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(TileMatrixSet tileMatrixSet,
			CoordinateConverter coordinateConverter,
			DistanceConverter distanceConverter) {

		if (tileMatrixSet == null) {
			throw new GeoPackageException("Non null "
					+ TileMatrixSet.class.getSimpleName()
					+ " is required to create " + TileDao.class.getSimpleName());
		}

		// Get the Tile Matrix collection, order by zoom level ascending & pixel
		// size descending per requirement 51
		List<TileMatrix> tileMatrices;
		try {
			TileMatrixDao tileMatrixDao = getTileMatrixDao();
			QueryBuilder<TileMatrix, TileMatrixKey> qb = tileMatrixDao
					.queryBuilder();
			qb.where().eq(TileMatrix.COLUMN_TABLE_NAME,
					tileMatrixSet.getTableName());
			qb.orderBy(TileMatrix.COLUMN_ZOOM_LEVEL, true);
			qb.orderBy(TileMatrix.COLUMN_PIXEL_X_SIZE, false);
			qb.orderBy(TileMatrix.COLUMN_PIXEL_Y_SIZE, false);
			PreparedQuery<TileMatrix> query = qb.prepare();
			tileMatrices = tileMatrixDao.query(query);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve "
					+ TileDao.class.getSimpleName() + " for table name: "
					+ tileMatrixSet.getTableName() + ". Exception retrieving "
					+ TileMatrix.class.getSimpleName() + " collection.", e);
		}

		// Read the existing table and create the dao
		TileTableReader tableReader = new TileTableReader(
				tileMatrixSet.getTableName());
		final TileTable tileTable = tableReader.readTable(database);
		TileDao dao = new TileDao(database, tileMatrixSet, tileMatrices,
				tileTable, coordinateConverter, distanceConverter);

		// Register the table to wrap cursors with the tile cursor
		cursorFactory.registerTable(tileMatrixSet.getTableName(),
				new GeoPackageCursorWrapper() {

					@Override
					public Cursor wrapCursor(Cursor cursor) {
						return new TileCursor(tileTable, cursor);
					}
				});

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(Contents contents) {
		return getTileDao(contents, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(Contents contents,
			CoordinateConverter coordinateConverter,
			DistanceConverter distanceConverter) {

		if (contents == null) {
			throw new GeoPackageException("Non null "
					+ Contents.class.getSimpleName()
					+ " is required to create " + TileDao.class.getSimpleName());
		}

		TileMatrixSet tileMatrixSet = contents.getTileMatrixSet();
		if (tileMatrixSet == null) {
			throw new GeoPackageException("No "
					+ TileMatrixSet.class.getSimpleName() + " exists for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		return getTileDao(tileMatrixSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(String tableName) {
		return getTileDao(tableName, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(String tableName,
			CoordinateConverter coordinateConverter,
			DistanceConverter distanceConverter) {

		TileMatrixSetDao dao = getTileMatrixSetDao();
		List<TileMatrixSet> tileMatrixSetList;
		try {
			tileMatrixSetList = dao.queryForEq(TileMatrixSet.COLUMN_TABLE_NAME,
					tableName);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve "
					+ TileDao.class.getSimpleName() + " for table name: "
					+ tableName + ". Exception retrieving "
					+ TileMatrixSet.class.getSimpleName() + ".", e);
		}
		if (tileMatrixSetList.isEmpty()) {
			throw new GeoPackageException(
					"No Tile Table exists for table name: " + tableName);
		} else if (tileMatrixSetList.size() > 1) {
			// This shouldn't happen with the table name primary key on tile
			// matrix set table
			throw new GeoPackageException("Unexpected state. More than one "
					+ TileMatrixSet.class.getSimpleName()
					+ " matched for table name: " + tableName + ", count: "
					+ tileMatrixSetList.size());
		}
		return getTileDao(tileMatrixSetList.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTable(TileTable table) {
		tableCreator.createTable(table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumnsDao getDataColumnsDao() {
		return createDao(DataColumns.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createDataColumnsTable() {
		boolean created = false;
		DataColumnsDao dao = getDataColumnsDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createDataColumns() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ DataColumns.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumnConstraintsDao getDataColumnConstraintsDao() {
		return createDao(DataColumnConstraints.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createDataColumnConstraintsTable() {
		boolean created = false;
		DataColumnConstraintsDao dao = getDataColumnConstraintsDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createDataColumnConstraints() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ DataColumnConstraints.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetadataDao getMetadataDao() {
		return createDao(Metadata.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createMetadataTable() {
		boolean created = false;
		MetadataDao dao = getMetadataDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createMetadata() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ Metadata.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetadataReferenceDao getMetadataReferenceDao() {
		return createDao(MetadataReference.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createMetadataReferenceTable() {
		boolean created = false;
		MetadataReferenceDao dao = getMetadataReferenceDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createMetadataReference() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ MetadataReference.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtensionsDao getExtensionsDao() {
		return createDao(Extensions.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createExtensionsTable() {
		boolean created = false;
		ExtensionsDao dao = getExtensionsDao();
		try {
			if (!dao.isTableExists()) {
				created = tableCreator.createExtensions() > 0;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if "
					+ Extensions.class.getSimpleName()
					+ " table exists and create it", e);
		}
		return created;
	}

	/**
	 * Create a dao
	 * 
	 * @param type
	 * @return
	 */
	private <T, S extends BaseDaoImpl<T, ?>> S createDao(Class<T> type) {
		S dao;
		try {
			dao = DaoManager.createDao(connectionSource, type);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create "
					+ type.getSimpleName() + " dao", e);
		}
		return dao;
	}

	/**
	 * Verify table or view exists
	 * 
	 * @param dao
	 */
	private void verifyTableExists(BaseDaoImpl<?, ?> dao) {
		try {
			if (!dao.isTableExists()) {
				throw new GeoPackageException(
						"Table or view does not exist for: "
								+ dao.getDataClass().getSimpleName());
			}
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to detect if table or view exists for dao: "
							+ dao.getDataClass().getSimpleName(), e);
		}
	}

}
