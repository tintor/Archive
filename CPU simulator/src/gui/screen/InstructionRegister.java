package gui.screen;

import gui.Screen;
import logic.Gate;

/**
 * @author Marko Tintor
 * @author Ivan Vukčević
 * @date 06/2006
 */
public class InstructionRegister extends Screen {
	public InstructionRegister() {
		super("Instruction register");

		wire(cpu.ldIR1, "69@35 D43 R10"); //ldIR1
		label(cpu.MDR, 150, 40); //MDR
		label(cpu.IR_1, 165, 100); //IR1

		wire(cpu.ldIR2, "286@35 D43 R10"); //ldIR2
		label(cpu.MDR, 367, 40); //MDR
		label(cpu.IR_2, 382, 100); //IR2

		wire(cpu.ldIR3, "505@35 D43 R10"); //ldIR3
		label(cpu.MDR, 584, 40); //MDR
		label(cpu.IR_3, 599, 100); //IR3

		Gate IR18 = cpu.IR.bit(18), IR19 = cpu.IR.bit(19);
		wire(cpu.IR20_23jednakoF, "26@304 D96 R81 D15"); //AND(IR23-20)
		wire(cpu.IR20_23nije0, "92@305 D12 R69 D21"); //OR(IR23-20)	A
		wire(cpu.IR20_23nijeF, "171@308 D30"); //NAND(IR23-20)			B
		wire(IR19, "217@297 L28 D34"); //IR19				C
		wire(IR18, "217@313 L18 D19"); //IR18				D
		wire(cpu.L1desnoIkolo, "180@379 D21 L51 D15"); //A*B*NOT(C)*NOT(D)
		wire(cpu.L1, "118@448 D17"); //L1
		wire(cpu.L2, "278@294 D33"); //L2

		wire(cpu.IR20_23nije0, "341@305 D18 R39 D11"); //OR(IR23-20)	E
		wire(cpu.IR20_23nijeF, "431@307 D16 L39 D11"); //NAND(IR23-20)	F
		wire(cpu.jumbo,
				"386@377 D11 L115 D32 R16 D13 -45@11 D32 R16 D13 0@11 R19 D32 R16 D13 19@11 R65 D32 R16 D13"); //E AND F
		wire(IR19, "306@419 D13"); //IR19
		wire(IR18, "312@419 D13"); //IR18
		wire(IR19, "376@419 D13"); //IR19
		wire(IR18.not(), "383@419 D7"); //NOT(IR18)
		wire(IR19.not(), "440@419 D9"); //NOT(IR19)
		wire(IR18, "447@419 D13"); //IR18
		wire(IR19.not(), "505@419 D9"); //NOT(IR19)
		wire(IR18.not(), "512@419 D9"); //NOT(IR18)

		wire(cpu.pcrel, "296@472 D22"); //pcrel
		wire(cpu.regind, "367@472 D22"); //regind
		wire(cpu.imm, "431@472 D22"); //imm
		wire(cpu.regdir, "495@472 D22"); //regdir

		wire(cpu.STORE, "533@253 R36"); //STORE	G
		wire(cpu.INC, "533@268 R50"); //INC	H
		wire(cpu.DEC, "533@282 R56"); //DEC	I
		wire(cpu.ASL, "533@298 R60"); //ASL	J
		wire(cpu.ASR, "533@315 R58"); //ASR	K
		wire(cpu.LSR, "533@330 R52"); //LSR	L
		wire(cpu.NOT, "533@346 R39"); //NOT	M
		wire(cpu.OR, "633@299 R33"); //OR(G-M)
		wire(cpu.imm, "633@271 R33"); //imm
		wire(cpu.grADR, "701@284 R19"); //grADR

		wire(cpu.grADR, "615@392 R23"); //grADR
		wire(cpu.grOPR, "615@417 R23"); //grCOD
		wire(cpu.grADRCOD, "673@407 R20"); //grADRCOD

		wire(cpu.imm, "612@463 R23"); //imm
		wire(cpu.regdir, "612@488 R23"); //regdir
		wire(cpu.immreg, "670@477 R20"); //immreg
	}
}
