package tintor.units;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import tintor.patterns.Flyweight;

@Flyweight @SuppressWarnings("unchecked") public final class SUnit extends Unit {
	private final static Map<Map<SUnit, Integer>, SUnit> composite = new HashMap<Map<SUnit, Integer>, SUnit>();
	private final static Map<String, SUnit> base = new HashMap<String, SUnit>();

	// Constants
	public static SUnit None = new SUnit();

	public static SUnit Mass = base("kg");
	public static SUnit Distance = base("m");
	public static SUnit Angle = base("rad");
	public static SUnit Time = base("s");

	public static SUnit Surface = Distance.pow(2);
	public static SUnit Volume = Distance.pow(3);

	public static SUnit Velocity = Distance.div(Time).name("m/s");
	public static SUnit Acceleration = Velocity.div(Time).name("m/s^2");
	public static SUnit Impulse = Mass.mul(Velocity).name("kg_m/s");
	public static SUnit Force = Mass.mul(Acceleration).name("kg_m/s^2");
	public static SUnit Energy = Force.mul(Distance).name("J");
	public static SUnit Power = Energy.div(Time).name("W");

	public static SUnit AngVelocity = Angle.div(Time).name("rad/s");
	public static SUnit AngAcceleration = AngVelocity.div(Time).name("rad/s^2");
	public static SUnit InertialMoment = Mass.mul(Distance, 2);
	public static SUnit Torque = InertialMoment.mul(AngAcceleration);

	// Interface
	public SUnit pow(int power) {
		return pow(power, 1);
	}

	public SUnit pow(int nom, int den) {
		if (den <= 0) throw new RuntimeException();
		if (nom == 0) return None;
		if (nom == 1 && den == 1) return this;

		Map<SUnit, Integer> m = empty();
		for (Map.Entry<SUnit, Integer> e : units.entrySet()) {
			int p = e.getValue() * nom;
			if (p % den != 0) throw new RuntimeException();
			m.put(e.getKey(), p / den);
		}
		return get(m);
	}

	public SUnit sqrt() {
		return pow(1, 2);
	}

	public SUnit inv() {
		return pow(-1, 1);
	}

	public SUnit mul(SUnit unit) {
		return mul(unit, 1);
	}

	public SUnit mul(SUnit... units) {
		if (units.length == 0) return this;

		Map<SUnit, Integer> m = empty();
		multiply(m, 1);
		for (SUnit u : units)
			u.multiply(m, 1);
		return get(m);
	}

	public SUnit mul(SUnit unit, int power) {
		if (unit == None) return this;

		Map<SUnit, Integer> m = empty();
		multiply(m, 1);
		unit.multiply(m, power);
		return get(m);
	}

	public SUnit div(SUnit unit) {
		return mul(unit, -1);
	}

	public SUnit div(SUnit... units) {
		if (units.length == 0) return this;

		Map<SUnit, Integer> m = empty();
		multiply(m, 1);
		for (SUnit u : units)
			u.multiply(m, -1);

		return get(m);
	}

	public SUnit name(String name) {
		if (this.name == null) this.name = name;
		return this;
	}

	@Override public String toString() {
		return name != null ? name : canonical();
	}

	public String canonical() {
		if (canonical == null) {
			StringBuilder b = new StringBuilder();
			for (SUnit u : units.keySet()) {
				if (b.length() > 0) b.append('_');
				b.append(u.name);
				int p = units.get(u);
				if (p != 1) b.append('^').append(p);
			}
			canonical = b.toString();
		}
		return canonical;
	}

	// Static Interface
	public static SUnit base(String name) {
		SUnit u = base.get(name);
		if (u == null) {
			u = new SUnit(name);
			base.put(name, u);
		}
		return u;
	}

	public static SUnit multiply(SUnit... units) {
		if (units.length == 0) return None;
		if (units.length == 1) return units[0];

		Map<SUnit, Integer> m = empty();
		for (SUnit u : units)
			u.multiply(m, 1);
		return get(m);
	}

	// Implementation
	private String name, canonical;
	private final Map<SUnit, Integer> units;

	private SUnit() {
		name = canonical = "";
		units = Collections.EMPTY_MAP;
	}

	private SUnit(String name) {
		this.name = canonical = name;
		units = Collections.singletonMap(this, 1);
	}

	private SUnit(Map<SUnit, Integer> units) {
		this.units = units;
	}

	@Override protected double value() {
		return 1;
	}

	@Override protected SUnit siunit() {
		return this;
	}

	private void multiply(Map<SUnit, Integer> m, int power) {
		for (Map.Entry<SUnit, Integer> e : units.entrySet()) {
			SUnit u = e.getKey();
			int p = power * e.getValue();
			if (!m.containsKey(u))
				m.put(u, p);
			else {
				int np = m.get(u) + p;
				if (np == 0)
					m.remove(u);
				else
					m.put(u, np);
			}
		}
	}

	// Static Implementation
	static SUnit get(Map<SUnit, Integer> m) {
		if (m.size() <= 1) {
			if (m.size() == 0) return None;

			Map.Entry<SUnit, Integer> e = m.entrySet().iterator().next();
			if (e.getValue() == 1) return e.getKey();
		}

		SUnit u = composite.get(m);
		if (u == null) {
			u = new SUnit(m);
			composite.put(m, u);
		}
		return u;
	}

	static Map<SUnit, Integer> empty() {
		return new IdentityHashMap<SUnit, Integer>();
	}
}