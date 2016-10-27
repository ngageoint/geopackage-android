package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Core SQL Utility methods
 *
 * @author osbornb
 * @since 1.3.1
 */
public class SQLUtils {

    /**
     * Wrap the content values names in quotes
     *
     * @param values content values
     * @return quoted content values
     */
    public static ContentValues quoteWrap(ContentValues values) {
        ContentValues quoteValues = null;
        if (values != null) {

            Map<String, Object> quoteMap = new HashMap<>();
            for (Map.Entry<String, Object> value : values.valueSet()) {
                quoteMap.put(CoreSQLUtils.quoteWrap(value.getKey()), value.getValue());
            }

            Parcel parcel = Parcel.obtain();
            parcel.writeMap(quoteMap);
            parcel.setDataPosition(0);
            quoteValues = ContentValues.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        }

        return quoteValues;
    }

}
