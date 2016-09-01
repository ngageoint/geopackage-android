package mil.nga.geopackage.db;

/**
 * SQL Utility methods
 *
 * @author osbornb
 * @since 1.3.1
 */
public class SQLUtils {

    /**
     * Wrap the name in double quotes
     *
     * @param name name
     * @return quoted name
     */
    public static String quoteWrap(String name) {
        String quoteName = null;
        if (name != null) {
            quoteName = "\"" + name + "\"";
        }
        return quoteName;
    }

    /**
     * Wrap the names in double quotes
     *
     * @param names names
     * @return quoted names
     */
    public static String[] quoteWrap(String[] names) {
        String[] quoteNames = null;
        if (names != null) {
            quoteNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                quoteNames[i] = quoteWrap(names[i]);
            }
        }
        return quoteNames;
    }

}
