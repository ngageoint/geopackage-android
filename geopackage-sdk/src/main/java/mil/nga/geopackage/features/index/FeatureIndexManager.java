package mil.nga.geopackage.features.index;

import android.content.Context;
import android.database.Cursor;

import com.j256.ormlite.dao.CloseableIterator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.FeatureIndexer;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.projection.Projection;
import mil.nga.wkb.geom.GeometryEnvelope;

/**
 * Feature Index Manager to manage indexing of feature geometries in both Android metadata and
 * within a GeoPackage using the Geometry Index Extension
 *
 * @author osbornb
 * @see mil.nga.geopackage.db.FeatureIndexer
 * @see mil.nga.geopackage.extension.index.FeatureTableIndex
 * @since 1.1.0
 */
public class FeatureIndexManager {

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Feature Table Index, for indexing within a GeoPackage extension
     */
    private final FeatureTableIndex featureTableIndex;

    /**
     * Feature Indexer, for indexing within Android metadata
     */
    private final FeatureIndexer featureIndexer;

    /**
     * Ordered set of index locations to check in order when checking if features are indexed
     * and when querying for features
     */
    private Set<FeatureIndexType> indexLocationQueryOrder = new LinkedHashSet<>();

    /**
     * Index location, when set index calls without specifying a location go to this location
     */
    private FeatureIndexType indexLocation;

    /**
     * Constructor
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature DAO
     */
    public FeatureIndexManager(Context context, GeoPackage geoPackage, FeatureDao featureDao) {
        this.featureDao = featureDao;
        featureTableIndex = new FeatureTableIndex(geoPackage, featureDao);
        featureIndexer = new FeatureIndexer(context, featureDao);

        // Set the default indexed check and query order
        indexLocationQueryOrder.add(FeatureIndexType.GEOPACKAGE);
        indexLocationQueryOrder.add(FeatureIndexType.METADATA);
    }

    /**
     * Close the index connections
     */
    public void close() {
        featureTableIndex.close();
        featureIndexer.close();
    }

    /**
     * Get the feature DAO
     *
     * @return feature DAO
     * @since 1.2.5
     */
    public FeatureDao getFeatureDao() {
        return featureDao;
    }

    /**
     * Get the feature table index, used to index inside the GeoPackage as an extension
     *
     * @return feature table index
     */
    public FeatureTableIndex getFeatureTableIndex() {
        return featureTableIndex;
    }

    /**
     * Get the feature indexer, used to index in metadata tables
     *
     * @return feature indexer
     */
    public FeatureIndexer getFeatureIndexer() {
        return featureIndexer;
    }

    /**
     * Get the index location
     *
     * @return index location or null if not set
     */
    public FeatureIndexType getIndexLocation() {
        return indexLocation;
    }

    /**
     * Prioritize the query location order.  All types are placed at the front of the query order
     * in the order they are given. Omitting a location leaves it at it's current priority location.
     *
     * @param types feature index types
     */
    public void prioritizeQueryLocation(FeatureIndexType... types) {
        // Create a new query order set
        Set<FeatureIndexType> queryOrder = new LinkedHashSet<>();
        for (FeatureIndexType type : types) {
            queryOrder.add(type);
        }
        // Add any locations not provided to this method
        queryOrder.addAll(indexLocationQueryOrder);
        // Update the query order set
        indexLocationQueryOrder = queryOrder;
    }

    /**
     * Set the index location
     *
     * @param indexLocation feature index type
     */
    public void setIndexLocation(FeatureIndexType indexLocation) {
        this.indexLocation = indexLocation;
    }

    /**
     * Set the GeoPackage Progress
     *
     * @param progress GeoPackage progress
     */
    public void setProgress(GeoPackageProgress progress) {
        featureTableIndex.setProgress(progress);
        featureIndexer.setProgress(progress);
    }

    /**
     * Index the feature table if needed, using the set index location
     *
     * @return count
     */
    public int index() {
        return index(verifyIndexLocation(), false);
    }

    /**
     * Index the feature tables if needed for the index types
     *
     * @param types feature index types
     * @return largest count of indexed features
     * @since 2.0.0
     */
    public int index(List<FeatureIndexType> types) {
        int count = 0;
        for (FeatureIndexType type : types) {
            int typeCount = index(type);
            count = Math.max(count, typeCount);
        }
        return count;
    }

