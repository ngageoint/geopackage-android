package mil.nga.geopackage.test.metadata.reference;

import java.sql.SQLException;
import java.util.Date;
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
import mil.nga.geopackage.metadata.reference.ReferenceScopeType;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Metadata Reference Utility test methods
 * 
 * @author osbornb
 */
public class MetadataReferenceUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		MetadataReferenceDao dao = geoPackage.getMetadataReferenceDao();

		if (dao.isTableExists()) {
			List<MetadataReference> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of metadata reference rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (MetadataReference result : results) {
					TestCase.assertNotNull(result.getReferenceScope());
					TestCase.assertNotNull(result.getTimestamp());
					TestCase.assertNotNull(result.getFileId());

					Metadata metadata = result.getMetadata();
					TestCase.assertNotNull(metadata);
					TestCase.assertNotNull(metadata.getId());
					TestCase.assertNotNull(metadata.getMetadataScope());
					TestCase.assertNotNull(metadata.getStandardUri());
					TestCase.assertNotNull(metadata.getMimeType());
					TestCase.assertNotNull(metadata.getMetadata());

					Metadata parentMetadata = result.getParentMetadata();
					if (parentMetadata != null) {
						TestCase.assertNotNull(parentMetadata.getId());
						TestCase.assertNotNull(parentMetadata
								.getMetadataScope());
						TestCase.assertNotNull(parentMetadata.getStandardUri());
						TestCase.assertNotNull(parentMetadata.getMimeType());
						TestCase.assertNotNull(parentMetadata.getMetadata());
					}
				}

				// Choose random metadata reference
				int random = (int) (Math.random() * results.size());
				MetadataReference metadataReference = results.get(random);

				// Query by id
				List<MetadataReference> queryMetadataReferenceList = dao
						.queryByMetadata(metadataReference.getFileId(),
								metadataReference.getParentId());
				TestCase.assertNotNull(queryMetadataReferenceList);
				for (MetadataReference queryMetadataReference : queryMetadataReferenceList) {
					TestCase.assertEquals(metadataReference.getFileId(),
							queryMetadataReference.getFileId());
					TestCase.assertEquals(metadataReference.getParentId(),
							queryMetadataReference.getParentId());
				}

				// Query for equal
				queryMetadataReferenceList = dao.queryForEq(
						MetadataReference.COLUMN_REFERENCE_SCOPE,
						metadataReference.getReferenceScope().getValue());
				TestCase.assertNotNull(queryMetadataReferenceList);
				TestCase.assertTrue(queryMetadataReferenceList.size() >= 1);
				boolean found = false;
				for (MetadataReference queryMetadataReferenceValue : queryMetadataReferenceList) {
					TestCase.assertEquals(
							metadataReference.getReferenceScope(),
							queryMetadataReferenceValue.getReferenceScope());
					if (!found) {
						found = metadataReference.getFileId() == queryMetadataReferenceValue
								.getFileId()
								&& metadataReference.getParentId() == queryMetadataReferenceValue
										.getParentId();
					}
				}
				TestCase.assertTrue(found);

				// Query for fields values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(MetadataReference.COLUMN_REFERENCE_SCOPE,
						metadataReference.getReferenceScope().getValue());
				fieldValues.put(MetadataReference.COLUMN_FILE_ID,
						metadataReference.getFileId());
				queryMetadataReferenceList = dao
						.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryMetadataReferenceList);
				TestCase.assertTrue(queryMetadataReferenceList.size() >= 1);
				found = false;
				for (MetadataReference queryMetadataReferenceValue : queryMetadataReferenceList) {
					TestCase.assertEquals(
							metadataReference.getReferenceScope(),
							queryMetadataReferenceValue.getReferenceScope());
					TestCase.assertEquals(metadataReference.getFileId(),
							queryMetadataReferenceValue.getFileId());
					if (!found) {
						found = metadataReference.getFileId() == queryMetadataReferenceValue
								.getFileId()
								&& metadataReference.getParentId() == queryMetadataReferenceValue
										.getParentId();
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

		MetadataReferenceDao dao = geoPackage.getMetadataReferenceDao();

		if (dao.isTableExists()) {
			List<MetadataReference> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random metadata reference
				int random = (int) (Math.random() * results.size());
				MetadataReference metadataReference = results.get(random);

				// Update
				ReferenceScopeType updatedScopeType = ReferenceScopeType.ROW;
				metadataReference.setReferenceScope(updatedScopeType);
				dao.update(metadataReference);

				// Verify update
				dao = geoPackage.getMetadataReferenceDao();
				List<MetadataReference> updatedMetadataReferenceList = dao
						.queryByMetadata(metadataReference.getFileId(),
								metadataReference.getParentId());
				for (MetadataReference updatedMetadataReference : updatedMetadataReferenceList) {
					TestCase.assertEquals(updatedScopeType,
							updatedMetadataReference.getReferenceScope());
				}

				// Prepared update
				String updatedTable = "UPDATED_TABLE";
				UpdateBuilder<MetadataReference, Void> ub = dao.updateBuilder();
				ub.updateColumnValue(MetadataReference.COLUMN_TABLE_NAME,
						updatedTable);
				ub.where().ne(MetadataReference.COLUMN_REFERENCE_SCOPE,
						ReferenceScopeType.GEOPACKAGE.getValue());
				PreparedUpdate<MetadataReference> update = ub.prepare();
				int updated = dao.update(update);

				// Verify prepared update
				results = dao.queryForAll();
				int count = 0;
				boolean found = false;
				for (MetadataReference preparedUpdateMetadataReference : results) {
					if (!preparedUpdateMetadataReference.getReferenceScope()
							.equals(ReferenceScopeType.GEOPACKAGE)) {
						TestCase.assertEquals(updatedTable,
								preparedUpdateMetadataReference.getTableName());
						count++;
						if (!found) {
							found = metadataReference.getFileId() == preparedUpdateMetadataReference
									.getFileId()
									&& metadataReference.getParentId() == preparedUpdateMetadataReference
											.getParentId();
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

		MetadataReferenceDao dao = geoPackage.getMetadataReferenceDao();
		MetadataDao metadataDao = geoPackage.getMetadataDao();

		if (dao.isTableExists()) {
			// Get current count
			long count = dao.countOf();

			long fileId = 5555;
			long parentId = 5556;

			// Create new metadata
			Metadata metadata = new Metadata();
			metadata.setId(fileId);
			metadata.setMetadataScope(MetadataScopeType.FEATURE);
			metadata.setStandardUri("https://www.nga.mil");
			metadata.setMimeType("text/xml");
			metadata.setMetadata("Create metadata text");
			metadataDao.create(metadata);

			Metadata metadata2 = new Metadata();
			metadata2.setId(parentId);
			metadata2.setMetadataScope(MetadataScopeType.FEATURE_TYPE);
			metadata2.setStandardUri("https://www.nga.mil");
			metadata2.setMimeType("text/xml");
			metadata2.setMetadata("Create metadata text 2");
			metadataDao.create(metadata2);

			ReferenceScopeType scopeType = ReferenceScopeType.ROW;
			String tableName = "CREATE_TABLE_NAME";
			long rowIdValue = 50;
			Date timestamp = new Date();

			MetadataReference reference = new MetadataReference();
			reference.setReferenceScope(scopeType);
			reference.setTableName(tableName);
			reference.setRowIdValue(rowIdValue);
			reference.setTimestamp(timestamp);
			reference.setMetadata(metadata);
			reference.setParentMetadata(metadata2);
			dao.create(reference);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved metadata
			List<MetadataReference> queryMetadataReferenceList = dao
					.queryByMetadata(fileId, parentId);
			TestCase.assertNotNull(queryMetadataReferenceList);
			TestCase.assertEquals(1, queryMetadataReferenceList.size());
			MetadataReference queryMetadataReference = queryMetadataReferenceList
					.get(0);
			TestCase.assertEquals(scopeType,
					queryMetadataReference.getReferenceScope());
			TestCase.assertEquals(tableName,
					queryMetadataReference.getTableName());
			TestCase.assertNull(queryMetadataReference.getColumnName());
			TestCase.assertEquals(rowIdValue, queryMetadataReference
					.getRowIdValue().longValue());
			TestCase.assertEquals(timestamp,
					queryMetadataReference.getTimestamp());
			TestCase.assertEquals(fileId, queryMetadataReference.getFileId());
			TestCase.assertEquals(parentId, queryMetadataReference
					.getParentId().longValue());
			TestCase.assertNotNull(queryMetadataReference.getMetadata());
			TestCase.assertNotNull(queryMetadataReference.getParentMetadata());
		}

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		MetadataReferenceDao dao = geoPackage.getMetadataReferenceDao();

		if (dao.isTableExists()) {
			List<MetadataReference> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random metadata
				int random = (int) (Math.random() * results.size());
				MetadataReference metadataReference = results.get(random);

				// Delete the metadata reference
				dao.delete(metadataReference);

				// Verify deleted
				List<MetadataReference> queryMetadataReferenceList = dao
						.queryByMetadata(metadataReference.getFileId(),
								metadataReference.getParentId());
				TestCase.assertTrue(queryMetadataReferenceList.isEmpty());

				// Choose prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random metadata
					random = (int) (Math.random() * results.size());
					metadataReference = results.get(random);

					// Find which metadata to delete
					QueryBuilder<MetadataReference, Void> qb = dao
							.queryBuilder();
					qb.where().eq(MetadataReference.COLUMN_FILE_ID,
							metadataReference.getFileId());
					PreparedQuery<MetadataReference> query = qb.prepare();
					List<MetadataReference> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<MetadataReference, Void> db = dao
							.deleteBuilder();
					db.where().eq(MetadataReference.COLUMN_FILE_ID,
							metadataReference.getFileId());
					PreparedDelete<MetadataReference> deleteQuery = db
							.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
