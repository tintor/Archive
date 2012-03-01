package tintor.units;

import tintor.geometry.Vector3;

public abstract class Unit {
	// Interface
	public double convert(final double v, final Unit b) {
		if (this == b) return v;
		if (siunit() != b.siunit()) throw new RuntimeException("inconvertable");
		return v * value() / b.value();
	}

	public Vector3 convert(final Vector3 v, final Unit b) {
		if (this == b) return v;
		if (siunit() != b.siunit()) throw new RuntimeException("inconvertable");
		return v.mul(value() / b.value());
	}

	public Unit mul(final double a, final String name) {
		return new EUnit(name, value() * a, siunit());
	}

	public Unit div(final Unit a, final String name) {
		if (this instanceof SUnit && a instanceof SUnit) return ((SUnit) this).div((SUnit) a);
		return new EUnit(name, value() / a.value(), siunit().div(a.siunit()));
	}

	@Override public abstract String toString();

	// Implementation
	protected abstract double value();

	protected abstract SUnit siunit();
}