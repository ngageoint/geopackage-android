package mil.nga.geopackage.tiles.retriever;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * GeoPackage Tile Retriever, retrieves a tile from a GeoPackage from XYZ coordinates
 *
 * @author osbornb
 * @since 1.2.0
 */
public class GeoPackageTileRetriever implements TileRetriever {

    /**
     * Tile Creator
     */
    private final TileCreator tileCreator;

    /**
     * Constructor using GeoPackage tile sizes
     *
     * @param tileDao tile dao
     */
    public GeoPackageTileRetriever(TileDao tileDao) {
        this(tileDao, null, null);
    }

    /**
     * Constructor with specified tile size
     *
     * @param tileDao tile dao
     * @param width   width
     * @param height  height
     */
    public GeoPackageTileRetriever(TileDao tileDao, Integer width, Integer height) {

        tileDao.adjustTileMatrixLengths();

        Projection webMercator = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

        tileCreator = new TileCreator(tileDao, width, height, webMercator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTile(int x, int y, int zoom) {

        // Get the bounding box of the requested tile
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        boolean hasTile = tileCreator.hasTile(webMercatorBoundingBox);

        return hasTile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackageTile getTile(int x, int y, int zoom) {

        // Get the bounding box of the requested tile
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        GeoPackageTile tile = tileCreator.getTile(webMercatorBoundingBox);

        return tile;
    }

}
