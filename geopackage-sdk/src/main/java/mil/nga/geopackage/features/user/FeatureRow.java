package mil.nga.geopackage.features.user;

import android.content.ContentValues;

import java.io.IOException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserRow;

/**
 * Feature Row containing the values from a single cursor row
 *
 * @author osbornb
 */
public class FeatureRow extends UserRow<FeatureColumn, FeatureTable> {

    /**
     * Constructor
     *
     * @param table
     * @param columnTypes
     * @param values
     */
    FeatureRow(FeatureTable table, int[] columnTypes, Object[] values) {
        super(table, columnTypes, values);
    }

    /**
     * Constructor to create an empty row
     *
     * @param table
     */
    FeatureRow(FeatureTable table) {
        super(table);
    }

    /**
     * Copy Constructor
     *
     * @param featureRow feature row to copy
     */
    public FeatureRow(FeatureRow featureRow) {
        super(featureRow);
    }

    /**
     * Get the geometry column index
     *
     * @return
     */
    public int getGeometryColumnIndex() {
        return getTable().getGeometryColumnIndex();
    }

    /**
     * Get the geometry feature column
     *
     * @return
     */
    public FeatureColumn getGeometryColumn() {
        return getTable().getGeometryColumn();
    }

    /**
     * Get the geometry
     *
     * @return
     */
    public GeoPackageGeometryData getGeometry() {
        GeoPackageGeometryData geometryData = null;
        Object value = getValue(getGeometryColumnIndex());
        if (value != null) {
            geometryData = (GeoPackageGeometryData) value;
        }
        return geometryData;
    }

    /**
     * Set the geometry data
     *
     * @param geometryData
     */
    public void setGeometry(GeoPackageGeometryData geometryData) {
        setValue(getGeometryColumnIndex(), geometryData);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles geometry columns
     */
    @Override
    protected void columnToContentValue(ContentValues contentValues,
                                        FeatureColumn column, Object value) {

        if (column.isGeometry()) {

            String columnName = column.getName();

            if (value instanceof GeoPackageGeometryData) {
                GeoPackageGeometryData geometryData = (GeoPackageGeometryData) value;
                try {
                    contentValues.put(columnName, geometryData.toBytes());
                } catch (IOException e) {
                    throw new GeoPackageException(
                            "Failed to write Geometry Data bytes. column: "
                                    + columnName, e);
                }
            } else if (value instanceof byte[]) {
                contentValues.put(columnName, (byte[]) value);
            } else {
                throw new GeoPackageException(
                        "Unsupported update geometry column value type. column: "
                                + columnName + ", value type: "
                                + value.getClass().getName());
            }
        } else {
            super.columnToContentValue(contentValues, column, value);
        }

    }

}
