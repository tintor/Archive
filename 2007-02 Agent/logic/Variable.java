package logic;

public class Variable extends Term {
	public String name;

	public Variable(String name) {
		this.name = name;
	}
	
	public Every every(Sentence s) {
		return new Every(this, s);
	}

	public Exists exists(Sentence s) {
		return new Exists(this, s);
	}
}