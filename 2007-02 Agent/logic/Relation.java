package logic;

public class Relation extends Sentence {
	public String name;
	public Term[] arguments;
	
	public Relation(String name, Term ... arguments) {
		this.name = name;
		this.arguments = arguments.clone();
	}
}