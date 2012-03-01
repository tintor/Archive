package tintor.sokoban2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import tintor.sokoban2.cell.Loader;
import tintor.sokoban2.cell.Optimizer;
import tintor.stream.DoubleOutputStream;

public class Main {
	final static String name = "levels/original:2";

	public static void main(final String[] args) throws Exception {
		final String consoleFile = String.format("logs/%1$tF-%1$tH-%1$tM-%1$tS", new Date());
		final OutputStream consoleStream = new BufferedOutputStream(new DoubleOutputStream(System.out,
				new FileOutputStream(consoleFile)));
		System.setOut(new PrintStream(consoleStream, true));

		System.out.println(name);
		System.out.println();

		final Key level = Optimizer.optimize(Loader.load(name));
		System.out.println(level);

		Monitor.start();
		final Key result = Solver.astar(level);
		Monitor.stop();

		if (result == null)
			System.out.println("no solution!");
		else {
			System.out.println("solution");
			System.out.println(result);
			//			for (final Key k : Util.expand(result))
			//				System.out.println(k);
		}
	}
}