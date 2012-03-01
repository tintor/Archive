package logic;

public class Not extends Sentence {
	public Sentence sentence;
	
	public Not(Sentence sentence) {
		this.sentence = sentence;
	}
}