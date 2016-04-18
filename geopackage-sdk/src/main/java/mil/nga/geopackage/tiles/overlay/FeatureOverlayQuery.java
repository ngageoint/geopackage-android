package mil.nga.geopackage.tiles.overlay;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.j256.ormlite.dao.DaoManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxMapUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.util.GeometryPrinter;

/**
 * Used to query the features represented by tiles, either being drawn from or linked to the features
 *
 * @author osbornb
 * @since 1.1.0
 */
public class FeatureOverlayQuery {

    /**
     * Context
     */
    private final Context context;

    /**
     * Bounded Overlay
     */
    private final BoundedOverlay boundedOverlay;

    /**
     * Feature Tiles
     */
    private final FeatureTiles featureTiles;

    /**
     * Geometry Type
     */
    private final GeometryType geometryType;

    /**
     * Table name used when building text
     */
    private String name;

    /**
     * Screen click percentage between 0.0 and 1.0 for how close a feature on the screen must be
     * to be included in a click query
     */
    private float screenClickPercentage;

    /**
     * Flag indicating if building info messages for tiles with features over the max is enabled
     */
    private boolean maxFeaturesInfo;

    /**
     * Flag indicating if building info messages for clicked features is enabled
     */
    private boolean featuresInfo;

    /**
     * Max number of points clicked to return detailed information about
     */
    private int maxPointDetailedInfo;

    /**
     * Max number of features clicked to return detailed information about
     */
    private int maxFeatureDetailedInfo;

    /**
     * Print Point geometries within detailed info when true
     */
    private boolean detailedInfoPrintPoints;

    /**
     * Print Feature geometries within detailed info when true
     */
    private boolean detailedInfoPrintFeatures;

    /**
     * Constructor
     *
     * @param context
     * @param featureOverlay
     */
    public FeatureOverlayQuery(Context context, FeatureOverlay featureOverlay) {
        this(context, featureOverlay, featureOverlay.getFeatureTiles());
    }

    /**
     * Constructor
     *
     * @param context
     * @param boundedOverlay
     * @param featureTiles
     * @since 1.2.5
     */
    public FeatureOverlayQuery(Context context, BoundedOverlay boundedOverlay, FeatureTiles featureTiles) {
        this.context = context;
        this.boundedOverlay = boundedOverlay;
        this.featureTiles = featureTiles;

        FeatureDao featureDao = featureTiles.getFeatureDao();
        geometryType = featureDao.getGeometryType();
        name = featureDao.getDatabase() + " - " + featureDao.getTableName();

        Resources resources = context.getResources();

        // Get the screen percentage to determine when a feature is clicked
        TypedValue screenPercentage = new TypedValue();
        resources.getValue(R.dimen.map_feature_overlay_click_screen_percentage, screenPercentage, true);
        screenClickPercentage = screenPercentage.getFloat();

        maxFeaturesInfo = resources.getBoolean(R.bool.map_feature_overlay_max_features_info);
        featuresInfo = resources.getBoolean(R.bool.map_feature_overlay_features_info);

        maxPointDetailedInfo = resources.getInteger(R.integer.map_feature_overlay_max_point_detailed_info);
        maxFeatureDetailedInfo = resources.getInteger(R.integer.map_feature_overlay_max_feature_detailed_info);

        detailedInfoPrintPoints = resources.getBoolean(R.bool.map_feature_overlay_detailed_info_print_points);
        detailedInfoPrintFeatures = resources.getBoolean(R.bool.map_feature_overlay_detailed_info_print_features);
    }

    /**
     * Close the feature overlay query connection
     *
     * @since 1.2.7
     */
    public void close() {
        if (featureTiles != null) {
            featureTiles.close();
        }
    }

    /**
     * Get the bounded overlay
     *
     * @return bounded overlay
     * @since 1.2.5
     */
    public BoundedOverlay getBoundedOverlay() {
        return boundedOverlay;
    }

    /**
     * Get the feature tiles
     *
     * @return feature tiles
     */
    public FeatureTiles getFeatureTiles() {
        return featureTiles;
    }

    /**
     * Get the geometry type
     *
     * @return geometry type
     */
    public GeometryType getGeometryType() {
        return geometryType;
    }

