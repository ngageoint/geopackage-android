package mil.nga.geopackage;

import android.database.Cursor;

import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * A single GeoPackage database connection
 *
 * @author osbornb
 */
public interface GeoPackage extends GeoPackageCore {

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
     * Perform a raw query on the database
     *
     * @param sql
     * @param args
     * @return cursor
     * @since 1.2.1
     */
    public Cursor rawQuery(String sql, String[] args);

    /**
     * Perform a foreign key check on the database
     *
     * @return null if check passed, open cursor with results if failed
     * @since 1.2.1
     */
    public Cursor foreignKeyCheck();

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
