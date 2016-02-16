package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;

import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GoogleAPIGeoPackageTileRetriever;
import mil.nga.geopackage.tiles.retriever.TileRetriever;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * GeoPackage Map Overlay Tile Provider, assumes the Google Maps API zoom level
 * and grid
 *
 * @author osbornb
 */
public class GoogleAPIGeoPackageOverlay extends BoundedOverlay {

    /**
     * Tile retriever
     */
    private final TileRetriever retriever;

    /**
     * Constructor
     *
     * @param tileDao
     */
    public GoogleAPIGeoPackageOverlay(TileDao tileDao) {
        this.retriever = new GoogleAPIGeoPackageTileRetriever(tileDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTileToRetrieve(int x, int y, int zoom) {
        return retriever.hasTile(x, y, zoom);
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
