package logic;

public class Exists extends Sentence {
	// public boolean exactlyOne;
	public Variable var;
	public Sentence sentence;	

	public Exists(Variable var, Sentence sentence) {
		this.var = var;
		this.sentence = sentence;
	}
}