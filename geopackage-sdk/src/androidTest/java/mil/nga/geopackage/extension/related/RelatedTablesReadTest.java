package mil.nga.geopackage.extension.related;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.LoadGeoPackageTestCase;
import mil.nga.geopackage.TestConstants;

/**
 * Test Related Tables Extension reading
 *
 * @author osbornb
 */
public class RelatedTablesReadTest extends LoadGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedTablesReadTest() {
        super(TestConstants.RTE_DB_NAME, TestConstants.RTE_DB_FILE_NAME);
    }

    /**
     * Test get tile
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testGetRelationships() throws SQLException, IOException {

        // 1. has
        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);
        TestCase.assertTrue(rte.has());

        // 4. get relationships
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());

        for (ExtendedRelation extendedRelation : extendedRelations) {

            // 9. get mappings by base ID
            Map<Long, List<Long>> baseIdMappings = new HashMap<>();
            FeatureDao baseDao = geoPackage.getFeatureDao(extendedRelation
                    .getBaseTableName());
            FeatureColumn pkColumn = baseDao.getTable().getPkColumn();
            FeatureCursor frs = baseDao.queryForAll();
            while (frs.moveToNext()) {
                long baseId = frs.getLong(pkColumn.getIndex());
                List<Long> relatedIds = rte.getMappingsForBase(
                        extendedRelation, baseId);
                TestCase.assertFalse(relatedIds.isEmpty());
                baseIdMappings.put(baseId, relatedIds);
            }
            frs.close();

            // 10. get mappings by related ID
            Map<Long, List<Long>> relatedIdMappings = new HashMap<>();
            AttributesDao relatedDao = geoPackage
                    .getAttributesDao(extendedRelation.getRelatedTableName());
            AttributesColumn pkColumn2 = relatedDao.getTable().getPkColumn();
            AttributesCursor ars = relatedDao.queryForAll();
            while (ars.moveToNext()) {
                long relatedId = ars.getLong(pkColumn2.getIndex());
                List<Long> baseIds = rte.getMappingsForRelated(
                        extendedRelation, relatedId);
                TestCase.assertFalse(baseIds.isEmpty());
                relatedIdMappings.put(relatedId, baseIds);
            }
            ars.close();

            // Verify the related ids map back to the base ids
            for (Entry<Long, List<Long>> baseIdMap : baseIdMappings.entrySet()) {
                for (Long relatedId : baseIdMap.getValue()) {
                    TestCase.assertTrue(relatedIdMappings.get(relatedId)
                            .contains(baseIdMap.getKey()));
                }
            }

            // Verify the base ids map back to the related ids
            for (Entry<Long, List<Long>> relatedIdMap : relatedIdMappings
                    .entrySet()) {
                for (Long baseId : relatedIdMap.getValue()) {
                    TestCase.assertTrue(baseIdMappings.get(baseId).contains(
                            relatedIdMap.getKey()));
                }
            }

        }
    }
}
