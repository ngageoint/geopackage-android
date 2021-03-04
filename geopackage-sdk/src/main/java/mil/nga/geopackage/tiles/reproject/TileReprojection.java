package mil.nga.geopackage.tiles.reproject;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.proj.Projection;

/**
 * Tile Reprojection for reprojecting an existing tile table
 *
 * @author osbornb
 * @since 5.0.0
 */
public class TileReprojection extends TileReprojectionCore {

    /**
     * Create a Reprojection from a GeoPackage tile table, replacing the
     * existing tiles
     *
     * @param geoPackage GeoPackage
     * @param table      tile table
     * @param projection desired projection
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          Projection projection) {
        return create(geoPackage, table, table, projection);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     * within the GeoPackage
     *
     * @param geoPackage     GeoPackage
     * @param table          tile table
     * @param reprojectTable new reprojected tile table
     * @param projection     desired projection
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          String reprojectTable, Projection projection) {
        return create(geoPackage, table, geoPackage, reprojectTable,
                projection);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table in
     * a specified GeoPackage
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param projection          desired projection
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          GeoPackage reprojectGeoPackage, String reprojectTable,
                                          Projection projection) {
        return create(geoPackage.getTileDao(table), reprojectGeoPackage,
                reprojectTable, projection);
    }

    /**
     * Create a Reprojection from a tile table to a new tile table in a
     * specified GeoPackage
     *
     * @param tileDao        tile DAO
     * @param geoPackage     GeoPackage for reprojected tile table
     * @param reprojectTable new reprojected tile table
     * @param projection     desired projection
     * @return tile reprojection
     */
    public static TileReprojection create(TileDao tileDao,
                                          GeoPackage geoPackage, String reprojectTable,
                                          Projection projection) {
        return new TileReprojection(tileDao, geoPackage, reprojectTable,
                projection);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     *
     * @param geoPackage       GeoPackage
     * @param table            tile table
     * @param reprojectTileDao reprojection tile DAO
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          TileDao reprojectTileDao) {
        return create(geoPackage.getTileDao(table), reprojectTileDao);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     *
     * @param tileDao          tile DAO
     * @param reprojectTileDao reprojection tile DAO
     * @return tile reprojection
     */
    public static TileReprojection create(TileDao tileDao,
                                          TileDao reprojectTileDao) {
        return new TileReprojection(tileDao, reprojectTileDao);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTileDao    reprojection tile DAO
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          GeoPackage reprojectGeoPackage, TileDao reprojectTileDao) {
        return create(geoPackage.getTileDao(table), reprojectGeoPackage,
                reprojectTileDao);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     *
     * @param tileDao             tile DAO
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTileDao    reprojection tile DAO
     * @return tile reprojection
     */
    public static TileReprojection create(TileDao tileDao,
                                          GeoPackage reprojectGeoPackage, TileDao reprojectTileDao) {
        return new TileReprojection(tileDao, reprojectGeoPackage,
                reprojectTileDao);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table, replacing the
     * existing tiles
     *
     * @param geoPackage GeoPackage
     * @param table      tile table
     * @param optimize   desired optimization
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          TileReprojectionOptimize optimize) {
        return create(geoPackage, table, table, optimize);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table
     * within the GeoPackage
     *
     * @param geoPackage     GeoPackage
     * @param table          tile table
     * @param reprojectTable new reprojected tile table
     * @param optimize       desired optimization
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          String reprojectTable, TileReprojectionOptimize optimize) {
        return create(geoPackage, table, geoPackage, reprojectTable, optimize);
    }

    /**
     * Create a Reprojection from a GeoPackage tile table to a new tile table in
     * a specified GeoPackage
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param optimize            desired optimization
     * @return tile reprojection
     */
    public static TileReprojection create(GeoPackage geoPackage, String table,
                                          GeoPackage reprojectGeoPackage, String reprojectTable,
                                          TileReprojectionOptimize optimize) {
        return create(geoPackage.getTileDao(table), reprojectGeoPackage,
                reprojectTable, optimize);
    }

    /**
     * Create a Reprojection from a tile table to a new tile table in a
     * specified GeoPackage
     *
     * @param tileDao             tile DAO
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param optimize            desired optimization
     * @return tile reprojection
     */
    public static TileReprojection create(TileDao tileDao,
                                          GeoPackage reprojectGeoPackage, String reprojectTable,
                                          TileReprojectionOptimize optimize) {
        TileReprojection tileReprojection = new TileReprojection(tileDao,
                reprojectGeoPackage, reprojectTable, optimize.getProjection());
        tileReprojection.setOptimize(optimize);
        return tileReprojection;
    }

    /**
     * Reproject a GeoPackage tile table, replacing the existing tiles
     *
     * @param geoPackage GeoPackage
     * @param table      tile table
     * @param projection desired projection
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                Projection projection) {
        return create(geoPackage, table, projection).reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table within the
     * GeoPackage
     *
     * @param geoPackage     GeoPackage
     * @param table          tile table
     * @param reprojectTable new reprojected tile table
     * @param projection     desired projection
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                String reprojectTable, Projection projection) {
        return create(geoPackage, table, reprojectTable, projection)
                .reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table in a specified
     * GeoPackage
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param projection          desired projection
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                GeoPackage reprojectGeoPackage, String reprojectTable,
                                Projection projection) {
        return create(geoPackage, table, reprojectGeoPackage, reprojectTable,
                projection).reproject();
    }

    /**
     * Reproject a tile table to a new tile table in a specified GeoPackage
     *
     * @param tileDao             tile DAO
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param projection          desired projection
     * @return created tiles
     */
    public static int reproject(TileDao tileDao, GeoPackage reprojectGeoPackage,
                                String reprojectTable, Projection projection) {
        return create(tileDao, reprojectGeoPackage, reprojectTable, projection)
                .reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table
     *
     * @param geoPackage       GeoPackage
     * @param table            tile table
     * @param reprojectTileDao reprojection tile DAO
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                TileDao reprojectTileDao) {
        return create(geoPackage, table, reprojectTileDao).reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table
     *
     * @param tileDao          tile DAO
     * @param reprojectTileDao reprojection tile DAO
     * @return created tiles
     */
    public static int reproject(TileDao tileDao, TileDao reprojectTileDao) {
        return create(tileDao, reprojectTileDao).reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTileDao    reprojection tile DAO
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                GeoPackage reprojectGeoPackage, TileDao reprojectTileDao) {
        return create(geoPackage, table, reprojectGeoPackage, reprojectTileDao)
                .reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table
     *
     * @param tileDao             tile DAO
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTileDao    reprojection tile DAO
     * @return created tiles
     */
    public static int reproject(TileDao tileDao, GeoPackage reprojectGeoPackage,
                                TileDao reprojectTileDao) {
        return create(tileDao, reprojectGeoPackage, reprojectTileDao)
                .reproject();
    }

    /**
     * Reproject a GeoPackage tile table, replacing the existing tiles
     *
     * @param geoPackage GeoPackage
     * @param table      tile table
     * @param optimize   desired optimization
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                TileReprojectionOptimize optimize) {
        return create(geoPackage, table, optimize).reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table within the
     * GeoPackage
     *
     * @param geoPackage     GeoPackage
     * @param table          tile table
     * @param reprojectTable new reprojected tile table
     * @param optimize       desired optimization
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                String reprojectTable, TileReprojectionOptimize optimize) {
        return create(geoPackage, table, reprojectTable, optimize).reproject();
    }

    /**
     * Reproject a GeoPackage tile table to a new tile table in a specified
     * GeoPackage
     *
     * @param geoPackage          GeoPackage
     * @param table               tile table
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param optimize            desired optimization
     * @return created tiles
     */
    public static int reproject(GeoPackage geoPackage, String table,
                                GeoPackage reprojectGeoPackage, String reprojectTable,
                                TileReprojectionOptimize optimize) {
        return create(geoPackage, table, reprojectGeoPackage, reprojectTable,
                optimize).reproject();
    }

    /**
     * Reproject a tile table to a new tile table in a specified GeoPackage
     *
     * @param tileDao             tile DAO
     * @param reprojectGeoPackage GeoPackage for reprojected tile table
     * @param reprojectTable      new reprojected tile table
     * @param optimize            desired optimization
     * @return created tiles
     */
    public static int reproject(TileDao tileDao, GeoPackage reprojectGeoPackage,
                                String reprojectTable, TileReprojectionOptimize optimize) {
        return create(tileDao, reprojectGeoPackage, reprojectTable, optimize)
                .reproject();
    }

    /**
     * Constructor, reproject a tile table to a new tile table in a specified
     * GeoPackage
     *
     * @param tileDao    tile DAO
     * @param geoPackage GeoPackage for reprojected tile table
     * @param table      new reprojected tile table
     * @param projection desired projection
     */
    public TileReprojection(TileDao tileDao, GeoPackage geoPackage,
                            String table, Projection projection) {
        super(tileDao, geoPackage, table, projection);
    }

    /**
     * Constructor, reproject a GeoPackage tile table to a new tile table
     *
     * @param tileDao          tile DAO
     * @param reprojectTileDao reprojection tile DAO
     */
    public TileReprojection(TileDao tileDao, TileDao reprojectTileDao) {
        super(tileDao, reprojectTileDao);
    }

    /**
     * Constructor, reproject a GeoPackage tile table to a new tile table
     *
     * @param tileDao          tile DAO
     * @param geoPackage       GeoPackage for reprojected tile table
     * @param reprojectTileDao reprojection tile DAO
     */
    public TileReprojection(TileDao tileDao, GeoPackage geoPackage,
                            TileDao reprojectTileDao) {
        super(tileDao, geoPackage, reprojectTileDao);
    }

    /**
     * Get the tile DAO
     *
     * @return tile DAO
     */
    public TileDao getTileDao() {
        return (TileDao) super.tileDao;
    }

    /**
     * Get the GeoPackage
     *
     * @return GeoPackage
     */
    public GeoPackage getGeoPackage() {
        return (GeoPackage) super.geoPackage;
    }

    /**
     * Get the reprojection tile DAO
     *
     * @return reprojection tile DAO
     */
    public TileDao getReprojectTileDao() {
        return (TileDao) super.reprojectTileDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long getOptimizeZoom() {
        TileDao tileDao = getTileDao();
        return tileDao.getMapZoom(tileDao.getTileMatrixAtMinZoom());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TileDao createReprojectTileDao(String table) {
        return getGeoPackage().getTileDao(table);
    }

    /**
     * Get the corresponding tile dao
     *
     * @param reproject true for reprojection
     * @return tile dao
     */
    public TileDao getTileDao(boolean reproject) {
        TileDao tileDao = null;
        if (reproject) {
            tileDao = getReprojectTileDao();
        } else {
            tileDao = getTileDao();
        }
        return tileDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TileMatrixSet getTileMatrixSet(boolean reproject) {
        return getTileDao(reproject).getTileMatrixSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TileMatrix> getTileMatrices(boolean reproject) {
        return getTileDao(reproject).getTileMatrices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TileMatrix getTileMatrix(boolean reproject, long zoom) {
        return getTileDao(reproject).getTileMatrix(zoom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deleteTileMatrices(boolean reproject, String table) {
        try {
            getTileDao(reproject).getTileMatrixDao().deleteByTableName(table);
        } catch (SQLException e) {
            throw new GeoPackageException(
                    "Failed to delete tile matrices for tile table. GeoPackage: "
                            + reprojectTileDao.getDatabase() + ", Tile Table: "
                            + table,
                    e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected long getMapZoom(boolean reproject, TileMatrix tileMatrix) {
        return getTileDao(reproject).getMapZoom(tileMatrix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createTileMatrix(TileMatrix tileMatrix) {
        try {
            getReprojectTileDao().getTileMatrixDao().createOrUpdate(tileMatrix);
        } catch (SQLException e) {
            throw new GeoPackageException(
                    "Failed to create tile matrix. GeoPackage: "
                            + reprojectTileDao.getDatabase() + ", Tile Table: "
                            + tileMatrix.getTableName(),
                    e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int reproject(long zoom, long toZoom, BoundingBox boundingBox,
                            long matrixWidth, long matrixHeight, long tileWidth,
                            long tileHeight) {

        int tiles = 0;

        TileDao tileDao = getTileDao();
        TileDao reprojectTileDao = getReprojectTileDao();

        double minLongitude = boundingBox.getMinLongitude();
        double maxLatitude = boundingBox.getMaxLatitude();

        double longitudeRange = boundingBox.getLongitudeRange();
        double latitudeRange = boundingBox.getLatitudeRange();

        BoundingBox zoomBounds = tileDao.getBoundingBox(zoom,
                reprojectTileDao.getProjection());
        TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(boundingBox,
                matrixWidth, matrixHeight, zoomBounds);

        TileCreator tileCreator = new TileCreator(tileDao, (int) tileWidth,
                (int) tileHeight, reprojectTileDao.getProjection());

        for (long tileRow = tileGrid.getMinY(); tileRow <= tileGrid
                .getMaxY(); tileRow++) {

            double tileMaxLatitude = maxLatitude
                    - ((tileRow / (double) matrixHeight) * latitudeRange);
            double tileMinLatitude = maxLatitude
                    - (((tileRow + 1) / (double) matrixHeight) * latitudeRange);

            for (long tileColumn = tileGrid.getMinX(); isActive()
                    && tileColumn <= tileGrid.getMaxX(); tileColumn++) {

                double tileMinLongitude = minLongitude
                        + ((tileColumn / (double) matrixWidth)
                        * longitudeRange);
                double tileMaxLongitude = minLongitude
                        + (((tileColumn + 1) / (double) matrixWidth)
                        * longitudeRange);

                BoundingBox tileBounds = new BoundingBox(tileMinLongitude,
                        tileMinLatitude, tileMaxLongitude, tileMaxLatitude);

                GeoPackageTile tile = tileCreator.getTile(tileBounds, zoom);

                if (tile != null) {

                    TileRow row = reprojectTileDao.queryForTile(tileColumn,
                            tileRow, toZoom);

                    boolean insert = row == null;
                    if (insert) {
                        row = reprojectTileDao.newRow();
                        row.setTileColumn(tileColumn);
                        row.setTileRow(tileRow);
                        row.setZoomLevel(toZoom);
                    }

                    row.setTileData(tile.getData());

                    if (insert) {
                        reprojectTileDao.insert(row);
                    } else {
                        reprojectTileDao.update(row);
                    }
                    tiles++;

                    if (progress != null) {
                        progress.addProgress(1);
                    }
                }
            }

        }

        return tiles;
    }

}