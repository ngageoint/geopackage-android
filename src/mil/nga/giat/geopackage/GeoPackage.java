package mil.nga.giat.geopackage;

import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSfSqlDao;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemSqlMmDao;
import mil.nga.giat.geopackage.extension.ExtensionsDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSfSqlDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsSqlMmDao;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureTable;
import mil.nga.giat.geopackage.metadata.MetadataDao;
import mil.nga.giat.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.giat.geopackage.schema.columns.DataColumnsDao;
import mil.nga.giat.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

/**
 * A single GeoPackage database connection
 * 
 * @author osbornb
 */
public interface GeoPackage {

	/**
	 * Close the GeoPackage connection
	 */
	public void close();

	/**
	 * Get the SQLite database
	 * 
	 * @return
	 */
	public SQLiteDatabase getDatabase();

	/**
	 * Get the connection source
	 * 
	 * @return
	 */
	public ConnectionSource getConnectionSource();

	/**
	 * Get a Spatial Reference System DAO
	 * 
	 * @return
	 */
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao();

	/**
	 * Get a SQL/MM Spatial Reference System DAO
	 * 
	 * @return
	 */
	public SpatialReferenceSystemSqlMmDao getSpatialReferenceSystemSqlMmDao();

	/**
	 * Get a SF/SQL Spatial Reference System DAO
	 * 
	 * @return
	 */
	public SpatialReferenceSystemSfSqlDao getSpatialReferenceSystemSfSqlDao();

	/**
	 * Get a Contents DAO
	 * 
	 * @return
	 */
	public ContentsDao getContentsDao();

	/**
	 * Get a Geometry Columns DAO
	 * 
	 * @return
	 */
	public GeometryColumnsDao getGeometryColumnsDao();

	/**
	 * Get a SQL/MM Geometry Columns DAO
	 * 
	 * @return
	 */
	public GeometryColumnsSqlMmDao getGeometryColumnsSqlMmDao();

	/**
	 * Get a SF/SQL Geometry Columns DAO
	 * 
	 * @return
	 */
	public GeometryColumnsSfSqlDao getGeometryColumnsSfSqlDao();

	/**
	 * Create the Geometry Columns table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createGeometryColumnsTable();

	/**
	 * Get a Feature DAO from Geometry Columns
	 * 
	 * @param geometryColumns
	 * @return
	 */
	public FeatureDao getFeatureDao(GeometryColumns geometryColumns);

	/**
	 * Get a Feature DAO from Contents
	 * 
	 * @param contents
	 * @return
	 */
	public FeatureDao getFeatureDao(Contents contents);

	/**
	 * Get a Feature DAO from a table name
	 * 
	 * @param tableName
	 * @return
	 */
	public FeatureDao getFeatureDao(String tableName);

	/**
	 * Create a new feature table
	 * 
	 * @param table
	 */
	public void createTable(FeatureTable table);

	/**
	 * Get a Tile Matrix Set DAO
	 * 
	 * @return
	 */
	public TileMatrixSetDao getTileMatrixSetDao();

	/**
	 * Create the Tile Matrix Set table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createTileMatrixSetTable();

	/**
	 * Get a Tile Matrix DAO
	 * 
	 * @return
	 */
	public TileMatrixDao getTileMatrixDao();

	/**
	 * Create the Tile Matrix table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createTileMatrixTable();

	/**
	 * Get a Tile DAO from Tile Matrix Set
	 * 
	 * @param tileMatrixSet
	 * @return
	 */
	public TileDao getTileDao(TileMatrixSet tileMatrixSet);

	/**
	 * Get a Tile DAO from Contents
	 * 
	 * @param contents
	 * @return
	 */
	public TileDao getTileDao(Contents contents);

	/**
	 * Get a Tile DAO from a table name
	 * 
	 * @param tableName
	 * @return
	 */
	public TileDao getTileDao(String tableName);

	/**
	 * Create a new tile table
	 * 
	 * @param table
	 */
	public void createTable(TileTable table);

	/**
	 * Get a Data Columns DAO
	 * 
	 * @return
	 */
	public DataColumnsDao getDataColumnsDao();

	/**
	 * Create the Data Columns table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createDataColumnsTable();

	/**
	 * Get a Data Column Constraints DAO
	 * 
	 * @return
	 */
	public DataColumnConstraintsDao getDataColumnConstraintsDao();

	/**
	 * Create the Data Column Constraints table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createDataColumnConstraintsTable();

	/**
	 * Get a Metadata DAO
	 * 
	 * @return
	 */
	public MetadataDao getMetadataDao();

	/**
	 * Create the Metadata table if it does not already exist
	 * 
	 * @return
	 */
	public boolean createMetadataTable();

	/**
	 * Get a Metadata Reference DAO
	 * 
	 * @return
	 */
	public MetadataReferenceDao getMetadataReferenceDao();

	/**
	 * Create the Metadata Reference table if it does not already exist
	 * 
	 * @return
	 */
	public boolean createMetadataReferenceTable();

	/**
	 * Get an Extensions DAO
	 * 
	 * @return
	 */
	public ExtensionsDao getExtensionsDao();

	/**
	 * Create the Extensions table if it does not already exist
	 * 
	 * @return
	 */
	public boolean createExtensionsTable();

}
