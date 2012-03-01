package gui.screen;

import gui.Screen;

/**
 * @author Marko Tintor
 * @author Ivan Vukčević
 * @date 06/2006
 */
public class InstructionDecode extends Screen {
	public InstructionDecode() {
		super("Instruction Decode");

		wire(cpu.IR.bit(16), "220@38 R15");
		wire(cpu.IR.bit(17), "220@70 R15"); //IR17
		wire(cpu.IR.bit(18), "220@102 R15"); //IR18

		wire(cpu.INT, "333@13 R32"); //INT
		wire(cpu.JSR, "333@29 R32"); //JSR
		wire(cpu.JZ, "333@45 R32"); //JZ
		wire(cpu.JNZ, "333@61 R32"); //JNZ
		wire(cpu.JV, "333@77 R32"); //JV
		wire(cpu.JC, "333@93 R32"); //JC
		wire(cpu.JNEG, "333@109 R32"); //JNEG
		wire(cpu.JMP, "333@126 R32"); //JMP

		wire(cpu.IR.bit(19), "174@142 D86 R38 0@23 R37"); //IR19
		wire(cpu.DC2, "250@171 R301 35@0 U37"); //E gornjeg DC
		wire(cpu.IR.bit(17), "445@130 R33"); //IR17
		wire(cpu.IR.bit(18), "445@153 R33"); //IR18
		wire(cpu.IR.bit(17).or(cpu.IR.bit(18)), "508@141 R27 D14 R18"); //IR17 OR IR 18
		wire(cpu.JUMP, "586@162 R27"); // JUMP

		wire(cpu.SKOK, "140@259 R54 U83 R21 54@-46 R19"); // DC srednji 0
		wire(cpu.LOAD, "140@269 R20"); // LOAD
		wire(cpu.STORE, "140@279 R20"); // STORE
		wire(cpu.NOT, "140@289 R20"); // NOT
		wire(cpu.OR, "140@299 R20"); // OR
		wire(cpu.XOR, "140@309 R20"); // XOR
		wire(cpu.AND, "140@318 R20"); // AND
		wire(cpu.ASL, "140@328 R20"); // ASL
		wire(cpu.ASR, "140@337 R20"); // ASR
		wire(cpu.LSR, "140@347 R20"); // LSR
		wire(cpu.ADD, "140@357 R20"); // ADD
		wire(cpu.SUB, "140@367 R20"); // SUB
		wire(cpu.CMP, "140@376 R20"); // CMP
		wire(cpu.INC, "140@386 R20"); // INC
		wire(cpu.DEC, "140@395 R20"); // DEC
		wire(cpu.DC3, "140@405 R95 D47"); // DC srednji F

		wire(cpu.grOPR1, "360@613 R28"); // grOPR1

		wire(cpu.IR.bit(20), "24@279 R13"); // IR20
		wire(cpu.IR.bit(21), "22@311 R15"); // IR21
		wire(cpu.IR.bit(22), "24@350 R13"); // IR22
		wire(cpu.IR.bit(23), "24@382 R13"); // IR23

		wire(cpu.IR.bit(16), "172@488 R12"); // IR16
		wire(cpu.IR.bit(17), "169@528 R15"); // IR17
		wire(cpu.IR.bit(18), "171@571 R13"); // IR18
		wire(cpu.IR.bit(19), "173@616 R11"); // IR19
		wire(cpu.grOPR2, "247@220 R68"); // DC_srednji_0 AND IR19
		wire(cpu.grOPR1, "278@268 U32 R36"); // grOPR1
		wire(cpu.grOPR, "341@228 R16"); // grOPR

		wire(cpu.RTS, "285@464 R19"); // RTS
		wire(cpu.RTI, "285@476 R19"); // RTI
		wire(cpu.INTE, "285@488 R19"); // INTE
		wire(cpu.INTD, "285@500 R19"); // INTD
		wire(cpu.TRPE, "285@512 R19"); // TRPE
		wire(cpu.TRPD, "285@524 R19"); // TRPD
		wire(cpu.STIVTP, "285@536 R19"); // STIVP
		wire(cpu.STIMR, "285@548 R19"); // STIMR
		wire(cpu.STSP, "285@560 R19"); // STSP
		wire(cpu.HALT, "285@572 R19"); // HALT
		wire(cpu.grA, "285@584 R42"); // DC donji A
		wire(cpu.grB, "285@596 R48"); // DC donji B
		wire(cpu.grC, "285@608 R50"); // DC donji C
		wire(cpu.grD, "285@620 R50"); // DC donji D
		wire(cpu.grE, "285@632 R48"); // DC donji E
		wire(cpu.grF, "285@644 R42"); // DC donji F

		wire(cpu.LOAD, "504@232 R47"); // LOAD
		wire(cpu.ADD, "504@246 R53"); // ADD
		wire(cpu.SUB, "504@261 R56"); // SUB
		wire(cpu.CMP, "504@275 R57"); // CMP
		wire(cpu.AND, "504@289 R56"); // AND
		wire(cpu.OR, "504@306 R54"); // OR
		wire(cpu.XOR, "504@322 R48"); // XOR
		wire(cpu.OP1, "585@275 R28"); // OP1

		wire(cpu.INC, "504@355 R48"); // INC
		wire(cpu.DEC, "504@369 R55"); // DEC
		wire(cpu.NOT, "504@383 R58"); // NOT
		wire(cpu.ASL, "504@398 R57"); // ASL
		wire(cpu.LSR, "504@412 R56"); // LSR
		wire(cpu.ASR, "504@426 R49"); // ASR
		wire(cpu.OP2, "585@389 R28"); // OP2

		wire(cpu.INTE, "506@477 R48"); // INTE
		wire(cpu.INTD, "506@491 R56"); // INTD
		wire(cpu.TRPE, "506@506 R58"); // TRPE
		wire(cpu.TRPD, "506@520 R58"); // TRPD
		wire(cpu.HALT, "506@534 R56"); // HALT
		wire(cpu.STIMR, "506@549 R50"); // STIMR
		wire(cpu.CTRL, "589@512 R29"); // CTRL

		wire(cpu.STIVTP, "533@596 R27"); // STIVP
		wire(cpu.STSP, "533@618 R27"); // STSP
		wire(cpu.STSYS, "585@607 R27"); // STSYS
	}
}