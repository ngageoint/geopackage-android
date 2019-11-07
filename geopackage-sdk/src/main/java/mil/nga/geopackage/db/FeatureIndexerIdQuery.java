package mil.nga.geopackage.db;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Feature Indexer Id query with nested SQL and arguments
 *
 * @author osbornb
 * @since 3.3.1
 */
public class FeatureIndexerIdQuery {

    /**
     * Set of ids
     */
    private Set<Long> ids = new LinkedHashSet<>();

    /**
     * Constructor
     */
    public FeatureIndexerIdQuery() {

    }

    /**
     * Add an id argument
     *
     * @param id id value
     */
    public void addArgument(long id) {
        ids.add(id);
    }

    /**
     * Get the number of ids
     *
     * @return count
     */
    public int getCount() {
        return ids.size();
    }

    /**
     * Get the set of ids
     *
     * @return ids
     */
    public Set<Long> getIds() {
        return ids;
    }

    /**
     * Check if the query has the id
     *
     * @param id id
     * @return true if has id
     */
    public boolean hasId(long id) {
        return ids.contains(id);
    }

    /**
     * Check if the total number of query arguments is above the maximum allowed in a single query
     *
     * @return true if above the maximum allowed query arguments
     */
    public boolean aboveMaxArguments() {
        return aboveMaxArguments(0);
    }

    /**
     * Check if the total number of query arguments is above the maximum allowed in a single query
     *
     * @param additionalArgs additional arguments
     * @return true if above the maximum allowed query arguments
     */
    public boolean aboveMaxArguments(String[] additionalArgs) {
        int additionalArgCount = 0;
        if (additionalArgs != null) {
            additionalArgCount += additionalArgs.length;
        }
        return aboveMaxArguments(additionalArgCount);
    }

    /**
     * Check if the total number of query arguments is above the maximum allowed in a single query
     *
     * @param additionalArgs additional argument count
     * @return true if above the maximum allowed query arguments
     */
    public boolean aboveMaxArguments(int additionalArgs) {
        return getCount() + additionalArgs > 999;
    }

    /**
     * Get the SQL statement
     *
     * @return SQL
     */
    public String getSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        int count = getCount();
        if (count > 0) {
            sqlBuilder.append("?");
            for (int i = 1; i < count; i++) {
                sqlBuilder.append(", ?");
            }
        }
        return sqlBuilder.toString();
    }

    /**
     * Get the arguments
     *
     * @return args
     */
    public String[] getArgs() {
        String[] args = new String[getCount()];
        int index = 0;
        for (long id : ids) {
            args[index++] = String.valueOf(id);
        }
        return args;
    }

}
