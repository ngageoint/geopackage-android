package mil.nga.geopackage.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/**
 * Conversions between Bitmaps and image bytes
 *
 * @author osbornb
 */
public class BitmapConverter {

	/**
	 * Decode the bytes to a bitmap
	 * 
	 * @param bytes
	 * 
	 * @return
	 */
	public static Bitmap toBitmap(byte[] bytes) {
		return toBitmap(bytes, null);
	}

	/**
	 * Decode the bytes to a bitmap, with options
	 * 
	 * @param bytes
	 * @param options
	 * @return
	 */
	public static Bitmap toBitmap(byte[] bytes, Options options) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
				options);
		return bitmap;
	}

	/**
	 * Compress the bitmap to a byte array at full quality
	 * 
	 * @param bitmap
	 * @param format
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(Bitmap bitmap, CompressFormat format)
			throws IOException {
		return toBytes(bitmap, format, 100);
	}

	/**
	 * Compress the bitmap to a byte array
	 * 
	 * @param bitmap
	 * @param format
	 * @param quality
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(Bitmap bitmap, CompressFormat format,
			int quality) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			bitmap.compress(format, quality, byteStream);
			bytes = byteStream.toByteArray();
		} finally {
			byteStream.close();
		}
		return bytes;
	}

}
