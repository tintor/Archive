package logic;

public class Sentence {
	public Sentence implies(Sentence q) {
		return new Implies(this, q);
	}

	public Sentence not() {
		return new Not(this);
	}

	public Sentence or(Sentence b) {
		return new Or(this, b);
	}

	public Sentence and(Sentence b) {
		return new And(this, b);
	}
}