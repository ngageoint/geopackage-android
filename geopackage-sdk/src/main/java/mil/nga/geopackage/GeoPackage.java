package mil.nga.geopackage;

import android.content.Context;
import android.database.Cursor;

import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.factory.GeoPackageCursorFactory;
import mil.nga.geopackage.factory.GeoPackageCursorWrapper;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * A single GeoPackage database connection
 *
 * @author osbornb
 */
public interface GeoPackage extends GeoPackageCore {

    /**
     * Get the cursor factory
     *
     * @return cursor factory
     * @since 3.4.0
     */
    public GeoPackageCursorFactory getCursorFactory();

    /**
     * Register a GeoPackage Cursor Wrapper for table name
     *
     * @param table         table name
     * @param cursorWrapper cursor wrapper
     * @since 3.0.1
     */
    public void registerCursorWrapper(String table, GeoPackageCursorWrapper cursorWrapper);

    /**
     * Get a Feature DAO from Geometry Columns
     *
     * @param geometryColumns geometry columns
     * @return feature dao
     */
    public FeatureDao getFeatureDao(GeometryColumns geometryColumns);

    /**
     * Get a Feature DAO from Contents
     *
     * @param contents contents
     * @return feature dao
     */
    public FeatureDao getFeatureDao(Contents contents);

    /**
     * Get a Feature DAO from a table name
     *
     * @param tableName table name
     * @return feature dao
     */
    public FeatureDao getFeatureDao(String tableName);

    /**
     * Get a Tile DAO from Tile Matrix Set
     *
     * @param tileMatrixSet tile matrix set
     * @return tile dao
     */
    public TileDao getTileDao(TileMatrixSet tileMatrixSet);

    /**
     * Get a Tile DAO from Contents
     *
     * @param contents contents
     * @return tile dao
     */
    public TileDao getTileDao(Contents contents);

    /**
     * Get a Tile DAO from a table name
     *
     * @param tableName table name
     * @return tile dao
     */
    public TileDao getTileDao(String tableName);

    /**
     * Get an Attributes DAO from Contents
     *
     * @param contents contents
     * @return attributes dao
     * @since 1.3.1
     */
    public AttributesDao getAttributesDao(Contents contents);

    /**
     * Get an Attributes DAO from a table name
     *
     * @param tableName table name
     * @return attributes dao
     * @since 1.3.1
     */
    public AttributesDao getAttributesDao(String tableName);

    /**
     * Get a User Custom DAO from a table name
     *
     * @param tableName table name
     * @return user custom dao
     * @since 3.3.0
     */
    public UserCustomDao getUserCustomDao(String tableName);

    /**
     * Get a User Custom DAO from a table
     *
     * @param table table
     * @return user custom dao
     * @since 3.4.0
     */
    public UserCustomDao getUserCustomDao(UserCustomTable table);

    /**
     * Perform a raw query on the database
     *
     * @param sql  sql statement
     * @param args arguments
     * @return cursor
     * @since 1.2.1
     */
    public Cursor rawQuery(String sql, String[] args);

    /**
     * Get the GeoPackage connection
     *
     * @return GeoPackage connection
     * @since 2.0.1
     */
    public GeoPackageConnection getConnection();

    /**
     * Get the application context
     *
     * @return context
     * @since 3.2.0
     */
    public Context getContext();

    /**
     * Perform a foreign key check on the database
     *
     * @return null if check passed, open cursor with results if failed
     * @since 1.2.1
     */
    public Cursor foreignKeyCheck();

    /**
     * Perform a foreign key check on the database table
     *
     * @param tableName table name
     * @return null if check passed, open cursor with results if failed
     * @since 3.3.0
     */
    public Cursor foreignKeyCheck(String tableName);

    /**
     * Perform an integrity check on the database
     *
     * @return null if check passed, open cursor with results if failed
     * @since 1.2.1
     */
    public Cursor integrityCheck();

    /**
     * Perform a quick integrity check on the database
     *
     * @return null if check passed, open cursor with results if failed
     * @since 1.2.1
     */
    public Cursor quickCheck();

}
