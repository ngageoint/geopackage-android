package mil.nga.geopackage.tiles.user;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;

import java.io.IOException;

import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.user.UserRow;

/**
 * Tile Row containing the values from a single cursor row
 *
 * @author osbornb
 */
public class TileRow extends UserRow<TileColumn, TileTable> {

    /**
     * Constructor
     *
     * @param table       tile table
     * @param columns     columns
     * @param columnTypes column types
     * @param values      values
     * @since 3.5.0
     */
    TileRow(TileTable table, TileColumns columns, int[] columnTypes, Object[] values) {
        super(table, columns, columnTypes, values);
    }

    /**
     * Constructor to create an empty row
     *
     * @param table
     */
    TileRow(TileTable table) {
        super(table);
    }

    /**
     * Copy Constructor
     *
     * @param tileRow tile row to copy
     * @since 1.4.0
     */
    public TileRow(TileRow tileRow) {
        super(tileRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileColumns getColumns() {
        return (TileColumns) super.getColumns();
    }

    /**
     * Get the zoom level column index
     *
     * @return zoom level column index
     */
    public int getZoomLevelColumnIndex() {
        return getColumns().getZoomLevelIndex();
    }

    /**
     * Get the zoom level column
     *
     * @return zoom level column
     */
    public TileColumn getZoomLevelColumn() {
        return getColumns().getZoomLevelColumn();
    }

    /**
     * Get the zoom level
     *
     * @return zoom level
     */
    public long getZoomLevel() {
        return ((Number) getValue(getZoomLevelColumnIndex())).longValue();
    }

    /**
     * Set the zoom level
     *
     * @param zoomLevel zoom level
     */
    public void setZoomLevel(long zoomLevel) {
        setValue(getZoomLevelColumnIndex(), zoomLevel);
    }

    /**
     * Get the tile column column index
     *
     * @return tile column column index
     */
    public int getTileColumnColumnIndex() {
        return getColumns().getTileColumnIndex();
    }

    /**
     * Get the tile column column
     *
     * @return tile column column
     */
    public TileColumn getTileColumnColumn() {
        return getColumns().getTileColumnColumn();
    }

    /**
     * Get the tile column
     *
     * @return tile column
     */
    public long getTileColumn() {
        return ((Number) getValue(getTileColumnColumnIndex())).longValue();
    }

    /**
     * Set the tile column
     *
     * @param tileColumn tile column
     */
    public void setTileColumn(long tileColumn) {
        setValue(getTileColumnColumnIndex(), tileColumn);
    }

    /**
     * Get the tile row column index
     *
     * @return tile row column index
     */
    public int getTileRowColumnIndex() {
        return getColumns().getTileRowIndex();
    }

    /**
     * Get the tile row column
     *
     * @return tile column
     */
    public TileColumn getTileRowColumn() {
        return getColumns().getTileRowColumn();
    }

    /**
     * Get the tile row
     *
     * @return tile row
     */
    public long getTileRow() {
        return ((Number) getValue(getTileRowColumnIndex())).longValue();
    }

    /**
     * Set the tile row
     *
     * @param tileRow tile row
     */
    public void setTileRow(long tileRow) {
        setValue(getTileRowColumnIndex(), tileRow);
    }

    /**
     * Get the tile data column index
     *
     * @return tile data column index
     */
    public int getTileDataColumnIndex() {
        return getColumns().getTileDataIndex();
    }

    /**
     * Get the tile data column
     *
     * @return tile data column
     */
    public TileColumn getTileDataColumn() {
        return getColumns().getTileDataColumn();
    }

    /**
     * Get the tile data
     *
     * @return tile data bytes
     */
    public byte[] getTileData() {
        return (byte[]) getValue(getTileDataColumnIndex());
    }

    /**
     * Set the tile data
     *
     * @param tileData tile data bytes
     */
    public void setTileData(byte[] tileData) {
        setValue(getTileDataColumnIndex(), tileData);
    }

    /**
     * Get the tile data bitmap
     *
     * @return tile data bitmap
     */
    public Bitmap getTileDataBitmap() {
        return getTileDataBitmap(null);
    }

    /**
     * Get the tile data bitmap with decoding options
     *
     * @param options bitmap options
     * @return tile data bitmap
     */
    public Bitmap getTileDataBitmap(Options options) {
        return BitmapConverter.toBitmap(getTileData(), options);
    }

    /**
     * Set the tile data from a full quality bitmap
     *
     * @param bitmap tile bitmap
     * @param format compress format
     * @throws IOException upon failure
     */
    public void setTileData(Bitmap bitmap, CompressFormat format)
            throws IOException {
        setTileData(bitmap, format, 100);
    }

    /**
     * Set the tile data from a bitmap
     *
     * @param bitmap  tile bitmap
     * @param format  compress format
     * @param quality quality
     * @throws IOException upon failure
     */
    public void setTileData(Bitmap bitmap, CompressFormat format, int quality)
            throws IOException {
        byte[] tileData = BitmapConverter.toBytes(bitmap, format, quality);
        setTileData(tileData);
    }

    /**
     * Copy the row
     *
     * @return row copy
     * @since 3.0.1
     */
    public TileRow copy() {
        return new TileRow(this);
    }

}
