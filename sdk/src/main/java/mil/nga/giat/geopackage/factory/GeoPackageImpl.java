package mil.nga.giat.geopackage.factory;

import android.database.Cursor;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.db.GeoPackageConnection;
import mil.nga.giat.geopackage.db.GeoPackageTableCreator;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.features.user.FeatureConnection;
import mil.nga.giat.geopackage.features.user.FeatureCursor;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureTable;
import mil.nga.giat.geopackage.features.user.FeatureTableReader;
import mil.nga.giat.geopackage.features.user.FeatureWrapperConnection;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.user.TileConnection;
import mil.nga.giat.geopackage.tiles.user.TileCursor;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import mil.nga.giat.geopackage.tiles.user.TileTableReader;
import mil.nga.giat.geopackage.tiles.user.TileWrapperConnection;

/**
 * A single GeoPackage database connection implementation
 *
 * @author osbornb
 */
class GeoPackageImpl extends GeoPackageCoreImpl implements GeoPackage {

    /**
     * Database connection
     */
    private final GeoPackageConnection database;

    /**
     * Cursor factory
     */
    private final GeoPackageCursorFactory cursorFactory;

    /**
     * Constructor
     *
     * @param name
     * @param database
     * @param cursorFactory
     * @param tableCreator
     */
    GeoPackageImpl(String name, GeoPackageConnection database,
                   GeoPackageCursorFactory cursorFactory,
                   GeoPackageTableCreator tableCreator) {
        super(name, database, tableCreator);
        this.database = database;
        this.cursorFactory = cursorFactory;
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
        final FeatureTable featureTable = tableReader.readTable(new FeatureWrapperConnection(database));
        FeatureConnection userDb = new FeatureConnection(database);
        FeatureDao dao = new FeatureDao(database, userDb, geometryColumns, featureTable);

        // Register the table to wrap cursors with the feature cursor
        cursorFactory.registerTable(geometryColumns.getTableName(),
                new GeoPackageCursorWrapper() {

                    @Override
                    public Cursor wrapCursor(Cursor cursor) {
                        return new FeatureCursor(featureTable, cursor);
                    }
                });

        // TODO
        // GeoPackages created with SQLite version 4.2.0+ with GeoPackage
        // support are not supported in Android (Lollipop uses SQLite version
        // 3.8.4.3). To edit features, drop the following triggers. May be able
        // to define the missing functions needed using SQLite C library with
        // NDK.
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_insert");
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_update1");
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_update2");
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_update3");
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_update4");
        database.execSQL("DROP TRIGGER IF EXISTS rtree_"
                + geometryColumns.getTableName() + "_"
                + geometryColumns.getColumnName() + "_delete");

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
    public TileDao getTileDao(TileMatrixSet tileMatrixSet) {

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
        final TileTable tileTable = tableReader.readTable(new TileWrapperConnection(database));
        TileConnection userDb = new TileConnection(database);
        TileDao dao = new TileDao(database, userDb, tileMatrixSet, tileMatrices,
                tileTable);

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

}
