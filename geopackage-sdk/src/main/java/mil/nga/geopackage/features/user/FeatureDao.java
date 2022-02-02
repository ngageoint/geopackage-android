package mil.nga.geopackage.features.user;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.user.UserDao;
import mil.nga.proj.Projection;
import mil.nga.sf.GeometryType;

/**
 * Feature DAO for reading feature user data tables
 *
 * @author osbornb
 */
public class FeatureDao extends
        UserDao<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor> {

    /**
     * Feature connection
     */
    private final FeatureConnection featureDb;

    /**
     * Geometry Columns
     */
    private final GeometryColumns geometryColumns;

    /**
     * Constructor
     *
     * @param database        database name
     * @param db              connection
     * @param geometryColumns geometry columns
     * @param table           feature table
     */
    public FeatureDao(String database, GeoPackageConnection db, GeometryColumns geometryColumns,
                      FeatureTable table) {
        super(database, db, new FeatureConnection(db), table);

        this.featureDb = (FeatureConnection) getUserDb();
        this.geometryColumns = geometryColumns;
        if (geometryColumns.getContents() == null) {
            throw new GeoPackageException(GeometryColumns.class.getSimpleName()
                    + " " + geometryColumns.getId() + " has null "
                    + Contents.class.getSimpleName());
        }
        if (geometryColumns.getSrs() == null) {
            throw new GeoPackageException(GeometryColumns.class.getSimpleName()
                    + " " + geometryColumns.getId() + " has null "
                    + SpatialReferenceSystem.class.getSimpleName());
        }

        projection = geometryColumns.getProjection();
    }

    /**
     * Constructor, semi copy constructor with separate connection states
     *
     * @param featureDao feature dao
     * @since 3.4.0
     */
    public FeatureDao(FeatureDao featureDao) {
        this(featureDao.getDatabase(), featureDao.getDb(), featureDao.getGeometryColumns(), featureDao.getTable());
    }

    /**
     * Copy the feature dao for separate connection states, shares geometry columns and feature table memory
     *
     * @return feature dao
     * @since 3.4.0
     */
    public FeatureDao copy() {
        return new FeatureDao(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
        return getBoundingBox(projection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox(Projection projection) {
        Contents contents = geometryColumns.getContents();
        BoundingBox boundingBox = contents.getBoundingBox(projection);
        return boundingBox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureRow newRow() {
        return new FeatureRow(getTable());
    }

    /**
     * Get the Feature connection
     *
     * @return feature connection
     */
    public FeatureConnection getFeatureDb() {
        return featureDb;
    }

    /**
     * Get the Geometry Columns
     *
     * @return geometry columns
     */
    public GeometryColumns getGeometryColumns() {
        return geometryColumns;
    }

    /**
     * Get the Geometry Column name
     *
     * @return geometry column name
     */
    public String getGeometryColumnName() {
        return geometryColumns.getColumnName();
    }

    /**
     * Get the Geometry Type
     *
     * @return geometry type
     */
    public GeometryType getGeometryType() {
        return geometryColumns.getGeometryType();
    }

    /**
     * Get the Spatial Reference System
     *
     * @return srs
     * @since 4.0.0
     */
    public SpatialReferenceSystem getSrs() {
        return geometryColumns.getSrs();
    }

    /**
     * Get the Spatial Reference System id
     *
     * @return srs id
     * @since 4.0.0
     */
    public long getSrsId() {
        return geometryColumns.getSrsId();
    }

    /**
     * Get the Id Column
     *
     * @return id column
     * @since 3.5.0
     */
    public FeatureColumn getIdColumn() {
        return getPkColumn();
    }

    /**
     * Get the Id Column name
     *
     * @return id column name
     * @since 3.5.0
     */
    public String getIdColumnName() {
        return getPkColumnName();
    }

    /**
     * Get the Id and Geometry Column names
     *
     * @return column names
     * @since 3.5.0
     */
    public String[] getIdAndGeometryColumnNames() {
        return getTable().getIdAndGeometryColumnNames();
    }

}
