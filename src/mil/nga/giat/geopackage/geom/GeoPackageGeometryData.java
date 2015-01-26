package mil.nga.giat.geopackage.geom;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import mil.nga.giat.geopackage.geom.wkb.WkbGeometryReader;
import mil.nga.giat.geopackage.util.ByteReader;
import mil.nga.giat.geopackage.util.GeoPackageException;

/**
 * GeoPackage Geometry Data
 * 
 * @author osbornb
 * 
 */
public class GeoPackageGeometryData {

	/**
	 * Expected magic number
	 */
	private static final String MAGIC = "GP";

	/**
	 * Expected version 1 value
	 */
	private static final byte VERSION_1 = 0;

	/**
	 * True if an extended geometry, false if standard
	 */
	private boolean extended;

	/**
	 * True if the geometry is empty
	 */
	private boolean empty;

	/**
	 * Byte ordering, big or little endian
	 */
	private ByteOrder byteOrder;

	/**
	 * Spatial Reference System Id
	 */
	private int srsId;

	/**
	 * Envelope
	 */
	private GeoPackageGeometryEnvelope envelope;

	/**
	 * Geometry
	 */
	private GeoPackageGeometry geometry;

	/**
	 * Constructor
	 * 
	 * @param bytes
	 */
	public GeoPackageGeometryData(byte[] bytes) {

		ByteReader reader = new ByteReader(bytes);

		// Get 2 bytes as the magic number and validate
		String magic = null;
		try {
			magic = reader.readString(2);
		} catch (UnsupportedEncodingException e) {
			throw new GeoPackageException(
					"Unexpected GeoPackage Geometry magic number character encoding: Expected: "
							+ MAGIC);
		}
		if (!magic.equals(MAGIC)) {
			throw new GeoPackageException(
					"Unexpected GeoPackage Geometry magic number: " + magic
							+ ", Expected: " + MAGIC);
		}

		// Get a byte as the version and validate, value of 0 = version 1
		byte version = reader.readByte();
		if (version != VERSION_1) {
			throw new GeoPackageException(
					"Unexpected GeoPackage Geometry version: " + version
							+ ", Expected: " + VERSION_1);
		}

		// Get a flags byte and then read the flag values
		byte flags = reader.readByte();
		int envelopeIndicator = readFlags(flags);
		reader.setByteOrder(byteOrder);

		// Read the 5th - 8th bytes as the srs id
		srsId = reader.readInt();

		// Read the envelope
		envelope = readEnvelope(envelopeIndicator, reader);

		// Read the Well-Known Binary Geometry
		geometry = WkbGeometryReader.readGeometry(reader);

	}

	/**
	 * Read the flags from the flag byte and return the envelope indicator
	 * 
	 * @param flags
	 * @return envelope indicator
	 */
	private int readFlags(byte flags) {

		// Verify the reserved bits at 7 and 6 are 0
		int reserved7 = (flags >> 7) & 1;
		int reserved6 = (flags >> 6) & 1;
		if (reserved7 != 0 || reserved6 != 0) {
			throw new GeoPackageException(
					"Unexpected GeoPackage Geometry flags. Flag bit 7 and 6 should both be 0, 7="
							+ reserved7 + ", 6=" + reserved6);
		}

		// Get the binary type from bit 5, 0 for standard and 1 for extended
		int binaryType = (flags >> 5) & 1;
		extended = binaryType == 1;

		// Get the empty geometry flag from bit 4, 0 for non-empty and 1 for
		// empty
		int emptyValue = (flags >> 4) & 1;
		empty = emptyValue == 1;

		// Get the envelope contents indicator code (3-bit unsigned integer from
		// bits 3, 2, and 1)
		int envelopeIndicator = (flags >> 1) & 7;
		if (envelopeIndicator > 4) {
			throw new GeoPackageException(
					"Unexpected GeoPackage Geometry flags. Envelope contents indicator must be between 0 and 4. Actual: "
							+ envelopeIndicator);
		}

		// Get the byte order from bit 0, 0 for Big Endian and 1 for Little
		// Endian
		int byteOrderValue = flags & 1;
		byteOrder = byteOrderValue == 0 ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN;

		return envelopeIndicator;
	}

	/**
	 * Read the envelope based upon the indicator value
	 * 
	 * @param envelopeIndicator
	 * @return
	 */
	private GeoPackageGeometryEnvelope readEnvelope(int envelopeIndicator,
			ByteReader reader) {

		GeoPackageGeometryEnvelope envelope = null;

		if (envelopeIndicator > 0) {

			// Read x and y values and create envelope
			double minX = reader.readDouble();
			double maxX = reader.readDouble();
			double minY = reader.readDouble();
			double maxY = reader.readDouble();

			envelope = new GeoPackageGeometryEnvelope(minX, maxX, minY, maxY);

			// Read and set z values
			if (envelopeIndicator == 2 || envelopeIndicator == 4) {
				double minZ = reader.readDouble();
				double maxZ = reader.readDouble();

				envelope.setMinZ(minZ);
				envelope.setMaxZ(maxZ);
			}

			// Read and set m values
			if (envelopeIndicator == 3 || envelopeIndicator == 4) {
				double minM = reader.readDouble();
				double maxM = reader.readDouble();

				envelope.setMinM(minM);
				envelope.setMaxM(maxM);
			}
		}

		return envelope;
	}

	public boolean isExtended() {
		return extended;
	}

	public boolean isEmpty() {
		return empty;
	}

	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public int getSrsId() {
		return srsId;
	}

	public GeoPackageGeometryEnvelope getEnvelope() {
		return envelope;
	}

	public GeoPackageGeometry getGeometry() {
		return geometry;
	}

}
