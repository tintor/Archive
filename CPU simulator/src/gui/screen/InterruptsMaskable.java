package gui.screen;

import gui.Screen;

public class InterruptsMaskable extends Screen {
	public InterruptsMaskable() {
		super("Interrupts - maskable");
		wire(cpu.IMR1,"193@52 R206 D39");
		wire(cpu.IMR2,"193@35 R128 D57");
		wire(cpu.IMR3,"193@18 R46 D75");
		
		wire(cpu.PRPER3,"499@18 L233 D75");
		wire(cpu.PRPER2,"499@35 L150 D57");
		wire(cpu.PRPER1,"499@52 L73 D39");
		
		wire(cpu.PRPER,"57@341 R24");
		
		wire(cpu.PSWI,"156@340 L25");
		
		wire(cpu.intIMR3,"252@136 D117 R101 D67");//IZLAZ IZ AND GDE JE IMR3 I PRPER3		
		wire(cpu.intIMR3,"252@168 L76 D61");//ISTO TO SAMO GRANANJE
		
		wire(cpu.intIMR2,"333@136 D89 R48 D95");//IZLAZ IZ AND GDE JE IMR2 I PRPER2
		wire(cpu.intIMR2,"333@185 L140 D49");//ISTO TO SAMO GRANANJE
		
		wire(cpu.intIMR1,"412@136 D184");//IZLAZ IZ AND GDE JE IMR1 I PRPER1
		wire(cpu.intIMR1,"412@202 L202 D28");//ISTO TO SAMO GRANANJE
		
		wire(cpu.intIMR,"193@270 D56 L61");//IZLAZ IZ OR KOLA
		
		wire(cpu.intG,"429@647 D31 L253 U321 L44"); //IZLAZ G IZ KOMPARATORA
		
		wire(cpu.prL0,"567@490 L137");
		wire(cpu.prL0,"430@436 D90 L20 D40");
		
		wire(cpu.prL1,"566@465 L204");
		wire(cpu.prL1,"362@436 D90 R20 D40");
		
		wire(cpu.PSWL0,"512@538 D27");
		wire(cpu.PSWL1,"485@538 D27");
		
		
	}
}