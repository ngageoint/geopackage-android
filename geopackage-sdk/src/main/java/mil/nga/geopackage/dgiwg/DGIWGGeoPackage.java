package mil.nga.geopackage.dgiwg;

import android.graphics.Bitmap;

import java.util.Collection;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageImpl;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.extension.metadata.Metadata;
import mil.nga.geopackage.extension.metadata.MetadataScopeType;
import mil.nga.geopackage.extension.metadata.reference.MetadataReference;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.GeometryType;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage
 * implementation
 *
 * @author osbornb
 * @since 6.7.0
 */
public class DGIWGGeoPackage extends GeoPackageImpl {

    /**
     * DGIWG File
     */
    private final GeoPackageFile file;

    /**
     * Validate errors when validated
     */
    private DGIWGValidationErrors errors;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public DGIWGGeoPackage(GeoPackage geoPackage) {
        super(geoPackage.getContext(), geoPackage.getName(), geoPackage.getPath(),
                geoPackage.getConnection(), geoPackage.getCursorFactory(),
                geoPackage.isWritable());
        this.file = new GeoPackageFile(getPath(), getName());
    }

    /**
     * Get the DGIWG file
     *
     * @return DGIWG file
     */
    public GeoPackageFile getFile() {
        return file;
    }

    /**
     * Get the DGIWG file name
     *
     * @return DGIWG file name
     */
    public GeoPackageFileName getFileName() {
        return file.getFileName();
    }

    /**
     * Is the GeoPackage valid according to the DGIWG GeoPackage Profile
     *
     * @return true if valid
     */
    public boolean isValid() {
        return validate().isValid();
    }

    /**
     * Validate the GeoPackage against the DGIWG GeoPackage Profile
     *
     * @return validation errors
     */
    public DGIWGValidationErrors validate() {
        errors = DGIWGValidate.validate(this);
        validate(errors, getTables());
        return errors;
    }

    /**
     * Get the most recent {@link #validate()} results
     *
     * @return validation errors, null if not yet validated
     */
    public DGIWGValidationErrors getErrors() {
        return errors;
    }

    /**
     * Validate the GeoPackage table against the DGIWG GeoPackage Profile
     *
     * @param table table
     * @return validation errors
     */
    public DGIWGValidationErrors validate(String table) {
        DGIWGValidationErrors errors = DGIWGValidate.validate(this, table);
        validate(errors, table);
        return errors;
    }

    /**
     * Validate the GeoPackage tables against the DGIWG GeoPackage Profile
     *
     * @param tables tables
     * @return validation errors
     */
    public DGIWGValidationErrors validate(List<String> tables) {
        DGIWGValidationErrors errors = DGIWGValidate.validate(this, tables);
        validate(errors, tables);
        return errors;
    }

    /**
     * Validate the GeoPackage table names for non core tested requirements
     *
     * @param errors
     *            validation errors
     * @param tables
     *            table names
     */
    private void validate(DGIWGValidationErrors errors, List<String> tables) {
        for (String table : tables) {
            validate(errors, table);
        }
    }

    /**
     * Validate the GeoPackage table name for non core tested requirements
     *
     * @param errors
     *            validation errors
     * @param table
     *            table name
     */
    private void validate(DGIWGValidationErrors errors, String table) {
        ContentsDataType dataType = getTableCoreDataType(table);
        if (dataType != null) {
            switch (dataType) {
                case TILES:
                    validateTileTable(errors, table);
                    break;
                default:
            }
        }
    }

