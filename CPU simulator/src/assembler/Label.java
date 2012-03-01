package assembler;

import java.util.ArrayList;

public class Label {
	private boolean defined;
	private int position;
	private String ln;
	ArrayList<Integer> posList;

	public final static int NO_REFS = -1;

	public Label(String LabelName) {
		ln = LabelName;
		defined = false;
		posList = new ArrayList<Integer>();
	}

	public void setReference(int pos) {
		posList.add(new Integer(pos));
	}

	public void setPosition(int pos) {
		defined = true;
		position = pos;
	}

	public boolean equals(Object a) {
		if(!a.getClass().getName().equals(this.getClass().getName())) return false;
		return (ln.equals(((Label)a).ln));
	}

	public boolean isDefined() {
		return defined;
	}

	public String getName() {
		return ln;
	}

	public int getPosition() {
		return position;
	}

	public int getNextReference() {
		if(posList.isEmpty()) return NO_REFS; // posto ne moze da bude negativno mesto
		Integer res = posList.get(0); // referisanja labele, koristicu to kao gresku
		posList.remove(0);
		return res.intValue();
	}

	public String toString() {
		return "" + ln + ": defined=" + defined + "; refcount: " + posList.size();
	}
}
