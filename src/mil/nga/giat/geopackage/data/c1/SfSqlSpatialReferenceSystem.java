package mil.nga.giat.geopackage.data.c1;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * SF/SQL Spatial Reference System View object
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "spatial_ref_sys")
public class SfSqlSpatialReferenceSystem {

	@DatabaseField(columnName = "srid", id = true, canBeNull = false)
	private int srid;

	@DatabaseField(columnName = "auth_name", canBeNull = false)
	private String authName;

	@DatabaseField(columnName = "auth_srid", canBeNull = false)
	private int authSrid;

	@DatabaseField(columnName = "srtext", canBeNull = false)
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
