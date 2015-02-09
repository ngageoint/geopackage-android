package mil.nga.giat.geopackage.metadata;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.geopackage.metadata.reference.MetadataReference;
import mil.nga.giat.geopackage.metadata.reference.MetadataReferenceDao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Metadata Data Access Object
 * 
 * @author osbornb
 */
public class MetadataDao extends BaseDaoImpl<Metadata, Long> {

	/**
	 * Metadata Reference DAO
	 */
	private MetadataReferenceDao metadataReferenceDao;

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public MetadataDao(ConnectionSource connectionSource,
			Class<Metadata> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * Delete the Metadata, cascading
	 * 
	 * @param metadata
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(Metadata metadata) throws SQLException {
		int count = 0;

		if (metadata != null) {

			// Delete Metadata References and remove parent references
			MetadataReferenceDao dao = getMetadataReferenceDao();
			dao.deleteByMetadata(metadata.getId());
			dao.removeMetadataParent(metadata.getId());

			// Delete
			count = delete(metadata);
		}
		return count;
	}

	/**
	 * Delete the collection of Metadata, cascading
	 * 
	 * @param metadataCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(Collection<Metadata> metadataCollection)
			throws SQLException {
		int count = 0;
		if (metadataCollection != null) {
			for (Metadata metadata : metadataCollection) {
				count += deleteCascade(metadata);
			}
		}
		return count;
	}

	/**
	 * Delete the Metadata matching the prepared query, cascading
	 * 
	 * @param preparedDelete
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(PreparedQuery<Metadata> preparedDelete)
			throws SQLException {
		int count = 0;
		if (preparedDelete != null) {
			List<Metadata> metadataList = query(preparedDelete);
			count = deleteCascade(metadataList);
		}
		return count;
	}

	/**
	 * Delete a Metadata by id, cascading
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public int deleteByIdCascade(Long id) throws SQLException {
		int count = 0;
		if (id != null) {
			Metadata metadata = queryForId(id);
			if (metadata != null) {
				count = deleteCascade(metadata);
			}
		}
		return count;
	}

	/**
	 * Delete the Metadata with the provided ids, cascading
	 * 
	 * @param idCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteIdsCascade(Collection<Long> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (Long id : idCollection) {
				count += deleteByIdCascade(id);
			}
		}
		return count;
	}

	/**
	 * Get or create a Metadata Reference DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private MetadataReferenceDao getMetadataReferenceDao() throws SQLException {
		if (metadataReferenceDao == null) {
			metadataReferenceDao = DaoManager.createDao(connectionSource,
					MetadataReference.class);
		}
		return metadataReferenceDao;
	}

}
