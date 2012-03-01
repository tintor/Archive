package simulator;

import logic.Bus;
import logic.D_FF;
import logic.Gate;
import logic.LowZ;
import logic.RS_FF;
import logic.Register;
import logic.Value;

/**
 * @author Marko Tintor
 * @author Đorđe Jevđić
 * @date 06/2006
 */
public class CPU {

	//Brojac koraka

	public final Register CNT = new Register(7);

	// Flip flop HLT

	public final RS_FF hlt = new RS_FF();
	public final Gate not_halt = hlt.not();
	public final RS_FF PSWL0 = new RS_FF();
	public final RS_FF PSWL1 = new RS_FF();
	public final RS_FF PSWT = new RS_FF();
	public final RS_FF PSWI = new RS_FF();
	public final Gate PSWL=Gate.merge(PSWL0, PSWL1);
	public final Register BH = new Register(8), BL = new Register(8);
	public final Gate B=Gate.merge(BH, BL);
	
	//INTERFACE 1
	
	public final RS_FF in1_RS_1 = new RS_FF(), in1_RS_2 = new RS_FF();

	
	// MORAJU DA SE STAVE I T-OVI da jer ih treba crtati
	//CONTROL UNIT STEPS
	public final Gate T00 = CNT.equal(0x00);
	public final Gate T01 = CNT.equal(0x01);
	public final Gate T02 = CNT.equal(0x02);
	public final Gate T03 = CNT.equal(0x03);
	public final Gate T04 = CNT.equal(0x04);
	public final Gate T05 = CNT.equal(0x05);
	public final Gate T06 = CNT.equal(0x06);
	public final Gate T07 = CNT.equal(0x07);
	public final Gate T08 = CNT.equal(0x08);
	public final Gate T09 = CNT.equal(0x09);
	public final Gate T0A = CNT.equal(0x0A);
	public final Gate T0B = CNT.equal(0x0B);
	public final Gate T0C = CNT.equal(0x0C);
	public final Gate T0D = CNT.equal(0x0D);
	public final Gate T0E = CNT.equal(0x0E);
	public final Gate T0F = CNT.equal(0x0F);
	public final Gate T10 = CNT.equal(0x10);
	public final Gate T11 = CNT.equal(0x11);
	public final Gate T12 = CNT.equal(0x12);
	public final Gate T13 = CNT.equal(0x13);
	public final Gate T14 = CNT.equal(0x14);
	public final Gate T15 = CNT.equal(0x15);
	public final Gate T16 = CNT.equal(0x16);
	public final Gate T17 = CNT.equal(0x17);
	public final Gate T18 = CNT.equal(0x18);
	public final Gate T19 = CNT.equal(0x19);
	public final Gate T1A = CNT.equal(0x1A);
	public final Gate T1B = CNT.equal(0x1B);
	public final Gate T1C = CNT.equal(0x1C);
	public final Gate T1D = CNT.equal(0x1D);
	public final Gate T1E = CNT.equal(0x1E);
	public final Gate T1F = CNT.equal(0x1F);
	public final Gate T20 = CNT.equal(0x20);
	public final Gate T21 = CNT.equal(0x21);
	public final Gate T22 = CNT.equal(0x22);
	public final Gate T23 = CNT.equal(0x23);
	public final Gate T24 = CNT.equal(0x24);
	public final Gate T25 = CNT.equal(0x25);
	public final Gate T26 = CNT.equal(0x26);
	public final Gate T27 = CNT.equal(0x27);
	public final Gate T28 = CNT.equal(0x28);
	public final Gate T29 = CNT.equal(0x29);
	public final Gate T2A = CNT.equal(0x2A);
	public final Gate T2B = CNT.equal(0x2B);
	public final Gate T2C = CNT.equal(0x2C);
	public final Gate T2D = CNT.equal(0x2D);
	public final Gate T2E = CNT.equal(0x2E);
	public final Gate T2F = CNT.equal(0x2F);
	public final Gate T30 = CNT.equal(0x30);
	public final Gate T31 = CNT.equal(0x31);
	public final Gate T32 = CNT.equal(0x32);
	public final Gate T33 = CNT.equal(0x33);
	public final Gate T34 = CNT.equal(0x34);
	public final Gate T35 = CNT.equal(0x35);
	public final Gate T36 = CNT.equal(0x36);
	public final Gate T37 = CNT.equal(0x37);
	public final Gate T38 = CNT.equal(0x38);
	public final Gate T39 = CNT.equal(0x39);
	public final Gate T3A = CNT.equal(0x3A);
	public final Gate T3B = CNT.equal(0x3B);
	public final Gate T3C = CNT.equal(0x3C);
	public final Gate T3D = CNT.equal(0x3D);
	public final Gate T3E = CNT.equal(0x3E);
	public final Gate T3F = CNT.equal(0x3F);
	public final Gate T40 = CNT.equal(0x40);
	public final Gate T41 = CNT.equal(0x41);
	public final Gate T42 = CNT.equal(0x42);
	public final Gate T43 = CNT.equal(0x43);
	public final Gate T44 = CNT.equal(0x44);
	public final Gate T45 = CNT.equal(0x45);
	public final Gate T46 = CNT.equal(0x46);