    /**
     * Validate the GeoPackage tile table name for non core tested requirements
     *
     * @param errors
     *            validation errors
     * @param table
     *            table name
     */
    private void validateTileTable(DGIWGValidationErrors errors, String table) {

        // Validate the size of just the first tile
        TileDao tileDao = getTileDao(table);
        TileCursor cursor = tileDao.query();
        try {
            if (cursor.moveToNext()) {
                TileRow tileRow = cursor.getRow();
                Bitmap tile = tileRow.getTileDataBitmap();

                if (tile == null
                        || tile.getWidth() != DGIWGConstants.TILE_WIDTH) {
                    errors.add(new DGIWGValidationError(table,
                            tileRow.getTileDataColumn().getName(),
                            tile != null ? tile.getWidth() : null,
                            "Tile width of " + DGIWGConstants.TILE_WIDTH,
                            DGIWGRequirement.TILE_SIZE_DATA,
                            new DGIWGValidationKey(
                                    tileRow.getPkColumn().getName(),
                                    tileRow.getId())));
                }

                if (tile == null
                        || tile.getHeight() != DGIWGConstants.TILE_HEIGHT) {
                    errors.add(new DGIWGValidationError(table,
                            tileRow.getTileDataColumn().getName(),
                            tile != null ? tile.getHeight() : null,
                            "Tile height of " + DGIWGConstants.TILE_HEIGHT,
                            DGIWGRequirement.TILE_SIZE_DATA,
                            new DGIWGValidationKey(
                                    tileRow.getPkColumn().getName(),
                                    tileRow.getId())));
                }
            }

        } catch (Exception e) {
            throw new GeoPackageException(
                    "Failed to query tile table: " + table, e);
        } finally {
            cursor.close();
        }

    }

    /**
     * Create tiles table
     *
     * @param table table name
     * @param crs   coordinate reference system
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table,
                                     CoordinateReferenceSystem crs) {
        return createTiles(table, table, table, crs);
    }

    /**
     * Create tiles table
     *
     * @param table       table name
     * @param identifier  contents identifier
     * @param description contents description
     * @param crs         coordinate reference system
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, CoordinateReferenceSystem crs) {
        return createTiles(table, identifier, description, null, crs);
    }

    /**
     * Create tiles table
     *
     * @param table             table name
     * @param identifier        contents identifier
     * @param description       contents description
     * @param informativeBounds informative contents bounds
     * @param crs               coordinate reference system
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, BoundingBox informativeBounds,
                                     CoordinateReferenceSystem crs) {
        SpatialReferenceSystem srs = crs.createTilesSpatialReferenceSystem();
        return createTiles(table, identifier, description, informativeBounds,
                srs, crs.getBounds());
    }

    /**
     * Create tiles table
     *
     * @param table        table name
     * @param crs          coordinate reference system
     * @param extentBounds crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table,
                                     CoordinateReferenceSystem crs, BoundingBox extentBounds) {
        return createTiles(table, table, table, crs, extentBounds);
    }

    /**
     * Create tiles table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param crs          coordinate reference system
     * @param extentBounds crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, CoordinateReferenceSystem crs,
                                     BoundingBox extentBounds) {
        return createTiles(table, identifier, description, null, crs,
                extentBounds);
    }

    /**
     * Create tiles table
     *
     * @param table             table name
     * @param identifier        contents identifier
     * @param description       contents description
     * @param informativeBounds informative contents bounds
     * @param crs               coordinate reference system
     * @param extentBounds      crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, BoundingBox informativeBounds,
                                     CoordinateReferenceSystem crs, BoundingBox extentBounds) {
        return createTiles(table, identifier, description, informativeBounds,
                crs.createTilesSpatialReferenceSystem(), extentBounds);
    }

    /**
     * Create tiles table
     *
     * @param table        table name
     * @param srs          spatial reference system
     * @param extentBounds crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, SpatialReferenceSystem srs,
                                     BoundingBox extentBounds) {
        return createTiles(table, table, table, srs, extentBounds);
    }

    /**
     * Create tiles table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param srs          spatial reference system
     * @param extentBounds crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, SpatialReferenceSystem srs,
                                     BoundingBox extentBounds) {
        return createTiles(table, identifier, description, null, srs,
                extentBounds);
    }

    /**
     * Create tiles table
     *
     * @param table             table name
     * @param identifier        contents identifier
     * @param description       contents description
     * @param informativeBounds informative contents bounds
     * @param srs               spatial reference system
     * @param extentBounds      crs extent bounds
     * @return created tile matrix set
     */
    public TileMatrixSet createTiles(String table, String identifier,
                                     String description, BoundingBox informativeBounds,
                                     SpatialReferenceSystem srs, BoundingBox extentBounds) {
        return DGIWGGeoPackageUtils.createTiles(this, table, identifier,
                description, informativeBounds, srs, extentBounds);
    }

