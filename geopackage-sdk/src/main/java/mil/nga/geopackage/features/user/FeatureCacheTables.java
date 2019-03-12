package mil.nga.geopackage.features.user;

import android.annotation.TargetApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Feature Row Cache for multiple feature tables in a single GeoPackage
 *
 * @author osbornb
 * @since 3.2.0
 */
public class FeatureCacheTables {

    /**
     * Mapping between feature table name and a feature row cache
     */
    private Map<String, FeatureCache> tableCache = new HashMap<>();

    /**
     * Cache size
     */
    private int maxCacheSize;

    /**
     * Constructor, created with cache size of {@link FeatureCache#DEFAULT_CACHE_MAX_SIZE}
     */
    public FeatureCacheTables() {
        this(FeatureCache.DEFAULT_CACHE_MAX_SIZE);
    }

    /**
     * Constructor
     *
     * @param maxCacheSize max feature rows to retain in each feature table cache
     */
    public FeatureCacheTables(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Get the max cache size used when creating new feature row caches
     *
     * @return max cache size
     */
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Set the max cache size to use when creating new feature row caches
     *
     * @param maxCacheSize feature row max cache size
     */
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Get the feature table names with a feature row cache
     *
     * @return feature table names
     */
    public Set<String> getTables() {
        return tableCache.keySet();
    }

    /**
     * Get or create a feature row cache for the table name
     *
     * @param tableName feature table name
     * @return feature row cache
     */
    public FeatureCache getCache(String tableName) {
        FeatureCache cache = tableCache.get(tableName);
        if (cache == null) {
            cache = new FeatureCache(maxCacheSize);
            tableCache.put(tableName, cache);
        }
        return cache;
    }

    /**
     * Get or create a feature row cache for the feature row
     *
     * @param featureRow feature row
     * @return feature row cache
     */
    public FeatureCache getCache(FeatureRow featureRow) {
        return getCache(featureRow.getTable().getTableName());
    }

    /**
     * Get the cache max size for the table name
     *
     * @param tableName feature table name
     * @return max size
     */
    public int getMaxSize(String tableName) {
        return getCache(tableName).getMaxSize();
    }

    /**
     * Get the current cache size, number of feature rows cached, for the table name
     *
     * @param tableName feature table name
     * @return cache size
     */
    public int getSize(String tableName) {
        return getCache(tableName).getSize();
    }

    /**
     * Get the cached feature row by table name and feature id
     *
     * @param tableName feature table name
     * @param featureId feature row id
     * @return feature row or null
     */
    public FeatureRow get(String tableName, long featureId) {
        return getCache(tableName).get(featureId);
    }

    /**
     * Cache the feature row
     *
     * @param featureRow feature row
     * @return previous cached feature row or null
     */
    public FeatureRow put(FeatureRow featureRow) {
        return getCache(featureRow).put(featureRow);
    }

    /**
     * Remove the cached feature row
     *
     * @param featureRow feature row
     * @return removed feature row or null
     */
    public FeatureRow remove(FeatureRow featureRow) {
        return remove(featureRow.getTable().getTableName(), featureRow.getId());
    }

    /**
     * Remove the cached feature row by id
     *
     * @param tableName feature table name
     * @param featureId feature row id
     * @return removed feature row or null
     */
    public FeatureRow remove(String tableName, long featureId) {
        return getCache(tableName).remove(featureId);
    }

    /**
     * Clear the feature table cache
     *
     * @param tableName feature table name
     */
    public void clear(String tableName) {
        tableCache.remove(tableName);
    }

    /**
     * Clear all caches
     */
    public void clear() {
        tableCache.clear();
    }

    /**
     * Resize the feature table cache
     *
     * @param tableName    feature table name
     * @param maxCacheSize max cache size
     */
    @TargetApi(21)
    public void resize(String tableName, int maxCacheSize) {
        getCache(tableName).resize(maxCacheSize);
    }

    /**
     * Resize all caches and update the max cache size
     *
     * @param maxCacheSize max cache size
     */
    @TargetApi(21)
    public void resize(int maxCacheSize) {
        setMaxCacheSize(maxCacheSize);
        for (FeatureCache cache : tableCache.values()) {
            cache.resize(maxCacheSize);
        }
    }

    /**
     * Clear and resize the feature table cache
     *
     * @param tableName    feature table name
     * @param maxCacheSize max cache size
     */
    public void clearAndResize(String tableName, int maxCacheSize) {
        getCache(tableName).clearAndResize(maxCacheSize);
    }

    /**
     * Clear and resize all caches and update the max cache size
     *
     * @param maxCacheSize max cache size
     */
    public void clearAndResize(int maxCacheSize) {
        setMaxCacheSize(maxCacheSize);
        for (FeatureCache cache : tableCache.values()) {
            cache.clearAndResize(maxCacheSize);
        }
    }

}
