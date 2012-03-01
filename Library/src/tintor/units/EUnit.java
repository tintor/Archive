package tintor.units;

public final class EUnit extends Unit {
	// Interface
	@Override
	public String toString() {
		return name;
	}

	// Implementation
	private final String name;
	private final double value;
	private final SUnit siunit;

	EUnit(String name, double value, Unit unit) {
		this.name = name;
		this.value = value / unit.value();
		this.siunit = unit.siunit();
	}

	@Override
	protected double value() {
		return value;
	}

	@Override
	protected SUnit siunit() {
		return siunit;
	}
}