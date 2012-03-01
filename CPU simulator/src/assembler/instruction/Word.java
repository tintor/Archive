package assembler.instruction;

public class Word {
	private static final String wordPattern = "[a-zA-Z_][a-zA-Z0-9_]+";
	private boolean isWord;
	private String w;

	public Word(String s) {
		w = s;
		isWord = s.matches(wordPattern);
	}

	public boolean isWord() {
		return isWord;
	}

	public String getWord() {
		return w;
	}
}
