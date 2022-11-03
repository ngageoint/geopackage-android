package mil.nga.geopackage.dgiwg;

/**
 * DGIWG Example Create parts
 * 
 * @author osbornb
 */
public class DGIWGExampleCreate {

	/**
	 * Create features
	 */
	public boolean features = false;

	/**
	 * Create tiles
	 */
	public boolean tiles = false;

	/**
	 * Create optional national metadata
	 */
	public boolean nationalMetadata = false;

	/**
	 * Create optional features metadata
	 */
	public boolean featuresMetadata = false;

	/**
	 * Create optional tiles metadata
	 */
	public boolean tilesMetadata = false;

	/**
	 * Create schema
	 */
	public boolean schema = false;

	/**
	 * Create coverage data
	 */
	public boolean coverage = false;

	/**
	 * Create related tables media
	 */
	public boolean relatedMedia = false;

	/**
	 * Create related tables tiles
	 */
	public boolean relatedTiles = false;

	/**
	 * Create the base
	 * 
	 * @return create
	 */
	public static DGIWGExampleCreate base() {
		return new DGIWGExampleCreate();
	}

	/**
	 * Create all parts
	 * 
	 * @return create
	 */
	public static DGIWGExampleCreate all() {
		DGIWGExampleCreate create = featuresAndTiles();
		create.nationalMetadata = true;
		create.featuresMetadata = true;
		create.tilesMetadata = true;
		create.schema = true;
		create.coverage = true;
		create.relatedMedia = true;
		create.relatedTiles = true;
		return create;
	}

	/**
	 * Create the base
	 * 
	 * @return create
	 */
	public static DGIWGExampleCreate featuresAndTiles() {
		DGIWGExampleCreate create = base();
		create.features = true;
		create.tiles = true;
		return create;
	}

	/**
	 * Create features
	 * 
	 * @return create
	 */
	public static DGIWGExampleCreate features() {
		DGIWGExampleCreate create = base();
		create.features = true;
		return create;
	}

	/**
	 * Create tiles
	 * 
	 * @return create
	 */
	public static DGIWGExampleCreate tiles() {
		DGIWGExampleCreate create = base();
		create.tiles = true;
		return create;
	}

}
