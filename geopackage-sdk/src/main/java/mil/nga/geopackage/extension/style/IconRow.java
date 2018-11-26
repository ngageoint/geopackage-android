package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.related.media.MediaRow;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * Icon Row containing the values from a single result set row
 *
 * @author osbornb
 * @since 3.1.1
 */
public class IconRow extends MediaRow {

    /**
     * Constructor to create an empty row
     */
    public IconRow() {
        this(new IconTable());
    }

    /**
     * Constructor to create an empty row
     *
     * @param table icon table
     */
    public IconRow(IconTable table) {
        super(table);
    }

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    public IconRow(UserCustomRow userCustomRow) {
        super(userCustomRow);
    }

    /**
     * Copy Constructor
     *
     * @param iconRow icon row to copy
     */
    public IconRow(IconRow iconRow) {
        super(iconRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IconTable getTable() {
        return (IconTable) super.getTable();
    }

    /**
     * Get the name column index
     *
     * @return name column index
     */
    public int getNameColumnIndex() {
        return getTable().getNameColumnIndex();
    }

    /**
     * Get the name column
     *
     * @return name column
     */
    public UserCustomColumn getNameColumn() {
        return getTable().getNameColumn();
    }

    /**
     * Get the name
     *
     * @return name
     */
    public String getName() {
        return getValueString(getNameColumnIndex());
    }

    /**
     * Set the name
     *
     * @param name Feature Icon name
     */
    public void setName(String name) {
        setValue(getNameColumnIndex(), name);
    }

    /**
     * Get the description column index
     *
     * @return description column index
     */
    public int getDescriptionColumnIndex() {
        return getTable().getDescriptionColumnIndex();
    }

    /**
     * Get the description column
     *
     * @return description column
     */
    public UserCustomColumn getDescriptionColumn() {
        return getTable().getDescriptionColumn();
    }

    /**
     * Get the description
     *
     * @return description
     */
    public String getDescription() {
        return getValueString(getDescriptionColumnIndex());
    }

    /**
     * Set the description
     *
     * @param description Feature Icon description
     */
    public void setDescription(String description) {
        setValue(getDescriptionColumnIndex(), description);
    }

    /**
     * Get the width column index
     *
     * @return width column index
     */
    public int getWidthColumnIndex() {
        return getTable().getWidthColumnIndex();
    }

    /**
     * Get the width column
     *
     * @return width column
     */
    public UserCustomColumn getWidthColumn() {
        return getTable().getWidthColumn();
    }

    /**
     * Get the width
     *
     * @return width
     */
    public Double getWidth() {
        return (Double) getValue(getWidthColumnIndex());
    }

    /**
     * Set the width
     *
     * @param width Icon display width, when null use actual icon width
     */
    public void setWidth(Double width) {
        if (width != null && width < 0.0) {
            throw new GeoPackageException(
                    "Width must be greater than or equal to 0.0, invalid value: "
                            + width);
        }
        setValue(getWidthColumnIndex(), width);
    }

    /**
     * Get the height column index
     *
     * @return height column index
     */
    public int getHeightColumnIndex() {
        return getTable().getHeightColumnIndex();
    }

    /**
     * Get the height column
     *
     * @return height column
     */
    public UserCustomColumn getHeightColumn() {
        return getTable().getHeightColumn();
    }

    /**
     * Get the height
     *
     * @return height
     */
    public Double getHeight() {
        return (Double) getValue(getHeightColumnIndex());
    }

    /**
     * Set the height
     *
     * @param height Icon display height, when null use actual icon height
     */
    public void setHeight(Double height) {
        if (height != null && height < 0.0) {
            throw new GeoPackageException(
                    "Height must be greater than or equal to 0.0, invalid value: "
                            + height);
        }
        setValue(getHeightColumnIndex(), height);
    }

    /**
     * Get the anchor u column index
     *
     * @return anchor u column index
     */
    public int getAnchorUColumnIndex() {
        return getTable().getAnchorUColumnIndex();
    }

    /**
     * Get the anchor u column
     *
     * @return anchor u column
     */
    public UserCustomColumn getAnchorUColumn() {
        return getTable().getAnchorUColumn();
    }

    /**
     * Get the anchor u
     *
     * @return anchor u
     */
    public Double getAnchorU() {
        return (Double) getValue(getAnchorUColumnIndex());
    }

    /**
     * Set the anchor u
     *
     * @param anchor UV Mapping horizontal anchor distance inclusively between 0.0
     *               and 1.0 from the left edge, when null assume 0.5 (middle of
     *               icon)
     */
    public void setAnchorU(Double anchor) {
        validateAnchor(anchor);
        setValue(getAnchorUColumnIndex(), anchor);
    }

    /**
     * Get the anchor u value or the default value of 0.5
     *
     * @return anchor u value
     */
    public double getAnchorUOrDefault() {
        Double anchorU = getAnchorU();
        if (anchorU == null) {
            anchorU = 0.5;
        }
        return anchorU;
    }

    /**
     * Get the anchor v column index
     *
     * @return anchor v column index
     */
    public int getAnchorVColumnIndex() {
        return getTable().getAnchorVColumnIndex();
    }

    /**
     * Get the anchor v column
     *
     * @return anchor v column
     */
    public UserCustomColumn getAnchorVColumn() {
        return getTable().getAnchorVColumn();
    }

    /**
     * Get the anchor v
     *
     * @return anchor v
     */
    public Double getAnchorV() {
        return (Double) getValue(getAnchorVColumnIndex());
    }

    /**
     * Set the anchor v
     *
     * @param anchor UV Mapping vertical anchor distance inclusively between 0.0
     *               and 1.0 from the top edge, when null assume 1.0 (bottom of
     *               icon)
     */
    public void setAnchorV(Double anchor) {
        validateAnchor(anchor);
        setValue(getAnchorVColumnIndex(), anchor);
    }

    /**
     * Get the anchor v value or the default value of 1.0
     *
     * @return anchor v value
     */
    public double getAnchorVOrDefault() {
        Double anchorV = getAnchorV();
        if (anchorV == null) {
            anchorV = 1.0;
        }
        return anchorV;
    }

    /**
     * Validate the anchor value
     *
     * @param anchor anchor
     */
    private void validateAnchor(Double anchor) {
        if (anchor != null && (anchor < 0.0 || anchor > 1.0)) {
            throw new GeoPackageException(
                    "Anchor must be set inclusively between 0.0 and 1.0, invalid value: "
                            + anchor);
        }
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public IconRow copy() {
        return new IconRow(this);
    }

}
