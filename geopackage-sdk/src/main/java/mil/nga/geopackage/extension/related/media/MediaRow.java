package mil.nga.geopackage.extension.related.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Media Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 3.0.1
 */
public class MediaRow extends UserCustomRow {

    /**
     * Constructor to create an empty row
     *
     * @param table media table
     */
    protected MediaRow(MediaTable table) {
        super(table);
    }

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    public MediaRow(UserCustomRow userCustomRow) {
        super(userCustomRow.getTable(), userCustomRow.getRowColumnTypes(),
                userCustomRow.getValues());
    }

    /**
     * Copy Constructor
     *
     * @param mediaRow media row to copy
     */
    public MediaRow(MediaRow mediaRow) {
        super(mediaRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaTable getTable() {
        return (MediaTable) super.getTable();
    }

    /**
     * Get the id column index
     *
     * @return id column index
     */
    public int getIdColumnIndex() {
        return getTable().getIdColumnIndex();
    }

    /**
     * Get the id column
     *
     * @return id column
     */
    public UserCustomColumn getIdColumn() {
        return getTable().getIdColumn();
    }

    /**
     * Get the id
     *
     * @return id
     */
    public long getId() {
        return ((Number) getValue(getIdColumnIndex())).longValue();
    }

    /**
     * Get the data column index
     *
     * @return data column index
     */
    public int getDataColumnIndex() {
        return getTable().getDataColumnIndex();
    }

    /**
     * Get the data column
     *
     * @return data column
     */
    public UserCustomColumn getDataColumn() {
        return getTable().getDataColumn();
    }

    /**
     * Get the data
     *
     * @return data
     */
    public byte[] getData() {
        return (byte[]) getValue(getDataColumnIndex());
    }

    /**
     * Set the data
     *
     * @param data data
     */
    public void setData(byte[] data) {
        setValue(getDataColumnIndex(), data);
    }

    /**
     * Read the data bounds without allocating pixel memory.
     * Access values using:
     * {@link BitmapFactory.Options#outWidth},
     * {@link BitmapFactory.Options#outHeight},
     * {@link BitmapFactory.Options#outMimeType},
     * {@link BitmapFactory.Options#outColorSpace},
     * and {@link BitmapFactory.Options#outConfig}
     *
     * @return bounds options
     * @since 3.1.1
     */
    public BitmapFactory.Options getDataBounds() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        byte[] data = getData();
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return options;
    }

    /**
     * Get the data bitmap
     *
     * @return data bitmap
     * @since 3.1.1
     */
    public Bitmap getDataBitmap() {
        return getDataBitmap(null);
    }

    /**
     * Get the data bitmap with decoding options
     *
     * @param options bitmap options
     * @return data bitmap
     * @since 3.1.1
     */
    public Bitmap getDataBitmap(BitmapFactory.Options options) {
        return BitmapConverter.toBitmap(getData(), options);
    }

    /**
     * Set the data from a full quality bitmap
     *
     * @param bitmap bitmap
     * @param format compress format
     * @throws IOException upon failure
     * @since 3.1.1
     */
    public void setData(Bitmap bitmap, Bitmap.CompressFormat format)
            throws IOException {
        setData(bitmap, format, 100);
    }

    /**
     * Set the data from a bitmap
     *
     * @param bitmap  bitmap
     * @param format  compress format
     * @param quality quality
     * @throws IOException upon failure
     * @since 3.1.1
     */
    public void setData(Bitmap bitmap, Bitmap.CompressFormat format, int quality)
            throws IOException {
        setData(BitmapConverter.toBytes(bitmap, format, quality));
    }

    /**
     * Get the content type column index
     *
     * @return content type column index
     */
    public int getContentTypeColumnIndex() {
        return getTable().getContentTypeColumnIndex();
    }

    /**
     * Get the content type column
     *
     * @return content type column
     */
    public UserCustomColumn getContentTypeColumn() {
        return getTable().getContentTypeColumn();
    }

    /**
     * Get the content type
     *
     * @return content type
     */
    public String getContentType() {
        return getValue(getContentTypeColumnIndex()).toString();
    }

    /**
     * Set the content type
     *
     * @param contentType content type
     */
    public void setContentType(String contentType) {
        setValue(getContentTypeColumnIndex(), contentType);
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public MediaRow copy() {
        return new MediaRow(this);
    }

}
