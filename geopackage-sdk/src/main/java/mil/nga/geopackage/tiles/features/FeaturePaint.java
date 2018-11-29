package mil.nga.geopackage.tiles.features;

import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

/**
 * Paint objects for drawing the different types for a single feature
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeaturePaint {

    /**
     * Map between draw types and paint objects
     */
    private Map<FeatureDrawType, Paint> featurePaints = new HashMap<>();

    /**
     * Constructor
     */
    public FeaturePaint() {

    }

    /**
     * Get the paint for the draw type
     *
     * @param type draw type
     * @return paint
     */
    public Paint getPaint(FeatureDrawType type) {
        return featurePaints.get(type);
    }

    /**
     * Set the paint for the draw type
     *
     * @param type  draw type
     * @param paint paint
     */
    public void setPaint(FeatureDrawType type, Paint paint) {
        featurePaints.put(type, paint);
    }

}
