package mil.nga.geopackage.extension.nga.style;

import android.graphics.BitmapFactory;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.related.media.MediaRow;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * Icon Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 3.2.0
 */
public class IconRow extends MediaRow {

    /**
     * Table icon flag
     */
    private boolean tableIcon;

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
     * Is a table icon
     *
     * @return table icon flag
     * @since 3.5.0
     */
    public boolean isTableIcon() {
        return tableIcon;
    }

    /**
     * Set table icon flag
     *
     * @param tableIcon table icon flag
     * @since 3.5.0
     */
    public void setTableIcon(boolean tableIcon) {
        this.tableIcon = tableIcon;
    }

    /**
     * Get the name column index
     *
     * @return name column index
     */
    public int getNameColumnIndex() {
        return getColumns().getColumnIndex(IconTable.COLUMN_NAME);
    }

    /**
     * Get the name column
     *
     * @return name column
     */
    public UserCustomColumn getNameColumn() {
        return getColumns().getColumn(IconTable.COLUMN_NAME);
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
        return getColumns().getColumnIndex(IconTable.COLUMN_DESCRIPTION);
    }

    /**
     * Get the description column
     *
     * @return description column
     */
    public UserCustomColumn getDescriptionColumn() {
        return getColumns().getColumn(IconTable.COLUMN_DESCRIPTION);
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
        return getColumns().getColumnIndex(IconTable.COLUMN_WIDTH);
    }

    /**
     * Get the width column
     *
     * @return width column
     */
    public UserCustomColumn getWidthColumn() {
        return getColumns().getColumn(IconTable.COLUMN_WIDTH);
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
     * Get the width or derived width from the icon data and scaled as needed for the height
     *
     * @return derived width
     */
    public double getDerivedWidth() {

        Double width = getWidth();

        if (width == null) {
            width = getDerivedDimensions()[0];
        }

        return width;
    }

    /**
     * Get the height column index
     *
     * @return height column index
     */
    public int getHeightColumnIndex() {
        return getColumns().getColumnIndex(IconTable.COLUMN_HEIGHT);
    }

    /**
     * Get the height column
     *
     * @return height column
     */
    public UserCustomColumn getHeightColumn() {
        return getColumns().getColumn(IconTable.COLUMN_HEIGHT);
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
     * Get the height or derived height from the icon data and scaled as needed for the width
     *
     * @return derived height
     */
    public double getDerivedHeight() {

        Double height = getHeight();

        if (height == null) {
            height = getDerivedDimensions()[1];
        }

        return height;
    }

    /**
     * Get the derived width and height from the values and icon data, scaled as
     * needed
     *
     * @return derived dimensions array with two values, width at index 0, height at index 1
     */
    public double[] getDerivedDimensions() {

        Double width = getWidth();
        Double height = getHeight();

        if (width == null || height == null) {

            BitmapFactory.Options options = getDataBounds();
            int dataWidth = options.outWidth;
            int dataHeight = options.outHeight;

            if (width == null) {
                width = (double) dataWidth;

                if (height != null) {
                    width *= (height / dataHeight);
                }
            }

            if (height == null) {
                height = (double) dataHeight;

                if (width != null) {
                    height *= (width / dataWidth);
                }
            }

        }

        return new double[]{width, height};
    }

    /**
     * Get the anchor u column index
     *
     * @return anchor u column index
     */
    public int getAnchorUColumnIndex() {
        return getColumns().getColumnIndex(IconTable.COLUMN_ANCHOR_U);
    }

    /**
     * Get the anchor u column
     *
     * @return anchor u column
     */
    public UserCustomColumn getAnchorUColumn() {
        return getColumns().getColumn(IconTable.COLUMN_ANCHOR_U);
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
        return getColumns().getColumnIndex(IconTable.COLUMN_ANCHOR_V);
    }

    /**
     * Get the anchor v column
     *
     * @return anchor v column
     */
    public UserCustomColumn getAnchorVColumn() {
        return getColumns().getColumn(IconTable.COLUMN_ANCHOR_V);
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
