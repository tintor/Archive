
package gui.screen;

import gui.Screen;

public class ControlSignals extends Screen {
	public ControlSignals() {
		super("Control Signals");
		wire(cpu.bruncnd, "95@46 R76 D37 R17"); //bruncnd
		wire(cpu.brL1, "95@61 R7"); //brL1			A
		wire(cpu.L1, "95@74 R7"); //L1			B
		wire(cpu.brL2, "95@92 R7"); //brL2			C
		wire(cpu.L2, "95@105 R7"); //L2			D
		wire(cpu.brgrADRCOD, "95@122 R7"); //brgrADRCOD	E
		wire(cpu.grADRCOD, "95@135 R7"); //grADRCOD	F
		wire(cpu.brregdir, "95@153 R7"); //brregdir	G
		wire(cpu.regdir, "95@166 R7"); //regdir		H
		wire(cpu.brimmreg, "95@184 R7"); //brimmreg	I
		wire(cpu.immreg, "95@197 R7"); //immreg		J
		wire(cpu.brnotcond, "95@215 R7"); //brnotcond	K
		wire(cpu.not_cond, "95@228 R7"); //notcond		L
		wire(cpu.brnotPREKID, "95@247 R7"); //brnotPREKID	M
		wire(cpu.not_PREKID, "95@260 R7"); //notPREKID	N
		wire(cpu.brL1.and(cpu.L1), "137@68 R20 D33 R31"); //A*B
		wire(cpu.brL2.and(cpu.L2), "137@99 R12 D23 R39"); //C*D
		wire(cpu.brgrADRCOD.and(cpu.grADRCOD), "137@129 R12 D7 R42"); //E*F
		wire(cpu.brregdir.and(cpu.regdir), "137@160 R13 U6 R41"); //G*H
		wire(cpu.brimmreg.and(cpu.immreg), "137@191 R16 U22 R35"); //I*J
		wire(cpu.brnotcond.and(cpu.not_cond), "137@222 R12 U29 R39"); //K*L
		wire(cpu.brnotPREKID.and(cpu.not_PREKID), "137@253 R33 U42 R17"); //M*N
		wire(cpu.branch, "234@145 R42"); //branch
		
		wire(cpu.T0C, "119@315 R39"); //T0C
		wire(cpu.T12, "119@330 R39"); //T12
		wire(cpu.T14, "119@345 R17 D10 R22"); //T14
		wire(cpu.T1B, "119@360 R9 D7 R35"); //T1B
		wire(cpu.T27, "119@375 R44"); //T27
		wire(cpu.T2A, "119@390 R9 U7 R35"); //T2A
		wire(cpu.T32, "119@406 R17 U10 R22"); //T32
		wire(cpu.T39, "119@421 R39"); //T39
		wire(cpu.T3A, "119@436 R39"); //T3A
		wire(cpu.brnotPREKID, "199@375 R55"); //brnotPREKID
		
		wire(cpu.T08, "323@17 R115"); //T08
		wire(cpu.T04, "323@38 R115"); //T04
		wire(cpu.T06, "323@61 R115"); //T06
		wire(cpu.T03, "323@84 R115"); //T03
		wire(cpu.T0E, "323@107 R115"); //T0E
		wire(cpu.T16, "323@130 R115"); //T16
		wire(cpu.T1D, "323@153 R115"); //T1D
		wire(cpu.T31, "323@175 R115"); //T31
		
		wire(cpu.T09, "369@230 R39"); //T09
		wire(cpu.T0A, "369@245 R39"); //T0A
		wire(cpu.T0D, "369@260 R17 D10 R22"); //T0D
		wire(cpu.T13, "369@275 R9 D7 R35"); //T13
		wire(cpu.T15, "369@290 R44"); //T15
		wire(cpu.T1C, "369@306 R9 U8 R35"); //T1C
		wire(cpu.T28, "369@321 R17 U10 R22"); //T28
		wire(cpu.T2B, "369@336 R39"); //T2B
		wire(cpu.T33, "369@351 R39"); //T33
		wire(cpu.val3B, "449@290 R50"); //val3B
		
		wire(cpu.T0B, "369@382 R41"); //T0B
		wire(cpu.T0C, "369@397 R41"); //T0C
		wire(cpu.T12, "369@412 R41"); //T12
		wire(cpu.T14, "369@425 R25 D10 R16"); //T14
		wire(cpu.T1B, "369@437 R18 D11 R27"); //T1B
		wire(cpu.T27, "369@449 R11 D7 R36"); //T27
		wire(cpu.T2A, "369@462 R45"); //T2A
		wire(cpu.T32, "369@482 R24 U7 R16"); //T32
		wire(cpu.T39, "369@497 R41"); //T39
		wire(cpu.T3A, "369@513 R41"); //T3A
		wire(cpu.T46, "369@528 R41"); //T46
		wire(cpu.val00, "449@455 R68"); //val00
		
		wire(cpu.T09, "578@17 R41"); //T09
		wire(cpu.T0A, "578@32 R41"); //T0A
		wire(cpu.T0B, "578@47 R41"); //T0B
		wire(cpu.T0D, "578@60 R25 D10 R16"); //T0D
		wire(cpu.T13, "578@72 R18 D11 R27"); //T13
		wire(cpu.T15, "578@83 R11 D7 R36"); //T15
		wire(cpu.T1C, "578@97 R45"); //T1C
		wire(cpu.T28, "578@117 R24 U7 R16"); //T28
		wire(cpu.T2B, "578@132 R41"); //T2B
		wire(cpu.T33, "578@147 R41"); //T33
		wire(cpu.T46, "578@162 R41"); //T46
		wire(cpu.bruncnd, "658@89 R48"); //bruncnd
		
		wire(cpu.T04, "521@218 R135"); //T04
		wire(cpu.T06, "521@241 R135"); //T06
		wire(cpu.T03, "521@264 R135"); //T03
		wire(cpu.T16, "521@287 R135"); //T16
		
		wire(cpu.T0E, "520@309 R42"); //T0E
		wire(cpu.T1D, "520@324 R42"); //T1D
		wire(cpu.brregdir, "598@317 R60"); //brregdir
		
		wire(cpu.T31, "521@347 R135"); //T31
				
	}
}