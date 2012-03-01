package gui.screen;

import gui.Screen;

/**
 * @author Marko Tintor
 * @author Ivan Vukčević
 * @date 06/2006
 */
public class PC_and_SP extends Screen {
	public PC_and_SP() {
		super("PC and SP");

		wire(cpu.mxPC.bit(1), "162@116 R23"); //mxPC1
		wire(cpu.mxPC.bit(0), "162@141 R23"); //mxPC0

		wire(cpu.mxPC.bit(1), "553@116 R23"); //mxPC1
		wire(cpu.mxPC.bit(0), "553@141 R23"); //mxPC0

		wire(cpu.ldPCH, "142@289 R37"); //ldPCH
		wire(cpu.incPCH, "653@425 D15 L236 U151 L77"); //incPCH

		wire(cpu.ldPCL, "528@289 R39"); //ldPCL
		wire(cpu.incPC, "727@291 R40 25@0 D74 L91 D14"); //incPC

		wire(cpu.ldSYSREG, "77@536 R25"); //ldSYSREG
		wire(cpu.STSP, "77@563 R25"); //STSP
		wire(cpu.ldSYSREG.and(cpu.STSP), "144@551 R38"); //ldSYSREG AND STSP
		wire(cpu.incSP, "365@550 R17"); //incSP
		wire(cpu.decSP, "365@578 R17"); //decSP

		wire(cpu.ldADRTMP, "543@561 R22"); //ldADRTMP

		label(cpu.SP, 285, 595); //SPout
		label(cpu.ACC, 280, 515); //SPin

		label(cpu.PC, 660, 515); //ADRTMPin
		label(cpu.ADRTMP, 670, 590); //ADRTMPout

		label(cpu.PCH, 270, 330); //PCHout
		label(cpu.diPCH, 270, 237); //PCHin

		label(cpu.PCL, 660, 330); //PCLout
		label(cpu.diPCL, 650, 237); //PCLin
	}
}