    /**
     * Index the feature table if needed
     *
     * @param type index location type
     * @return count
     */
    public int index(FeatureIndexType type) {
        return index(type, false);
    }

    /**
     * Index the feature table, using the set index location
     *
     * @param force true to force re-indexing
     * @return count
     */
    public int index(boolean force) {
        return index(verifyIndexLocation(), force);
    }

    /**
     * Index the feature tables for the index types
     *
     * @param force true to force re-indexing
     * @param types feature index types
     * @return largest count of indexed features
     * @since 2.0.0
     */
    public int index(boolean force, List<FeatureIndexType> types) {
        int count = 0;
        for (FeatureIndexType type : types) {
            int typeCount = index(type, force);
            count = Math.max(count, typeCount);
        }
        return count;
    }

    /**
     * Index the feature table
     *
     * @param type  index location type
     * @param force true to force re-indexing
     * @return count
     */
    public int index(FeatureIndexType type, boolean force) {
        if (type == null) {
            throw new GeoPackageException("FeatureIndexType is required to index");
        }
        int count = 0;
        switch (type) {
            case GEOPACKAGE:
                count = featureTableIndex.index(force);
                break;
            case METADATA:
                count = featureIndexer.index(force);
                break;
            default:
                throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
        }
        return count;
    }

    /**
     * Index the feature row, using the set index location.
     * This method assumes that indexing has been completed and
     * maintained as the last indexed time is updated.
     *
     * @param row feature row to index
     * @return true if indexed
     */
    public boolean index(FeatureRow row) {
        return index(verifyIndexLocation(), row);
    }

    /**
     * Index the feature row for the index types.
     * This method assumes that indexing has been completed and
     * maintained as the last indexed time is updated.
     *
     * @param row   feature row to index
     * @param types feature index types
     * @return true if indexed from any type
     * @since 2.0.0
     */
    public boolean index(FeatureRow row, List<FeatureIndexType> types) {
        boolean indexed = false;
        for (FeatureIndexType type : types) {
            if (index(type, row)) {
                indexed = true;
            }
        }
        return indexed;
    }

    /**
     * Index the feature row. This method assumes that indexing has been completed and
     * maintained as the last indexed time is updated.
     *
     * @param type index location type
     * @param row  feature row to index
     * @return true if indexed
     */
    public boolean index(FeatureIndexType type, FeatureRow row) {
        boolean indexed = false;
        if (type == null) {
            throw new GeoPackageException("FeatureIndexType is required to index");
        }
        switch (type) {
            case GEOPACKAGE:
                indexed = featureTableIndex.index(row);
                break;
            case METADATA:
                indexed = featureIndexer.index(row);
                break;
            default:
                throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
        }
        return indexed;
    }

    /**
     * Delete the feature index
     *
     * @return true if deleted
     */
    public boolean deleteIndex() {
        return deleteIndex(verifyIndexLocation());
    }

    /**
     * Delete the feature index from the index types
     *
     * @param types feature index types
     * @return true if deleted from any type
     * @since 2.0.0
     */
    public boolean deleteIndex(List<FeatureIndexType> types) {
        boolean deleted = false;
        for (FeatureIndexType type : types) {
            if (deleteIndex(type)) {
                deleted = true;
            }
        }
        return deleted;
    }

    /**
     * Delete the feature index
     *
     * @param type feature index type
     * @return true if deleted
     */
    public boolean deleteIndex(FeatureIndexType type) {
        if (type == null) {
            throw new GeoPackageException("FeatureIndexType is required to delete index");
        }
        boolean deleted = false;
        switch (type) {
            case GEOPACKAGE:
                deleted = featureTableIndex.deleteIndex();
                break;
            case METADATA:
                deleted = featureIndexer.deleteIndex();
                break;
            default:
                throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
        }
        return deleted;
    }

    /**
     * Delete the feature index for the feature row
     *
     * @param row feature row
     * @return true if deleted
     */
    public boolean deleteIndex(FeatureRow row) {
        return deleteIndex(verifyIndexLocation(), row);
    }

    /**
     * Delete the feature index for the feature row from the index types
     *
     * @param row   feature row
     * @param types feature index types
     * @return true if deleted from any type
     * @since 2.0.0
     */
    public boolean deleteIndex(FeatureRow row, List<FeatureIndexType> types) {
        boolean deleted = false;
        for (FeatureIndexType type : types) {
            if (deleteIndex(type, row)) {
                deleted = true;
            }
        }
        return deleted;
    }

