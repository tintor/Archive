package gui.screen;

import gui.Screen;

/**
 * @author Marko Tintor
 * @author Ivan Vukčević
 * @date 06/2006
 */
public class Registers extends Screen {
	public Registers() {
		super("Registers");

		wire(cpu.mxREG, "163@22 D13"); //mxREG

		wire(cpu.IR.bit(16), "72@190 R24"); //IR16
		wire(cpu.IR.bit(17), "72@241 R24"); //IR17
		wire(cpu.ldR0, "215@173 R79 U92 R9"); //DC0
		wire(cpu.ldR1, "215@201 R79 D29 R10"); //DC1
		wire(cpu.ldR2, "215@228 R68 D141 R21"); //DC2
		wire(cpu.ldR3, "215@255 R17 D270 R72"); //DC3
		wire(cpu.ldREG, "157@274 D15"); //ldREG

		wire(cpu.IR.bit(16), "642@256 D21"); //IR16
		wire(cpu.IR.bit(17), "659@256 D21"); //IR17

		label(cpu.ACC, 85, 35); //ACC
		label(cpu.TEMP, 85, 70); //TEMP

		label(cpu.diREG, 385, 42); //R0in
		label(cpu.R0, 400, 105); //R0out

		label(cpu.diREG, 385, 192); //R1in
		label(cpu.R1, 400, 255); //R1out

		label(cpu.diREG, 385, 330); //R2in
		label(cpu.R2, 400, 395); //R2out

		label(cpu.diREG, 385, 485); //R3in
		label(cpu.R3, 400, 548); //R3out

		label(cpu.REG, 725, 315); //REG15..0

		hotspot("ALU", 5, 27, 72, 91);
		hotspot("IR", 39, 176, 71, 248);
	}
}