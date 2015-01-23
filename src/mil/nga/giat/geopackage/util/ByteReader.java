package mil.nga.giat.geopackage.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Read through a byte array
 * 
 * @author osbornb
 */
public class ByteReader {

	/**
	 * Character set
	 */
	private static final String CHAR_SET = "UTF-8";

	/**
	 * Next byte index to read
	 */
	private int nextByte = 0;

	/**
	 * Bytes to read
	 */
	private final byte[] bytes;

	/**
	 * Byte order
	 */
	private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	/**
	 * Constructor
	 * 
	 * @param bytes
	 */
	public ByteReader(byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * Get the next byte to be read
	 * 
	 * @return
	 */
	public int getNextByte() {
		return nextByte;
	}

	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	/**
	 * Read a String from the provided number of bytes
	 * 
	 * @param num
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String readString(int num) throws UnsupportedEncodingException {
		String value = new String(bytes, nextByte, num, CHAR_SET);
		nextByte += num;
		return value;
	}

	/**
	 * Read a byte
	 * 
	 * @return
	 */
	public byte readByte() {
		byte value = bytes[nextByte];
		nextByte++;
		return value;
	}

	/**
	 * Read an integer
	 * 
	 * @return
	 */
	public int readInt() {
		int value = ByteBuffer.wrap(bytes, nextByte, 4).order(byteOrder)
				.getInt();
		nextByte += 4;
		return value;
	}

	/**
	 * Read a double
	 * 
	 * @return
	 */
	public double readDouble() {
		double value = ByteBuffer.wrap(bytes, nextByte, 8).order(byteOrder)
				.getDouble();
		nextByte += 8;
		return value;
	}

}
