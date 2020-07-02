package mil.nga.geopackage.features.user;

import android.database.Cursor;

import java.util.List;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserCursor;
import mil.nga.geopackage.user.UserDao;
import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Feature Cursor to wrap a database cursor for feature queries
 *
 * @author osbornb
 */
public class FeatureCursor extends
        UserCursor<FeatureColumn, FeatureTable, FeatureRow> {

    /**
     * Constructor
     *
     * @param table  feature table
     * @param cursor cursor
     */
    public FeatureCursor(FeatureTable table, Cursor cursor) {
        super(table, cursor);
    }

    /**
     * Constructor
     *
     * @param table   feature table
     * @param columns columns
     * @param cursor  cursor
     * @since 3.5.0
     */
    public FeatureCursor(FeatureTable table, String[] columns,
                         Cursor cursor) {
        super(table, columns, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureRow getRow(int[] columnTypes, Object[] values) {
        return new FeatureRow(getTable(), getColumns(), columnTypes, values);
    }

    /**
     * {@inheritDoc}
     * Handles geometries
     */
    @Override
    public Object getValue(FeatureColumn column) {
        Object value;
        if (column.isGeometry()) {
            value = getGeometry();
        } else {
            value = super.getValue(column);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureColumns getColumns() {
        return (FeatureColumns) super.getColumns();
    }

    /**
     * Get the geometry
     *
     * @return geometry data
     */
    public GeoPackageGeometryData getGeometry() {

        GeoPackageGeometryData geometry = null;

        int columnIndex = getColumns().getGeometryIndex();
        int type = getType(columnIndex);

        if (type != FIELD_TYPE_NULL) {
            byte[] geometryBytes = getBlob(columnIndex);

            if (geometryBytes != null) {
                geometry = GeoPackageGeometryData.create(geometryBytes);
            }
        }

        return geometry;
    }

    /**
     * Enable requery attempt of invalid rows after iterating through original query rows.
     * Only supported for {@link #moveToNext()} and {@link #getRow()} usage.
     *
     * @param dao data access object used to perform requery
     * @since 2.0.0
     */
    public void enableInvalidRequery(FeatureDao dao) {
        super.enableInvalidRequery(dao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserInvalidCursor<FeatureColumn, FeatureTable, FeatureRow, ? extends UserCursor<FeatureColumn, FeatureTable, FeatureRow>, ? extends UserDao<FeatureColumn, FeatureTable, FeatureRow, ? extends UserCursor<FeatureColumn, FeatureTable, FeatureRow>>> createInvalidCursor(UserDao dao, UserCursor cursor, List<Integer> invalidPositions, List<FeatureColumn> blobColumns) {
        return new FeatureInvalidCursor((FeatureDao) dao, (FeatureCursor) cursor, invalidPositions, blobColumns);
    }

}
