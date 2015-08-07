package mil.nga.geopackage.io;

import android.content.Context;

import java.io.File;

/**
 * Input / Output Context utility methods
 *
 * @author osbornb
 */
public class ContextIOUtils {

    /**
     * Get the internal storage file for the file path
     *
     * @param context
     * @param filePath
     * @return
     */
    public static File getInternalFile(Context context, String filePath) {
        File internalFile = null;
        if (filePath != null) {
            internalFile = new File(context.getFilesDir(), filePath);
        } else {
            internalFile = context.getFilesDir();
        }
        return internalFile;
    }

    /**
     * Get the internal storage patch for the file path
     *
     * @param context
     * @param filePath
     * @return
     */
    public static String getInternalFilePath(Context context, String filePath) {
        return getInternalFile(context, filePath).getAbsolutePath();
    }

}
