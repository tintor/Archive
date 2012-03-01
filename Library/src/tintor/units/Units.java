package tintor.units;

public class Units {
	// Constants
	public static Unit Kilogram = SUnit.Mass;
	public static Unit Meter = SUnit.Distance;
	public static Unit Radian = SUnit.Angle;
	public static Unit Second = SUnit.Time;

	public static Unit Minute = Second.mul(60, "min");
	public static Unit Hour = Minute.mul(60, "hour");
	public static Unit Day = Hour.mul(24, "day");
	
	public static Unit MetersPerSecond = Meter.div(Second, "m/s");
	public static Unit Kilometer = Meter.mul(1000, "km");
	public static Unit KilometersPerHour = Kilometer.div(Hour, "km/h");

	public static Unit Degree = Radian.mul(Math.PI / 180, "degree");
	public static Unit PI = Radian.mul(Math.PI, "Pi");

	public static Unit Inch = Meter.mul(0.0254, "inch");
	public static Unit Mile = Meter.mul(1609.344, "mile");
	public static Unit MilesPerHour = Mile.div(Hour, "mph");
	
	// Test
	static void display(double a, Unit u, Unit q) {
		System.out.println(a + " " + u + " = " + u.convert(a, q) + " " + q.toString());
	}
	
	public static void main(String[] args) {
		display(0.5, PI, Degree);
		display(1, Hour, Second);
		display(0.5, Kilometer, Meter);
		display(72, KilometersPerHour, MetersPerSecond);
		display(100, KilometersPerHour, MilesPerHour);
	}
}