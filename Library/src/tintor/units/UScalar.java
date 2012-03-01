package tintor.units;

import java.util.Locale;

import tintor.patterns.Immutable;

@Immutable public class UScalar {
	public final double value;
	public final SUnit unit;

	public UScalar(final double value, final Unit unit) {
		this.value = value * unit.value();
		this.unit = unit.siunit();
	}

	public UScalar add(final UScalar a) {
		if (unit != a.unit) throw new RuntimeException("inconvertible");
		return new UScalar(value + a.value, unit);
	}

	public UScalar mul(final UScalar a) {
		return new UScalar(value * a.value, unit.mul(a.unit));
	}

	public UScalar mul(final double a) {
		return new UScalar(value * a, unit);
	}

	@Override public String toString() {
		return Prefix.toString(value) + unit;
	}

	public String toString(final String format) {
		return Prefix.toString(value, format) + unit;
	}

	public String toString(final Unit unit2, final String format) {
		return String.format(format, unit.convert(value, unit2)) + unit2;
	}

	// Test
	public static void main(final String[] args) {
		final UScalar a = new UScalar(0.1, Units.MetersPerSecond);
		Locale.setDefault(Locale.US);
		System.out.println(a.toString("%.0f "));
		System.out.println(a.toString(Units.KilometersPerHour, "%.2f "));
	}
}