    /**
     * Get the name used in text
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name used in text
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the screen click percentage, between 0.0 and 1.0
     *
     * @return screen click percentage
     */
    public float getScreenClickPercentage() {
        return screenClickPercentage;
    }

    /**
     * Set the screen click percentage, between 0.0 and 1.0
     *
     * @param screenClickPercentage
     */
    public void setScreenClickPercentage(float screenClickPercentage) {
        if (screenClickPercentage < 0.0 || screenClickPercentage > 1.0) {
            throw new GeoPackageException("Screen click percentage must be a float between 0.0 and 1.0, not " + screenClickPercentage);
        }
        this.screenClickPercentage = screenClickPercentage;
    }

    /**
     * Get the max points in a query to print detailed results about
     *
     * @return max point detailed info
     */
    public int getMaxPointDetailedInfo() {
        return maxPointDetailedInfo;
    }

    /**
     * Set the max points in a query to print detailed results about
     *
     * @param maxPointDetailedInfo
     */
    public void setMaxPointDetailedInfo(int maxPointDetailedInfo) {
        this.maxPointDetailedInfo = maxPointDetailedInfo;
    }

    /**
     * Get the max features in a query to print detailed results about
     *
     * @return max feature detailed info
     */
    public int getMaxFeatureDetailedInfo() {
        return maxFeatureDetailedInfo;
    }

    /**
     * Set the max features in a query to print detailed results about
     *
     * @param maxFeatureDetailedInfo
     */
    public void setMaxFeatureDetailedInfo(int maxFeatureDetailedInfo) {
        this.maxFeatureDetailedInfo = maxFeatureDetailedInfo;
    }

    /**
     * Is the detailed info going to print point geometries
     *
     * @return detailed info print points flag
     */
    public boolean isDetailedInfoPrintPoints() {
        return detailedInfoPrintPoints;
    }

    /**
     * Set the detailed info to print point geometries
     *
     * @param detailedInfoPrintPoints
     */
    public void setDetailedInfoPrintPoints(boolean detailedInfoPrintPoints) {
        this.detailedInfoPrintPoints = detailedInfoPrintPoints;
    }

    /**
     * Is the detailed info going to print feature geometries
     *
     * @return detailed info print features flag
     */
    public boolean isDetailedInfoPrintFeatures() {
        return detailedInfoPrintFeatures;
    }

    /**
     * Set the detailed info to print feature geometries
     *
     * @param detailedInfoPrintFeatures
     */
    public void setDetailedInfoPrintFeatures(boolean detailedInfoPrintFeatures) {
        this.detailedInfoPrintFeatures = detailedInfoPrintFeatures;
    }

    /**
     * Get the current zoom level of the map
     *
     * @param map
     * @return current zoom level
     */
    public float getCurrentZoom(GoogleMap map) {
        return map.getCameraPosition().zoom;
    }

    /**
     * Determine if the the feature overlay is on for the current zoom level of the map at the location
     *
     * @param map
     * @param latLng lat lon location
     * @return true if on
     * @since 1.2.6
     */
    public boolean isOnAtCurrentZoom(GoogleMap map, LatLng latLng) {
        float zoom = getCurrentZoom(map);
        boolean on = isOnAtCurrentZoom(zoom, latLng);
        return on;
    }

    /**
     * Determine if the feature overlay is on for the provided zoom level at the location
     *
     * @param zoom   zoom level
     * @param latLng lat lon location
     * @return true if on
     * @since 1.2.6
     */
    public boolean isOnAtCurrentZoom(double zoom, LatLng latLng) {

        Point point = new Point(latLng.longitude, latLng.latitude);
        TileGrid tileGrid = TileBoundingBoxUtils.getTileGridFromWGS84(point, (int) zoom);

        boolean on = boundedOverlay.hasTile((int) tileGrid.getMinX(), (int) tileGrid.getMinY(), (int) zoom);
        return on;
    }

    /**
     * Get the count of features in the tile at the lat lng coordinate and zoom level
     *
     * @param latLng lat lng location
     * @param zoom   zoom level
     * @return
     */
    public long tileFeatureCount(LatLng latLng, double zoom) {
        int zoomValue = (int) zoom;
        long tileFeaturesCount = tileFeatureCount(latLng, zoomValue);
        return tileFeaturesCount;
    }