    /**
     * Create tile matrices for zoom levels
     *
     * @param tileMatrixSet tile matrix set
     * @param minZoom       min zoom level
     * @param maxZoom       max zoom level
     * @param matrixWidth   matrix width
     * @param matrixHeight  matrix height
     */
    public void createTileMatrices(TileMatrixSet tileMatrixSet, long minZoom,
                                   long maxZoom, long matrixWidth, long matrixHeight) {
        createTileMatrices(tileMatrixSet.getTableName(),
                tileMatrixSet.getBoundingBox(), minZoom, maxZoom, matrixWidth,
                matrixHeight);
    }

    /**
     * Create tile matrices for zoom levels
     *
     * @param table        table name
     * @param boundingBox  bounding box
     * @param minZoom      min zoom level
     * @param maxZoom      max zoom level
     * @param matrixWidth  matrix width
     * @param matrixHeight matrix height
     */
    public void createTileMatrices(String table, BoundingBox boundingBox,
                                   long minZoom, long maxZoom, long matrixWidth, long matrixHeight) {
        DGIWGGeoPackageUtils.createTileMatrices(this, table, boundingBox,
                minZoom, maxZoom, matrixWidth, matrixHeight);
    }

    /**
     * Create tile matrices for zoom levels
     *
     * @param tileMatrixSet tile matrix set
     * @param zoomLevels    zoom levels
     * @param matrixWidth   matrix width
     * @param matrixHeight  matrix height
     */
    public void createTileMatrices(TileMatrixSet tileMatrixSet,
                                   Collection<Long> zoomLevels, long matrixWidth, long matrixHeight) {
        createTileMatrices(tileMatrixSet.getTableName(),
                tileMatrixSet.getBoundingBox(), zoomLevels, matrixWidth,
                matrixHeight);
    }

    /**
     * Create tile matrices for zoom levels
     *
     * @param table        table name
     * @param boundingBox  bounding box
     * @param zoomLevels   zoom levels
     * @param matrixWidth  matrix width
     * @param matrixHeight matrix height
     */
    public void createTileMatrices(String table, BoundingBox boundingBox,
                                   Collection<Long> zoomLevels, long matrixWidth, long matrixHeight) {
        DGIWGGeoPackageUtils.createTileMatrices(this, table, boundingBox,
                zoomLevels, matrixWidth, matrixHeight);
    }

    /**
     * Create a tile matrix for a zoom level
     *
     * @param tileMatrixSet tile matrix set
     * @param zoom          zoom level
     * @param matrixWidth   matrix width
     * @param matrixHeight  matrix height
     */
    public void createTileMatrix(TileMatrixSet tileMatrixSet, long zoom,
                                 long matrixWidth, long matrixHeight) {
        createTileMatrix(tileMatrixSet.getTableName(),
                tileMatrixSet.getBoundingBox(), zoom, matrixWidth,
                matrixHeight);
    }

    /**
     * Create a tile matrix for a zoom level
     *
     * @param table        table name
     * @param boundingBox  bounding box
     * @param zoom         zoom level
     * @param matrixWidth  matrix width
     * @param matrixHeight matrix height
     */
    public void createTileMatrix(String table, BoundingBox boundingBox,
                                 long zoom, long matrixWidth, long matrixHeight) {
        DGIWGGeoPackageUtils.createTileMatrix(this, table, boundingBox, zoom,
                matrixWidth, matrixHeight);
    }

