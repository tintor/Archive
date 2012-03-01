package gui.screen;

import gui.Screen;

/**
 * @author Branislav Aleksić
 * @author Đorđe Jevđić
 * @date 05/2006
 */
public class PSW_LTI extends Screen {
	public PSW_LTI() {
		super("PSW LTI");

		wire(cpu.PSWin, "292@18 D24 L196 D313 R43");
		wire(cpu.PSWin, "96@87 R47");
		wire(cpu.PSWin, "96@165 R43");
		wire(cpu.PSWin, "96@277 R47");

		wire(cpu.PSWin, "292@18 D24 R203 D348 R41");
		wire(cpu.PSWin, "494@114 R37");
		wire(cpu.PSWin, "494@198 R41");
		wire(cpu.PSWin, "494@306 R37");

		wire(cpu.ldPSWL, "20@99 D290 R28");
		wire(cpu.ldPSWL, "20@134 R28");
		wire(cpu.ldPSWL, "20@199 R28");
		wire(cpu.ldPSWL, "20@324 R28");

		wire(cpu.prL0, "35@245 U99 R13");
		wire(cpu.prL0, "35@210 R9");

		wire(cpu.prL1, "35@436 U99 R13");
		wire(cpu.prL1, "35@400 R9");

		wire(cpu.MDR.bit(4), "153@63 L41 D113 R21"); //MDR4
		wire(cpu.MDR.bit(4), "112@98 R31");//MDR4

		wire(cpu.MDR.bit(5), "153@245 L41 D123 R21"); //MDR5
		wire(cpu.MDR.bit(5), "112@288 R31");//MDR5

		wire(cpu.MDR.bit(6), "484@163 R33 U37 R12"); //MDR6
		wire(cpu.MDR.bit(6), "517@164 D46 R12");//MDR6

		wire(cpu.MDR.bit(7), "484@354 R33 U37 R12"); //MDR7
		wire(cpu.MDR.bit(7), "517@354 D46 R12");//MDR7

		wire(cpu.setPSWL02, "83@141 R137");
		wire(cpu.resetPSWL02, "83@205 R137");

		wire(cpu.setPSWL12, "83@331 R137");
		wire(cpu.resetPSWL12, "83@396 R137");

		wire(cpu.setPSWL01, "179@93 R17 D29 R23");
		wire(cpu.resetPSWL01, "175@172 R13 D13 R34");

		wire(cpu.setPSWL11, "179@283 R17 D29 R23");
		wire(cpu.resetPSWL11, "175@362 R13 D13 R34");

		wire(cpu.setPSWL0, "248@133 R44");
		wire(cpu.resetPSWL0, "248@197 R44");
		wire(cpu.setPSWL1, "248@323 R44");
		wire(cpu.resetPSWL1, "248@387 R44");

		wire(cpu.PSWL0, "385@133 L12");
		wire(cpu.PSWL1, "385@324 L12");

		wire(cpu.stPSWT, "578@141 R29");
		wire(cpu.clPSWT, "578@186 R29");

		wire(cpu.stPSWI, "578@331 R29");
		wire(cpu.clPSWI, "578@376 R29");

		wire(cpu.setPSWT0, "565@122 R42");
		wire(cpu.resetPSWT0, "568@204 R39");
		wire(cpu.setPSWI0, "565@312 R42");
		wire(cpu.resetPSWI0, "568@396 R39");

		wire(cpu.setPSWT, "635@133 R57");
		wire(cpu.resetPSWT, "635@197 R57");
		wire(cpu.setPSWI, "635@324 R57");
		wire(cpu.resetPSWI, "635@388 R57");

		wire(cpu.PSWT, "784@133 L12");
		wire(cpu.PSWI, "784@324 L12");

		wire(cpu.ldCONTROL, "128@495 R13");
		wire(cpu.TRPD, "128@519 R13");
		wire(cpu.clPSWT0, "181@509 R19 U10 R31");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.ack, "200@477 R31");
		wire(cpu.clPSWT, "260@489 R16");

		wire(cpu.ldCONTROL, "128@556 R13");
		wire(cpu.TRPE, "128@581 R13");
		wire(cpu.stPSWT, "182@570 R15");

		wire(cpu.ldCONTROL, "568@495 R13");
		wire(cpu.INTD, "568@519 R13");
		wire(cpu.clPSWI0, "622@509 R19 U10 R31");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.ack, "640@477 R31");
		wire(cpu.clPSWI, "700@489 R16");

		wire(cpu.ldCONTROL, "568@556 R13");
		wire(cpu.INTE, "568@581 R13");
		wire(cpu.stPSWI, "622@570 R15");
	}
}