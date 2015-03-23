package mil.nga.giat.geopackage.metadata;

import java.util.Locale;

/**
 * Metadata Scope type enumeration from spec Table 15
 * 
 * @author osbornb
 */
public enum MetadataScopeType {

	UNDEFINED("undefined", "NA", "Metadata information scope is undefined"),

	FIELD_SESSION("fieldSession", "012",
			"Information applies to the field session"),

	COLLECTION_SESSION("collectionSession", "004",
			"Information applies to the collection session"),

	SERIES("series", "006", "Information applies to the (dataset) series"),

	DATASET("dataset", "005",
			"Information applies to the (geographic feature) dataset"),

	FEATURE_TYPE("featureType", "010",
			"Information applies to a feature type (class)"),

	FEATURE("feature", "009", "Information applies to a feature (instance)"),

	ATTRIBUTE_TYPE("attributeType", "002",
			"Information applies to the attribute class"),

	ATTRIBUTE("attribute", "001",
			"Information applies to the characteristic of a feature (instance)"),

	TILE("tile", "016",
			"Information applies to a tile, a spatial subset of geographic data"),

	MODEL(
			"model",
			"015",
			"Information applies to a copy or imitation of an existing or hypothetical object"),

	CATALOG("catalog", "NA", "Metadata applies to a feature catalog"),

	SCHEMA("schema", "NA", "Metadata applies to an application schema"),

	TAXONOMY("taxonomy", "NA",
			"Metadata applies to a taxonomy or knowledge system"),

	SOFTWARE("software", "013",
			"Information applies to a computer program or routine"),

	SERVICE(
			"service",
			"014",
			"Information applies to a capability which a service provider entity makes available to a service user entity through a set of interfaces that define a behaviour, such as a use case"),

	COLLECTION_HARDWARE("collectionHardware", "003",
			"Information applies to the collection hardware class"),

	NON_GEOGRAPHIC_DATASET("nonGeographicDataset", "007",
			"Information applies to non-geographic data"),

	DIMENSION_GROUP("dimensionGroup", "008",
			"Information applies to a dimension group");

	/**
	 * Name
	 */
	private final String name;

	/**
	 * Code
	 */
	private final String code;

	/**
	 * Definition
	 */
	private final String definition;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param code
	 * @param definition
	 */
	private MetadataScopeType(String name, String code, String definition) {
		this.name = name;
		this.code = code;
		this.definition = definition;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getDefinition() {
		return definition;
	}

	/**
	 * Get the metadata scope from the name
	 * 
	 * @param name
	 * @return
	 */
	public static MetadataScopeType fromName(String name) {

		StringBuilder enumName = new StringBuilder();

		for (String part : name.split("(?<!^)(?=[A-Z])")) {
			if (enumName.length() > 0) {
				enumName.append("_");
			}
			enumName.append(part.toUpperCase(Locale.US));
		}

		return MetadataScopeType.valueOf(enumName.toString());
	}

}
