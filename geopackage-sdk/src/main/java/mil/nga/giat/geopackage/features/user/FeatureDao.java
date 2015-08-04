package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.db.GeoPackageConnection;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.projection.ProjectionFactory;
import mil.nga.giat.geopackage.user.UserDao;
import mil.nga.giat.wkb.geom.GeometryType;

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
     * @param database
     * @param db
     * @param featureDb
     * @param geometryColumns
     * @param table
     */
    public FeatureDao(String database, GeoPackageConnection db, FeatureConnection featureDb, GeometryColumns geometryColumns,
                      FeatureTable table) {
        super(database, db, featureDb, table);

        this.featureDb = featureDb;
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

        projection = ProjectionFactory.getProjection(geometryColumns.getSrs()
                .getOrganizationCoordsysId());
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
     * @return
     */
    public FeatureConnection getFeatureDb() {
        return featureDb;
    }

    /**
     * Get the Geometry Columns
     *
     * @return
     */
    public GeometryColumns getGeometryColumns() {
        return geometryColumns;
    }

    /**
     * The the Geometry Column name
     *
     * @return
     */
    public String getGeometryColumnName() {
        return geometryColumns.getColumnName();
    }

    /**
     * Get the Geometry Type
     *
     * @return
     */
    public GeometryType getGeometryType() {
        return geometryColumns.getGeometryType();
    }

}
