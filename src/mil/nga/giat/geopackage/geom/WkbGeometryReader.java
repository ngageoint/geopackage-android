package mil.nga.giat.geopackage.geom;

import java.nio.ByteOrder;

import mil.nga.giat.geopackage.data.c3.GeometryType;
import mil.nga.giat.geopackage.util.ByteReader;

/**
 * Well Known Binary reader
 * 
 * @author osbornb
 */
public class WkbGeometryReader {

	/**
	 * Read a geometry from the byte reader
	 * 
	 * @param reader
	 * @return
	 */
	public static GeoPackageGeometry readGeometry(ByteReader reader) {

		// Read the single byte order byte
		byte byteOrderValue = reader.readByte();
		ByteOrder byteOrder = byteOrderValue == 0 ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN;

		// Read the geometry type integer
		int geometryTypeWkbCode = reader.readInt();

		// Look at the last 2 digits to find the geometry type code (1 - 14)
		int geometryTypeCode = geometryTypeWkbCode % 1000;

		// Look at the first digit to find the options (z when 1 or 3, m when 2
		// or 3)
		int geometryTypeMode = geometryTypeWkbCode / 1000;

		// Determine if the geometry has a z (3d) or m (linear referencing
		// system) value
		boolean hasZ = false;
		boolean hasM = false;
		switch (geometryTypeMode) {
		case 0:
			break;

		case 1:
			hasZ = true;
			break;

		case 2:
			hasM = true;
			break;

		case 3:
			hasZ = true;
			hasM = true;
			break;
		}

		GeometryType geometryType = GeometryType.fromCode(geometryTypeCode);

		GeoPackageGeometry geometry = new GeoPackageGeometry();

		switch (geometryType) {

		case POINT:
			geometry = readPoint(reader, hasZ, hasM);
			break;
		// TODO
		default:
			// TODO exception
		}

		return geometry;
	}

	/**
	 * Read a point
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackagePoint readPoint(ByteReader reader, boolean hasZ,
			boolean hasM) {

		double x = reader.readDouble();
		double y = reader.readDouble();

		GeoPackagePoint point = new GeoPackagePoint(x, y);

		if (hasZ) {
			double z = reader.readDouble();
			point.setZ(z);
		}

		if (hasM) {
			double m = reader.readDouble();
			point.setM(m);
		}

		return point;
	}

}
