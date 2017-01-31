package mil.nga.geopackage.tiles.overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of rows from a feature table
 *
 * @author osbornb
 * @since 1.2.7
 */
public class FeatureTableData {

    /**
     * Table name
     */
    private String name;

    /**
     * Row count
     */
    private long count;

    /**
     * List of rows
     */
    private List<FeatureRowData> rows;

    /**
     * Constructor
     *
     * @param name  table name
     * @param count row count
     */
    public FeatureTableData(String name, long count) {
        this(name, count, null);
    }

    /**
     * Constructor
     *
     * @param name  table name
     * @param count row count
     * @param rows  feature rows
     */
    public FeatureTableData(String name, long count, List<FeatureRowData> rows) {
        this.name = name;
        this.count = count;
        this.rows = rows;
    }

    /**
     * Get the table name
     *
     * @return table name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the row count
     *
     * @return row count
     */
    public long getCount() {
        return count;
    }

    /**
     * Get the feature rows
     *
     * @return feature rows
     */
    public List<FeatureRowData> getRows() {
        return rows;
    }

    /**
     * Build a JSON compatible object
     *
     * @return JSON compatiable object
     */
    public Object jsonCompatible() {
        return jsonCompatible(true, true);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includePoints true to include point geometries, but no other geometry types
     * @return JSON compatiable object
     */
    public Object jsonCompatibleWithPoints(boolean includePoints) {
        return jsonCompatible(includePoints, false);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includeGeometries true to include all geometries, false for no geometries
     * @return JSON compatiable object
     */
    public Object jsonCompatibleWithGeometries(boolean includeGeometries) {
        return jsonCompatible(includeGeometries, includeGeometries);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includePoints     true to include point geometries, ignored if includeGeometries is true
     * @param includeGeometries true to include all geometry types
     * @return JSON compatiable object
     */
    public Object jsonCompatible(boolean includePoints, boolean includeGeometries) {
        Object jsonObject = null;
        if (rows == null || rows.isEmpty()) {
            jsonObject = count;
        } else {
            List<Object> jsonRows = new ArrayList<>();
            for (FeatureRowData row : rows) {
                jsonRows.add(row.jsonCompatible(includePoints, includeGeometries));
            }
            jsonObject = jsonRows;
        }

        return jsonObject;
    }

}
