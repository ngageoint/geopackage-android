package mil.nga.giat.geopackage.core.srs;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * SF/SQL {@link SpatialReferenceSystem} View object
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "spatial_ref_sys", daoClass = SpatialReferenceSystemSfSqlDao.class)
public class SpatialReferenceSystemSfSql {

	/**
	 * Table name
	 */
	public static final String TABLE_NAME = "spatial_ref_sys";

	/**
	 * srid field name
	 */
	public static final String COLUMN_SRID = "srid";

	/**
	 * id field name, srid
	 */
	public static final String COLUMN_ID = COLUMN_SRID;

	/**
	 * authName field name
	 */
	public static final String COLUMN_AUTH_NAME = "auth_name";

	/**
	 * authSrid field name
	 */
	public static final String COLUMN_AUTH_SRID = "auth_srid";

	/**
	 * srtext field name
	 */
	public static final String COLUMN_SRTEXT = "srtext";

	/**
	 * Unique identifier for each Spatial Reference System within a GeoPackage
	 */
	@DatabaseField(columnName = COLUMN_SRID, id = true, canBeNull = false)
	private int srid;

	/**
	 * Case-insensitive name of the defining organization e.g. EPSG or epsg
	 */
	@DatabaseField(columnName = COLUMN_AUTH_NAME, canBeNull = false)
	private String authName;

	/**
	 * Numeric ID of the Spatial Reference System assigned by the organization
	 */
	@DatabaseField(columnName = COLUMN_AUTH_SRID, canBeNull = false)
	private int authSrid;

	/**
	 * Well-known Text [32] Representation of the Spatial Reference System
	 */
	@DatabaseField(columnName = COLUMN_SRTEXT, canBeNull = false)
	private String srtext;

	/**
	 * Default Constructor
	 */
	public SpatialReferenceSystemSfSql() {

	}

	public int getId() {
		return srid;
	}

	public void setId(int id) {
		this.srid = id;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public String getAuthName() {
		return authName;
	}

	public void setAuthName(String authName) {
		this.authName = authName;
	}

	public int getAuthSrid() {
		return authSrid;
	}

	public void setAuthSrid(int authSrid) {
		this.authSrid = authSrid;
	}

	public String getSrtext() {
		return srtext;
	}

	public void setSrtext(String srtext) {
		this.srtext = srtext;
	}

}