    /**
     * Get the count of features in the tile at the lat lng coordinate and zoom level
     *
     * @param latLng lat lng location
     * @param zoom   zoom level
     * @return
     */
    public long tileFeatureCount(LatLng latLng, int zoom) {
        Point point = new Point(latLng.longitude, latLng.latitude);
        long tileFeaturesCount = tileFeatureCount(point, zoom);
        return tileFeaturesCount;
    }

    /**
     * Get the count of features in the tile at the point coordinate and zoom level
     *
     * @param point point location
     * @param zoom  zoom level
     * @return
     */
    public long tileFeatureCount(Point point, double zoom) {
        int zoomValue = (int) zoom;
        long tileFeaturesCount = tileFeatureCount(point, zoomValue);
        return tileFeaturesCount;
    }

    /**
     * Get the count of features in the tile at the point coordinate and zoom level
     *
     * @param point point location
     * @param zoom  zoom level
     * @return
     */
    public long tileFeatureCount(Point point, int zoom) {
        TileGrid tileGrid = TileBoundingBoxUtils.getTileGridFromWGS84(point, zoom);
        return featureTiles.queryIndexedFeaturesCount((int) tileGrid.getMinX(), (int) tileGrid.getMinY(), zoom);
    }

    /**
     * Determine if the provided count of features in the tile is more than the configured max features per tile
     *
     * @param tileFeaturesCount
     * @return true if more than the max features, false if less than or no configured max features
     */
    public boolean isMoreThanMaxFeatures(long tileFeaturesCount) {
        return featureTiles.getMaxFeaturesPerTile() != null && tileFeaturesCount > featureTiles.getMaxFeaturesPerTile().intValue();
    }

    /**
     * Build a bounding box using the click location, map view, and map. The bounding box can be
     * used to query for features that were clicked
     *
     * @param latLng click location
     * @param view   view
     * @param map    Google map
     * @return bounding box
     */
    public BoundingBox buildClickBoundingBox(LatLng latLng, View view, GoogleMap map) {

        // Get the screen width and height a click occurs from a feature
        int width = (int) Math.round(view.getWidth() * screenClickPercentage);
        int height = (int) Math.round(view.getHeight() * screenClickPercentage);

        // Get the screen click location
        Projection projection = map.getProjection();
        android.graphics.Point clickLocation = projection.toScreenLocation(latLng);

        // Get the screen click locations in each width or height direction
        android.graphics.Point left = new android.graphics.Point(clickLocation);
        android.graphics.Point up = new android.graphics.Point(clickLocation);
        android.graphics.Point right = new android.graphics.Point(clickLocation);
        android.graphics.Point down = new android.graphics.Point(clickLocation);
        left.offset(-width, 0);
        up.offset(0, -height);
        right.offset(width, 0);
        down.offset(0, height);

        // Get the coordinates of the bounding box points
        LatLng leftCoordinate = projection.fromScreenLocation(left);
        LatLng upCoordinate = projection.fromScreenLocation(up);
        LatLng rightCoordinate = projection.fromScreenLocation(right);
        LatLng downCoordinate = projection.fromScreenLocation(down);

        // Create the bounding box to query for features
        BoundingBox boundingBox = new BoundingBox(
                leftCoordinate.longitude,
                rightCoordinate.longitude,
                downCoordinate.latitude,
                upCoordinate.latitude);

        return boundingBox;
    }

    /**
     *  Build a bounding box using the location coordinate click location and map view bounds
     *
     *  @param latLng  click location
     *  @param mapBounds map bounds
     *
     *  @return bounding box
     *  @since 1.2.7
     */
    public BoundingBox buildClickBoundingBox(LatLng latLng, BoundingBox mapBounds){

        // Get the screen width and height a click occurs from a feature
        double width = TileBoundingBoxMapUtils.getLongitudeDistance(mapBounds) * screenClickPercentage;
        double height = TileBoundingBoxMapUtils.getLatitudeDistance(mapBounds) * screenClickPercentage;

        LatLng leftCoordinate = SphericalUtil.computeOffset(latLng, width, 270);
        LatLng upCoordinate = SphericalUtil.computeOffset(latLng, height, 0);
        LatLng rightCoordinate = SphericalUtil.computeOffset(latLng, width, 90);
        LatLng downCoordinate = SphericalUtil.computeOffset(latLng, height, 180);

        BoundingBox boundingBox = new BoundingBox(leftCoordinate.longitude, rightCoordinate.longitude, downCoordinate.latitude, upCoordinate.latitude);

        return boundingBox;
    }

