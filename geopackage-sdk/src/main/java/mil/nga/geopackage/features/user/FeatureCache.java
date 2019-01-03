package mil.nga.geopackage.features.user;

import android.annotation.TargetApi;
import android.util.LruCache;

/**
 * Feature Row Cache for a single feature table
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureCache {

    /**
     * Default max number of feature rows to retain in cache
     */
    public static final int DEFAULT_CACHE_MAX_SIZE = 1000;

    /**
     * Feature Row cache
     */
    private LruCache<Long, FeatureRow> cache;

    /**
     * Constructor, created with cache max size of {@link #DEFAULT_CACHE_MAX_SIZE}
     */
    public FeatureCache() {
        this(DEFAULT_CACHE_MAX_SIZE);
    }

    /**
     * Constructor
     *
     * @param maxSize max feature rows to retain in the cache
     */
    public FeatureCache(int maxSize) {
        cache = new LruCache<>(maxSize);
    }

    /**
     * Get the cache max size
     *
     * @return max size
     */
    public int getMaxSize() {
        return cache.maxSize();
    }

    /**
     * Get the current cache size, number of feature rows cached
     *
     * @return cache size
     */
    public int getSize() {
        return cache.size();
    }

    /**
     * Get the cached feature row by feature id
     *
     * @param featureId feature row id
     * @return feature row or null
     */
    public FeatureRow get(long featureId) {
        return cache.get(featureId);
    }

    /**
     * Cache the feature row
     *
     * @param featureRow feature row
     * @return previous cached feature row or null
     */
    public FeatureRow put(FeatureRow featureRow) {
        return cache.put(featureRow.getId(), featureRow);
    }

    /**
     * Remove the cached feature row
     *
     * @param featureRow feature row
     * @return removed feature row or null
     */
    public FeatureRow remove(FeatureRow featureRow) {
        return remove(featureRow.getId());
    }

    /**
     * Remove the cached feature row by id
     *
     * @param featureId feature row id
     * @return removed feature row or null
     */
    public FeatureRow remove(long featureId) {
        return cache.remove(featureId);
    }

    /**
     * Clear the cache
     */
    public void clear() {
        cache.evictAll();
    }

    /**
     * Resize the cache
     *
     * @param maxSize max size
     */
    @TargetApi(21)
    public void resize(int maxSize) {
        cache.resize(maxSize);
    }

    /**
     * Clear and resize the cache
     *
     * @param maxSize max size of the cache
     */
    public void clearAndResize(int maxSize) {
        clear();
        cache = new LruCache<>(maxSize);
    }

}
