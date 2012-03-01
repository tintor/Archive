package gui.screen;

import gui.Screen;

public class ControlUnit extends Screen {
	public ControlUnit() {
		super("Control Unit");
		wire(cpu.CTRL, "37@77 D13"); //CTRL
		wire(cpu.STSYS, "63@77 D13"); //STSYS
		wire(cpu.STORE, "90@77 D13"); //STORE
		wire(cpu.OP1, "118@77 D13"); //OP1
		wire(cpu.OP2, "145@77 D13"); //OP2
		wire(cpu.JSR, "167@77 D13"); //JSR
		wire(cpu.JUMP, "190@77 D13"); //JUMP
		wire(cpu.RTI, "213@77 D13"); //RTI
		wire(cpu.RTS, "235@77 D13"); //RTS
		
		wire(cpu.val00, "307@77 D13"); //val00
		wire(cpu.val08, "334@77 D13"); //val08
		wire(cpu.val09, "360@77 D13"); //val09
		wire(cpu.val0A, "388@77 D13"); //val0A
		wire(cpu.val14, "417@77 D13"); //val14
		wire(cpu.val1B, "443@77 D13"); //val1B
		wire(cpu.val29, "468@77 D13"); //val29
		wire(cpu.val3A, "495@77 D13"); //val3A
		wire(cpu.val3B, "522@77 D13"); //val3B
		
		wire(cpu.brOPR, "316@230 R17"); //brOPR
		
		//wire(cpu.dummy, "50@344 R7"); //CLK
		wire(cpu.ldCNT, "195@335 R29 U3 R8"); //ld
		wire(cpu.incCNT, "195@354 R29 D4 R8"); //inc
		wire(cpu.run, "254@325 R23 U11 R8 23@0 D33 L23"); //run
		wire(cpu.not_halt, "255@363 R15 D12 R15 15@0 U32 L15"); //not(halt)
		wire(cpu.brOPR, "313@339 R10"); //brOPR
		wire(cpu.branch, "313@350 R10"); //branch
		wire(cpu.branchOPR, "254@337 R8 D7 R31 8@7 D8 L3"); //brOPR OR branch
		
		wire(cpu.brOPR, "309@646 U12"); //brOPR
		wire(cpu.branch, "326@646 U12"); //branch
		wire(cpu.val00, "341@646 U12"); //val00
		wire(cpu.val08, "356@646 U12"); //val08
		wire(cpu.val09, "372@646 U12"); //val09
		wire(cpu.val0A, "387@646 U12"); //val0A
		wire(cpu.val14, "406@646 U12"); //val14
		wire(cpu.val1B, "422@646 U12"); //val1B
		wire(cpu.val29, "440@646 U12"); //val29
		wire(cpu.val3A, "456@646 U12"); //val3A
		wire(cpu.val3B, "473@646 U12"); //val3B
		
		wire(cpu.L1, "284@547 R17"); //L1
		wire(cpu.L2, "284@562 R17"); //L2
		wire(cpu.regdir, "284@577 R17"); //regdir
		wire(cpu.immreg, "284@592 R17"); //immreg
		wire(cpu.not_cond, "284@607 R17"); //not(cond)
		wire(cpu.grADRCOD, "284@624 R17"); //grADRCOD
		wire(cpu.not_PREKID, "478@585 R15"); //NOT(PREKID)
		
		label(cpu.KMOP,145,140); //KMOP
		label(cpu.KMBRANCH,430,140); //KMBRANCH
		label(cpu.diCNT,270,275); //MPout
		label(cpu.CNT,85,370); //CNTout
	}
}