    /**
     * Create a tile matrix for a zoom level
     *
     * @param table        table name
     * @param zoom         zoom level
     * @param matrixWidth  matrix width
     * @param matrixHeight matrix height
     * @param pixelXSize   pixel x size
     * @param pixelYSize   pixel y size
     */
    public void createTileMatrix(String table, long zoom, long matrixWidth,
                                 long matrixHeight, double pixelXSize, double pixelYSize) {
        DGIWGGeoPackageUtils.createTileMatrix(this, table, zoom, matrixWidth,
                matrixHeight, pixelXSize, pixelYSize);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param geometryType geometry type
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table,
                                          GeometryType geometryType, CoordinateReferenceSystem crs) {
        return createFeatures(table, geometryType, null, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param geometryType geometry type
     * @param columns      feature columns
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table,
                                          GeometryType geometryType, List<FeatureColumn> columns,
                                          CoordinateReferenceSystem crs) {
        return createFeatures(table, table, table, geometryType, columns, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param geometryType geometry type
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, GeometryType geometryType,
                                          CoordinateReferenceSystem crs) {
        return createFeatures(table, identifier, description, geometryType,
                null, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param geometryType geometry type
     * @param columns      feature columns
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, GeometryType geometryType,
                                          List<FeatureColumn> columns, CoordinateReferenceSystem crs) {
        SpatialReferenceSystem srs = crs.createFeaturesSpatialReferenceSystem();
        DataType dataType = crs.getFeaturesDataTypes().iterator().next();
        return createFeatures(table, identifier, description, crs.getBounds(),
                geometryType, dataType, columns, srs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, BoundingBox bounds,
                                          GeometryType geometryType, CoordinateReferenceSystem crs) {
        return createFeatures(table, bounds, geometryType, null, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param columns      feature columns
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, BoundingBox bounds,
                                          GeometryType geometryType, List<FeatureColumn> columns,
                                          CoordinateReferenceSystem crs) {
        return createFeatures(table, table, table, bounds, geometryType,
                columns, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, BoundingBox bounds, GeometryType geometryType,
                                          CoordinateReferenceSystem crs) {
        return createFeatures(table, identifier, description, bounds,
                geometryType, null, crs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param columns      feature columns
     * @param crs          coordinate reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, BoundingBox bounds, GeometryType geometryType,
                                          List<FeatureColumn> columns, CoordinateReferenceSystem crs) {
        SpatialReferenceSystem srs = crs.createFeaturesSpatialReferenceSystem();
        DataType dataType = crs.getFeaturesDataTypes().iterator().next();
        return createFeatures(table, identifier, description, bounds,
                geometryType, dataType, columns, srs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param dataType     data type
     * @param srs          spatial reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, BoundingBox bounds,
                                          GeometryType geometryType, DataType dataType,
                                          SpatialReferenceSystem srs) {
        return createFeatures(table, bounds, geometryType, dataType, null, srs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param dataType     data type
     * @param columns      feature columns
     * @param srs          spatial reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, BoundingBox bounds,
                                          GeometryType geometryType, DataType dataType,
                                          List<FeatureColumn> columns, SpatialReferenceSystem srs) {
        return createFeatures(table, table, table, bounds, geometryType,
                dataType, columns, srs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param dataType     data type
     * @param srs          spatial reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, BoundingBox bounds, GeometryType geometryType,
                                          DataType dataType, SpatialReferenceSystem srs) {
        return createFeatures(table, identifier, description, bounds,
                geometryType, dataType, null, srs);
    }

    /**
     * Create features table
     *
     * @param table        table name
     * @param identifier   contents identifier
     * @param description  contents description
     * @param bounds       contents bounds
     * @param geometryType geometry type
     * @param dataType     data type
     * @param columns      feature columns
     * @param srs          spatial reference system
     * @return created geometry columns
     */
    public GeometryColumns createFeatures(String table, String identifier,
                                          String description, BoundingBox bounds, GeometryType geometryType,
                                          DataType dataType, List<FeatureColumn> columns,
                                          SpatialReferenceSystem srs) {

        GeometryColumns geometryColumns = DGIWGGeoPackageUtils.createFeatures(
                this, table, identifier, description, bounds, geometryType,
                dataType, columns, srs);

        // TODO RTree write functionality currently not supported in Android
        // RTreeIndexExtension extension = new RTreeIndexExtension(this);
        // extension.create(getFeatureDao(geometryColumns).getTable());

        return geometryColumns;
    }

    /**
     * Create metadata and metadata reference
     *
     * @param metadata  metadata
     * @param reference metadata reference
     */
    public void createMetadata(Metadata metadata, MetadataReference reference) {
        DGIWGGeoPackageUtils.createMetadata(this, metadata, reference);
    }

    /**
     * Create metadata
     *
     * @param metadata metadata
     */
    public void createMetadata(Metadata metadata) {
        DGIWGGeoPackageUtils.createMetadata(this, metadata);
    }

    /**
     * Create metadata reference
     *
     * @param metadata  the reference metadata
     * @param reference metadata reference
     */
    public void createMetadataReference(Metadata metadata,
                                        MetadataReference reference) {
        DGIWGGeoPackageUtils.createMetadataReference(this, metadata, reference);

    }

    /**
     * Create metadata reference
     *
     * @param reference metadata reference
     */
    public void createMetadataReference(MetadataReference reference) {
        DGIWGGeoPackageUtils.createMetadataReference(this, reference);
    }

    /**
     * Create GeoPackage metadata with a series scope and metadata reference
     *
     * @param uri      URI
     * @param metadata metadata
     * @return metadata reference
     */
    public MetadataReference createGeoPackageSeriesMetadata(String uri,
                                                            String metadata) {
        return DGIWGGeoPackageUtils.createGeoPackageSeriesMetadata(this, uri,
                metadata);
    }

    /**
     * Create GeoPackage metadata with a dataset scope and metadata reference
     *
     * @param uri      URI
     * @param metadata metadata
     * @return metadata reference
     */
    public MetadataReference createGeoPackageDatasetMetadata(String uri,
                                                             String metadata) {
        return DGIWGGeoPackageUtils.createGeoPackageDatasetMetadata(this, uri,
                metadata);
    }

    /**
     * Create GeoPackage metadata and metadata reference
     *
     * @param scope    metadata scope type
     * @param uri      URI
     * @param metadata metadata
     * @return metadata reference
     */
    public MetadataReference createGeoPackageMetadata(MetadataScopeType scope,
                                                      String uri, String metadata) {
        return DGIWGGeoPackageUtils.createGeoPackageMetadata(this, scope, uri,
                metadata);
    }

    /**
     * Create metadata and metadata reference
     *
     * @param scope     metadata scope type
     * @param uri       URI
     * @param metadata  metadata
     * @param reference metadata reference
     * @return metadata reference
     */
    public MetadataReference createMetadata(MetadataScopeType scope, String uri,
                                            String metadata, MetadataReference reference) {
        return DGIWGGeoPackageUtils.createMetadata(this, scope, uri, metadata,
                reference);
    }

    /**
     * Query for GeoPackage DGIWG Metadata Foundation (DMF) metadata
     *
     * @return metadata references
     */
    public List<MetadataReference> queryGeoPackageDMFMetadata() {
        return DGIWGMetadata.queryGeoPackageDMFMetadata(this);
    }

    /**
     * Query for GeoPackage NSG Metadata Foundation (NMF) NSG Application Schema
     * (NAS) metadata
     *
     * @return metadata references
     */
    public List<MetadataReference> queryGeoPackageNASMetadata() {
        return DGIWGMetadata.queryGeoPackageNASMetadata(this);
    }

    /**
     * Query for GeoPackage metadata
     *
     * @param baseURI base URI
     * @return metadata references
     */
    public List<MetadataReference> queryGeoPackageMetadata(String baseURI) {
        return DGIWGMetadata.queryGeoPackageMetadata(this, baseURI);
    }

}
