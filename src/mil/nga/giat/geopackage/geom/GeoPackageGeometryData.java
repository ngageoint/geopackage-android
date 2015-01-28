package mil.nga.giat.geopackage.geom;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import mil.nga.giat.geopackage.geom.wkb.WkbGeometryReader;
import mil.nga.giat.geopackage.geom.wkb.WkbGeometryWriter;
import mil.nga.giat.geopackage.util.ByteReader;
import mil.nga.giat.geopackage.util.ByteWriter;
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
	 * Bytes
	 */
	private byte[] bytes;

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
	 * Well-Known Binary Geometry index of where the bytes start
	 */
	private int wkbGeometryIndex;

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
		fromBytes(bytes);
	}

	/**
	 * Populate the geometry data from the bytes
	 * 
	 * @param bytes
	 */
	public void fromBytes(byte[] bytes) {
		this.bytes = bytes;

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

		// Save off where the WKB bytes start
		wkbGeometryIndex = reader.getNextByte();

		// Read the Well-Known Binary Geometry if not marked as empty
		if (!empty) {
			geometry = WkbGeometryReader.readGeometry(reader);
		}

	}

	/**
	 * Write the geometry to bytes
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toBytes() throws IOException {

		ByteWriter writer = new ByteWriter();

		// Write GP as the 2 byte magic number
		writer.writeString(MAGIC);

		// Write a byte as the version, value of 0 = version 1
		writer.writeByte(VERSION_1);

		// Build and write a flags byte
		byte flags = buildFlagsByte();
		writer.writeByte(flags);
		writer.setByteOrder(byteOrder);

		// Write the 4 byte srs id int
		writer.writeInt(srsId);

		// Write the envelope
		writeEnvelope(writer);

		// Save off where the WKB bytes start
		wkbGeometryIndex = writer.size();

		// Write the Well-Known Binary Geometry if not marked as empty
		if (!empty) {
			WkbGeometryWriter.writeGeometry(writer, geometry);
		}

		// Get the bytes
		bytes = writer.getBytes();

		// Close the writer
		writer.close();

		return bytes;
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
	 * Build the flags byte from the flag values
	 * 
	 * @return envelope indicator
	 */
	private byte buildFlagsByte() {

		byte flag = 0;

		// Add the binary type to bit 5, 0 for standard and 1 for extended
		int binaryType = extended ? 1 : 0;
		flag += (binaryType << 5);

		// Add the empty geometry flag to bit 4, 0 for non-empty and 1 for
		// empty
		int emptyValue = empty ? 1 : 0;
		flag += (emptyValue << 4);

		// Add the envelope contents indicator code (3-bit unsigned integer to
		// bits 3, 2, and 1)
		int envelopeIndicator = envelope == null ? 0 : envelope.getIndicator();
		flag += (envelopeIndicator << 1);

		// Add the byte order to bit 0, 0 for Big Endian and 1 for Little
		// Endian
		int byteOrderValue = (byteOrder == ByteOrder.BIG_ENDIAN) ? 0 : 1;
		flag += byteOrderValue;

		return flag;
	}

	/**
	 * Read the envelope based upon the indicator value
	 * 
	 * @param envelopeIndicator
	 * @param reader
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

			boolean hasZ = false;
			Double minZ = null;
			Double maxZ = null;

			boolean hasM = false;
			Double minM = null;
			Double maxM = null;

			// Read z values
			if (envelopeIndicator == 2 || envelopeIndicator == 4) {
				hasZ = true;
				minZ = reader.readDouble();
				maxZ = reader.readDouble();
			}

			// Read m values
			if (envelopeIndicator == 3 || envelopeIndicator == 4) {
				hasM = true;
				minM = reader.readDouble();
				maxM = reader.readDouble();
			}

			envelope = new GeoPackageGeometryEnvelope(hasZ, hasM);

			envelope.setMinX(minX);
			envelope.setMaxX(maxX);
			envelope.setMinY(minY);
			envelope.setMaxY(maxY);

			if (hasZ) {
				envelope.setMinZ(minZ);
				envelope.setMaxZ(maxZ);
			}

			if (hasM) {
				envelope.setMinM(minM);
				envelope.setMaxM(maxM);
			}
		}

		return envelope;
	}

	/**
	 * Write the envelope bytes
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void writeEnvelope(ByteWriter writer) throws IOException {

		if (envelope != null) {

			// Write x and y values
			writer.writeDouble(envelope.getMinX());
			writer.writeDouble(envelope.getMaxX());
			writer.writeDouble(envelope.getMinY());
			writer.writeDouble(envelope.getMaxY());

			// Write z values
			if (envelope.hasZ()) {
				writer.writeDouble(envelope.getMinZ());
				writer.writeDouble(envelope.getMaxZ());
			}

			// Write m values
			if (envelope.hasM()) {
				writer.writeDouble(envelope.getMinM());
				writer.writeDouble(envelope.getMaxM());
			}
		}
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

	public void setExtended(boolean extended) {
		this.extended = extended;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	public void setSrsId(int srsId) {
		this.srsId = srsId;
	}

	public void setEnvelope(GeoPackageGeometryEnvelope envelope) {
		this.envelope = envelope;
	}

	public void setGeometry(GeoPackageGeometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * Get the bytes of the entire GeoPackage geometry including GeoPackage
	 * header and WKB bytes
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Get the GeoPackage header bytes
	 * 
	 * @return
	 */
	public byte[] getHeaderBytes() {
		byte[] headerBytes = new byte[wkbGeometryIndex];
		System.arraycopy(bytes, 0, headerBytes, 0, wkbGeometryIndex);
		return headerBytes;
	}

	/**
	 * Get the GeoPackage header bytes already ordered in a Byte Buffer
	 * 
	 * @return
	 */
	public ByteBuffer getHeaderByteBuffer() {
		return ByteBuffer.wrap(bytes, 0, wkbGeometryIndex).order(byteOrder);
	}

	/**
	 * Get the Well-Known Binary Geometry bytes
	 * 
	 * @return
	 */
	public byte[] getWkbBytes() {
		int wkbByteCount = bytes.length - wkbGeometryIndex;
		byte[] wkbBytes = new byte[wkbByteCount];
		System.arraycopy(bytes, wkbGeometryIndex, wkbBytes, 0, wkbByteCount);
		return wkbBytes;
	}

	/**
	 * Get the Well-Known Binary Geometry bytes already ordered in a Byte Buffer
	 * 
	 * @return
	 */
	public ByteBuffer getWkbByteBuffer() {
		return ByteBuffer.wrap(bytes, wkbGeometryIndex,
				bytes.length - wkbGeometryIndex).order(byteOrder);
	}

	/**
	 * Return the byte index where the Well-Known Binary bytes start
	 * 
	 * @return
	 */
	public int getWkbGeometryIndex() {
		return wkbGeometryIndex;
	}

}
