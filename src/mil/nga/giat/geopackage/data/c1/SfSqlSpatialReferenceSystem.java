package mil.nga.giat.geopackage.data.c1;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * SF/SQL Spatial Reference System View object
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "spatial_ref_sys", daoClass = SfSqlSpatialReferenceSystemDao.class)
public class SfSqlSpatialReferenceSystem {

	/**
	 * srid field name
	 */
	public static final String SRID = "srid";

	/**
	 * authName field name
	 */
	public static final String AUTH_NAME = "auth_name";

	/**
	 * authSrid field name
	 */
	public static final String AUTH_SRID = "auth_srid";

	/**
	 * srtext field name
	 */
	public static final String SRTEXT = "srtext";

	/**
	 * Unique identifier for each Spatial Reference System within a GeoPackage
	 */
	@DatabaseField(columnName = SRID, id = true, canBeNull = false)
	private int srid;

	/**
	 * Case-insensitive name of the defining organization e.g. EPSG or epsg
	 */
	@DatabaseField(columnName = AUTH_NAME, canBeNull = false)
	private String authName;

	/**
	 * Numeric ID of the Spatial Reference System assigned by the organization
	 */
	@DatabaseField(columnName = AUTH_SRID, canBeNull = false)
	private int authSrid;

	/**
	 * Well-known Text [32] Representation of the Spatial Reference System
	 */
	@DatabaseField(columnName = SRTEXT, canBeNull = false)
	private String srtext;

	/**
	 * Default Constructor
	 */
	public SfSqlSpatialReferenceSystem() {

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
