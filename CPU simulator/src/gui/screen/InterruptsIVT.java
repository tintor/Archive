package gui.screen;

import gui.Screen;

public class InterruptsIVT extends Screen {
	public InterruptsIVT() {
		super("Interrupts - IVT");
		
		wire(cpu.int4CDW,"114@19 U15 R202 D52 R120");//IZLAZ W IZ CD
		wire(cpu.PRPER,"402@75 R39");
		wire(cpu.INT,"402@95 R33");
		wire(cpu.not_PREKID,"493@74 R32");//NEGACIJA OD PREKID
		
		wire(cpu.PSWT,"52@34 R14");
		wire(cpu.PRINM,"52@57 R14");
		wire(cpu.PRADR,"52@80 R14");
		wire(cpu.PRCOD,"52@103 R14");
		
		wire(cpu.int4CD0,"166@48 R51 D81"); //IZLAZ 0 IZ CD
		wire(cpu.int4CD1,"166@91 R24 D39");//IZLAZ 1 IZ CD
		
		wire(cpu.prL0,"278@109 D22");
		wire(cpu.prL1,"252@109 D22");
		
		wire(cpu.PRCOD,"561@172 L25");
		wire(cpu.PRADR,"561@186 L28");
		wire(cpu.PRINM,"561@201 L25");
		wire(cpu.PRNIJEINTERNI,"495@186 L25");//IZLAZ IZ PRETHODNA TRI
		
		wire(cpu.PRPER,"501@218 U8 L29");
		wire(cpu.PREXT,"428@198 L85 D47 R236 D106 R56");//IZLAZ IZ OR ZA PRETHODNA DVA
		wire(cpu.PREXT,"293@245 R168 D65");//ISTO TO SAMO GRANANJE
		
		wire(cpu.INT,"544@428 R153");
		wire(cpu.INT,"579@415 D12");
		
		wire(cpu.ack,"679@380 D35 R18");
		wire(cpu.ack,"650@401 R30");
		wire(cpu.ackPRINT,"734@421 R18");//ackINT
		
		wire(cpu.ackPRINM,"751@286 L30");
		wire(cpu.ackPRADR,"751@300 L30");
		wire(cpu.ackPRCOD,"751@315 L30");
		wire(cpu.ackPER1,"751@343 L30");
		wire(cpu.ackPER2,"751@357 L30");
		wire(cpu.ackPER3,"751@372 L30");
		
		wire(cpu.ldSYSREG,"82@417 R18");
		wire(cpu.STIVTP,"82@438 R18");
		wire(cpu.ldIVTP,"139@428 R26");//IZLAZ IZ AND OD PRETHODNA DVA
		
		wire(cpu.int4MP1out.bit(0),"599@292 R37"); 
		// Djole dodao razdvojene ulaze u dekoder
		wire(cpu.int4MP1out.bit(1),"599@321 R37");
		
		

		wire(cpu.ack,"585@556 R50");
		wire(cpu.PREXT,"585@578 R50");
		wire(cpu.ldPSWL,"673@567 R22");
		
		
		label(cpu.int4CD,208,168); //ULAZ 0 U MP
		label(cpu.prL,270,168); //ULAZ 1 U MP
		label(cpu.int4MP1out,244,306); //IZLAZ IZ MP
		
		label(cpu.int4MP2levo,457,352); //ULAZ 0 U MP
		label(cpu.IR_2,521,352); //ULAZ 1 U MP
		label(cpu.int4MP2out,491,489); //ULAZ IZ MP
		
		label(cpu.ACC,235,388); //ULAZ U IVTP
		label(cpu.IVTP,235,453); //IZLAZ IZ IVTP
		
		label(cpu.int4ADDdesno,325,538); //ULAZ 1 U ADD
		label(cpu.ADRIVT,290,593); //IZLAZ IZ ADD



		
	}
}