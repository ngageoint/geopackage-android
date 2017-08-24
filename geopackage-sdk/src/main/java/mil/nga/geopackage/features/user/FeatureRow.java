package mil.nga.geopackage.features.user;

import android.content.ContentValues;

import java.io.IOException;
import java.util.Arrays;

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
     * @since 1.4.0
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
     * {@inheritDoc}
     * Handles geometry columns
     */
    @Override
    public void setValue(int index, Object value) {
        if (index == getGeometryColumnIndex() && value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            value = new GeoPackageGeometryData(bytes);
        }
        super.setValue(index, value);
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
     * Handles geometry columns
     */
    @Override
    protected Object copyValue(FeatureColumn column, Object value) {

        Object copyValue = null;

        if (column.isGeometry() && value instanceof GeoPackageGeometryData) {

            GeoPackageGeometryData geometryData = (GeoPackageGeometryData) value;
            try {
                byte[] bytes = geometryData.toBytes();
                byte[] copyBytes = Arrays.copyOf(bytes, bytes.length);
                copyValue = new GeoPackageGeometryData(copyBytes);
            } catch (IOException e) {
                throw new GeoPackageException(
                        "Failed to copy Geometry Data bytes. column: "
                                + column.getName(), e);
            }

        } else {
            copyValue = super.copyValue(column, value);
        }

        return copyValue;
    }

    /**
     * {@inheritDoc}
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
