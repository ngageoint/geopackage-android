package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;

/**
 * Feature Preview for drawing a preview tile from a feature table
 *
 * @author osbornb
 * @since 3.5.0
 */
public class FeaturePreview {

    /**
     * GeoPackage
     */
    private final GeoPackage geoPackage;

    /**
     * Feature Tiles for drawing
     */
    private final FeatureTiles featureTiles;

    /**
     * Manual bounding box query flag for non indexed and empty contents bounds
     * feature tables
     */
    private boolean manual = false;

    /**
     * Buffer percentage for drawing empty non features edges (>= 0.0 && < 0.5)
     */
    private double bufferPercentage = 0.0;

    /**
     * Query columns
     */
    private Set<String> columns = new LinkedHashSet<>();

    /**
     * Where clause
     */
    private String where;

    /**
     * Where clause arguments
     */
    private String[] whereArgs = null;

    /**
     * Query feature limit
     */
    private Integer limit = null;

    /**
     * Constructor
     *
     * @param context      context
     * @param geoPackage   GeoPackage
     * @param featureTable feature table
     */
    public FeaturePreview(Context context, GeoPackage geoPackage, String featureTable) {
        this(context, geoPackage, geoPackage.getFeatureDao(featureTable));
    }

    /**
     * Constructor
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature DAO
     */
    public FeaturePreview(Context context, GeoPackage geoPackage, FeatureDao featureDao) {
        this(geoPackage, new DefaultFeatureTiles(context, geoPackage, featureDao));
    }

    /**
     * Constructor
     *
     * @param geoPackage   GeoPackage
     * @param featureTiles feature tiles
     */
    public FeaturePreview(GeoPackage geoPackage, FeatureTiles featureTiles) {
        this.geoPackage = geoPackage;
        this.featureTiles = featureTiles;
        FeatureDao featureDao = featureTiles.getFeatureDao();
        columns.add(featureDao.getIdColumnName());
        columns.add(featureDao.getGeometryColumnName());
        where = CoreSQLUtils.quoteWrap(featureDao.getGeometryColumnName())
                + " IS NOT NULL";
    }

    /**
     * Get the GeoPackage
     *
     * @return GeoPackage
     */
    public GeoPackage getGeoPackage() {
        return geoPackage;
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
     * Is manual bounding box query enabled for non indexed and empty contents
     * bounds feature tables
     *
     * @return manual flag
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * Set the manual bounding box query flag for non indexed and empty contents
     * bounds feature tables
     *
     * @param manual manual flag
     */
    public void setManual(boolean manual) {
        this.manual = manual;
    }

    /**
     * Get the buffer percentage for drawing empty non features edges (i.e. 0.1
     * equals 10% buffer edges)
     *
     * @return buffer percentage (>= 0.0 && < 0.5)
     */
    public double getBufferPercentage() {
        return bufferPercentage;
    }

    /**
     * Set the buffer percentage for drawing empty non features edges (i.e. 0.1
     * equals 10% buffer edges)
     *
     * @param bufferPercentage buffer percentage (>= 0.0 && < 0.5)
     */
    public void setBufferPercentage(double bufferPercentage) {
        if (bufferPercentage < 0.0 || bufferPercentage >= 0.5) {
            throw new GeoPackageException(
                    "Buffer percentage must be in the range: 0.0 <= bufferPercentage < 0.5. invalid value: "
                            + bufferPercentage);
        }
        this.bufferPercentage = bufferPercentage;
    }

    /**
     * Get the query columns
     *
     * @return columns
     */
    public Set<String> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    /**
     * Add query columns
     *
     * @param columns columns
     */
    public void addColumns(Collection<String> columns) {
        this.columns.addAll(columns);
    }

    /**
     * Add query columns
     *
     * @param columns columns
     */
    public void addColumns(String[] columns) {
        for (String column : columns) {
            addColumn(column);
        }
    }

    /**
     * Add a query column
     *
     * @param column column
     */
    public void addColumn(String column) {
        columns.add(column);
    }

    /**
     * Get the where clause
     *
     * @return where
     */
    public String getWhere() {
        return where;
    }

    /**
     * Set the where clause
     *
     * @param where where
     */
    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * Append to the where clause
     *
     * @param where where
     */
    public void appendWhere(String where) {
        this.where = (this.where != null ? this.where + " AND " : "") + where;
    }

    /**
     * Get the where arguments
     *
     * @return where args
     */
    public String[] getWhereArgs() {
        return whereArgs;
    }

    /**
     * Set the where arguments
     *
     * @param whereArgs where arguments
     */
    public void setWhereArgs(String[] whereArgs) {
        this.whereArgs = whereArgs;
    }

    /**
     * Get the feature query limit
     *
     * @return limit
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * Set the feature query limit
     *
     * @param limit limit
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * Draw a preview image
     *
     * @return preview image
     */
    public Bitmap draw() {

        Bitmap image = null;

        FeatureDao featureDao = featureTiles.getFeatureDao();
        String table = featureDao.getTableName();

        Projection webMercator = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

        BoundingBox boundingBox = geoPackage.getFeatureBoundingBox(webMercator,
                table, false);
        if (boundingBox == null) {
            boundingBox = geoPackage.getContentsBoundingBox(webMercator, table);
        }
        if (boundingBox == null && manual) {
            boundingBox = geoPackage.getFeatureBoundingBox(webMercator, table,
                    manual);
        }
        if (boundingBox != null) {
            boundingBox = TileBoundingBoxUtils
                    .boundWebMercatorBoundingBox(boundingBox);
            BoundingBox expandedBoundingBox = boundingBox
                    .squareExpand(bufferPercentage);
            expandedBoundingBox = TileBoundingBoxUtils
                    .boundWebMercatorBoundingBox(expandedBoundingBox);
            int zoom = TileBoundingBoxUtils.getZoomLevel(expandedBoundingBox);

            FeatureCursor results = featureDao.query(
                    columns.toArray(new String[]{}), where, whereArgs, null,
                    null, null, limit != null ? limit.toString() : null);
            image = featureTiles.drawTile(zoom, expandedBoundingBox, results);
        }

        return image;
    }

    /**
     * Close the feature tiles connection
     */
    public void close() {
        featureTiles.close();
    }

}