package mil.nga.giat.geopackage.data.c3;

/**
 * Geometry Type enumeration (Spec Requirement 25)
 * 
 * @author osbornb
 */
public enum GeometryType {

	/**
	 * The root of the geometry type hierarchy
	 */
	GEOMETRY(0),

	/**
	 * A single location in space. Each point has an X and Y coordinate. A point
	 * MAY optionally also have a Z and/or an M value.
	 */
	POINT(1),

	/**
	 * A Curve that connects two or more points in space.
	 */
	LINESTRING(2),

	/**
	 * A restricted form of CurvePolygon where each ring is defined as a simple,
	 * closed LineString.
	 */
	POLYGON(3),

	/**
	 * A restricted form of GeometryCollection where each Geometry in the
	 * collection must be of type Point.
	 */
	MULTIPOINT(4),

	/**
	 * A restricted form of MultiCurve where each Curve in the collection must
	 * be of type LineString.
	 */
	MULTILINESTRING(5),

	/**
	 * A restricted form of MultiSurface where each Surface in the collection
	 * must be of type Polygon.
	 */
	MULTIPOLYGON(6),

	/**
	 * A collection of zero or more Geometry instances.
	 */
	GEOMETRYCOLLECTION(7),

	/**
	 * Circular String, Curve sub type
	 */
	CIRCULARSTRING(8),

	/**
	 * Compound Curve, Curve sub type
	 */
	COMPOUNDCURVE(9),

	/**
	 * A planar surface defined by an exterior ring and zero or more interior
	 * ring. Each ring is defined by a Curve instance.
	 */
	CURVEPOLYGON(10),

	/**
	 * A restricted form of GeometryCollection where each Geometry in the
	 * collection must be of type Curve.
	 */
	MULTICURVE(11),

	/**
	 * A restricted form of GeometryCollection where each Geometry in the
	 * collection must be of type Surface.
	 */
	MULTISURFACE(12),

	/**
	 * The base type for all 1-dimensional geometry types. A 1-dimensional
	 * geometry is a geometry that has a length, but no area. A curve is
	 * considered simple if it does not intersect itself (except at the start
	 * and end point). A curve is considered closed its start and end point are
	 * coincident. A simple, closed curve is called a ring.
	 */
	CURVE(13),

	/**
	 * The base type for all 2-dimensional geometry types. A 2-dimensional
	 * geometry is a geometry that has an area.
	 */
	SURFACE(14);

	/**
	 * Geometry type code
	 */
	private final int code;

	/**
	 * Constructor
	 * 
	 * @param code
	 */
	private GeometryType(int code) {
		this.code = code;
	}

	/**
	 * Get the name, just use the enum name since they are the same
	 * 
	 * @return
	 */
	public String getName() {
		return name();
	}

	/**
	 * Get the code
	 * 
	 * @return
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get the Geometry Type from the code
	 * 
	 * @param code
	 * @return
	 */
	public static GeometryType fromCode(int code) {
		GeometryType geometryType = null;

		switch (code) {
		case 0:
			geometryType = GEOMETRY;
			break;
		case 1:
			geometryType = POINT;
			break;
		case 2:
			geometryType = LINESTRING;
			break;
		case 3:
			geometryType = POLYGON;
			break;
		case 4:
			geometryType = MULTIPOINT;
			break;
		case 5:
			geometryType = MULTILINESTRING;
			break;
		case 6:
			geometryType = MULTIPOLYGON;
			break;
		case 7:
			geometryType = GEOMETRYCOLLECTION;
			break;
		case 8:
			geometryType = CIRCULARSTRING;
			break;
		case 9:
			geometryType = COMPOUNDCURVE;
			break;
		case 10:
			geometryType = CURVEPOLYGON;
			break;
		case 11:
			geometryType = MULTICURVE;
			break;
		case 12:
			geometryType = MULTISURFACE;
			break;
		case 13:
			geometryType = CURVE;
			break;
		case 14:
			geometryType = SURFACE;
			break;
		}

		return geometryType;
	}

}
