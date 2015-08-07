package mil.nga.geopackage.test.metadata;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.metadata.Metadata;
import mil.nga.geopackage.metadata.MetadataDao;
import mil.nga.geopackage.metadata.MetadataScopeType;
import mil.nga.geopackage.metadata.reference.MetadataReference;
import mil.nga.geopackage.metadata.reference.MetadataReferenceDao;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Metadata Utility test methods
 * 
 * @author osbornb
 */
public class MetadataUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		MetadataDao dao = geoPackage.getMetadataDao();

		if (dao.isTableExists()) {
			List<Metadata> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals("Unexpected number of metadata rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (Metadata result : results) {
					TestCase.assertNotNull(result.getId());
					TestCase.assertNotNull(result.getMetadataScope());
					TestCase.assertNotNull(result.getStandardUri());
					TestCase.assertNotNull(result.getMimeType());
					TestCase.assertNotNull(result.getMetadata());
				}

				// Choose random metadata
				int random = (int) (Math.random() * results.size());
				Metadata metadata = results.get(random);

				// Query by id
				Metadata queryMetadata = dao.queryForId(metadata.getId());
				TestCase.assertNotNull(queryMetadata);
				TestCase.assertEquals(metadata.getId(), queryMetadata.getId());

				// Query for equal
				List<Metadata> queryMetadataList = dao.queryForEq(
						Metadata.COLUMN_SCOPE, metadata.getMetadataScope()
								.getName());
				TestCase.assertNotNull(queryMetadataList);
				TestCase.assertTrue(queryMetadataList.size() >= 1);
				boolean found = false;
				for (Metadata queryMetadataValue : queryMetadataList) {
					TestCase.assertEquals(metadata.getMetadataScope(),
							queryMetadataValue.getMetadataScope());
					if (!found) {
						found = metadata.getId() == queryMetadataValue.getId();
					}
				}
				TestCase.assertTrue(found);

				// Query for fields values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(Metadata.COLUMN_SCOPE, metadata
						.getMetadataScope().getName());
				fieldValues.put(Metadata.COLUMN_MIME_TYPE,
						metadata.getMimeType());
				queryMetadataList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryMetadataList);
				TestCase.assertTrue(queryMetadataList.size() >= 1);
				found = false;
				for (Metadata queryMetadataValue : queryMetadataList) {
					TestCase.assertEquals(metadata.getMetadataScope(),
							queryMetadataValue.getMetadataScope());
					TestCase.assertEquals(metadata.getMimeType(),
							queryMetadataValue.getMimeType());
					if (!found) {
						found = metadata.getId() == queryMetadataValue.getId();
					}
				}
				TestCase.assertTrue(found);
			}
		}
	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		MetadataDao dao = geoPackage.getMetadataDao();

		if (dao.isTableExists()) {
			List<Metadata> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random metadata
				int random = (int) (Math.random() * results.size());
				Metadata metadata = results.get(random);

				// Update
				MetadataScopeType updatedScopeType = MetadataScopeType.CATALOG;
				metadata.setMetadataScope(updatedScopeType);
				dao.update(metadata);

				// Verify update
				dao = geoPackage.getMetadataDao();
				Metadata updatedMetadata = dao.queryForId(metadata.getId());
				TestCase.assertEquals(updatedScopeType,
						updatedMetadata.getMetadataScope());

				// Prepared update
				String updatedMetadataText = "UPDATED METADATA";
				UpdateBuilder<Metadata, Long> ub = dao.updateBuilder();
				ub.updateColumnValue(Metadata.COLUMN_METADATA,
						updatedMetadataText);
				ub.where()
						.eq(Metadata.COLUMN_MIME_TYPE, metadata.getMimeType());
				PreparedUpdate<Metadata> update = ub.prepare();
				int updated = dao.update(update);

				// Verify prepared update
				results = dao.queryForAll();
				int count = 0;
				boolean found = false;
				for (Metadata preparedUpdateMetadata : results) {
					if (preparedUpdateMetadata.getMimeType().equals(
							metadata.getMimeType())) {
						TestCase.assertEquals(updatedMetadataText,
								preparedUpdateMetadata.getMetadata());
						count++;
						if (!found) {
							found = metadata.getId() == preparedUpdateMetadata
									.getId();
						}
					}
				}
				TestCase.assertEquals(updated, count);
				TestCase.assertTrue(found);
			}
		}

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		MetadataDao dao = geoPackage.getMetadataDao();

		if (dao.isTableExists()) {
			// Get current count
			long count = dao.countOf();

			long id = 12345;
			MetadataScopeType scopeType = MetadataScopeType.SOFTWARE;
			String standardUri = "https://www.nga.mil";
			String mimeType = "text/xml";
			String metadataText = "Create metadata text";

			// Create new metadata
			Metadata metadata = new Metadata();
			metadata.setId(id);
			metadata.setMetadataScope(scopeType);
			metadata.setStandardUri(standardUri);
			metadata.setMimeType(mimeType);
			metadata.setMetadata(metadataText);
			dao.create(metadata);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved metadata
			Metadata queryMetadata = dao.queryForId(id);
			TestCase.assertEquals(id, queryMetadata.getId());
			TestCase.assertEquals(scopeType, queryMetadata.getMetadataScope());
			TestCase.assertEquals(standardUri, queryMetadata.getStandardUri());
			TestCase.assertEquals(mimeType, queryMetadata.getMimeType());
			TestCase.assertEquals(metadataText, queryMetadata.getMetadata());
		}

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		testDeleteHelper(geoPackage, false);

	}

	/**
	 * Test delete cascade
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDeleteCascade(GeoPackage geoPackage)
			throws SQLException {

		testDeleteHelper(geoPackage, true);

	}

	/**
	 * Test delete helper
	 * 
	 * @param geoPackage
	 * @param cascade
	 * @throws SQLException
	 */
	private static void testDeleteHelper(GeoPackage geoPackage, boolean cascade)
			throws SQLException {

		MetadataDao dao = geoPackage.getMetadataDao();

		if (dao.isTableExists()) {
			List<Metadata> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random metadata
				int random = (int) (Math.random() * results.size());
				Metadata metadata = results.get(random);

				// Get reference counts
				MetadataReferenceDao metadataReferenceDao = geoPackage
						.getMetadataReferenceDao();
				int referenceCount = metadataReferenceDao.queryByMetadata(
						metadata.getId()).size();
				List<MetadataReference> metadataReferenceList = metadataReferenceDao
						.queryByMetadataParent(metadata.getId());
				int referenceParentCount = metadataReferenceList.size();

				// Delete the metadata
				if (cascade) {
					dao.deleteCascade(metadata);
				} else {
					dao.delete(metadata);
				}

				// Verify deleted
				Metadata queryMetadata = dao.queryForId(metadata.getId());
				TestCase.assertNull(queryMetadata);

				// Verify that references were deleted
				List<MetadataReference> queryMetadataReference = metadataReferenceDao
						.queryByMetadata(metadata.getId());
				List<MetadataReference> queryMetadataParentReference = metadataReferenceDao
						.queryByMetadataParent(metadata.getId());
				if (cascade) {
					TestCase.assertTrue(queryMetadataReference.isEmpty());
					TestCase.assertTrue(queryMetadataParentReference.isEmpty());
				} else {
					TestCase.assertEquals(referenceCount,
							queryMetadataReference.size());
					TestCase.assertEquals(referenceParentCount,
							queryMetadataParentReference.size());
				}
				for(MetadataReference metadataReference: metadataReferenceList){
					TestCase.assertNotNull(metadataReferenceDao.queryByMetadata(metadataReference.getFileId(), metadataReference.getParentId()));
				}

				// Choose prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random metadata
					random = (int) (Math.random() * results.size());
					metadata = results.get(random);

					// Find which metadata to delete
					QueryBuilder<Metadata, Long> qb = dao.queryBuilder();
					qb.where().eq(Metadata.COLUMN_MIME_TYPE,
							metadata.getMimeType());
					PreparedQuery<Metadata> query = qb.prepare();
					List<Metadata> queryResults = dao.query(query);
					int count = queryResults.size();
					referenceCount = metadataReferenceDao.queryByMetadata(
							metadata.getId()).size();
					referenceParentCount = metadataReferenceDao
							.queryByMetadataParent(metadata.getId()).size();

					// Delete
					int deleted;
					if (cascade) {
						deleted = dao.deleteCascade(query);
					} else {
						DeleteBuilder<Metadata, Long> db = dao.deleteBuilder();
						db.where().eq(Metadata.COLUMN_MIME_TYPE,
								metadata.getMimeType());
						PreparedDelete<Metadata> deleteQuery = db.prepare();
						deleted = dao.delete(deleteQuery);
					}
					TestCase.assertEquals(count, deleted);

					// Verify that references were deleted
					queryMetadataReference = metadataReferenceDao
							.queryByMetadata(metadata.getId());
					queryMetadataParentReference = metadataReferenceDao
							.queryByMetadataParent(metadata.getId());
					if (cascade) {
						TestCase.assertTrue(queryMetadataReference.isEmpty());
						TestCase.assertTrue(queryMetadataParentReference
								.isEmpty());
					} else {
						TestCase.assertEquals(referenceCount,
								queryMetadataReference.size());
						TestCase.assertEquals(referenceParentCount,
								queryMetadataParentReference.size());
					}
					for(MetadataReference metadataReference: metadataReferenceList){
						TestCase.assertNotNull(metadataReferenceDao.queryByMetadata(metadataReference.getFileId(), metadataReference.getParentId()));
					}
				}
			}
		}
	}

}