	// CONTROL SIGNALS OP UNIT
	public final Gate mxMDR0 = Gate.or(T0E, T23, T2C, T3B, T40);
	public final Gate mxMDR1 = Gate.or(T10, T23, T2F, T3E, T40);
	public final Gate mxMDR2 = Gate.or(T25, T2C, T2F, T3B, T3E, T40);
	public final Gate mxMDR = Gate.merge(mxMDR2, mxMDR1, mxMDR0); // CHECK

	public final Gate ldMDR = Gate.or(T0E, T10, T23, T25, T2C, T2F, T3B, T3E, T40);

	public final Gate mxMAR0 = Gate.or(T08, T2D, T2F, T3C, T3E, T40, T42);
	public final Gate mxMAR1 = Gate.or(T0E, T16, T1D, T23, T42);
	public final Gate mxMAR = Gate.merge(mxMAR1, mxMAR0);

	public final Gate ldMAR = Gate.or(T00, T02, T08, T0E, T16, T1D, T23, T2D, T2F, T3C, T3E, T40, T42);
	public final Gate incMAR = Gate.or(T06, T10, T18, T1F, T25, T35, T37, T44);

	public final Gate read = Gate.or(T01, T05, T07, T17, T19, T1E, T20, T34, T36, T38, T43, T45);
	public final Gate write = Gate.or(T0F, T11, T24, T26, T2E, T30, T3D, T3F, T41);
	public final Gate incSP = Gate.or(T35, T37, T39);
	public final Gate decSP = Gate.or(T2C, T2D, T3B, T3C, T3E);

	public final Gate ldPCL = Gate.or(T0A, T32, T37, T44);
	public final Gate ldPCH = Gate.or(T0A, T32, T39, T46);
	public final Gate mxPC0 = Gate.or(T0A, T37, T39, T44, T46);
	public final Gate mxPC1 = T0A.or(T32);
	public final Gate mxPC = Gate.merge(mxPC1, mxPC0);
	public final Gate incPC = Gate.or(T00, T06, T08);

	public final Gate ldBH = Gate.or(T16, T1A, T1D, T21);
	public final Gate ldBL = Gate.or(T16, T18, T1D, T1F);
	public final Gate aluOP1 = T1B;
	public final Gate aluOP2 = T22.or(T29);
	public final Gate ldFLAGS = Gate.or(T1B, T22, T29);

	public final Gate ldREG = T14.or(T2A);
	public final Gate mxREG = T2A;
	public final Gate ldINTEXT = T02;
	public final Gate ldADRTMP = T00;
	public final Gate ldIR1 = T02;
	public final Gate ldIR2 = T08; // NOTE zamenili smo T08 i T06
	public final Gate ldIR3 = T06;

	public final Gate stPRINT = T09;
	public final Gate ldPRCODADR = T0A;
	public final Gate ldCONTROL = T0B;
	public final Gate ldSYSREG = T0C;
	public final Gate PSWin = T35;
	public final Gate ack = T42;

	//!!!!! TODO

	//	RAZNO	
	public final Gate intr1 = Gate.zero;
	public final Gate intr2 = Gate.zero;
	public final Gate intr3 = Gate.zero;
	public final Gate inm = Gate.zero;

	// INSTRUCTION REGISTER
	public final Register IR_1 = new Register(8), IR_2 = new Register(8), IR_3 = new Register(8);
	public final Gate IR = Gate.merge(IR_1, IR_2, IR_3);
	public final Gate IR20_23 = IR.bits(20, 23);

	// Ovi dole trebaju zbog iscrtavanja

	public final Gate IR20_23jednakoF = IR20_23.equal(15);
	public final Gate IR20_23nije0 = IR20_23.notEqual(0);
	public final Gate IR20_23nijeF = IR20_23.notEqual(15);
	public final Gate L1desnoIkolo = Gate.and(IR20_23nije0, IR20_23nijeF, IR.bit(18).not(), IR.bit(19)
			.not());

	public final Gate L1 = IR20_23jednakoF.or(L1desnoIkolo);
	public final Gate L2 = IR_1.equal(0);

	public final Gate jumbo = IR20_23nije0.and(IR20_23nijeF);
	public final Gate IR18_19 = IR.bits(18, 19);
	public final Gate pcrel = IR18_19.equal(3, jumbo);
	public final Gate regind = IR18_19.equal(2, jumbo);
	public final Gate imm = IR18_19.equal(1, jumbo);
	public final Gate regdir = IR18_19.equal(0, jumbo);

	public final Gate immreg = imm.or(regdir);