    /**
     * Delete the feature index for the feature row
     *
     * @param type feature index type
     * @param row  feature row
     * @return true if deleted
     */
    public boolean deleteIndex(FeatureIndexType type, FeatureRow row) {
        return deleteIndex(type, row.getId());
    }

    /**
     * Delete the feature index for the geometry id
     *
     * @param geomId geometry id
     * @return true if deleted
     */
    public boolean deleteIndex(long geomId) {
        return deleteIndex(verifyIndexLocation(), geomId);
    }

    /**
     * Delete the feature index for the geometry id from the index types
     *
     * @param geomId geometry id
     * @param types  feature index types
     * @return true if deleted from any type
     * @since 2.0.0
     */
    public boolean deleteIndex(long geomId, List<FeatureIndexType> types) {
        boolean deleted = false;
        for (FeatureIndexType type : types) {
            if (deleteIndex(type, geomId)) {
                deleted = true;
            }
        }
        return deleted;
    }

    /**
     * Delete the feature index for the geometry id
     *
     * @param type   feature index type
     * @param geomId geometry id
     * @return true if deleted
     */
    public boolean deleteIndex(FeatureIndexType type, long geomId) {
        if (type == null) {
            throw new GeoPackageException("FeatureIndexType is required to delete index");
        }
        boolean deleted = false;
        switch (type) {
            case GEOPACKAGE:
                deleted = featureTableIndex.deleteIndex(geomId) > 0;
                break;
            case METADATA:
                deleted = featureIndexer.deleteIndex(geomId);
                break;
            default:
                throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
        }
        return deleted;
    }

    /**
     * Determine if the feature table is indexed
     *
     * @return true if indexed
     */
    public boolean isIndexed() {
        boolean indexed = false;
        for (FeatureIndexType type : indexLocationQueryOrder) {
            indexed = isIndexed(type);
            if (indexed) {
                break;
            }
        }
        return indexed;
    }

    /**
     * Is the feature table indexed in the provided type location
     *
     * @param type index location type
     * @return true if indexed
     */
    public boolean isIndexed(FeatureIndexType type) {
        boolean indexed = false;
        if (type == null) {
            indexed = isIndexed();
        } else {
            switch (type) {
                case GEOPACKAGE:
                    indexed = featureTableIndex.isIndexed();
                    break;
                case METADATA:
                    indexed = featureIndexer.isIndexed();
                    break;
                default:
                    throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
            }
        }
        return indexed;
    }

    /**
     * Get the indexed types that are currently indexed
     *
     * @return indexed types
     * @since 2.0.0
     */
    public List<FeatureIndexType> getIndexedTypes() {
        List<FeatureIndexType> indexed = new ArrayList<>();
        for (FeatureIndexType type : indexLocationQueryOrder) {
            if (isIndexed(type)) {
                indexed.add(type);
            }
        }
        return indexed;
    }

    /**
     * Get the date last indexed
     *
     * @return last indexed date or null
     */
    public Date getLastIndexed() {
        Date lastIndexed = null;
        for (FeatureIndexType type : indexLocationQueryOrder) {
            lastIndexed = getLastIndexed(type);
            if (lastIndexed != null) {
                break;
            }
        }
        return lastIndexed;
    }

    /**
     * Get the date last indexed
     *
     * @param type feature index type
     * @return last indexed date or null
     */
    public Date getLastIndexed(FeatureIndexType type) {
        Date lastIndexed = null;
        if (type == null) {
            lastIndexed = getLastIndexed();
        } else {
            switch (type) {
                case GEOPACKAGE:
                    lastIndexed = featureTableIndex.getLastIndexed();
                    break;
                case METADATA:
                    lastIndexed = featureIndexer.getLastIndexed();
                    break;
                default:
                    throw new GeoPackageException("Unsupported FeatureIndexType: " + type);
            }
        }
        return lastIndexed;
    }

