package tintor.rigidbody.main;

import tintor.rigidbody.main.worlds.Wall;
import tintor.rigidbody.model.World;

public class ConsoleMain {
	public static void main(final String[] args) {
		while (true) {
			final World w = new Wall();
			w.step(2000);
			System.out.println(w.timer);
		}
	}
}