package tintor.commander;

import org.eclipse.swt.widgets.Display;

public class Application {
	public static boolean running = true;

	public static void main(final String[] args) {
		final Display display = new Display();
		@SuppressWarnings("unused") final Main main = new Main();
		while (running)
			if (!display.readAndDispatch()) display.sleep();
		Icons.dispose();
		display.dispose();
	}
}