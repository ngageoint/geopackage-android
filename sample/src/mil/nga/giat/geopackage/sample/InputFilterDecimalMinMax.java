package mil.nga.giat.geopackage.sample;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Input filter to force a double min and max range
 */
public class InputFilterDecimalMinMax implements InputFilter {

	private double min, max;

	public InputFilterDecimalMinMax(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public InputFilterDecimalMinMax(String min, String max) {
		this.min = Double.parseDouble(min);
		this.max = Double.parseDouble(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		try {
			String value = dest.subSequence(0, dstart).toString()
					+ source.subSequence(start, end)
					+ dest.subSequence(dend, dest.length());
			if (value.isEmpty() || value.equals("-")) {
				return null;
			}
			double input = Double.parseDouble(value);
			if (isInRange(min, max, input)) {
				return null;
			}
		} catch (NumberFormatException nfe) {
		}
		return dest.subSequence(dstart, dend);
	}

	private boolean isInRange(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}

}
