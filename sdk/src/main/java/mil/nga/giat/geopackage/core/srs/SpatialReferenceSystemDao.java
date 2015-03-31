package mil.nga.giat.geopackage.core.srs;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import android.content.Context;
import android.content.res.Resources;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Spatial Reference System Data Access Object
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemDao extends
		BaseDaoImpl<SpatialReferenceSystem, Long> {

	/**
	 * Contents DAO
	 */
	private ContentsDao contentsDao;

	/**
	 * Geometry Columns DAO
	 */
	private GeometryColumnsDao geometryColumnsDao;

	/**
	 * Tile Matrix Set DAO
	 */
	private TileMatrixSetDao tileMatrixSetDao;

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public SpatialReferenceSystemDao(ConnectionSource connectionSource,
			Class<SpatialReferenceSystem> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * Creates the required EPSG WGS84 Spatial Reference System (spec Requirement 11)
	 *
     * @return
	 * @throws SQLException
	 */
	public SpatialReferenceSystem createWgs84(Context context) throws SQLException {

		Resources resources = context.getResources();
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setSrsName(resources
				.getString(R.string.geopackage_srs_wgs84_srs_name));
		srs.setSrsId(resources.getInteger(R.integer.geopackage_srs_wgs84_srs_id));
		srs.setOrganization(resources
				.getString(R.string.geopackage_srs_wgs84_organization));
		srs.setOrganizationCoordsysId(resources
				.getInteger(R.integer.geopackage_srs_wgs84_organization_coordsys_id));
		srs.setDefinition(resources
				.getString(R.string.geopackage_srs_wgs84_definition));
		srs.setDescription(resources
				.getString(R.string.geopackage_srs_wgs84_description));
		create(srs);

        return srs;
	}

	/**
	 * Creates the required Undefined Cartesian Spatial Reference System (spec
	 * Requirement 11)
	 *
     * @return
	 * @throws SQLException
	 */
	public SpatialReferenceSystem createUndefinedCartesian(Context context) throws SQLException {

		Resources resources = context.getResources();
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setSrsName(resources
				.getString(R.string.geopackage_srs_undefined_cartesian_srs_name));
		srs.setSrsId(resources
				.getInteger(R.integer.geopackage_srs_undefined_cartesian_srs_id));
		srs.setOrganization(resources
				.getString(R.string.geopackage_srs_undefined_cartesian_organization));
		srs.setOrganizationCoordsysId(resources
				.getInteger(R.integer.geopackage_srs_undefined_cartesian_organization_coordsys_id));
		srs.setDefinition(resources
				.getString(R.string.geopackage_srs_undefined_cartesian_definition));
		srs.setDescription(resources
				.getString(R.string.geopackage_srs_undefined_cartesian_description));
		create(srs);

        return srs;
	}

	/**
	 * Creates the required Undefined Geographic Spatial Reference System (spec
	 * Requirement 11)
	 *
     * @return
	 * @throws SQLException
	 */
	public SpatialReferenceSystem createUndefinedGeographic(Context context) throws SQLException {

		Resources resources = context.getResources();
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setSrsName(resources
				.getString(R.string.geopackage_srs_undefined_geographic_srs_name));
		srs.setSrsId(resources
				.getInteger(R.integer.geopackage_srs_undefined_geographic_srs_id));
		srs.setOrganization(resources
				.getString(R.string.geopackage_srs_undefined_geographic_organization));
		srs.setOrganizationCoordsysId(resources
				.getInteger(R.integer.geopackage_srs_undefined_geographic_organization_coordsys_id));
		srs.setDefinition(resources
				.getString(R.string.geopackage_srs_undefined_geographic_definition));
		srs.setDescription(resources
				.getString(R.string.geopackage_srs_undefined_geographic_description));
		create(srs);

        return srs;
	}

    /**
     * Creates the Web Mercator Spatial Reference System if it does not already exist
     *
     * @return
     * @throws SQLException
     */
    public SpatialReferenceSystem createWebMercator(Context context) throws SQLException {

        Resources resources = context.getResources();
        SpatialReferenceSystem srs = new SpatialReferenceSystem();
        srs.setSrsName(resources
                .getString(R.string.geopackage_srs_web_mercator_srs_name));
        srs.setSrsId(resources.getInteger(R.integer.geopackage_srs_web_mercator_srs_id));
        srs.setOrganization(resources
                .getString(R.string.geopackage_srs_web_mercator_organization));
        srs.setOrganizationCoordsysId(resources
                .getInteger(R.integer.geopackage_srs_web_mercator_organization_coordsys_id));
        srs.setDefinition(resources
                .getString(R.string.geopackage_srs_web_mercator_definition));
        srs.setDescription(resources
                .getString(R.string.geopackage_srs_web_mercator_description));
        create(srs);

        return srs;
    }

    /**
     * Get or Create the Spatial Reference System for the provided id
     * @param context
     * @param srsId
     * @return
     */
    public SpatialReferenceSystem getOrCreate(Context context, long srsId) throws SQLException{

        SpatialReferenceSystem srs = queryForId(srsId);

        if(srs == null) {
            switch ((int) srsId) {
                case 4326:
                    srs = createWgs84(context);
                    break;
                case -1:
                    srs = createUndefinedCartesian(context);
                    break;
                case 0:
                    srs = createUndefinedGeographic(context);
                    break;
                case 3857:
                    srs = createWebMercator(context);
                    break;
                default:
                    throw new GeoPackageException("Spatial Reference System not supported for metadata creation: " + srsId);
            }
        }

        return srs;
    }

	/**
	 * Delete the Spatial Reference System, cascading
	 * 
	 * @param srs
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(SpatialReferenceSystem srs) throws SQLException {
		int count = 0;

		if (srs != null) {

			// Delete Contents
			ForeignCollection<Contents> contentsCollection = srs.getContents();
			if (!contentsCollection.isEmpty()) {
				ContentsDao dao = getContentsDao();
				dao.deleteCascade(contentsCollection);
			}

			// Delete Geometry Columns
			GeometryColumnsDao geometryColumnsDao = getGeometryColumnsDao();
			if (geometryColumnsDao.isTableExists()) {
				ForeignCollection<GeometryColumns> geometryColumnsCollection = srs
						.getGeometryColumns();
				if (!geometryColumnsCollection.isEmpty()) {
					geometryColumnsDao.delete(geometryColumnsCollection);
				}
			}

			// Delete Tile Matrix Set
			TileMatrixSetDao tileMatrixSetDao = getTileMatrixSetDao();
			if (tileMatrixSetDao.isTableExists()) {
				ForeignCollection<TileMatrixSet> tileMatrixSetCollection = srs
						.getTileMatrixSet();
				if (!tileMatrixSetCollection.isEmpty()) {
					tileMatrixSetDao.delete(tileMatrixSetCollection);
				}
			}

			// Delete
			count = delete(srs);
		}
		return count;
	}

	/**
	 * Delete the collection of Spatial Reference Systems, cascading
	 * 
	 * @param srsCollection
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(Collection<SpatialReferenceSystem> srsCollection)
			throws SQLException {
		int count = 0;
		if (srsCollection != null) {
			for (SpatialReferenceSystem srs : srsCollection) {
				count += deleteCascade(srs);
			}
		}
		return count;
	}

	/**
	 * Delete the Spatial Reference Systems matching the prepared query,
	 * cascading
	 * 
	 * @param preparedDelete
	 * @return
	 * @throws SQLException
	 */
	public int deleteCascade(
			PreparedQuery<SpatialReferenceSystem> preparedDelete)
			throws SQLException {
		int count = 0;
		if (preparedDelete != null) {
			List<SpatialReferenceSystem> srsList = query(preparedDelete);
			count = deleteCascade(srsList);
		}
		return count;
	}

	/**
	 * Delete a Spatial Reference System by id, cascading
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public int deleteByIdCascade(Long id) throws SQLException {
		int count = 0;
		if (id != null) {
			SpatialReferenceSystem srs = queryForId(id);
			if (srs != null) {
				count = deleteCascade(srs);
			}
		}
		return count;
	}

	/**
	 * Delete the Spatial Reference Systems with the provided ids, cascading
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
	 * Get or create a Contents DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	private ContentsDao getContentsDao() throws SQLException {
		if (contentsDao == null) {
			contentsDao = DaoManager
					.createDao(connectionSource, Contents.class);
		}
		return contentsDao;
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

}
