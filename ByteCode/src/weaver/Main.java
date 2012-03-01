package weaver;

public class Main {
	public static String timeBuildString(final int length) {
		System.out.println("Building string");
		String result = "";
		for (int i = 0; i < length; i++)
			result += (char) (i % 26 + 'a');
		return result;
	}

	public static void main(final String[] args) {
		System.out.println("Hello World!");
		timeBuildString(8000);
	}
}