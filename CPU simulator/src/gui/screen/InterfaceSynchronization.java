package gui.screen;

import gui.Screen;

public class InterfaceSynchronization extends Screen {
	public InterfaceSynchronization() {
		super("Interface - synchronization");

		wire(cpu.busHOLD, "56@113 R12"); //busHOLD
		wire(cpu.read, "56@133 R12"); //read
		wire(cpu.read.and(cpu.busHOLD), "103@124 R52 29@0 U38 R183"); // busHOLD AND read
		wire(cpu.in2_D_1.not(), "224@180 R70 U74 R21"); // read D FF NOTQ
		wire(cpu.rd, "350@98 R43"); //rd

		wire(cpu.busHOLD, "56@306 R12"); //busHOLD
		wire(cpu.write, "56@326 R12"); //write
		wire(cpu.write.and(cpu.busHOLD), "103@318 R52 29@0 U38 R183"); // busHOLD AND write
		wire(cpu.in2_D_2.not(), "224@373 R70 U73 R21"); // write D FF NOTQ        
		wire(cpu.wr, "350@291 R42"); //wr

		wire(cpu.busHOLD, "541@83 R16"); //busHOLD
		wire(cpu.brqSTOP.not().and(cpu.busHOLD), "587@93 R10 D11 R27"); //bus AND NOT brqSTOP
		wire(cpu.busCNT.bit(1), "671@140 D33"); //CNT2 out1
		wire(cpu.busCNT.bit(0), "694@140 D33"); //CNT2 out0
		wire(cpu.brqSTOP, "682@207 D13 L146 U119 R17 0@13 D38 -146@-78 R87"); // brqSTOP

		wire(cpu.write, "462@307 R15"); //write
		wire(cpu.read, "462@326 R17"); //read
		wire(cpu.write.or(cpu.read), "503@318 R55 32@0 U38 R182"); //read OR write
		wire(cpu.in2_D_3.not(), "627@373 R70 U73 R20"); //read OR write D FF Q complement
		wire(cpu.brqSTART, "752@291 R15");

		wire(cpu.write, "334@433 R17"); //write
		wire(cpu.read, "334@452 R19"); //read
		wire(cpu.write.or(cpu.read).not(), "391@446 R32"); //not( read OR write )
		wire(cpu.brqSTOP, "392@404 D21 R31"); // brqSTOP
		wire(cpu.run, "451@437 R58"); //run
	}
}