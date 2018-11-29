package mil.nga.geopackage.tiles.features;

import android.annotation.TargetApi;
import android.graphics.Paint;
import android.util.LruCache;

import mil.nga.geopackage.extension.style.StyleRow;

/**
 * Feature Paint Cache of Paint objects for each feature id and draw type
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeaturePaintCache {

    /**
     * Default max number of feature style paints to maintain
     */
    public static final int DEFAULT_STYLE_PAINT_CACHE_SIZE = 100;

    /**
     * Feature paint cache
     */
    private final LruCache<Long, FeaturePaint> paintCache;

    /**
     * Constructor
     */
    public FeaturePaintCache() {
        this(DEFAULT_STYLE_PAINT_CACHE_SIZE);
    }

    /**
     * Constructor
     */
    public FeaturePaintCache(int size) {
        paintCache = new LruCache<>(DEFAULT_STYLE_PAINT_CACHE_SIZE);
    }

    /**
     * Clear the cache
     */
    public void clear() {
        paintCache.evictAll();
    }

    /**
     * Resize the cache
     *
     * @param maxSize max size
     */
    @TargetApi(21)
    public void resize(int maxSize) {
        paintCache.resize(maxSize);
    }

    /**
     * Get the feature paint for the style row
     *
     * @param styleRow style row
     * @return feature paint
     */
    public FeaturePaint getFeaturePaint(StyleRow styleRow) {
        return getFeaturePaint(styleRow.getId());
    }

    /**
     * Get the feature paint for the style row id
     *
     * @param styleId style row id
     * @return feature paint
     */
    public FeaturePaint getFeaturePaint(long styleId) {
        return paintCache.get(styleId);
    }

    /**
     * Get the paint for the style row and draw type
     *
     * @param styleRow style row
     * @param type     feature draw type
     * @return paint
     */
    public Paint getPaint(StyleRow styleRow, FeatureDrawType type) {
        return getPaint(styleRow.getId(), type);
    }

    /**
     * Get the paint for the style row id and draw type
     *
     * @param styleId style row id
     * @param type    feature draw type
     * @return paint
     */
    public Paint getPaint(long styleId, FeatureDrawType type) {
        Paint paint = null;
        FeaturePaint featurePaint = getFeaturePaint(styleId);
        if (featurePaint != null) {
            paint = featurePaint.getPaint(type);
        }
        return paint;
    }

    /**
     * Set the paint for the style id and draw type
     *
     * @param styleRow style row
     * @param type     feature draw type
     * @param paint    paint
     */
    public void setPaint(StyleRow styleRow, FeatureDrawType type, Paint paint) {
        setPaint(styleRow.getId(), type, paint);
    }

    /**
     * Set the paint for the style id and draw type
     *
     * @param styleId style row id
     * @param type    feature draw type
     * @param paint   paint
     */
    public void setPaint(long styleId, FeatureDrawType type, Paint paint) {
        FeaturePaint featurePaint = getFeaturePaint(styleId);
        if (featurePaint == null) {
            featurePaint = new FeaturePaint();
            paintCache.put(styleId, featurePaint);
        }
        featurePaint.setPaint(type, paint);
    }

}
