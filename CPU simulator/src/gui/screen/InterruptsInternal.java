package gui.screen;

import gui.Screen;

public class InterruptsInternal extends Screen {
	public InterruptsInternal() {
		super("Interrupts - internal");

		wire(cpu.ldCONTROL, "190@22 L18"); //ldCONTROL
		wire(cpu.STIMR, "191@51 L19"); //STIMR
		wire(cpu.ldIMR, "122@39 L33 D494 R22 -33@63 R22 -33@145 R22 -33@234 R22 -33@315 R22 -33@411 R22"); //ldCONTROL AND STIMR
		//         

		wire(cpu.ACC.bit(3), "48@126 R10 D82 R48 10@0 R52"); //ACC3
		wire(cpu.SIMR3, "156@114 R25"); //IMR3 SET
		wire(cpu.RIMR3, "156@196 R26"); // IMR3 RESET 
		wire(cpu.IMR3, "285@114 R15"); // IMR3
		wire(cpu.ACC.bit(2), "48@297 R10 D82 R48 10@0 R52"); //ACC2
		wire(cpu.SIMR2, "156@285 R25"); //IMR2 SET
		wire(cpu.RIMR2, "156@367 R26"); // IMR2 RESET
		wire(cpu.IMR2, "285@285 R15"); // IMR2
		wire(cpu.ACC.bit(1), "48@475 R10 D82 R48 10@0 R52"); //ACC1
		wire(cpu.SIMR1, "156@463 R25"); //IMR1 SET
		wire(cpu.RIMR1, "156@545 R26"); // IMR1 RESET
		wire(cpu.IMR1, "285@463 R15"); // IMR1

		wire(cpu.grADR, "471@102 R23"); // grADR 
		wire(cpu.ldPRCODADR, "471@127 R23"); // ldPRCODADR
		wire(cpu.S_PRADR, "539@114 R32"); // grADR AND ldPRCODADR
		wire(cpu.ackPRADR, "557@196 R15"); // ackPRADR
		wire(cpu.PRADR, "672@114 R15"); // PRADR

		wire(cpu.grOPR, "471@278 R23"); // grCOD
		wire(cpu.ldPRCODADR, "471@302 R23"); // ldPRCODADR
		wire(cpu.S_PRCOD, "539@290 R32"); // grCOD AND ldPRCODADR
		wire(cpu.ackPRCOD, "556@372 R15"); // ackPRCOD
		wire(cpu.PRCOD, "672@290 R15"); // PRCOD

		wire(cpu.stPRINT, "556@467 R15"); // stPRINT
		wire(cpu.ackPRINT, "556@549 R15"); // ackPrint
		wire(cpu.PRINT, "672@467 R15"); // PRINT
	}
}