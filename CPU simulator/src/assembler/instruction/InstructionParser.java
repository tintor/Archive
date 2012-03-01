package assembler.instruction;

import assembler.Util;

public class InstructionParser {

	private boolean definesLabel;
	private String labelDefinition;
	private Instruction instr;

	public InstructionParser(String s) throws LabelDefinitionError, InvalidInstructionError,
			BadArgFormatError, BadArgTypeError, ArgumentMissingError {
		int pos;

		instr = null;
		definesLabel = false;

		// obrisi postojece komentare...
		s = Util.removeComment(s);

		// vidi da li je mozda definisana labela
		pos = s.indexOf(':');
		if(pos != -1) {
			// ima labele( mozda, zato i proveravas)
			// labela je korektna samo ako je rec uz ':', tako da cu tako da trazim
			String mightBeLabel = Util.skipWhitespaces(s.substring(0, pos));
			Word w = new Word(mightBeLabel);
			if(w.isWord()) {
				definesLabel = true;
				labelDefinition = w.getWord();
			} else
				throw new LabelDefinitionError();
			// obrisi labelu iz stringa instrukcije
			s = s.substring(pos + 1);
		}

		// mozda je prazan red( ili prethodno imao komentar ili labelu, ali 
		// definitivno nema instrukciju )
		s = Util.removeWhitespaces(s);

		if(!(s.length() == 0)) {
			// nadji rec, koja bi trebala da predstavlja ime instrukcije
			pos = Util.findWhitespace(s);
			if(pos == -1) {
				// ne nadjoh nigde prazno, mozda je bezadresna instrukcija
				instr = InstructionSet.GetInstruction(s);
				if(instr == null) throw new InvalidInstructionError();
			} else {
				instr = InstructionSet.GetInstruction(s.substring(0, pos));
				if(instr == null) throw new InvalidInstructionError();
				s = Util.skipWhitespaces(s.substring(pos + 1));
				if(instr.hasArgument()) {
					if(s.equals("")) throw new ArgumentMissingError();
					try {
						instr.SetArg(new Argument(s));
					} catch(BadArgFormatError e) {
						throw e;
					} catch(BadArgTypeError e) {
						throw e;
					}
				}
			}
		}
	}

	public boolean labelDefined() {
		return definesLabel;
	}

	public boolean labelAddressed() {
		if(instr == null) return false;
		return instr.hasLabel();
	}

	public String getDefinedLabel() {
		return labelDefinition;
	}

	public String getAddressedLabel() {
		return instr.getLabel();
	}

	public byte[] encode() {
		if(instr == null) return new byte[0];
		return instr.encode();
	}

	public String toString() {
		String res = "";
		if(instr == null) {
			res += "Nema instrukcije u redu;";
		} else {
			res += instr.toString() + ";";

			if(instr.hasArgument()) {
				res += instr.getArg().toString() + ";";
			}
		}
		if(definesLabel) {
			res += "definise labelu[" + labelDefinition + "];";
		}
		if(labelAddressed()) {
			res += "koristi labelu[" + getAddressedLabel() + "];";
		}
		return res;
	}
}
