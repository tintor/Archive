package gui.screen;

import gui.Screen;

public class OperationalSignals extends Screen {
	public OperationalSignals() {
		super("Operational Signals");
		wire(cpu.T00, "25@49 R41"); //T00
		wire(cpu.T02, "25@64 R41"); //T02
		wire(cpu.T08, "25@79 R41"); //T08
		wire(cpu.T0E, "25@94 R41"); //T0E
		wire(cpu.T16, "25@107 R25 D10 R16"); //T16
		wire(cpu.T1D, "25@118 R18 D11 R27"); //T1D
		wire(cpu.T23, "25@130 R11 D7 R36"); //T23
		wire(cpu.T2D, "25@144 R45"); //T2D
		wire(cpu.T2F, "25@164 R24 U7 R16"); //T2F
		wire(cpu.T3C, "25@179 R41"); //T3C
		wire(cpu.T3E, "25@194 R41"); //T3E
		wire(cpu.T40, "25@209 R41"); //T40
		wire(cpu.T42, "25@224 R41"); //T42
		wire(cpu.ldMAR, "105@137 R68"); //ldMAR
		
		wire(cpu.T06, "28@246 R40"); //T06
		wire(cpu.T10, "28@261 R40"); //T10
		wire(cpu.T18, "28@276 R17 D2 R23"); //T18
		wire(cpu.T1F, "28@291 R42"); //T1F
		wire(cpu.T25, "28@306 R42"); //T25
		wire(cpu.T35, "28@325 R13 U6 R27"); //T35
		wire(cpu.T37, "28@340 R40"); //T37
		wire(cpu.T44, "28@355 R40"); //T44
		wire(cpu.incMAR, "107@299 R64"); //incMAR
		
		wire(cpu.T08, "28@380 R39"); //T08
		wire(cpu.T2D, "28@395 R24 D10 R15"); //T2D
		wire(cpu.T2F, "28@410 R16 D8 R25"); //T2F
		wire(cpu.T3C, "28@425 R42"); //T3C
		wire(cpu.T3E, "28@440 R16 U8 R25"); //T3E
		wire(cpu.T40, "28@456 R24 U10 R15"); //T40
		wire(cpu.T42, "28@471 R39"); //T42
		wire(cpu.mxMAR0, "106@425 R63"); //mxMAR0
		
		wire(cpu.T0E, "27@502 R40"); //T0E
		wire(cpu.T16, "27@517 R12 D10 R38"); //T16
		wire(cpu.T1D, "27@532 R52"); //T1D
		wire(cpu.T23, "27@548 R12 U10 R38"); //T23
		wire(cpu.T42, "27@563 R40"); //T42
		wire(cpu.mxMAR1, "105@532 R69"); //mxMAR1
		
		wire(cpu.T0E, "217@17 R39"); //T0E
		wire(cpu.T10, "217@32 R39"); //T10
		wire(cpu.T23, "217@47 R17 D10 R22"); //T23
		wire(cpu.T25, "217@62 R9 D7 R35"); //T25
		wire(cpu.T2C, "217@77 R44"); //T2C
		wire(cpu.T2F, "217@92 R9 U7 R35"); //T2F
		wire(cpu.T3B, "217@108 R17 U10 R22"); //T3B
		wire(cpu.T3E, "217@123 R39"); //T3E
		wire(cpu.T40, "217@138 R39"); //T40
		wire(cpu.ldMDR, "297@76 R55"); //ldMDR
		
		wire(cpu.T0E, "215@160 R40"); //T0E
		wire(cpu.T23, "215@175 R12 D10 R38"); //T23
		wire(cpu.T2C, "215@190 R52"); //T2C
		wire(cpu.T3B, "215@206 R12 U10 R38"); //T3B
		wire(cpu.T40, "215@221 R40"); //T40
		wire(cpu.mxMDR0, "293@190 R69"); //mxMDR0
		
		wire(cpu.T10, "219@251 R40"); //T10
		wire(cpu.T23, "219@266 R12 D10 R38"); //T23
		wire(cpu.T2F, "219@281 R52"); //T2F
		wire(cpu.T3E, "219@297 R12 U10 R38"); //T3E
		wire(cpu.T40, "219@312 R40"); //T40
		wire(cpu.mxMDR1, "297@281 R69"); //mxMDR1
		
		wire(cpu.T25, "219@334 R40"); //T25
		wire(cpu.T2C, "219@349 R17 D2 R23"); //T2C
		wire(cpu.T2F, "219@364 R42"); //T2F
		wire(cpu.T3B, "219@379 R42"); //T3B
		wire(cpu.T3E, "219@398 R13 U6 R27"); //T3E
		wire(cpu.T40, "219@413 R40"); //T40
		wire(cpu.mxMDR2, "298@372 R64"); //mxMDR2
		
		wire(cpu.T01, "221@438 R38"); //T01
		wire(cpu.T05, "221@453 R38"); //T05
		wire(cpu.T07, "221@468 R38"); //T07
		wire(cpu.T17, "221@483 R38"); //T17
		wire(cpu.T19, "221@499 R38"); //T19
		wire(cpu.T1E, "221@513 R42"); //T1E
		wire(cpu.T20, "221@528 R42"); //T20
		wire(cpu.T34, "221@540 R38"); //T34
		wire(cpu.T36, "221@558 R38"); //T36
		wire(cpu.T38, "221@573 R38"); //T38
		wire(cpu.T43, "221@588 R38"); //T43
		wire(cpu.T45, "221@604 R38"); //T45
		wire(cpu.read, "299@521 R52"); //read
		
		wire(cpu.T0F, "396@29 R39"); //T0F
		wire(cpu.T11, "396@44 R39"); //T11
		wire(cpu.T11, "396@59 R17 D10 R22"); //T24
		wire(cpu.T26, "396@74 R9 D7 R35"); //T26
		wire(cpu.T2E, "396@89 R44"); //T2E
		wire(cpu.T30, "396@104 R9 U7 R35"); //T30
		wire(cpu.T3D, "396@120 R17 U10 R22"); //T3D
		wire(cpu.T3F, "396@135 R39"); //T3F
		wire(cpu.T41, "396@150 R39"); //T41
		wire(cpu.write, "476@88 R55"); //write
		
		wire(cpu.T35, "398@169 R12 D10 R38"); //T35
		wire(cpu.T37, "398@184 R52"); //T37
		wire(cpu.T39, "398@200 R12 U10 R38"); //T39
		wire(cpu.incSP, "476@184 R69"); //incSP
		
		wire(cpu.T2C, "404@225 R40"); //T2C
		wire(cpu.T2D, "404@240 R12 D10 R38"); //T2D
		wire(cpu.T3B, "404@255 R52"); //T3B
		wire(cpu.T3C, "404@271 R12 U10 R38"); //T3C
		wire(cpu.T3E, "404@286 R40"); //T3E
		wire(cpu.decSP, "482@255 R69"); //decSP
		
		wire(cpu.T0A, "400@306 R48"); //T0A
		wire(cpu.T32, "400@321 R12 D2 R38"); //T32
		wire(cpu.T37, "400@336 R12 U2 R38"); //T37
		wire(cpu.T44, "400@351 R48"); //T44
		wire(cpu.ldPCL, "478@329 R48"); //ldPCL
		
		wire(cpu.T0A, "404@376 R41"); //T0A
		wire(cpu.T32, "404@391 R12 D2 R38"); //T32
		wire(cpu.T39, "404@407 R12 U2 R38"); //T39
		wire(cpu.T46, "404@422 R41"); //T46
		wire(cpu.ldPCH, "482@399 R48"); //ldPCH
		
		wire(cpu.T00, "404@444 R12 D10 R38"); //T00 
		wire(cpu.T06, "404@459 R52"); //T06
		wire(cpu.T08, "404@474 R12 U10 R38"); //T08
		wire(cpu.incPC, "482@459 R69"); //incPC
		
		wire(cpu.T0A, "404@497 R40"); //T0A
		wire(cpu.T37, "404@512 R12 D10 R38"); //T37
		wire(cpu.T39, "404@527 R52"); //T39
		wire(cpu.T44, "404@543 R12 U10 R38"); //T44
		wire(cpu.T46, "404@558 R40"); //T46
		wire(cpu.mxPC0, "482@528 R69"); //mxPC0
		
		wire(cpu.T0A, "400@584 R42"); //T0A
		wire(cpu.T32, "400@599 R42"); //T32
		wire(cpu.mxPC1, "479@592 R55"); //mxPC1
		
		wire(cpu.T16, "576@51 R48"); //T16
		wire(cpu.T1A, "576@66 R12 D2 R38"); //T1A
		wire(cpu.T1D, "576@81 R12 U2 R38"); //T1D
		wire(cpu.T21, "576@96 R48"); //T21
		wire(cpu.ldBH, "654@73 R45"); //ldBH
		
		wire(cpu.T16, "576@118 R41"); //T16
		wire(cpu.T18, "576@133 R12 D2 R38"); //T18
		wire(cpu.T1D, "576@149 R12 U2 R38"); //T1D
		wire(cpu.T1F, "576@164 R41"); //T1F
		wire(cpu.ldBL, "654@141 R42"); //ldBL
		
		wire(cpu.T1B, "574@187 R115"); //T1B
		
		wire(cpu.T22, "576@209 R42"); //T22
		wire(cpu.T29, "576@224 R42"); //T29
		wire(cpu.aluOP2, "654@217 R42"); //aluOP2
		
		wire(cpu.T1B, "578@247 R12 D10 R38"); //T1B
		wire(cpu.T22, "578@262 R52"); //T22
		wire(cpu.T29, "578@278 R12 U10 R38"); //T29
		wire(cpu.ldFLAGS, "656@262 R40"); //ldFLAGS
		
		wire(cpu.T14, "578@300 R42"); //T14
		wire(cpu.T2A, "578@315 R42"); //T2A
		wire(cpu.ldREG, "656@307 R40"); //ldREG
		
		wire(cpu.T2A, "578@338 R115"); //T2A
		wire(cpu.T02, "578@361 R115"); //T02
		wire(cpu.T00, "578@383 R115"); //T00
		wire(cpu.T02, "578@406 R115"); //T02
		wire(cpu.T06, "578@429 R115"); //T06
		wire(cpu.T08, "578@451 R115"); //T08
		wire(cpu.T09, "578@475 R115"); //T09
		wire(cpu.T0A, "578@497 R115"); //T0A
		wire(cpu.T0B, "578@519 R115"); //T0B
		wire(cpu.T0C, "578@543 R115"); //T0C
		wire(cpu.T35, "578@565 R115"); //T35
		wire(cpu.T42, "578@587 R115"); //T42
		
				
	}
}