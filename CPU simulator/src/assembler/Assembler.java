package assembler;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

import assembler.instruction.ArgumentMissingError;
import assembler.instruction.BadArgFormatError;
import assembler.instruction.BadArgTypeError;
import assembler.instruction.InstructionParser;
import assembler.instruction.InvalidInstructionError;
import assembler.instruction.LabelDefinitionError;

/**
 * @author Jovan Grbić
 * @date 05/2006
 */
public class Assembler {
	public static boolean assemble(Reader rdr, byte[] memory) throws IOException {
		int errorCount = 0;

		LineNumberReader lnr = new LineNumberReader(rdr);
		// memoriju popunjavam od adrese nula...
		int PC = 0;
		LabelList ll = new LabelList();
		/// broj linije od 1
		lnr.setLineNumber(1);
		while(true) {
			String s = null;
			String ErrorMessage = null;
			//			try {
			s = lnr.readLine();
			//			} catch(IOException e) {
			//				System.err.println("Nisam uspeo da procitam red ulaza!(takoreci greska citanja)");
			//				return false;
			//			}
			if(s == null) // kraj fajla
				break;

			// prvo provera da nije neka Direktiva
			DefDirective dd = new DefDirective(s);
			if(dd.isDirective()) {
				if(dd.isValid()) {
					/// ova funkcija ispod bi trebala da pravi gresku, u slucaju duple definicije
					try {
						ll.InsertDefinition(dd.getName(), dd.getVal());
					} catch(DuplicateDefinitionError e) {
						ErrorMessage = "Redefinisanje labele";
					}
					continue;
				}
				ErrorMessage = "Lose definisana direktiva";
			}
			OrgDirective od = new OrgDirective(s);
			if(od.isDirective()) {
				if(od.isValid()) {
					PC = od.getVal();
				} else {
					ErrorMessage = "Lose definisana direktiva";
				}
				continue;
			}

			/// parsuj red kao instrukciju
			InstructionParser ip;
			try {
				ip = new InstructionParser(s);
				byte[] coded = ip.encode();
				for(int i = 0; i < coded.length; i++)
					memory[PC + i] = coded[i];
				if(ip.labelAddressed()) {
					/// !!! PAZNJA !!!
					// u naredbi ispod nalazi se hack.
					// U listu referenci zapisuje se adresa u memoriji na kojoj
					// treba da se stavi vrednost labele kada je definisana...
					// Neka je to na umu svakome ko gleda ovo !!!
					ll.InsertReference(ip.getAddressedLabel(), PC + 1);
				}
				if(ip.labelDefined()) {
					try {
						ll.InsertDefinition(ip.getDefinedLabel(), PC);
					} catch(DuplicateDefinitionError e) {
						ErrorMessage = "Redefinisanje labele";
					}
				}
				// uvecaj PC za duzinu instrukcije
				PC += coded.length;
				// i to je to, idemo na sledeci red...
			} catch(BadArgFormatError e) {
				ErrorMessage = "Los format Argumenta";
			} catch(InvalidInstructionError e) {
				ErrorMessage = "Daj mi instrukciju sledeci put";
			} catch(BadArgTypeError e) {
				ErrorMessage = "Ne dolici ti taj argument ovoj instrukciji";
			} catch(LabelDefinitionError e) {
				ErrorMessage = "Propao ti je pokusaj da napises labelu ispred...";
			} catch(ArgumentMissingError e) {
				ErrorMessage = "Zadata instrukcija treba argument, a ti joj to nisi dao";
			}
			if(ErrorMessage != null) {
				System.err.println("Linija " + lnr.getLineNumber() + ":\n" + s + '\n' + ErrorMessage);
				errorCount++;
			}
		}

		// sada treba da zamenim labele u memoriji, i da saljem poruke za svaku
		// nedefinisanu labelu
		while(ll.moreLabels()) {
			Label l = ll.popNextLabel();
			if(!l.isDefined()) {
				System.err
						.println("[" + l.getName()
								+ "] : Mozda bi mogao negde da definises labelu");
				errorCount++;
			} else {
				int pos = l.getPosition();
				while(true) {
					int ref = l.getNextReference();
					if(ref == Label.NO_REFS) break;
					//////
					/// !!!! PAZNJA !!!!
					/// drugi deo hacka!!!
					/// Ovo bi moralo da se uradi na lepsi nacin !!!
					/// pogotovu sto ako promenim da bude small-endian, ili cu na mnogo
					/// mesta da menjam redosled funkcija, ili ce ove trenutne da izgube
					/// smisao...
					memory[ref] = Util.SMALLBYTE(pos);
					memory[ref + 1] = Util.BIGBYTE(pos);
				}
			}
		}
		if(errorCount > 0) {
			System.out.println("\nBroj gresaka: " + errorCount + "\nproces neuspeo.");
			return false;
		}
		return true;
	}

	//	public static void main(String[] args) {
	//		int argCount = args.length;
	//		if(argCount < 1) {
	//			System.out.println(" Ocekujem da mi zadas ime fajla za asembliranje");
	//			return;
	//		}
	//		File inFile = new File(args[0]);
	//		FileReader fr;
	//		try {
	//			fr = new FileReader(inFile);
	//		} catch(FileNotFoundException e) {
	//			System.out.println("Nisam mogao da nadjem ulazni fajl");
	//			return;
	//		}
	//		String OutFileName = args[0] + ".out";
	//		File outFile = new File(OutFileName);
	//		FileOutputStream ofs;
	//		try {
	//			ofs = new FileOutputStream(outFile);
	//		} catch(FileNotFoundException e) {
	//			System.out.println("Ne mogu da napravim izlazni fajl...");
	//			return;
	//		}
	//		byte[] ba = assemble(fr);
	//		/// tu bi verovatno trebalo da se cekaju nekakve greske?
	//		try {
	//			ofs.write(ba);
	//		} catch(IOException e) {
	//			System.out.println("Nisam uspeo da sacuvam asemblirano sranje...");
	//		}
	//
	//	}
}