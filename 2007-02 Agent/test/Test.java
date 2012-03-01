package test;

import logic.Term;
import logic.And;
import logic.Every;
import logic.Implies;
import logic.Relation;
import logic.Sentence;
import logic.Variable;

public class Test {
	public static Relation Enemy(Term ... terms) {
		return new Relation("Enemy", terms);
	}

	public static Relation Ally(Term ... terms) {
		return new Relation("Ally", terms);
	}
	
	public static void main() {
		Variable x = new Variable("x"), y = new Variable("y"), z = new Variable("z");
		Sentence s = x.every(y.every(z.every(Enemy(x, y).and(Enemy(y, z)).implies(Ally(x, z)))));
	}
}