    /**
     * Query for features in the WGS84 projected bounding box
     *
     * @param boundingBox query bounding box in WGS84 projection
     * @return feature index results, must be closed
     */
    public FeatureIndexResults queryFeatures(BoundingBox boundingBox) {
        FeatureIndexResults results = queryFeatures(boundingBox, null);
        return results;
    }

    /**
     * Query for features in the bounding box
     *
     * @param boundingBox query bounding box
     * @param projection  bounding box projection
     * @return feature index results, must be closed
     */
    public FeatureIndexResults queryFeatures(BoundingBox boundingBox, mil.nga.geopackage.projection.Projection projection) {

        if(projection == null){
            projection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        }

        // Query for features
        FeatureIndexManager indexManager = featureTiles.getIndexManager();
        if (indexManager == null) {
            throw new GeoPackageException("Index Manager is not set on the Feature Tiles and is required to query indexed features");
        }
        FeatureIndexResults results = indexManager.query(boundingBox, projection);
        return results;
    }

    /**
     * Check if the features are indexed
     *
     * @return true if indexed
     * @since 1.1.1
     */
    public boolean isIndexed() {
        return featureTiles.isIndexQuery();
    }

    /**
     * Get a max features information message
     *
     * @param tileFeaturesCount
     * @return max features message
     */
    public String buildMaxFeaturesInfoMessage(long tileFeaturesCount) {
        return name + "\n\t" + tileFeaturesCount + " features";
    }

    /**
     * Build a feature results information message and close the results
     *
     * @param results feature index results
     * @return results message or null if no results
     */
    public String buildResultsInfoMessageAndClose(FeatureIndexResults results) {
        return buildResultsInfoMessageAndClose(results, null, null);
    }

    /**
     * Build a feature results information message and close the results
     *
     * @param results feature index results
     * @param projection desired geometry projection
     * @return results message or null if no results
     * @since 1.2.7
     */
    public String buildResultsInfoMessageAndClose(FeatureIndexResults results, mil.nga.geopackage.projection.Projection projection) {
        return buildResultsInfoMessageAndClose(results, null, projection);
    }

    /**
     * Build a feature results information message and close the results
     *
     * @param results
     * @param clickLocation
     * @return results message or null if no results
     */
    public String buildResultsInfoMessageAndClose(FeatureIndexResults results, LatLng clickLocation) {
        return buildResultsInfoMessageAndClose(results, clickLocation, null);
    }