    /**
     * Query for all feature index results
     *
     * @return feature index results, close when done
     */
    public FeatureIndexResults query() {
        FeatureIndexResults results = null;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                long count = featureTableIndex.count();
                CloseableIterator<GeometryIndex> geometryIndices = featureTableIndex.query();
                results = new FeatureIndexGeoPackageResults(featureTableIndex, count, geometryIndices);
                break;
            case METADATA:
                Cursor geometryMetadata = featureIndexer.query();
                results = new FeatureIndexMetadataResults(featureIndexer, geometryMetadata);
                break;
        }
        return results;
    }

    /**
     * Query for all feature index count
     *
     * @return count
     */
    public long count() {
        long count = 0;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                count = featureTableIndex.count();
                break;
            case METADATA:
                count = featureIndexer.count();
                break;
        }
        return count;
    }

    /**
     * Query for feature index results within the bounding box, projected
     * correctly
     *
     * @param boundingBox bounding box
     * @return feature index results, close when done
     */
    public FeatureIndexResults query(BoundingBox boundingBox) {
        FeatureIndexResults results = null;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                long count = featureTableIndex.count(boundingBox);
                CloseableIterator<GeometryIndex> geometryIndices = featureTableIndex.query(boundingBox);
                results = new FeatureIndexGeoPackageResults(featureTableIndex, count, geometryIndices);
                break;
            case METADATA:
                Cursor geometryMetadata = featureIndexer.query(boundingBox);
                results = new FeatureIndexMetadataResults(featureIndexer, geometryMetadata);
                break;
        }
        return results;
    }

    /**
     * Query for feature index count within the bounding box, projected
     * correctly
     *
     * @param boundingBox bounding box
     * @return count
     */
    public long count(BoundingBox boundingBox) {
        long count = 0;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                count = featureTableIndex.count(boundingBox);
                break;
            case METADATA:
                count = featureIndexer.count(boundingBox);
                break;
        }
        return count;
    }

    /**
     * Query for feature index results within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return feature index results, close when done
     */
    public FeatureIndexResults query(GeometryEnvelope envelope) {
        FeatureIndexResults results = null;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                long count = featureTableIndex.count(envelope);
                CloseableIterator<GeometryIndex> geometryIndices = featureTableIndex.query(envelope);
                results = new FeatureIndexGeoPackageResults(featureTableIndex, count, geometryIndices);
                break;
            case METADATA:
                Cursor geometryMetadata = featureIndexer.query(envelope);
                results = new FeatureIndexMetadataResults(featureIndexer, geometryMetadata);
                break;
        }
        return results;
    }

    /**
     * Query for feature index count within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return count
     */
    public long count(GeometryEnvelope envelope) {
        long count = 0;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                count = featureTableIndex.count(envelope);
                break;
            case METADATA:
                count = featureIndexer.count(envelope);
                break;
        }
        return count;
    }

    /**
     * Query for feature index results within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature index results, close when done
     */
    public FeatureIndexResults query(BoundingBox boundingBox, Projection projection) {
        FeatureIndexResults results = null;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                long count = featureTableIndex.count(boundingBox, projection);
                CloseableIterator<GeometryIndex> geometryIndices = featureTableIndex.query(boundingBox, projection);
                results = new FeatureIndexGeoPackageResults(featureTableIndex, count, geometryIndices);
                break;
            case METADATA:
                Cursor geometryMetadata = featureIndexer.query(boundingBox, projection);
                results = new FeatureIndexMetadataResults(featureIndexer, geometryMetadata);
                break;
        }
        return results;
    }

    /**
     * Query for feature index count within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     */
    public long count(BoundingBox boundingBox, Projection projection) {
        long count = 0;
        switch (getIndexedType()) {
            case GEOPACKAGE:
                count = featureTableIndex.count(boundingBox, projection);
                break;
            case METADATA:
                count = featureIndexer.count(boundingBox, projection);
                break;
        }
        return count;
    }

    /**
     * Verify the index location is set
     *
     * @return feature index type
     */
    private FeatureIndexType verifyIndexLocation() {
        if (indexLocation == null) {
            throw new GeoPackageException("Index Location is not set, set the location or call an index method specifying the location");
        }
        return indexLocation;
    }

    /**
     * Get the indexed type or throw an error if not indexed
     */
    private FeatureIndexType getIndexedType() {

        FeatureIndexType indexType = null;

        // Check for an indexed type
        for (FeatureIndexType type : indexLocationQueryOrder) {
            if (isIndexed(type)) {
                indexType = type;
                break;
            }
        }

        // Verify features are indexed
        if (indexType == null) {
            throw new GeoPackageException("Features are not indexed. GeoPackage: "
                    + featureTableIndex.getGeoPackage().getName()
                    + ", Table: " + featureTableIndex.getTableName());
        }

        return indexType;
    }

}
