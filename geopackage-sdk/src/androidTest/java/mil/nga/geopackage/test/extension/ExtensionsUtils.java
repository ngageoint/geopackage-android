package mil.nga.geopackage.test.extension;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;

/**
 * Extensions Utility test methods
 * 
 * @author osbornb
 */
public class ExtensionsUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		ExtensionsDao dao = geoPackage.getExtensionsDao();

		if (dao.isTableExists()) {
			List<Extensions> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals("Unexpected number of extensions rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (Extensions result : results) {
					TestCase.assertNotNull(result.getExtensionName());
					TestCase.assertNotNull(result.getAuthor());
					TestCase.assertNotNull(result.getExtensionNameNoAuthor());
					TestCase.assertNotNull(result.getDefinition());
					TestCase.assertNotNull(result.getScope());
				}

				// Choose random extensions
				int random = (int) (Math.random() * results.size());
				Extensions extensions = results.get(random);

				// Query by unique columns
				Extensions queryExtensions = dao.queryByExtension(
						extensions.getExtensionName(),
						extensions.getTableName(), extensions.getColumnName());
				TestCase.assertNotNull(queryExtensions);
				TestCase.assertEquals(extensions.getExtensionName(),
						queryExtensions.getExtensionName());
				TestCase.assertEquals(extensions.getTableName(),
						queryExtensions.getTableName());
				TestCase.assertEquals(extensions.getColumnName(),
						queryExtensions.getColumnName());

				// Query for equal
				List<Extensions> queryExtensionsList = dao.queryForEq(
						Extensions.COLUMN_SCOPE, extensions.getScope()
								.getValue());
				TestCase.assertNotNull(queryExtensionsList);
				TestCase.assertTrue(queryExtensionsList.size() >= 1);
				boolean found = false;
				for (Extensions queryExtensionsValue : queryExtensionsList) {
					TestCase.assertEquals(extensions.getScope(),
							queryExtensionsValue.getScope());
					if (!found) {
						found = compare(extensions, queryExtensionsValue);
					}
				}
				TestCase.assertTrue(found);

				// Query for fields values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(Extensions.COLUMN_SCOPE, extensions.getScope()
						.getValue());
				if (extensions.getTableName() != null) {
					fieldValues.put(Extensions.COLUMN_TABLE_NAME,
							extensions.getTableName());
				}
				queryExtensionsList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryExtensionsList);
				TestCase.assertTrue(queryExtensionsList.size() >= 1);
				found = false;
				for (Extensions queryExtensionsValue : queryExtensionsList) {
					TestCase.assertEquals(extensions.getScope(),
							queryExtensionsValue.getScope());
					if (extensions.getTableName() != null) {
						TestCase.assertEquals(extensions.getTableName(),
								queryExtensionsValue.getTableName());
					}
					if (!found) {
						found = compare(extensions, queryExtensionsValue);
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

		ExtensionsDao dao = geoPackage.getExtensionsDao();

		if (dao.isTableExists()) {
			List<Extensions> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random extensions
				int random = (int) (Math.random() * results.size());
				Extensions extensions = results.get(random);

				// Update
				ExtensionScopeType updatedScopeType = ExtensionScopeType.WRITE_ONLY;
				extensions.setScope(updatedScopeType);
				dao.update(extensions);

				// Verify update
				dao = geoPackage.getExtensionsDao();
				Extensions updatedExtensions = dao.queryByExtension(
						extensions.getExtensionName(),
						extensions.getTableName(), extensions.getColumnName());
				TestCase.assertNotNull(updatedExtensions);
				TestCase.assertEquals(extensions.getExtensionName(),
						updatedExtensions.getExtensionName());
				TestCase.assertEquals(extensions.getTableName(),
						updatedExtensions.getTableName());
				TestCase.assertEquals(extensions.getColumnName(),
						updatedExtensions.getColumnName());

				// Prepared update
				String updatedDefinition = "UPDATED DEFINITION";
				UpdateBuilder<Extensions, Void> ub = dao.updateBuilder();
				ub.updateColumnValue(Extensions.COLUMN_DEFINITION,
						updatedDefinition);
				ub.where().eq(Extensions.COLUMN_EXTENSION_NAME,
						extensions.getExtensionName());
				PreparedUpdate<Extensions> update = ub.prepare();
				int updated = dao.update(update);

				// Verify prepared update
				results = dao.queryForAll();
				int count = 0;
				boolean found = false;
				for (Extensions preparedUpdateExtensions : results) {
					if (preparedUpdateExtensions.getExtensionName().equals(
							extensions.getExtensionName())) {
						TestCase.assertEquals(updatedDefinition,
								preparedUpdateExtensions.getDefinition());
						count++;
						if (!found) {
							found = compare(extensions,
									preparedUpdateExtensions);
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

		ExtensionsDao dao = geoPackage.getExtensionsDao();

		if (dao.isTableExists()) {
			// Get current count
			long count = dao.countOf();

			// Create new extensions
			String tableName = "CREATE_TABLE_NAME";
			String columnName = "CREATE_COLUMN_NAME";
			String author = "nga";
			String extension = "create_extension";
			String definition = "definition";
			ExtensionScopeType scopeType = ExtensionScopeType.READ_WRITE;

			Extensions extensions = new Extensions();
			extensions.setTableName(tableName);
			extensions.setColumnName(columnName);
			extensions.setExtensionName(author, extension);
			extensions.setDefinition(definition);
			extensions.setScope(scopeType);
			dao.create(extensions);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved extensions
			Extensions queryExtensions = dao.queryByExtension(
					extensions.getExtensionName(), extensions.getTableName(),
					extensions.getColumnName());
			TestCase.assertNotNull(queryExtensions);
			TestCase.assertEquals(tableName, queryExtensions.getTableName());
			TestCase.assertEquals(columnName, queryExtensions.getColumnName());
			TestCase.assertEquals(author + Extensions.EXTENSION_NAME_DIVIDER
					+ extension, queryExtensions.getExtensionName());
			TestCase.assertEquals(author, queryExtensions.getAuthor());
			TestCase.assertEquals(extension,
					queryExtensions.getExtensionNameNoAuthor());
			TestCase.assertEquals(definition, queryExtensions.getDefinition());
			TestCase.assertEquals(scopeType, queryExtensions.getScope());
		}

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		ExtensionsDao dao = geoPackage.getExtensionsDao();

		if (dao.isTableExists()) {
			List<Extensions> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random metadata
				int random = (int) (Math.random() * results.size());
				Extensions extensions = results.get(random);

				// Delete the extensions
				dao.delete(extensions);

				// Verify deleted
				Extensions queryExtensions = dao.queryByExtension(
						extensions.getExtensionName(),
						extensions.getTableName(), extensions.getColumnName());
				TestCase.assertNull(queryExtensions);

				// Choose prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random metadata
					random = (int) (Math.random() * results.size());
					extensions = results.get(random);

					// Find which metadata to delete
					QueryBuilder<Extensions, Void> qb = dao.queryBuilder();
					qb.where().eq(Extensions.COLUMN_EXTENSION_NAME,
							extensions.getExtensionName());
					PreparedQuery<Extensions> query = qb.prepare();
					List<Extensions> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<Extensions, Void> db = dao.deleteBuilder();
					db.where().eq(Extensions.COLUMN_EXTENSION_NAME,
							extensions.getExtensionName());
					PreparedDelete<Extensions> deleteQuery = db.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

	/**
	 * Compare the two extensions by their unique columns
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private static boolean compare(Extensions first, Extensions second) {
		return first.getExtensionName().equals(second.getExtensionName())
				&& (first.getTableName() == null ? second.getTableName() == null
						: first.getTableName().equals(second.getTableName()))
				&& (first.getColumnName() == null ? second.getColumnName() == null
						: first.getColumnName().equals(second.getColumnName()));
	}

}
