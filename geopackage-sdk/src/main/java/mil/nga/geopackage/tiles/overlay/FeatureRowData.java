package mil.nga.geopackage.tiles.overlay;

import java.util.HashMap;
import java.util.Map;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.geojson.FeatureConverter;

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
     * Id column
     */
    private String idColumn;

    /**
     * Geometry column
     */
    private String geometryColumn;

    /**
     * Constructor
     *
     * @param values column names and values
     * @since 6.3.1
     */
    public FeatureRowData(Map<String, Object> values) {
        this(values, null);
    }

    /**
     * Constructor
     *
     * @param values         column names and values
     * @param geometryColumn geometry column name
     */
    public FeatureRowData(Map<String, Object> values, String geometryColumn) {
        this(values, null, geometryColumn);
    }

    /**
     * Constructor
     *
     * @param values         column names and values
     * @param idColumn       id column name
     * @param geometryColumn geometry column name
     * @since 6.3.1
     */
    public FeatureRowData(Map<String, Object> values, String idColumn, String geometryColumn) {
        this.values = values;
        this.idColumn = idColumn;
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
     * Get the id column name
     *
     * @return id column
     * @since 6.3.1
     */
    public String getIdColumn() {
        return idColumn;
    }

    /**
     * Get the id
     *
     * @return id
     * @since 6.3.1
     */
    public Long getId() {
        Long id = null;
        if (idColumn != null) {
            id = (Long) values.get(idColumn);
        }
        return id;
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
        GeoPackageGeometryData geometryData = null;
        if (geometryColumn != null) {
            geometryData = (GeoPackageGeometryData) values.get(geometryColumn);
        }
        return geometryData;
    }

    /**
     * Get the geometry
     *
     * @return geometry
     */
    public Geometry getGeometry() {
        Geometry geometry = null;
        GeoPackageGeometryData geometryData = getGeometryData();
        if (geometryData != null) {
            geometry = geometryData.getGeometry();
        }
        return geometry;
    }

    /**
     * Get the geometry type
     *
     * @return geometry type
     * @since 6.3.1
     */
    public GeometryType getGeometryType() {
        GeometryType geometryType = null;
        Geometry geometry = getGeometry();
        if (geometry != null) {
            geometryType = geometry.getGeometryType();
        }
        return geometryType;
    }

    /**
     * Get the geometry envelope
     *
     * @return geometry envelope
     * @since 6.3.1
     */
    public GeometryEnvelope getGeometryEnvelope() {
        GeometryEnvelope envelope = null;
        GeoPackageGeometryData geometryData = getGeometryData();
        if (geometryData != null) {
            envelope = geometryData.getOrBuildEnvelope();
        }
        return envelope;
    }

    /**
     * Build a JSON compatible object
     *
     * @return JSON compatible object
     */
    public Object jsonCompatible() {
        return jsonCompatible(true, true);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includePoints true to include point geometries, but no other geometry types
     * @return JSON compatible object
     */
    public Object jsonCompatibleWithPoints(boolean includePoints) {
        return jsonCompatible(includePoints, false);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includeGeometries true to include all geometries, false for no geometries
     * @return JSON compatible object
     */
    public Object jsonCompatibleWithGeometries(boolean includeGeometries) {
        return jsonCompatible(includeGeometries, includeGeometries);
    }

    /**
     * Build a JSON compatible object
     *
     * @param includePoints     true to include point geometries, ignored if includeGeometries is true
     * @param includeGeometries true to include all geometry types
     * @return JSON compatible object
     */
    public Object jsonCompatible(boolean includePoints, boolean includeGeometries) {

        Map<String, Object> jsonValues = new HashMap<>(values);

        if (geometryColumn != null && jsonValues.containsKey(geometryColumn)) {

            if (includeGeometries || includePoints) {
                Geometry geometry = getGeometry();
                if (geometry != null) {
                    if (includeGeometries || (includePoints && geometry.getGeometryType() == GeometryType.POINT)) {
                        jsonValues.put(geometryColumn, FeatureConverter.toMap(geometry));
                    } else {
                        jsonValues.put(geometryColumn, null);
                    }
                }
            } else {
                jsonValues.put(geometryColumn, null);
            }

        }

        return jsonValues;
    }

}
