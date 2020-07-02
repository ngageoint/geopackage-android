package mil.nga.geopackage.features.user;

import android.content.ContentValues;

import java.io.IOException;
import java.util.Arrays;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserRow;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;

/**
 * Feature Row containing the values from a single cursor row
 *
 * @author osbornb
 */
public class FeatureRow extends UserRow<FeatureColumn, FeatureTable> {

    /**
     * Constructor
     *
     * @param table       feature table
     * @param columns     columns
     * @param columnTypes column types
     * @param values      values
     * @since 3.5.0
     */
    FeatureRow(FeatureTable table, FeatureColumns columns, int[] columnTypes,
               Object[] values) {
        super(table, columns, columnTypes, values);
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
     * {@inheritDoc}
     */
    @Override
    public FeatureColumns getColumns() {
        return (FeatureColumns) super.getColumns();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Handles geometry columns
     */
    @Override
    public void setValue(int index, Object value) {
        if (index == getGeometryColumnIndex() && value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            value = GeoPackageGeometryData.create(bytes);
        }
        super.setValue(index, value);
    }

    /**
     * {@inheritDoc}
     * <p>
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
                copyValue = GeoPackageGeometryData.create(copyBytes);
            } catch (IOException e) {
                throw new GeoPackageException(
                        "Failed to copy Geometry Data bytes. column: "
                                + column.getName(),
                        e);
            }

        } else {
            copyValue = super.copyValue(column, value);
        }

        return copyValue;
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
                                    + columnName,
                            e);
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

    /**
     * Get the geometry column index
     *
     * @return geometry column index
     */
    public int getGeometryColumnIndex() {
        return getColumns().getGeometryIndex();
    }

    /**
     * Get the geometry feature column
     *
     * @return geometry column
     */
    public FeatureColumn getGeometryColumn() {
        return getColumns().getGeometryColumn();
    }

    /**
     * Get the geometry column name
     *
     * @return geometry column name
     * @since 3.5.0
     */
    public String getGeometryColumnName() {
        return getColumns().getGeometryColumnName();
    }

    /**
     * Get the geometry
     *
     * @return geometry data
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
     * @param geometryData geometry data
     */
    public void setGeometry(GeoPackageGeometryData geometryData) {
        setValue(getGeometryColumnIndex(), geometryData);
    }

    /**
     * Get the simple features geometry value
     *
     * @return geometry
     * @since 3.1.0
     */
    public Geometry getGeometryValue() {
        GeoPackageGeometryData data = getGeometry();
        Geometry geometry = null;
        if (data != null) {
            geometry = data.getGeometry();
        }
        return geometry;
    }

    /**
     * Get the simple features geometry type
     *
     * @return geometry type
     * @since 3.2.0
     */
    public GeometryType getGeometryType() {
        Geometry geometry = getGeometryValue();
        GeometryType geometryType = null;
        if (geometry != null) {
            geometryType = geometry.getGeometryType();
        }
        return geometryType;
    }

    /**
     * Get the geometry envelope
     *
     * @return geometry envelope
     * @since 3.1.0
     */
    public GeometryEnvelope getGeometryEnvelope() {
        GeoPackageGeometryData data = getGeometry();
        GeometryEnvelope envelope = null;
        if (data != null) {
            envelope = data.getOrBuildEnvelope();
        }
        return envelope;
    }

    /**
     * Copy the row
     *
     * @return row copy
     * @since 3.0.1
     */
    public FeatureRow copy() {
        return new FeatureRow(this);
    }


}
