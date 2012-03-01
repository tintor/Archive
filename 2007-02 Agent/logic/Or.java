package logic;

public class Or extends Sentence {
	public Sentence[] sentences;
	
	public Or(Sentence ... sentences) {
		this.sentences = sentences.clone();
	}
}