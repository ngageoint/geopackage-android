package mil.nga.geopackage.features.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Feature Index Results Feature Row list implementation
 *
 * @author osbornb
 * @since 2.0.0
 */
public class FeatureIndexListResults implements FeatureIndexResults {

    /**
     * List of feature rows
     */
    private final List<FeatureRow> rows = new ArrayList<>();

    /**
     * Constructor
     */
    public FeatureIndexListResults() {
    }

    /**
     * Constructor
     *
     * @param row feature row
     */
    public FeatureIndexListResults(FeatureRow row) {
        addRow(row);
    }

    /**
     * Constructor
     *
     * @param rows feature rows
     */
    public FeatureIndexListResults(List<FeatureRow> rows) {
        addRows(rows);
    }

    /**
     * Add a feature row
     *
     * @param row feature row
     */
    public void addRow(FeatureRow row) {
        rows.add(row);
    }

    /**
     * Add feature rows
     *
     * @param rows feature rows
     */
    public void addRows(List<FeatureRow> rows) {
        this.rows.addAll(rows);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return rows.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        return rows.iterator();
    }

}