	// INTERFACE 2
	public final Register busCNT=new Register(2);
	public final Gate brqSTOP =busCNT.equal(3);
	
	public final D_FF in2_D_1 = new D_FF(),  in2_D_2 = new D_FF(), in2_D_3 = new D_FF();
	public final Gate run = brqSTOP.or(write.or(read).not());
	public final Gate brqSTART = write.or(read).and(in2_D_3.not());

	public final Gate busHOLD = in1_RS_1;
	public final Gate rd = Gate.and(in1_RS_1, read, in2_D_1.not());
	public final Gate wr = Gate.and(in1_RS_1, write, in2_D_2.not());


	// INSTRUCTION DECODE
	public final Gate IR16_18 = IR.bits(16, 18);
	public final Gate IR16_19 = IR.bits(16, 19);
	public final Gate IR19_23 = IR.bits(19, 23);
	public final Gate SKOK = IR20_23.equal(0);
	public final Gate DC2 = IR19_23.equal(0);
	public final Gate DC3 = IR20_23.equal(15);

	public final Gate INT = IR16_18.equal(0, DC2);
	public final Gate JSR = IR16_18.equal(1, DC2);
	public final Gate JZ = IR16_18.equal(2, DC2);
	public final Gate JNZ = IR16_18.equal(3, DC2);
	public final Gate JV = IR16_18.equal(4, DC2);
	public final Gate JC = IR16_18.equal(5, DC2);
	public final Gate JNEG = IR16_18.equal(6, DC2);
	public final Gate JMP = IR16_18.equal(7, DC2);

	public final Gate LOAD = IR20_23.equal(1);
	public final Gate STORE = IR20_23.equal(2);
	public final Gate NOT = IR20_23.equal(3);
	public final Gate OR = IR20_23.equal(4);
	public final Gate XOR = IR20_23.equal(5);
	public final Gate AND = IR20_23.equal(6);
	public final Gate ASL = IR20_23.equal(7);
	public final Gate ASR = IR20_23.equal(8);
	public final Gate LSR = IR20_23.equal(9);
	public final Gate ADD = IR20_23.equal(10);
	public final Gate SUB = IR20_23.equal(11);
	public final Gate CMP = IR20_23.equal(12);
	public final Gate INC = IR20_23.equal(13);
	public final Gate DEC = IR20_23.equal(14);

	public final Gate RTS = IR16_19.equal(0, DC3);
	public final Gate RTI = IR16_19.equal(1, DC3);
	public final Gate INTE = IR16_19.equal(2, DC3);
	public final Gate INTD = IR16_19.equal(3, DC3);
	public final Gate TRPE = IR16_19.equal(4, DC3);
	public final Gate TRPD = IR16_19.equal(5, DC3);
	public final Gate STIVTP = IR16_19.equal(6, DC3);
	public final Gate STIMR = IR16_19.equal(7, DC3);
	public final Gate STSP = IR16_19.equal(8, DC3);
	public final Gate HALT = IR16_19.equal(9, DC3);

	public final Gate JUMP = IR.bit(17).or(IR.bit(18)).and(DC2);
	public final Gate OP1 = Gate.or(LOAD, ADD, SUB, CMP, AND, OR, XOR);
	public final Gate OP2 = Gate.or(INC, DEC, NOT, ASL, ASR, LSR);
	public final Gate CTRL = Gate.or(INTE, INTD, TRPE, TRPD, HALT, STIMR);
	public final Gate STSYS = STIVTP.or(STSP);

	// greske
	public final Gate grA = IR16_19.equal(10, DC3);
	public final Gate grB = IR16_19.equal(11, DC3);
	public final Gate grC = IR16_19.equal(12, DC3);
	public final Gate grD = IR16_19.equal(13, DC3);
	public final Gate grE = IR16_19.equal(14, DC3);
	public final Gate grF = IR16_19.equal(15, DC3);

	public final Gate grOPR1 = Gate.or(grA, grB, grC, grD, grE, grF);
	public final Gate grOPR2 = IR.bit(19).and(SKOK);
	public final Gate grOPR = grOPR1.or(grOPR2);

	// INSTRUCTION REGISTER
	public final Gate grADR0 = Gate.or(STORE, INC, DEC, ASL, ASR, LSR, NOT); // izlaz ili kola
	public final Gate grADR = grADR0.and(imm);
	public final Gate grADRCOD = grADR.or(grOPR);

	// ALU
	// alu levo
	public final Gate incA = aluOP2.and(INC);
	public final Gate decA = aluOP2.and(DEC);
	public final Gate notA = aluOP2.and(NOT);
	public final Gate shlA = aluOP2.and(ASL);
	public final Gate shrA = aluOP2.and(ASR.or(LSR));
	public final Gate transferA = aluOP1.and(LOAD);

	// alu dole
	public final Gate ldACC = CMP.not().and(aluOP1);
	public final Register ACC = new Register(16);
	public final Register TEMP = new Register(16);

