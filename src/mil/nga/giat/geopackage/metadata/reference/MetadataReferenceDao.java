package mil.nga.giat.geopackage.metadata.reference;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Metadata Reference Data Access Object
 * 
 * @author osbornb
 */
public class MetadataReferenceDao extends BaseDaoImpl<MetadataReference, Void> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public MetadataReferenceDao(ConnectionSource connectionSource,
			Class<MetadataReference> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Update using the foreign key columns
	 */
	@Override
	public int update(MetadataReference metadataReference) throws SQLException {

		UpdateBuilder<MetadataReference, Void> ub = updateBuilder();
		ub.updateColumnValue(MetadataReference.COLUMN_REFERENCE_SCOPE,
				metadataReference.getReferenceScope().getValue());
		ub.updateColumnValue(MetadataReference.COLUMN_TABLE_NAME,
				metadataReference.getTableName());
		ub.updateColumnValue(MetadataReference.COLUMN_COLUMN_NAME,
				metadataReference.getColumnName());
		ub.updateColumnValue(MetadataReference.COLUMN_ROW_ID_VALUE,
				metadataReference.getRowIdValue());
		ub.updateColumnValue(MetadataReference.COLUMN_TIMESTAMP,
				metadataReference.getTimestamp());

		setFkWhere(ub.where(), metadataReference.getMdFileId(),
				metadataReference.getMdParentId());

		PreparedUpdate<MetadataReference> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Delete using the foreign key columns
	 */
	@Override
	public int delete(MetadataReference metadataReference) throws SQLException {

		DeleteBuilder<MetadataReference, Void> db = deleteBuilder();

		setFkWhere(db.where(), metadataReference.getMdFileId(),
				metadataReference.getMdParentId());

		int deleted = db.delete();

		return deleted;
	}

	/**
	 * Query by the metadata ids
	 * 
	 * @param mdFileId
	 * @param mdParentId
	 * @return
	 * @throws SQLException
	 */
	public List<MetadataReference> queryByMetadata(long mdFileId,
			Long mdParentId) throws SQLException {

		QueryBuilder<MetadataReference, Void> qb = queryBuilder();
		setFkWhere(qb.where(), mdFileId, mdParentId);
		List<MetadataReference> metadataReferences = qb.query();

		return metadataReferences;
	}

	/**
	 * Query by the metadata ids
	 * 
	 * @param mdFileId
	 * @return
	 * @throws SQLException
	 */
	public List<MetadataReference> queryByMetadata(long mdFileId)
			throws SQLException {

		QueryBuilder<MetadataReference, Void> qb = queryBuilder();
		setFkWhere(qb.where(), mdFileId);
		List<MetadataReference> metadataReferences = qb.query();

		return metadataReferences;
	}

	/**
	 * Set the foreign key column criteria in the where clause
	 * 
	 * @param where
	 * @param mdFileId
	 * @param mdParentId
	 * @throws SQLException
	 */
	private void setFkWhere(Where<MetadataReference, Void> where,
			long mdFileId, Long mdParentId) throws SQLException {

		where.eq(MetadataReference.COLUMN_MD_FILE_ID, mdFileId);
		if (mdParentId == null) {
			where.and().isNull(MetadataReference.COLUMN_MD_PARENT_ID);
		} else {
			where.and().eq(MetadataReference.COLUMN_MD_PARENT_ID, mdParentId);
		}

	}

	/**
	 * Set the foreign key column criteria in the where clause
	 * 
	 * @param where
	 * @param mdFileId
	 * @throws SQLException
	 */
	private void setFkWhere(Where<MetadataReference, Void> where, long mdFileId)
			throws SQLException {

		where.eq(MetadataReference.COLUMN_MD_FILE_ID, mdFileId);

	}

}
