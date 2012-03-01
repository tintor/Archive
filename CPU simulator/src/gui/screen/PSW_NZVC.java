package gui.screen;

import gui.Screen;

/**
 * @author Branislav Aleksić
 * @author Đorđe Jevđić
 * @date 05/2006
 */
public class PSW_NZVC extends Screen {
	public PSW_NZVC() {
		super("PSW NZVC");

		wire(cpu.ldFLAGS, "32@96 D317 R28");
		wire(cpu.aluZ, "47@424 R9");
		wire(cpu.resetPSWZ2, "95@420 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "32@136 R28");
		wire(cpu.aluN, "47@246 U98 R12");
		wire(cpu.setPSWN2, "95@143 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "32@200 R28");
		wire(cpu.aluN, "47@211 R9");
		wire(cpu.resetPSWN2, "95@207 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "32@349 R28");
		wire(cpu.aluZ, "47@458 U98 R12");
		wire(cpu.setPSWZ2, "95@356 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "443@96 D317 R28");
		wire(cpu.aluV, "458@424 R9");
		wire(cpu.resetPSWV2, "506@420 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "443@136 R28");
		wire(cpu.aluC, "458@246 U98 R12");
		wire(cpu.setPSWC2, "506@143 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "443@200 R28");
		wire(cpu.aluC, "458@211 R9");
		wire(cpu.resetPSWC2, "506@207 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.ldFLAGS, "443@349 R28");
		wire(cpu.aluV, "458@454 U93 R12");
		wire(cpu.setPSWV2, "506@356 R117");//IZLAZ AND IZ PRETHODNA DVA

		wire(cpu.PSWin, "321@23 D24 L214 D333 R24");
		wire(cpu.PSWin, "107@90 R28");
		wire(cpu.PSWin, "107@166 R24");
		wire(cpu.PSWin, "107@302 R28");

		wire(cpu.PSWin, "321@23 D24 R197 D333 R24");
		wire(cpu.PSWin, "517@90 R28");
		wire(cpu.PSWin, "517@166 R24");
		wire(cpu.PSWin, "517@302 R28");

		wire(cpu.MDR.bit(0), "151@66 L32 D112 R8");//MDR0
		wire(cpu.MDR.bit(0), "118@101 R18");//MDR0

		wire(cpu.MDR.bit(1), "151@279 L32 D112 R8");//MDR1
		wire(cpu.MDR.bit(1), "118@313 R18");//MDR1

		wire(cpu.MDR.bit(2), "561@66 L32 D112 R8");//MDR2
		wire(cpu.MDR.bit(2), "529@101 R18");//MDR2

		wire(cpu.MDR.bit(3), "561@279 L32 D112 R8");//MDR3
		wire(cpu.MDR.bit(3), "529@313 R18");//MDR3

		wire(cpu.setPSWN1, "171@96 R17 D28 R23");
		wire(cpu.setPSWC1, "582@96 R17 D28 R23");
		wire(cpu.setPSWZ1, "171@309 R17 D28 R23");
		wire(cpu.setPSWV1, "582@309 R17 D28 R23");

		wire(cpu.resetPSWN1, "167@173 R13 D15 R31");
		wire(cpu.resetPSWC1, "579@173 R13 D15 R31");
		wire(cpu.resetPSWZ1, "167@386 R13 D14 R31");
		wire(cpu.resetPSWV1, "579@386 R13 D14 R31");

		wire(cpu.setPSWN, "238@136 R44");
		wire(cpu.resetPSWN, "238@198 R44");
		wire(cpu.setPSWC, "651@136 R44");
		wire(cpu.resetPSWC, "651@198 R44");
		wire(cpu.setPSWZ, "238@349 R44");
		wire(cpu.resetPSWZ, "238@411 R44");
		wire(cpu.setPSWV, "651@349 R44");
		wire(cpu.resetPSWN, "651@411 R44");

		wire(cpu.PSWN, "375@136 L12");
		wire(cpu.PSWN.not(), "375@200 L12");

		wire(cpu.PSWZ, "375@349 L12");
		wire(cpu.PSWZ.not(), "375@413 L12");

		wire(cpu.PSWC, "785@136 L12");
		wire(cpu.PSWC.not(), "785@200 L12");

		wire(cpu.PSWV, "785@349 L12");
		wire(cpu.PSWV.not(), "785@413 L12");

		wire(cpu.JZ, "121@473 R13");
		wire(cpu.PSWZ, "121@487 R13");
		wire(cpu.JZ.and(cpu.PSWZ), "163@480 R20 D30 R66");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.JNZ, "121@519 R13");
		wire(cpu.PSWZ.not(), "121@534 R13");
		wire(cpu.JNZ.and(cpu.PSWZ.not()), "163@526 R11 D15 R83");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.JV, "121@549 R13");
		wire(cpu.PSWV, "121@564 R13");
		wire(cpu.JV.and(cpu.PSWV), "163@556 R94");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.JNEG, "121@580 R13");
		wire(cpu.PSWN, "121@596 R13");
		wire(cpu.JNEG.and(cpu.PSWN), "163@588 R90");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.JC, "121@617 R13");
		wire(cpu.PSWC, "121@632 R13");
		wire(cpu.JC.and(cpu.PSWC), "163@625 R20 U20 R66");//IZLAZ IZ AND ZA PRETHODNA DVA

		wire(cpu.JSR, "211@525 R44");
		wire(cpu.JMP, "211@571 R45");

		wire(cpu.not_cond, "292@555 R10");//VELIKO NILI ZA SVE PRETHODNO
	}
}