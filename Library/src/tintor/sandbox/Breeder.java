package tintor.sandbox;

import java.util.Arrays;
import java.util.Random;

import tintor.geometry.GMath;

public abstract class Breeder<T> {
	public static class Entry<T> implements Comparable<Entry<T>> {
		public double fitness;
		int count;
		public T object;

		public double score;

		// int fights;

		public int compareTo(final Entry<T> a) {
			return Double.compare(fitness, a.fitness);
		}
	}

	public final Entry<T>[] pool;
	public final double survival;

	@SuppressWarnings("unchecked") public Breeder(final int poolsize, final double survival) {
		this.pool = new Entry[poolsize];
		this.survival = survival;
		for (int i = 0; i < pool.length; i++) {
			final T a = spawn();
			pool[i] = new Entry<T>();
			pool[i].object = a;
			pool[i].fitness = fitness(a);
			pool[i].count = 1;
		}
		Arrays.sort(pool);
	}

	public final void iterate() {
		final Random rand = new Random();
		final int k = (int) (pool.length * survival);
		for (int i = 0; i < k; i++) {
			final double s = pool[i].fitness;
			final double f = fitness(pool[i].object);
			pool[i].fitness = Math.sqrt((f * f + s * s * pool[i].count) / (1 + pool[i].count));
			pool[i].count++;
		}
		for (int i = k; i < pool.length; i++) {
			// TODO better instances have more chances for crossover
			final int a = (int) (GMath.square(rand.nextDouble()) * k);
			int b = (int) (GMath.square(rand.nextDouble()) * k);
			while (b == a)
				b = (int) (GMath.square(rand.nextDouble()) * k);
			pool[i].object = crossover(pool[a].object, pool[b].object, pool[i].object);
			pool[i].count = 1;
			pool[i].fitness = fitness(pool[i].object);
		}

//		for (int i = 0; i < pool.length; i++) {
//			pool[i].fights = 0;
//			pool[i].score = 0;
//		}
//
//		for(int a = 0; a < 5; a++) {
//			for(int b = a+1; b < pool.length; b++) {
//				int c = compete(pool[a].object, pool[b].object);
//				double s = 0.5;
//				if(c > 0) s = 0;
//				if(c < 0) s = 1;
//
//				pool[a].score = (pool[a].score * pool[a].fights + s) / (pool[a].fights + 1);
//				pool[a].fights++;
//				pool[b].score = (pool[b].score * pool[b].fights + 1-s) / (pool[b].fights + 1);
//				pool[b].fights++;
//			}
//		}

		Arrays.sort(pool);
	}

	public abstract T spawn();

	public abstract T crossover(T a, T b, T old);

	public abstract int compete(T a, T b);

	public abstract double fitness(T a);
}