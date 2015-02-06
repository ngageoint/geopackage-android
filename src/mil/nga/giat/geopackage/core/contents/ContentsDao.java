package mil.nga.giat.geopackage.core.contents;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDatabaseUtils;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Contents Data Access Object
 * 
 * @author osbornb
 */
public class ContentsDao extends BaseDaoImpl<Contents, String> {

	/**
	 * Database connection
	 */
	private SQLiteDatabase db;

	/**
	 * Geometry Columns DAO
	 */
	private GeometryColumnsDao geometryColumnsDao;

	/**
	 * Tile Matrix Set DAO
	 */
	private TileMatrixSetDao tileMatrixSetDao;

	/**
	 * Tile Matrix DAO
	 */
	private TileMatrixDao tileMatrixDao;

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public ContentsDao(ConnectionSource connectionSource,
			Class<Contents> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * Set the database
	 * 
	 * @param db
	 */
	public void setDatabase(SQLiteDatabase db) {
		this.db = db;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Verify optional tables have been created
	 */
	@Override
	public int create(Contents contents) throws SQLException {
		verifyCreate(contents);
		return super.create(contents);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Verify optional tables have been created
	 */
	@Override
	public Contents createIfNotExists(Contents contents) throws SQLException {
		verifyCreate(contents);
		return super.createIfNotExists(contents);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Verify optional tables have been created
	 */
	@Override
	public CreateOrUpdateStatus createOrUpdate(Contents contents)
			throws SQLException {
		verifyCreate(contents);
		return super.createOrUpdate(contents);
	}

	/**
	 * Delete the Contents, cascading
	 * 
	 * @param contents
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(Contents contents) throws SQLException {
		int count = 0;

		if (contents != null) {

			// Delete Geometry Columns
			GeometryColumnsDao geometryColumnsDao = getGeometryColumnsDao();
			if (geometryColumnsDao.isTableExists()) {
				GeometryColumns geometryColumns = contents.getGeometryColumns();
				if (geometryColumns != null) {
					geometryColumnsDao.delete(geometryColumns);
				}
			}

			// Delete Tile Matrix collection
			TileMatrixDao tileMatrixDao = getTileMatrixDao();
			if (tileMatrixDao.isTableExists()) {
				ForeignCollection<TileMatrix> tileMatrixCollection = contents
						.getTileMatrix();
				if (!tileMatrixCollection.isEmpty()) {
					tileMatrixDao.delete(tileMatrixCollection);
				}
			}

			// Delete Tile Matrix Set
			TileMatrixSetDao tileMatrixSetDao = getTileMatrixSetDao();
			if (tileMatrixSetDao.isTableExists()) {
				TileMatrixSet tileMatrixSet = contents.getTileMatrixSet();
				if (tileMatrixSet != null) {
					tileMatrixSetDao.delete(tileMatrixSet);
				}
			}

			count = delete(contents);
		}
		return count;
	}

	/**
	 * Delete the collection of Contents, cascading
	 * 
	 * @param contentsCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(Collection<Contents> contentsCollection)
			throws SQLException {
		int count = 0;
		if (contentsCollection != null) {
			for (Contents contents : contentsCollection) {
				count += deleteCascade(contents);
			}
		}
		return count;
	}

	/**
	 * Delete the Contents matching the prepared query, cascading
	 * 
	 * @param preparedDelete
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(PreparedQuery<Contents> preparedDelete)
			throws SQLException {
		int count = 0;
		if (preparedDelete != null) {
			List<Contents> contentsList = query(preparedDelete);
			count = deleteCascade(contentsList);
		}
		return count;
	}

	/**
	 * Delete a Contents by id, cascading
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public int deleteByIdCascade(String id) throws SQLException {
		int count = 0;
		if (id != null) {
			Contents contents = queryForId(id);
			if (contents != null) {
				count = deleteCascade(contents);
			}
		}
		return count;
	}

	/**
	 * Delete the Contents with the provided ids, cascading
	 * 
	 * @param idCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteIdsCascade(Collection<String> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (String id : idCollection) {
				count += deleteByIdCascade(id);
			}
		}
		return count;
	}

	/**
	 * Verify the tables are in the expected state for the Contents create
	 * 
	 * @param contents
	 * @throws SQLException
	 */
	private void verifyCreate(Contents contents) throws SQLException {
		ContentsDataType dataType = contents.getDataType();
		if (dataType != null) {
			switch (dataType) {
			case FEATURES:

				// Features require Geometry Columns table (Spec Requirement 21)
				GeometryColumnsDao geometryColumnsDao = getGeometryColumnsDao();
				if (!geometryColumnsDao.isTableExists()) {
					throw new GeoPackageException(
							"A data type of "
									+ dataType.getName()
									+ " requires the "
									+ GeometryColumns.class.getSimpleName()
									+ " table to first be created using the GeoPackage.");
				}

				break;

			case TILES:
				// Tiles require Tile Matrix Set table (Spec Requirement 37)
				TileMatrixSetDao tileMatrixSetDao = getTileMatrixSetDao();
				if (!tileMatrixSetDao.isTableExists()) {
					throw new GeoPackageException(
							"A data type of "
									+ dataType.getName()
									+ " requires the "
									+ TileMatrixSet.class.getSimpleName()
									+ " table to first be created using the GeoPackage.");
				}

				// Tiles require Tile Matrix table (Spec Requirement 41)
				TileMatrixDao tileMatrixDao = getTileMatrixDao();
				if (!tileMatrixDao.isTableExists()) {
					throw new GeoPackageException(
							"A data type of "
									+ dataType.getName()
									+ " requires the "
									+ TileMatrix.class.getSimpleName()
									+ " table to first be created using the GeoPackage.");
				}

				break;

			default:
				throw new GeoPackageException("Unsupported data type: "
						+ dataType);
			}
		}

		// Verify the feature or tile table exists
		if (!GeoPackageDatabaseUtils.tableExists(db, contents.getTableName())) {
			throw new GeoPackageException(
					"No table exists for Content Table Name: "
							+ contents.getTableName()
							+ ". Table must first be created.");
		}
	}

	/**
	 * Get or create a Geometry Columns DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private GeometryColumnsDao getGeometryColumnsDao() throws SQLException {
		if (geometryColumnsDao == null) {
			geometryColumnsDao = DaoManager.createDao(connectionSource,
					GeometryColumns.class);
		}
		return geometryColumnsDao;
	}

	/**
	 * Get or create a Tile Matrix Set DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private TileMatrixSetDao getTileMatrixSetDao() throws SQLException {
		if (tileMatrixSetDao == null) {
			tileMatrixSetDao = DaoManager.createDao(connectionSource,
					TileMatrixSet.class);
		}
		return tileMatrixSetDao;
	}

	/**
	 * Get or create a Tile Matrix DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private TileMatrixDao getTileMatrixDao() throws SQLException {
		if (tileMatrixDao == null) {
			tileMatrixDao = DaoManager.createDao(connectionSource,
					TileMatrix.class);
		}
		return tileMatrixDao;
	}

}
