package logic;

public class Every extends Sentence {
	public Variable var;
	public Sentence sentence;
	
	public Every(Variable var, Sentence sentence) {
		this.var = var;
		this.sentence = sentence;
	}
}