	// alu gore
	public final Gate X = (aluOP2.or(LOAD)).select(ACC, B);
	public final Gate Y = B;

	// alu desno
	public final Gate add = aluOP1.and(ADD);
	public final Gate sub = aluOP1.and(SUB.or(CMP));
	public final Gate and = aluOP1.and(AND);
	public final Gate or = aluOP1.and(OR);
	public final Gate xor = aluOP1.and(XOR);
	public final Gate alu_IL = Gate.zero;
	public final Gate alu_IR = X.bit(15).and(LSR);
	public final Gate C0 = Gate.zero;

	public final ALU alu = new ALU(X, Y, incA, decA, notA, shlA, shrA, add, sub, and, or, xor, alu_IL, alu_IR, C0);

	// INTERFACE 3
	public final Gate ldMDRx = read.and(brqSTOP).or(ldMDR);
	public final Register MAR = new Register(16), MDR = new Register(8);

	// INDICATORS
	public final Gate X15 = X.bit(15), Y15 = Y.bit(15), F15 = alu.F.bit(15);
	
	public final Gate aluN = alu.F.bit(15);
	public final Gate aluZ = alu.F.equal(0);

	public final Gate aluC00 = Gate.or(SUB, CMP, DEC);
	public final Gate aluC0 = alu.C.not().and(aluC00);
	public final Gate aluC10 = ADD.or(INC);
	public final Gate aluC1 = alu.C.and(aluC10);
	public final Gate aluC2 = X.bit(15).or(ASL);
	public final Gate aluC30 = ASR.or(LSR);
	public final Gate aluC3 = X.bit(0).and(aluC30);
	public final Gate aluC = Gate.or(aluC0, aluC1, aluC2, aluC3);

	
	public final Gate aluV000 = Gate.and(X15, Y15, F15.not());	
	public final Gate aluV001 = Gate.and(X15.not(), Y15.not(), F15);
	public final Gate aluV00 = Gate.or(aluV000, aluV001);
	public final Gate aluV0 = aluV00.and(ADD);
	
	public final Gate aluV1 = Gate.and(INC, X15.not(), F15);
	public final Gate aluV2 = Gate.and(DEC, X15, F15.not());
	
	public final Gate aluV300 = Gate.and(X15, Y15.not(), F15.not());
	public final Gate aluV301 = Gate.and(X15.not(), Y15, F15);
	public final Gate aluV30 = Gate.or(aluV300, aluV301);
	public final Gate aluV3 = aluV30.and(SUB);
	public final Gate aluV = Gate.or(aluV0, aluV1, aluV2, aluV3);

	// PSW 1

	// pswN
	public final Gate resetPSWN1 = MDR.bit(0).not().and(PSWin);
	public final Gate resetPSWN2 = aluN.not().and(ldFLAGS);
	public final Gate resetPSWN = resetPSWN1.or(resetPSWN2);
	public final Gate setPSWN1 = MDR.bit(0).and(PSWin);
	public final Gate setPSWN2 = aluN.and(ldFLAGS);
	public final Gate setPSWN = setPSWN1.or(setPSWN2);
	public final RS_FF PSWN = new RS_FF();

	// pswZ
	public final Gate resetPSWZ1 = MDR.bit(1).not().and(PSWin);
	public final Gate resetPSWZ2 = aluZ.not().and(ldFLAGS);
	public final Gate resetPSWZ = resetPSWZ1.or(resetPSWZ2);
	public final Gate setPSWZ1 = MDR.bit(1).and(PSWin);
	public final Gate setPSWZ2 = aluZ.and(ldFLAGS);
	public final Gate setPSWZ = setPSWZ1.or(setPSWZ2);
	public final RS_FF PSWZ = new RS_FF();

	// pswC
	public final Gate resetPSWC1 = MDR.bit(2).not().and(PSWin);
	public final Gate resetPSWC2 = aluC.not().and(ldFLAGS);
	public final Gate resetPSWC = resetPSWC1.or(resetPSWC2);
	public final Gate setPSWC1 = MDR.bit(2).and(PSWin);
	public final Gate setPSWC2 = aluC.and(ldFLAGS);
	public final Gate setPSWC = setPSWC1.or(setPSWC2);
	public final RS_FF PSWC = new RS_FF();

	// pswV
	public final Gate resetPSWV1 = MDR.bit(2).not().and(PSWin);
	public final Gate resetPSWV2 = aluV.not().and(ldFLAGS);
	public final Gate resetPSWV = resetPSWV1.or(resetPSWV2);
	public final Gate setPSWV1 = MDR.bit(2).and(PSWin);
	public final Gate setPSWV2 = aluV.and(ldFLAGS);
	public final Gate setPSWV = setPSWC1.or(setPSWC2);
	public final RS_FF PSWV = new RS_FF();

