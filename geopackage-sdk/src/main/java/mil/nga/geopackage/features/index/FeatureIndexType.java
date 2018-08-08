package mil.nga.geopackage.features.index;

/**
 * Feature Index type enumeration of index location
 *
 * @author osbornb
 * @since 1.1.0
 */
public enum FeatureIndexType {

    /**
     * Metadata tables within the Android app
     */
    METADATA,

    /**
     * GeoPackage extension tables
     */
    GEOPACKAGE,

    /**
     * RTree Index extension
     *
     * @since 3.0.3
     */
    RTREE,

    /**
     * No index
     *
     * @since 3.0.3
     */
    NONE;

}
