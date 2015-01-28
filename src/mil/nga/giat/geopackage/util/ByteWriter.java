package mil.nga.giat.geopackage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

/**
 * Write a byte array
 * 
 * @author osbornb
 */
public class ByteWriter {

	/**
	 * Output stream to write bytes to
	 */
	private final ByteArrayOutputStream os = new ByteArrayOutputStream();

	/**
	 * Byte order
	 */
	private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	/**
	 * Constructor
	 */
	public ByteWriter() {
	}

	/**
	 * Close the byte writer
	 */
	public void close() {
		try {
			os.close();
		} catch (IOException e) {
			Log.w(ByteWriter.class.getName(), "Failed to close byte writer: "
					+ e.getMessage());
		}
	}

	/**
	 * Get the byte order
	 * 
	 * @return
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Set the byte order
	 * 
	 * @param byteOrder
	 */
	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	/**
	 * Get the written bytes
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		return os.toByteArray();
	}

	/**
	 * Get the current size in bytes written
	 * 
	 * @return
	 */
	public int size() {
		return os.size();
	}

	/**
	 * Write a String
	 * 
	 * @param value
	 * @throws IOException
	 */
	public void writeString(String value) throws IOException {
		byte[] valueBytes = value.getBytes();
		os.write(valueBytes);
	}

	/**
	 * Write a byte
	 * 
	 * @param value
	 */
	public void writeByte(byte value) {
		os.write(value);
	}

	/**
	 * Write an integer
	 * 
	 * @throws IOException
	 */
	public void writeInt(int value) throws IOException {
		byte[] valueBytes = new byte[4];
		ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
				.putInt(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

	/**
	 * Write a double
	 * 
	 * @throws IOException
	 */
	public void writeDouble(double value) throws IOException {
		byte[] valueBytes = new byte[8];
		ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(byteOrder)
				.putDouble(value);
		byteBuffer.flip();
		byteBuffer.get(valueBytes);
		os.write(valueBytes);
	}

}
