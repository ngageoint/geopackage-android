package mil.nga.giat.geopackage.tiles.user;

import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.user.UserDao;
import android.database.sqlite.SQLiteDatabase;

/**
 * Tile DAO for reading tile user tables
 * 
 * @author osbornb
 */
public class TileDao extends UserDao<TileTable, TileRow, TileCursor> {

	/**
	 * Tile Matrix Set
	 */
	private final TileMatrixSet tileMatrixSet;

	/**
	 * Tile Matrix
	 */
	private final List<TileMatrix> tileMatrix;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param tileMatrixSet
	 * @param tileMatrix
	 * @param table
	 */
	public TileDao(SQLiteDatabase db, TileMatrixSet tileMatrixSet,
			List<TileMatrix> tileMatrix, TileTable table) {
		super(db, table);

		this.tileMatrixSet = tileMatrixSet;
		this.tileMatrix = tileMatrix;
		if (tileMatrixSet.getContents() == null) {
			throw new GeoPackageException(TileMatrixSet.class.getSimpleName()
					+ " " + tileMatrixSet.getId() + " has null "
					+ Contents.class.getSimpleName());
		}
		if (tileMatrixSet.getSrs() == null) {
			throw new GeoPackageException(TileMatrixSet.class.getSimpleName()
					+ " " + tileMatrixSet.getId() + " has null "
					+ SpatialReferenceSystem.class.getSimpleName());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileRow newRow() {
		return new TileRow(getTable());
	}

	/**
	 * Get the tile matrix set
	 * 
	 * @return
	 */
	public TileMatrixSet getTileMatrixSet() {
		return tileMatrixSet;
	}

	/**
	 * Get the tile matrix list
	 * 
	 * @return
	 */
	public List<TileMatrix> getTileMatrix() {
		return tileMatrix;
	}

}
