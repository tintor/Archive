package logic;

public class And extends Sentence {
	public Sentence[] sentences;
	
	public And(Sentence ... sentences) {
		this.sentences = sentences.clone();
	}
}