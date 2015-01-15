package mil.nga.giat.geopackage.data.c1;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Spatial Reference System object
 * 
 * @author osbornb
 */
@DatabaseTable(tableName = "gpkg_spatial_ref_sys")
public class SpatialReferenceSystem {

	@DatabaseField(columnName = "srs_name", canBeNull = false)
	private String srsName;

	@DatabaseField(columnName = "srs_id", id = true, canBeNull = false)
	private int srsId;

	@DatabaseField(columnName = "organization", canBeNull = false)
	private String organization;

	@DatabaseField(columnName = "organization_coordsys_id", canBeNull = false)
	private int organizationCoordsysId;

	@DatabaseField(columnName = "definition", canBeNull = false)
	private String definition;

	@DatabaseField(columnName = "description")
	private String description;

	/**
	 * Default Constructor
	 */
	public SpatialReferenceSystem() {

	}

	public String getSrsName() {
		return srsName;
	}

	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	public int getSrsId() {
		return srsId;
	}

	public void setSrsId(int srsId) {
		this.srsId = srsId;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public int getOrganizationCoordsysId() {
		return organizationCoordsysId;
	}

	public void setOrganizationCoordsysId(int organizationCoordsysId) {
		this.organizationCoordsysId = organizationCoordsysId;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