	public final Gate not_cond = Gate.nor(JZ.and(PSWZ), JSR, JNZ.and(PSWZ.not()), JV.and(PSWV), JMP, JNEG
			.and(PSWN), JC.and(PSWC));

	// CONTROL UNIT

	//	 CONTROL SIGNALS CONTROL UNIT
	public final Gate brOPR = T08;
	public final Gate bruncnd = Gate.or(T09, T0A, T0B, T0D, T13, T15, T1C, T28, T2B, T33, T46);
	public final Gate brL1 = T04;
	public final Gate brL2 = T06;
	public final Gate brgrADRCOD = T03;
	public final Gate brimmreg = T16;
	public final Gate brregdir = T0E.or(T1D);
	public final Gate brnotcond = T31;
	public final Gate brnotPREKID = Gate.or(T0C, T12, T14, T1B, T27, T2A, T32, T39, T3A);


	public final Gate val00 = Gate.or(T0B, T0C, T12, T14, T1B, T27, T2A, T32, T39, T3A, T46);
	public final Gate val08 = T04;
	public final Gate val09 = T06;
	public final Gate val0A = T03;
	public final Gate val14 = T0E;
	public final Gate val1B = T16;
	public final Gate val29 = T1D;
	public final Gate val3A = T31;
	public final Gate val3B = Gate.or(T09, T0A, T0D, T13, T15, T1C, T28, T2B, T33);

	public final Gate KMBRANCH = new Gate(7) { 
		@Override
		public int func() {

			if(val00.val() == 1) return 0x00;
			if(val08.val() == 1) return 0x08;
			if(val09.val() == 1) return 0x09;
			if(val0A.val() == 1) return 0x0A;
			if(val14.val() == 1) return 0x14;
			if(val1B.val() == 1) return 0x1B;
			if(val29.val() == 1) return 0x29;
			if(val3A.val() == 1) return 0x3A;
			if(val3B.val() == 1) return 0x3B;
			return 0;
		}
	};
	public final Gate KMOP = new Gate(7) {
		@Override
		public int func() {
			assert CTRL.val() + STSYS.val() + STORE.val() + OP1.val() + OP2.val() + JSR.val()
					+ JUMP.val() + RTI.val() + RTS.val() <= 1;
			if(CTRL.val() == 1) return 0x0B;
			if(STSYS.val() == 1) return 0x0C;
			if(STORE.val() == 1) return 0x0E;
			if(OP1.val() == 1) return 0x16;
			if(OP2.val() == 1) return 0x1D;
			if(JSR.val() == 1) return 0x2C;
			if(JUMP.val() == 1) return 0x31;
			if(RTI.val() == 1) return 0x34;
			if(RTS.val() == 1) return 0x36;
			return 0;
		}
	};

	// INTERRUPTS 1
	// prihvatni flip-flopovi
	public final RS_FF int1_RS_intr1 = new RS_FF(), int1_RS_intr2 = new RS_FF(),
			int1_RS_intr3 = new RS_FF(), int1_RS_inm = new RS_FF();
	// flip-flopovi koji pamte prekide
	public final RS_FF int1_RS_prper1 = new RS_FF(), int1_RS_prper2 = new RS_FF(),
			int1_RS_prper3 = new RS_FF(), int1_RS_prinm = new RS_FF();

	public final Gate Fintr1 = int1_RS_intr1;
	public final Gate Fintr2 = int1_RS_intr2;
	public final Gate Fintr3 = int1_RS_intr3;
	public final Gate Finm = int1_RS_inm;

	public final Gate S_PRPER1 = Fintr1.and(ldINTEXT);
	public final Gate S_PRPER2 = Fintr2.and(ldINTEXT);
	public final Gate S_PRPER3 = Fintr3.and(ldINTEXT);
	public final Gate S_PRINM = Finm.and(ldINTEXT);

	public final Gate PRPER1 = int1_RS_prper1;
	public final Gate PRPER2 = int1_RS_prper2;
	public final Gate PRPER3 = int1_RS_prper3;
	public final Gate PRINM = int1_RS_prinm;

	// INTERRUPTS 2
	// registar IMR
	public final RS_FF int2_RS_IMR1 = new RS_FF(), int2_RS_IMR2 = new RS_FF(), int2_RS_IMR3 = new RS_FF();
	// flip-flopovi koji pamte prekide
	public final RS_FF int2_RS_pradr = new RS_FF(), int2_RS_prcod = new RS_FF(),
			int2_RS_print = new RS_FF();

	public final Gate IMR1 = int2_RS_IMR1;
	public final Gate IMR2 = int2_RS_IMR2;
	public final Gate IMR3 = int2_RS_IMR3;

	public final Gate ldIMR = ldCONTROL.and(STIMR);

	public final Gate S_PRCOD = ldPRCODADR.and(grOPR);
	public final Gate S_PRADR = ldPRCODADR.and(grADR);