    /**
     * Build a feature results information message and close the results
     *
     * @param results
     * @param clickLocation
     * @param projection desired geometry projection
     * @return results message or null if no results
     * @since 1.2.7
     */
    public String buildResultsInfoMessageAndClose(FeatureIndexResults results, LatLng clickLocation, mil.nga.geopackage.projection.Projection projection) {
        String message = null;

        try {
            message = buildResultsInfoMessage(results, clickLocation, projection);
        } finally {
            results.close();
        }

        return message;
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @return results message or null if no results
     */
    public String buildResultsInfoMessage(FeatureIndexResults results) {
        return buildResultsInfoMessage(results, null, null);
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @param projection desired geometry projection
     * @return results message or null if no results
     * @since 1.2.7
     */
    public String buildResultsInfoMessage(FeatureIndexResults results, mil.nga.geopackage.projection.Projection projection) {
        return buildResultsInfoMessage(results, null, projection);
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @param clickLocation
     * @return results message or null if no results
     */
    public String buildResultsInfoMessage(FeatureIndexResults results, LatLng clickLocation) {
        return buildResultsInfoMessage(results, clickLocation, null);
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @param clickLocation
     * @param projection desired geometry projection
     * @return results message or null if no results
     * @since 1.2.7
     */
    public String buildResultsInfoMessage(FeatureIndexResults results, LatLng clickLocation, mil.nga.geopackage.projection.Projection projection) {

        String message = null;

        long featureCount = results.count();
        if (featureCount > 0) {

            int maxFeatureInfo = 0;
            if (geometryType == GeometryType.POINT) {
                maxFeatureInfo = maxPointDetailedInfo;
            } else {
                maxFeatureInfo = maxFeatureDetailedInfo;
            }

            if (featureCount <= maxFeatureInfo) {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(name)
                        .append("\n");

                int featureNumber = 0;

                for (FeatureRow featureRow : results) {

                    featureNumber++;
                    if (featureNumber > maxFeatureInfo) {
                        break;
                    }

                    if (featureCount > 1) {
                        if (featureNumber > 1) {
                            messageBuilder.append("\n");
                        } else {
                            messageBuilder.append("\n")
                                    .append(featureCount)
                                    .append(" Features")
                                    .append("\n");
                        }
                        messageBuilder.append("\n")
                                .append("Feature ")
                                .append(featureNumber)
                                .append(":")
                                .append("\n");
                    }

                    int geometryColumn = featureRow.getGeometryColumnIndex();
                    for (int i = 0; i < featureRow.columnCount(); i++) {
                        if (i != geometryColumn) {
                            Object value = featureRow.getValue(i);
                            if (value != null) {
                                messageBuilder.append("\n")
                                        .append(featureRow.getColumnName(i))
                                        .append(": ")
                                        .append(value);
                            }
                        }
                    }

                    GeoPackageGeometryData geomData = featureRow.getGeometry();
                    if(geomData != null && geomData.getGeometry() != null){

                        boolean printFeatures = false;
                        if (geomData.getGeometry().getGeometryType() == GeometryType.POINT) {
                            printFeatures = detailedInfoPrintPoints;
                        } else {
                            printFeatures = detailedInfoPrintFeatures;
                        }

                        if(printFeatures){
                            if(projection != null){
                                projectGeometry(geomData, projection);
                            }
                            messageBuilder.append("\n\n");
                            messageBuilder.append(GeometryPrinter.getGeometryString(geomData.getGeometry()));
                        }
                    }

                }

                message = messageBuilder.toString();
            } else {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(getName())
                        .append("\n\t")
                        .append(featureCount)
                        .append(" features");
                if (clickLocation != null) {
                    messageBuilder.append(" near location:\n");
                    Point point = new Point(clickLocation.longitude, clickLocation.latitude);
                    messageBuilder.append(GeometryPrinter.getGeometryString(point));
                }
                message = messageBuilder.toString();
            }
        }

        return message;
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @param clickLocation
     * @return feature table data or null if not results
     * @since 1.2.7
     */
    public FeatureTableData buildTableDataAndClose(FeatureIndexResults results, LatLng clickLocation) {
        return buildTableDataAndClose(results, clickLocation, null);
    }

    /**
     * Build a feature results information message
     *
     * @param results
     * @param clickLocation
     * @param projection desired geometry projection
     * @return feature table data or null if not results
     * @since 1.2.7
     */
    public FeatureTableData buildTableDataAndClose(FeatureIndexResults results, LatLng clickLocation, mil.nga.geopackage.projection.Projection projection) {

        FeatureTableData tableData = null;

        long featureCount = results.count();
        if (featureCount > 0) {

            int maxFeatureInfo = 0;
            if (geometryType == GeometryType.POINT) {
                maxFeatureInfo = maxPointDetailedInfo;
            } else {
                maxFeatureInfo = maxFeatureDetailedInfo;
            }

            if (featureCount <= maxFeatureInfo) {

                List<FeatureRowData> rows = new ArrayList<>();

                for (FeatureRow featureRow : results) {

                    Map<String, Object> values = new HashMap<>();
                    String geometryColumnName = null;

                    int geometryColumn = featureRow.getGeometryColumnIndex();
                    for (int i = 0; i < featureRow.columnCount(); i++) {

                        Object value = featureRow.getValue(i);

                        String columnName = featureRow.getColumnName(i);
                        if(i == geometryColumn){
                            geometryColumnName = columnName;
                            if(projection != null && value != null){
                                GeoPackageGeometryData geomData = (GeoPackageGeometryData) value;
                                projectGeometry(geomData, projection);
                            }
                        }

                        if(value != null){
                            values.put(columnName, value);
                        }
                    }

                    FeatureRowData featureRowData = new FeatureRowData(values, geometryColumnName);
                    rows.add(featureRowData);
                }

                tableData = new FeatureTableData(featureTiles.getFeatureDao().getTableName(), featureCount, rows);
            } else {
                tableData = new FeatureTableData(featureTiles.getFeatureDao().getTableName(), featureCount);
            }
        }

        results.close();

        return tableData;
    }

    /**
     * Project the geometry into the provided projection
     *
     * @param geometryData
     * @param projection
     * @since 1.2.7
     */
    public void projectGeometry(GeoPackageGeometryData geometryData, mil.nga.geopackage.projection.Projection projection){

        if(geometryData.getGeometry() != null){

            try {
                SpatialReferenceSystemDao srsDao = DaoManager.createDao(featureTiles.getFeatureDao().getDb().getConnectionSource(), SpatialReferenceSystem.class);
                int srsId = geometryData.getSrsId();
                SpatialReferenceSystem srs = srsDao.getOrCreate(srsId);

                long epsg = srs.getOrganizationCoordsysId();

                if(projection.getEpsg() != epsg){

                    mil.nga.geopackage.projection.Projection geomProjection = ProjectionFactory.getProjection(epsg);
                    ProjectionTransform transform = geomProjection.getTransformation(projection);

                    Geometry projectedGeometry = transform.transform(geometryData.getGeometry());
                    geometryData.setGeometry(projectedGeometry);
                    geometryData.setSrsId((int)projection.getEpsg());
                }
            }catch(SQLException e){
                throw new GeoPackageException("Failed to project geometry to projection with EPSG: " + projection.getEpsg(), e);
            }
        }

    }

    /**
     * Perform a query based upon the map click location and build a info message
     *
     * @param latLng location
     * @param view view
     * @param map Google Map
     * @return information message on what was clicked, or null
     */
    public String buildMapClickMessage(LatLng latLng, View view, GoogleMap map) {
        return buildMapClickMessage(latLng, view, map, null);
    }

    /**
     * Perform a query based upon the map click location and build a info message
     *
     * @param latLng location
     * @param view view
     * @param map Google Map
     * @param projection desired geometry projection
     * @return information message on what was clicked, or null
     * @since 1.2.7
     */
    public String buildMapClickMessage(LatLng latLng, View view, GoogleMap map, mil.nga.geopackage.projection.Projection projection) {

        // Get the zoom level
        double zoom = getCurrentZoom(map);

        // Build a bounding box to represent the click location
        BoundingBox boundingBox = buildClickBoundingBox(latLng, view, map);

        String message = buildMapClickMessage(latLng, zoom, boundingBox, projection);

        return message;
    }

    /**
     * Perform a query based upon the map click location and build a info message
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param mapBounds map view bounds
     * @return information message on what was clicked, or nil
     * @since 1.2.7
     */
    public String buildMapClickMessageWithMapBounds(LatLng latLng, double zoom, BoundingBox mapBounds) {
        return buildMapClickMessageWithMapBounds(latLng, zoom, mapBounds, null);
    }

    /**
     * Perform a query based upon the map click location and build a info message
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param mapBounds map view bounds
     * @param projection desired geometry projection
     * @return information message on what was clicked, or nil
     * @since 1.2.7
     */
    public String buildMapClickMessageWithMapBounds(LatLng latLng, double zoom, BoundingBox mapBounds, mil.nga.geopackage.projection.Projection projection) {

        // Build a bounding box to represent the click location
        BoundingBox boundingBox = buildClickBoundingBox(latLng, mapBounds);

        String message = buildMapClickMessage(latLng, zoom, boundingBox, projection);

        return message;
    }

    /**
     * Perform a query based upon the map click location and build a info message
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param boundingBox click bounding box
     * @param projection desired geometry projection
     * @return information message on what was clicked, or null
     */
    private String buildMapClickMessage(LatLng latLng, double zoom, BoundingBox boundingBox, mil.nga.geopackage.projection.Projection projection) {
        String message = null;

        // Verify the features are indexed and we are getting information
        if (isIndexed() && (maxFeaturesInfo || featuresInfo)) {

            if (isOnAtCurrentZoom(zoom, latLng)) {

                // Get the number of features in the tile location
                long tileFeatureCount = tileFeatureCount(latLng, zoom);

                // If more than a configured max features to draw
                if (isMoreThanMaxFeatures(tileFeatureCount)) {

                    // Build the max features message
                    if (maxFeaturesInfo) {
                        message = buildMaxFeaturesInfoMessage(tileFeatureCount);
                    }

                }
                // Else, query for the features near the click
                else if (featuresInfo) {

                    // Query for results and build the message
                    FeatureIndexResults results = queryFeatures(boundingBox, projection);
                    message = buildResultsInfoMessageAndClose(results, latLng, projection);

                }

            }
        }

        return message;
    }

    /**
     * Perform a query based upon the map click location and build feature table data
     *
     * @param latLng location
     * @param view view
     * @param map Google Map
     * @return table data on what was clicked, or null
     * @since 1.2.7
     */
    public FeatureTableData buildMapClickTableData(LatLng latLng, View view, GoogleMap map) {
        return buildMapClickTableData(latLng, view, map, null);
    }

    /**
     * Perform a query based upon the map click location and build feature table data
     *
     * @param latLng location
     * @param view view
     * @param map Google Map
     * @param projection desired geometry projection
     * @return table data on what was clicked, or null
     * @since 1.2.7
     */
    public FeatureTableData buildMapClickTableData(LatLng latLng, View view, GoogleMap map, mil.nga.geopackage.projection.Projection projection) {

        // Get the zoom level
        double zoom = getCurrentZoom(map);

        // Build a bounding box to represent the click location
        BoundingBox boundingBox = buildClickBoundingBox(latLng, view, map);

        FeatureTableData tableData = buildMapClickTableData(latLng, zoom, boundingBox, projection);

        return tableData;
    }

    /**
     * Perform a query based upon the map click location and build feature table data
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param mapBounds map view bounds
     * @return table data on what was clicked, or null
     * @since 1.2.7
     */
    public FeatureTableData buildMapClickTableDataWithMapBounds(LatLng latLng, double zoom, BoundingBox mapBounds) {
        return buildMapClickTableDataWithMapBounds(latLng, zoom, mapBounds, null);
    }

    /**
     * Perform a query based upon the map click location and build feature table data
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param mapBounds map view bounds
     * @param projection desired geometry projection
     * @return table data on what was clicked, or null
     * @since 1.2.7
     */
    public FeatureTableData buildMapClickTableDataWithMapBounds(LatLng latLng, double zoom, BoundingBox mapBounds, mil.nga.geopackage.projection.Projection projection) {

        // Build a bounding box to represent the click location
        BoundingBox boundingBox = buildClickBoundingBox(latLng, mapBounds);

        FeatureTableData tableData = buildMapClickTableData(latLng, zoom, boundingBox, projection);

        return tableData;
    }

    /**
     * Perform a query based upon the map click location and build feature table data
     *
     * @param latLng location
     * @param zoom current zoom level
     * @param boundingBox click bounding box
     * @param projection desired geometry projection
     * @return table data on what was clicked, or null
     */
    private FeatureTableData buildMapClickTableData(LatLng latLng, double zoom, BoundingBox boundingBox, mil.nga.geopackage.projection.Projection projection) {
        FeatureTableData tableData = null;

        // Verify the features are indexed and we are getting information
        if (isIndexed() && (maxFeaturesInfo || featuresInfo)) {

            if (isOnAtCurrentZoom(zoom, latLng)) {

                // Get the number of features in the tile location
                long tileFeatureCount = tileFeatureCount(latLng, zoom);

                // If more than a configured max features to draw
                if (isMoreThanMaxFeatures(tileFeatureCount)) {

                    // Build the max features message
                    if (maxFeaturesInfo) {
                        tableData = new FeatureTableData(featureTiles.getFeatureDao().getTableName(), tileFeatureCount);
                    }

                }
                // Else, query for the features near the click
                else if (featuresInfo) {

                    // Query for results and build the message
                    FeatureIndexResults results = queryFeatures(boundingBox, projection);
                    tableData = buildTableDataAndClose(results, latLng, projection);
                }

            }
        }

        return tableData;
    }

}
