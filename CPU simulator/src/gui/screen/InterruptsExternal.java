package gui.screen;

import gui.Screen;

public class InterruptsExternal extends Screen {
	public InterruptsExternal() {
		super("Interrupts - external");

		wire(cpu.inm, "106@54 R60"); //inm
		wire(cpu.Finm, "253@54 R78"); //Finm
		wire(cpu.ackPRINM, "112@123 R55 26@0 D52 R254 U64 R15"); // ackPRINM
		wire(cpu.S_PRINM, "371@45 R39"); //Finm AND ldINTEXT
		wire(cpu.PRINM, "494@45 R13"); //PRINM

		wire(cpu.intr3, "106@226 R60"); //intr3
		wire(cpu.Fintr3, "253@226 R78"); //Fintr3
		wire(cpu.ackPER3, "108@296 R61 30@0 D52 R254 U61 R25"); // ackPER3
		wire(cpu.S_PRPER3, "373@217 R39"); //Fintr3 AND ldINTEXT
		wire(cpu.PRPER3, "499@217 R13"); //PRPER3

		wire(cpu.intr2, "106@399 R60"); //intr2
		wire(cpu.Fintr2, "253@399 R81"); //Fintr2
		wire(cpu.ackPER2, "108@468 R58 30@0 D52 R254 U63 R21"); // ackPER2
		wire(cpu.S_PRPER2, "374@390 R39"); //Fintr2 AND ldINTEXT
		wire(cpu.PRPER2, "500@390 R13"); //PRPER2

		wire(cpu.intr1, "106@572 R60"); //intr1
		wire(cpu.Fintr1, "253@572 R81"); //Fintr1
		wire(cpu.ackPER1, "108@641 R58 30@0 D52 R254 U61 R21"); // ackPER1
		wire(cpu.S_PRPER1, "374@562 R39"); //Fintr1 AND ldINTEXT
		wire(cpu.PRPER1, "500@562 R13"); //PRPER1

		wire(cpu.ldINTEXT, "230@11 R64 D538 R40 64@20 R37 64@193 R37 64@365 R40"); // ldINTEXT
	}
}