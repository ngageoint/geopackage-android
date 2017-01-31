package mil.nga.geopackage.tiles.overlay;

import java.util.HashMap;
import java.util.Map;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.util.GeometryJSONCompatible;

/**
 * Represents the values of a single feature row
 *
 * @author osbornb
 * @since 1.2.7
 */
public class FeatureRowData {

    /**
     * Column names and values
     */
    private Map<String, Object> values;

    /**
     * Geometry column
     */
    private String geometryColumn;

    /**
     * Constructor
     *
     * @param values         column names and values
     * @param geometryColumn geometry column name
     */
    public FeatureRowData(Map<String, Object> values, String geometryColumn) {
        this.values = values;
        this.geometryColumn = geometryColumn;
    }

    /**
     * Get the values
     *
     * @return column names and values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Get the geometry column name
     *
     * @return geometry column
     */
    public String getGeometryColumn() {
        return geometryColumn;
    }

    /**
     * Get the geometry data
     *
     * @return geometry data
     */
    public GeoPackageGeometryData getGeometryData() {
        return (GeoPackageGeometryData) values.get(geometryColumn);
    }

    /**
     * Get the geometry
     *
     * @return geometry
     */
    public Geometry getGeometry() {
        return getGeometryData().getGeometry();
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

        Map<String, Object> jsonValues = new HashMap<>();

        for (String key : values.keySet()) {
            Object jsonValue = null;
            Object value = values.get(key);
            if (key.equals(geometryColumn)) {
                GeoPackageGeometryData geometryData = (GeoPackageGeometryData) value;
                if (geometryData.getGeometry() != null) {
                    if (includeGeometries || (includePoints && geometryData.getGeometry().getGeometryType() == GeometryType.POINT)) {
                        jsonValue = GeometryJSONCompatible.getJSONCompatibleGeometry(geometryData.getGeometry());
                    }
                } else {
                    jsonValue = value;
                }
                if (jsonValue != null) {
                    jsonValues.put(key, jsonValue);
                }
            }
        }

        return jsonValues;
    }

}
