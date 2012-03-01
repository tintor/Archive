package tintor.external;

public class Main {
	public static void main(final String[] args) throws Exception {
		final ExternalSet map = ExternalSet.create("index", 10);

		final long a = System.nanoTime();
		for (int i = 0; i < 10000000; i++) {
			if (i % 100000 == 0) {
				System.out.println(i);
				System.out.flush();
			}
			map.add(Integer.toHexString(i));
		}
		System.out.println(System.nanoTime() - a);
	}
}