	public final Gate SIMR1 = ldIMR.and(ACC.bit(1));
	public final Gate SIMR2 = ldIMR.and(ACC.bit(2));
	public final Gate SIMR3 = ldIMR.and(ACC.bit(3));

	public final Gate RIMR1 = ldIMR.and(ACC.bit(1).not());
	public final Gate RIMR2 = ldIMR.and(ACC.bit(2).not());
	public final Gate RIMR3 = ldIMR.and(ACC.bit(3).not());

	public final Gate PRADR = int2_RS_pradr;
	public final Gate PRCOD = int2_RS_prcod;
	public final Gate PRINT = int2_RS_print;

	// INTERRUPTS 3

	// FIXME finish INTERRUPTS 3
	
	public final Gate intIMR1=IMR1.and(PRPER1); // prekid posle maskiranja
	public final Gate intIMR2=IMR1.and(PRPER2);
	public final Gate intIMR3=IMR1.and(PRPER3);
	public final Gate intIMR=Gate.or(intIMR1, intIMR2, intIMR3);
	public final Gate prL1=intIMR2.or(intIMR3);
	public final Gate prL0=intIMR3.or(intIMR1.and(intIMR2.not()));
	public final Gate prL=Gate.merge(prL1, prL0);
	public final Gate intG = prL.greaterUnsigned(PSWL);
	public final Gate PRPER = Gate.and(intG, PSWI, intIMR);

	
//	 INTERRUPTS 4
	   public final Gate int4CDW=Gate.or(PSWT,PRINM, PRADR, PRCOD);
	   public final Gate int4CD0=PRCOD.or(PRINM.and(PRADR.not()));
	   public final Gate int4CD1=PRADR.or(PRCOD);
	   public final Gate int4CD=Gate.merge(int4CD1, int4CD0);
	   public final Gate not_PREKID=(Gate.or(int4CDW,PRPER,INT)).not();
	   public final Gate PRNIJEINTERNI=(Gate.or(PRCOD, PRADR, PRINT)).not();
	   public final Gate PREXT=PRPER.and(PRNIJEINTERNI);
	   public final Gate int4MP1out=PREXT.select(int4CD, prL);
	   public final Gate int4MP2levo=Gate.merge(Gate.zero, Gate.zero, Gate.zero, 
			   Gate.zero, Gate.zero, PREXT, int4MP1out);
	   public final Gate int4MP2out=INT.select(int4MP2levo, IR_2);
	   public final Gate int4DCin=Gate.merge(PREXT, int4MP1out);
	   public final Gate ackPRINT=ack.and(INT);
	   public final Gate ackPRINM=ack.and(int4DCin.equal(1));
	   public final Gate ackPRADR=ack.and(int4DCin.equal(2));
	   public final Gate ackPRCOD=ack.and(int4DCin.equal(3));
	   public final Gate ackPER1=ack.and(int4DCin.equal(5));
	   public final Gate ackPER2=ack.and(int4DCin.equal(6));
	   public final Gate ackPER3=ack.and(int4DCin.equal(7));
	   public final Gate int4ADDdesno=Gate.merge(Gate.zero, Gate.zero, Gate.zero, 
			   Gate.zero, Gate.zero, Gate.zero, Gate.zero, int4MP2out, Gate.zero);
	   public final Gate ldIVTP=ldSYSREG.and(STIVTP);
	   public final Register IVTP=new Register(ACC, ldIVTP);
	   public final Gate ADRIVT=IVTP.add(int4ADDdesno);

	// INTERFACE 1
	
	public final Gate BRQ3 = (brqSTART.or(in1_RS_2)).and(brqSTOP.not()); 
	public final Gate BRQ0 = Gate.zero, BRQ1 = Gate.zero, BRQ2 = Gate.zero;
	public final Gate BRQCDW=Gate.or(BRQ3,BRQ2, BRQ1, BRQ0);
	public final Gate BRQCD1=BRQ3.or(BRQ2);
	public final Gate BRQCD0=BRQ3.or(BRQ1.and(BRQ2.not()));
	public final Gate BRQCD=Gate.merge(BRQCD1, BRQCD0);
	public final Gate BG0 = BRQCDW.and(BRQCD.equal(0)); 
	public final Gate BG1 = BRQCDW.and(BRQCD.equal(1));
	public final Gate BG2 = BRQCDW.and(BRQCD.equal(2));
	public final Gate BG3 = BRQCDW.and(BRQCD.equal(3));
	public final Gate BUSY = busHOLD; // TODO zameniti sa magistralom

	// PC and SP

	public final Register ADRTMP = new Register(16);
	public final Gate diPCL = mxPC.select(new Value(0, 8), MDR, IR.bits(0, 7), ADRTMP.bits(0, 7));
	public final Gate diPCH = mxPC.select(new Value(1, 8), MDR, IR.bits(8, 15), ADRTMP.bits(8, 15));
	public final Gate incPCL = incPC.and(ldIR2.and(L1).not());
	public final Register PCL = new Register(diPCL, ldPCL, incPCL);
	public final Gate incPCH = incPCL.and(PCL.equal(0xFF));
	public final Register PCH = new Register(diPCH, ldPCH, incPCH);
	public final Gate PC = Gate.merge(PCH, PCL);

