package logic;

public class Implies extends Sentence {
	public Sentence p, q;
	
	public Implies(Sentence p, Sentence q) {
		this.p = p;
		this.q = q;
	}
}