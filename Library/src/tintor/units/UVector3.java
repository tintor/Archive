package tintor.units;

import tintor.geometry.Vector3;
import tintor.patterns.Immutable;

@Immutable
public class UVector3 {
	public final Vector3 value;
	public final SUnit unit;

	public UVector3(double x, double y, double z, Unit unit) {
		this.value = new Vector3(x * unit.value(), y * unit.value(), z * unit.value());
		this.unit = unit.siunit();
	}

	public UVector3(Vector3 value, Unit unit) {
		this.value = value.mul(unit.value());
		this.unit = unit.siunit();
	}

	public UVector3 add(UVector3 a) {
		if (unit != a.unit) throw new RuntimeException("incompatible units");
		return new UVector3(value.add(a.value), unit);
	}

	public UVector3 mul(double a) {
		return new UVector3(value.mul(a), unit);
	}

	public UVector3 mul(UScalar a) {
		return new UVector3(value.mul(a.value), unit.mul(a.unit));
	}

	@Override
	public String toString() {
		return Prefix.toString(value) + unit;
	}

	// Test
	public static void main(String[] args) {
		UVector3 m = new UVector3(1e-9, 0, 0, SUnit.Velocity);
		System.out.println(m);
	}
}