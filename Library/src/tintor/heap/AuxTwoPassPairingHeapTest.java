package tintor.heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class AuxTwoPassPairingHeapTest {
	public static void main(final String[] args) {
		test2();
	}

	public static void test1() {
		final Random r = new Random();
		final int n = 1100000;

		final Queue<Integer> queue = new AuxTwoPassPairingHeap<Integer>();
		for (int i = 0; i < n; i++)
			queue.offer(r.nextInt(Integer.MAX_VALUE));
		final long time = System.nanoTime();
		while (!queue.isEmpty())
			queue.poll();
		System.out.println(System.nanoTime() - time);

		System.gc();

		final List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			list.add(r.nextInt(Integer.MAX_VALUE));
		final long time2 = System.nanoTime();
		Collections.sort(list);
		System.out.println(System.nanoTime() - time2);
	}

	public static void test2() {
		final Queue<String> queue = new AuxTwoPassPairingHeap<String>();
		queue.offer("Marko");
		queue.offer("Ananas");
		queue.offer("Banana");
		while (!queue.isEmpty())
			System.out.println(queue.poll());
	}
}