package tintor.heap.scrap;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import tintor.heap.scrap.HeavyLinkedPairingHeap;

public class Test {
	public static void main(String[] args) {
		final Random random = new Random();

		// warm up!
		for(int i = 0; i<1000000; i++) random.nextInt();
		
		for (int e = 4; e <= 5000000; e *= 2) {
			final int[] array = new int[e];
			for (int i = 0; i < array.length; i++)
				array[i] = random.nextInt();

			long t = test(array, new PriorityQueue<Integer>());
			long t2 = test(array, new PriorityQueue<Integer>(array.length));
			long pt = test(array, new HeavyLinkedPairingHeap<Integer>());

			System.out.printf("%d: %.2f %.2f\n", e, (double) t2 / t, (double) pt / t);
		}
	}

	public static long test(int[] array, Queue<Integer> queue) {
		array = Arrays.copyOf(array, array.length);
		System.gc();
		long t = System.nanoTime();
		
		for (int a : array)
			queue.offer(a);
		for (int i = 0; i < array.length; i++)
			queue.poll();
		
		return System.nanoTime() - t;
	}
}