	public final Gate ldSP = STSP.and(ldSYSREG);
	public final Register SP = new Register(ACC, ldSP, incSP, decSP);

	// REGISTERS
	public final Gate diREG = mxREG.select(ACC, TEMP);

	public final Gate IR16_17 = IR.bits(16, 17);
	public final Gate ldR0 = IR16_17.equal(0, ldREG);
	public final Gate ldR1 = IR16_17.equal(1, ldREG);
	public final Gate ldR2 = IR16_17.equal(2, ldREG);
	public final Gate ldR3 = IR16_17.equal(3, ldREG);

	public final Register R0 = new Register(diREG, ldR0);
	public final Register R1 = new Register(diREG, ldR1);
	public final Register R2 = new Register(diREG, ldR2);
	public final Register R3 = new Register(diREG, ldR3);

	public final Gate REG = IR16_17.select(R0, R1, R2, R3);

	// OPERAND FETCH
	public final Gate incDISP = Gate.zero;
	public final Gate OFMP1out=pcrel.select(REG, PC);
	public final Gate DISP = Gate.add(OFMP1out, IR.bits(0, 15), incDISP);

	public final Gate OPR = imm.select(REG, IR.bits(0, 15));
	public final Gate OPRH = OPR.bits(8, 15);
	public final Gate OPRL = OPR.bits(0, 7);
	public final Gate diBH = immreg.select(MDR, OPR.bits(8, 15));
	public final Gate diBL = immreg.select(MDR, OPR.bits(0, 7));

	// MEMORY // da, ovo je CPU.java :)
	public final byte[] memory = new byte[0x10000];

	private final static int memCycles = 3;
	public final Register memTimer = new Register(8);
	private final Gate memDone = memTimer.equal(memCycles);

	public int memLastAddress;
	public byte memLastData;

	public void writeMemory(int address, byte data) {
		memLastData = memory[address];
		memLastAddress = address;
		memory[address] = data;
	}

	private final Value memDataOut = new Value(8);
	private final Value memOE = new Value(1);

	// BUS
	public final Bus ABUS = new Bus(MAR, busHOLD), cRDBUS = new Bus(rd.not(), read.and(busHOLD)),
			cWRBUS = new Bus(wr.not(), write.and(busHOLD));

	// MEMORY
	@SuppressWarnings("unused")
	private final Gate mem1 = new Gate(1) {
		@Override
		public int func() {
			if(memDone.bool() && !cRDBUS.isHighZ()) {
				memDataOut.set(memory[ABUS.val()]);
				memOE.set(true);
			} else
				memOE.set(false);
			return 0;
		}
	};

	// BUS
	public final Bus DBUS = new Bus(MDR, write.and(busHOLD), memDataOut, memOE);

	// MEMORY
	@SuppressWarnings("unused")
	private final Gate mem2 = new Gate(1) {
		@Override
		public int func() {
			if(memDone.bool() && !cWRBUS.isHighZ()) {
				writeMemory(ABUS.val(), (byte)DBUS.val());
			}
			return 0;
		}
	};

	// INTERFACE 3
	public Gate PSW = Gate.merge(PSWI, PSWT, PSWL1, PSWL0, PSWV, PSWC, PSWZ, PSWN);
	public final Gate diMAR = mxMAR.select(PC, SP, DISP, ADRIVT);
	public final Gate diMDR = mxMDR.select(DBUS, ACC.bits(0, 7), ACC.bits(8, 15), TEMP.bits(0, 7), TEMP
			.bits(8, 15), PC.bits(8, 15), PC.bits(0, 7), PSW);

	// PSW 2
	public final Gate ldPSWL = ack.and(PREXT);

	// pswL0
	public final Gate resetPSWL01 = MDR.bit(4).not().and(PSWin);
	public final Gate resetPSWL02 = prL0.not().and(ldPSWL);
	public final Gate resetPSWL0 = resetPSWL01.or(resetPSWL02);
	public final Gate setPSWL01 = MDR.bit(4).and(PSWin);
	public final Gate setPSWL02 = prL0.and(ldPSWL);
	public final Gate setPSWL0 = setPSWL01.or(setPSWL02);

	// pswL1
	public final Gate resetPSWL11 = MDR.bit(5).not().and(PSWin);
	public final Gate resetPSWL12 = prL1.not().and(ldPSWL);
	public final Gate resetPSWL1 = resetPSWL11.or(resetPSWL12);
	public final Gate setPSWL11 = MDR.bit(5).and(PSWin);
	public final Gate setPSWL12 = prL1.and(ldPSWL);
	public final Gate setPSWL1 = setPSWL11.or(setPSWL12);
	
