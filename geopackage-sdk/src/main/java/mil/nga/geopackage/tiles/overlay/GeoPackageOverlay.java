package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;

import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.retriever.TileRetriever;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * GeoPackage Map Overlay Tile Provider
 *
 * @author osbornb
 */
public class GeoPackageOverlay extends BoundedOverlay {

    /**
     * Tile retriever
     */
    private final TileRetriever retriever;

    /**
     * Constructor using GeoPackage tile sizes
     *
     * @param tileDao
     */
    public GeoPackageOverlay(TileDao tileDao) {
        this.retriever = new GeoPackageTileRetriever(tileDao);
    }

    /**
     * Constructor with specified tile size
     *
     * @param tileDao
     * @param width
     * @param height
     */
    public GeoPackageOverlay(TileDao tileDao, int width, int height) {
        this.retriever = new GeoPackageTileRetriever(tileDao, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile retrieveTile(int x, int y, int zoom) {

        GeoPackageTile geoPackageTile = retriever.getTile(x, y, zoom);
        Tile tile = GeoPackageOverlayFactory.getTile(geoPackageTile);

        return tile;
    }

}
