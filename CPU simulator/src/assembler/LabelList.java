package assembler;

import java.util.ArrayList;

public class LabelList {
	private ArrayList<Label> ll;

	public LabelList() {
		ll = new ArrayList<Label>();
	}

	private int getIndex(String label) {
		Label temp = new Label(label);
		int index = ll.indexOf(temp);
		if(index == -1) {
			ll.add(temp);
			index = ll.size() - 1; // trebalo bi da radi pravilno
		}
		return index;
	}

	public boolean isDefined(String label) {
		int index = ll.indexOf(new Label(label));
		if(index == -1) return false;
		return ll.get(index).isDefined();
	}

	public void InsertReference(String label, int pos) {
		int index = getIndex(label);
		Label l = ll.get(index);
		l.setReference(pos);
	}

	public void InsertDefinition(String label, int pos) throws DuplicateDefinitionError {
		Label l = ll.get(getIndex(label));
		if(l.isDefined()) throw new DuplicateDefinitionError();
		l.setPosition(pos);
	}

	public boolean moreLabels() {
		return !ll.isEmpty();
	}

	public Label popNextLabel() {
		if(ll.isEmpty()) return null;
		Label res = ll.get(0);
		ll.remove(0);
		return res;
	}

	public String toString() {
		return "\nSadrzaj liste:\n" + ll.toString() + "\n";
	}
}