	// PSWT, PSWI
	public final Gate clPSWT0 = TRPD.and(ldCONTROL);
	public final Gate clPSWT = clPSWT0.or(ack);
	public final Gate clPSWI0 = INTD.and(ldCONTROL);
	public final Gate clPSWI = clPSWI0.or(ack);
	public final Gate stPSWT = TRPE.and(ldCONTROL);
	public final Gate stPSWI = INTE.and(ldCONTROL);
	
	public final Gate resetPSWT0 = MDR.bit(6).not().and(PSWin);
	public final Gate resetPSWT =resetPSWT0.or(clPSWT);
	public final Gate setPSWT0 = MDR.bit(6).and(PSWin);
	public final Gate setPSWT=setPSWT0.or(stPSWT);
	
	public final Gate resetPSWI0 = MDR.bit(7).not().and(PSWin);
	public final Gate resetPSWI =resetPSWI0.or(clPSWI);
	public final Gate setPSWI0 = MDR.bit(7).and(PSWin);
	public final Gate setPSWI=setPSWI0.or(stPSWI);
	
	// OVO SAM PREBACIO NA KRAJ DA BI MOGLO DA SE KOMPAJLIRA
	public final Gate branch = Gate.or(bruncnd, brL1.and(L1), brL2.and(L2), brgrADRCOD.and(grADRCOD),
			brregdir.and(regdir), brimmreg.and(immreg), brnotcond.and(not_cond), brnotPREKID
					.and(not_PREKID));
	public final Gate branchOPR = branch.or(brOPR);
	public final Gate diCNT = brOPR.select(KMBRANCH, KMOP);
	public final Gate ldCNT = Gate.and(run, not_halt, branch.or(brOPR));
	public final Gate incCNT = Gate.and(run, not_halt, branch.or(brOPR).not());


	public CPU() {
		// MEMORY
		memTimer.setInc(Gate.or(new LowZ(cRDBUS), new LowZ(cWRBUS)).and(memDone.not()));
		memTimer.setClear(memDone);

		//		Control Unit
		CNT.attach(diCNT, ldCNT, incCNT);
		hlt.attach(Gate.zero, ldCONTROL.and(HALT));

		// PC and SP
		ADRTMP.attach(PC, ldADRTMP);
		
		// OP FETCHADRIVT.attach(PC, ldADRTMP);
		BH.attach(diBH, ldBH);
		BL.attach(diBL, ldBL);

		// INTERRUPTS 1
		int1_RS_prper1.attach(ackPER1, S_PRPER1);
		int1_RS_prper2.attach(ackPER2, S_PRPER2);
		int1_RS_prper3.attach(ackPER3, S_PRPER3);
		int1_RS_prinm.attach(ackPRINM, S_PRINM);

		int1_RS_intr1.attach(ackPER1, intr1);
		int1_RS_intr2.attach(ackPER2, intr2);
		int1_RS_intr3.attach(ackPER3, intr3);
		int1_RS_inm.attach(ackPRINM, inm);

		// INTERRUPTS 2
		int2_RS_IMR1.attach(RIMR1, SIMR1);
		int2_RS_IMR2.attach(RIMR2, SIMR2);
		int2_RS_IMR3.attach(RIMR3, SIMR3);

		int2_RS_prcod.attach(ackPRCOD, S_PRCOD);
		int2_RS_pradr.attach(ackPRADR, S_PRADR);
		int2_RS_print.attach(ackPRINT, stPRINT);

		// INTERFACE 1
		in1_RS_1.attach(brqSTOP, BUSY.not().and(BG3));
		in1_RS_2.attach(brqSTOP, brqSTART);
		
		// INTERFACE 2
		in2_D_1.attach(read.and(busHOLD)); 
		in2_D_2.attach(write.and(busHOLD));
		in2_D_3.attach(write.or(read));
		busCNT.attach(new Value(2), brqSTOP, brqSTOP.not().and(busHOLD));
				
		// INTERFACE 3
		MAR.attach(diMAR, ldMAR, incMAR);
		MDR.attach(diMDR, read.and(brqSTOP).or(ldMDR));

		IR_1.attach(MDR, ldIR1);
		IR_2.attach(MDR, ldIR2);
		IR_3.attach(MDR, ldIR3);

		//PSW
		PSWN.attach(resetPSWN, setPSWN);
		PSWZ.attach(resetPSWZ, setPSWZ);
		PSWV.attach(resetPSWV, setPSWV);
		PSWC.attach(resetPSWC, setPSWC);
		PSWL0.attach(resetPSWL0, setPSWL0);
		PSWL1.attach(resetPSWL1, setPSWL1);
		PSWI.attach(resetPSWI, setPSWI);
		PSWT.attach(resetPSWT, setPSWT);
		
		// ALU
		ACC.attach(alu.F, ldACC);
		TEMP.attach(alu.F, aluOP2);
	}
}