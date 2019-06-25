package mil.nga.geopackage.features;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.Geometry;

/**
 * OGC API Feature Generator
 *
 * @author osbornb
 */
public class OAPIFeatureGenerator extends OAPIFeatureCoreGenerator {

    /**
     * Feature DAO
     */
    protected FeatureDao featureDao;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     * @param tableName  table name
     * @param server     server url
     * @param name       collection identifier
     */
    public OAPIFeatureGenerator(GeoPackage geoPackage, String tableName,
                                String server, String name) {
        super(geoPackage, tableName, server, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage getGeoPackage() {
        return (GeoPackage) geoPackage;
    }

    /**
     * Get the feature DAO
     *
     * @return feature DAO
     */
    public FeatureDao getFeatureDao() {
        return featureDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addColumn(FeatureColumn featureColumn) {
        featureDao.addColumn(featureColumn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFeature(Geometry geometry,
                                 Map<String, Object> properties) throws SQLException {

        if (srs == null) {
            createSrs();
        }

        if (featureDao == null) {
            GeometryColumns geometryColumns = createTable(properties);
            featureDao = getGeoPackage().getFeatureDao(geometryColumns);
        }

        Map<String, Object> values = new HashMap<>();

        for (Entry<String, Object> property : properties.entrySet()) {
            String column = property.getKey();
            Object value = getValue(column, property.getValue());
            values.put(column, value);
        }

        FeatureRow featureRow = featureDao.newRow();

        featureRow.setGeometry(createGeometryData(geometry));
        for (Entry<String, Object> value : values.entrySet()) {
            featureRow.setValue(value.getKey(), value.getValue());
        }

        saveFeature(featureRow);
    }

    /**
     * Save the feature row
     *
     * @param featureRow feature row
     */
    protected void saveFeature(FeatureRow featureRow) {
        featureDao.create(featureRow);
    }

}
