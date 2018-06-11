package mil.nga.geopackage.extension.related.media;

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
    MediaRow(MediaTable table) {
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
