package gui.screen;

import gui.Screen;
import logic.Gate;

/**
 * @author Marko Tintor
 * @author Ivan Vukčević
 * @date 06/2006
 */
public class ALU extends Screen {
	public ALU() {
		super("ALU");

		wire(cpu.aluOP2.or(cpu.LOAD), "325@108 R14"); //aluOP2 or LOAD
		wire(cpu.aluOP2, "360@99 R15"); //aluOP2
		wire(cpu.LOAD, "360@114 R15"); //LOAD

		wire(cpu.INC, "50@168 R42"); //INC
		wire(cpu.DEC, "50@216 R42"); //DEC
		wire(cpu.NOT, "50@265 R42"); //NOT
		wire(cpu.ASL, "50@315 R42"); //ASL
		wire(cpu.ASR.or(cpu.LSR), "34@387 U22 R58"); //ASR OR LSR
		wire(cpu.ASR, "24@437 U24"); //ASR
		wire(cpu.LSR, "46@437 U24"); //LSR
		wire(cpu.aluOP2, "73@466 U84 R20 0@-84 U50 R20 0@-133 U50 R20 0@-184 U48 R20 0@-233 U48 R20"); //aluOP2

		wire(cpu.INC.and(cpu.aluOP2), "122@178 R67 D58 R45"); //incA
		wire(cpu.DEC.and(cpu.aluOP2), "122@226 R43 D42 R80"); //decA
		wire(cpu.NOT.and(cpu.aluOP2), "122@275 R24 D23 R109"); //notA
		wire(cpu.ASL.and(cpu.aluOP2), "122@324 R142"); //shlA
		wire(cpu.ASR.or(cpu.LSR).and(cpu.aluOP2), "122@374 R45 U21 R108"); //shrA
		wire(cpu.LOAD.and(cpu.aluOP1), "213@417 R30 U37 R38"); //transferA
		wire(cpu.alu.C, "257@477 U48 R32"); //Cout

		label(cpu.X, 235, 185); //A
		label(cpu.Y, 500, 185); //B
		label(cpu.alu.F, 400, 454); //F

		wire(cpu.aluOP1, "690@397 U31 L23 0@-31 U47 L23 0@-78 U45 L23 0@-123 U48 L23 0@-172 U43 L23"); //aluOP1
		wire(cpu.ADD, "667@164 R42"); //ADD
		wire(cpu.SUB.or(cpu.CMP), "667@209 R40"); //SUB OR CMP
		wire(cpu.AND, "667@256 R42"); //AND
		wire(cpu.OR, "667@302 R42"); //OR
		wire(cpu.XOR, "667@348 R42"); //XOR
		wire(cpu.SUB, "728@200 R14"); //SUB
		wire(cpu.CMP, "728@215 R14"); //CMP

		wire(cpu.aluOP1.and(cpu.ADD), "638@172 L51 D64 L47"); //add
		wire(cpu.aluOP1.and(cpu.SUB.or(cpu.CMP)), "638@219 L37 D43 L71"); //sub
		wire(cpu.aluOP1.and(cpu.AND), "638@264 L24 D22 L92"); //and
		wire(cpu.aluOP1.and(cpu.OR), "638@310 L124"); //or
		wire(cpu.aluOP1.and(cpu.XOR), "638@356 L24 U22 L105"); //xor
		wire(cpu.X15.or(cpu.LSR), "491@385 R51"); //IR
		wire(cpu.X15, "571@377 R15"); //X15
		wire(cpu.LSR, "571@394 R15"); //LSR
		wire(cpu.C0, "486@429 R40"); //Cin

		wire(cpu.LOAD, "146@407 R37"); //LOAD
		wire(cpu.aluOP1, "146@425 R37 22@0 D79 L127 D54 R23"); //aluOP1

		wire(cpu.CMP, "40@579 R14"); //CMP
		wire(cpu.ldACC, "105@568 R25"); //ldACC

		label(cpu.B, 310, 50); //B
		label(cpu.ACC, 260, 50); //ACC
		label(cpu.alu.F, 235, 520); //ACCin
		label(cpu.ACC, 245, 605); //ACCout
		label(cpu.alu.F, 565, 520); //TEMPin
		label(cpu.TEMP, 580, 605); //TEMPout

		wire(cpu.aluOP2, "444@568 R20");

		wire(cpu.ldCONTROL, "65@708 R25"); //ldCONTROL
		wire(cpu.HALT, "65@729 R25"); //HALT
		wire(cpu.HALT.and(cpu.ldCONTROL), "130@718 R25"); //S
		wire(Gate.zero, "142@783 R10"); //init
		wire(cpu.hlt, "235@718 R10"); //hlt
		wire(cpu.hlt.not(), "235@783 R10"); //not(hlt)

		wire(cpu.ADD, "346@677 R28"); //ADD
		wire(cpu.SUB, "346@697 R28"); //SUB
		wire(cpu.CMP, "346@717 R28"); //CMP
		wire(cpu.INC, "346@738 R28"); //INC
		wire(cpu.DEC, "346@756 R28"); //DEC
		wire(cpu.ASR, "346@775 R28"); //ASR
		wire(cpu.LSR, "346@796 R28"); //LSR
		wire(cpu.ASL, "346@815 R28"); //ASL

		wire(cpu.X15, "411@653 D10"); //X15
		wire(cpu.Y15, "439@653 D10"); //Y15
		wire(cpu.X.bit(0), "467@653 D10"); //X0
		wire(cpu.alu.C, "493@653 D10");

		wire(cpu.aluN, "515@695 R22"); //aluN
		wire(cpu.aluZ, "515@731 R22"); //aluZ
		wire(cpu.aluC, "515@768 R22"); //aluC
		wire(cpu.aluV, "515@804 R22"); //aluV

		hotspot("Indicators", 373, 663, 514, 830);
	}
}