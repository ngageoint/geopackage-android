package mil.nga.geopackage.db;

/**
 * Feature Indexer Id query with nested SQL and arguments
 *
 * @author osbornb
 * @since 3.3.1
 */
public class FeatureIndexerIdQuery {

    /**
     * Nested SQL in statement
     */
    private final String sql;

    /**
     * Query arguments
     */
    private final String[] args;

    /**
     * Argument add index
     */
    private int argumentIndex = 0;

    /**
     * Constructor
     *
     * @param count result count
     */
    public FeatureIndexerIdQuery(int count) {
        args = new String[count];
        StringBuilder sqlBuilder = new StringBuilder();
        if (count > 0) {
            sqlBuilder.append("?");
            for (int i = 1; i < count; i++) {
                sqlBuilder.append(", ?");
            }
        }
        sql = sqlBuilder.toString();
    }

    /**
     * Add an id argument
     *
     * @param id id value
     */
    public void addArgument(long id) {
        addArgument(String.valueOf(id));
    }

    /**
     * Add an id argument
     *
     * @param id id value
     */
    public void addArgument(String id) {
        args[argumentIndex++] = id;
    }

    /**
     * Get the SQL statement
     *
     * @return SQL
     */
    public String getSql() {
        return sql;
    }

    /**
     * Get the arguments
     *
     * @return args
     */
    public String[] getArgs() {
        return args;
    }

}
