package simulator;

import logic.Gate;

/**
 * @author Tintor Marko
 * @date 06/2006 
 */
public class ALU extends Gate {
	public final Gate F = this.bits(0, 15), C = this.bit(16);
	private final Gate A, B, incA, decA, notA, shlA, shrA, add, sub, and, or, xor, IL, IR, Cin;

	// ne postoji transferA, ako nista nije ukljuceno onda se prosledjuje A
	public ALU(Gate A, Gate B, Gate incA, Gate decA, Gate notA, Gate shlA, Gate shrA, Gate add, Gate sub,
			Gate and, Gate or, Gate xor, Gate IL, Gate IR, Gate Cin) {
		super(17);
		this.A = A;
		this.B = B;

		this.incA = incA;
		this.decA = decA;
		this.notA = notA;
		this.shlA = shlA;
		this.shrA = shrA;

		this.add = add;
		this.sub = sub;
		this.and = and;
		this.or = or;
		this.xor = xor;

		this.IL = IL;
		this.IR = IR;
		this.Cin = Cin;
	}

	public int func() {
		assert incA.val() + decA.val() + notA.val() + shlA.val() + shrA.val() + add.val() + sub.val()
				+ and.val() + or.val() + xor.val() <= 1;

		if(incA.bool()) return A.val() + 1 + Cin.val();
		if(decA.bool()) return trim(A.val() - 1 - Cin.val());
		if(notA.bool()) return trim(~A.val());
		if(shlA.bool()) return ((A.val() << 1) & 0xFFFF) | IL.val();
		if(shrA.bool()) return (A.val() >>> 1) | (IR.val() << 15);

		if(add.bool()) return A.val() + B.val() + Cin.val();
		if(sub.bool()) return A.val() - B.val() - Cin.val();
		if(and.bool()) return A.val() & B.val();
		if(or.bool()) return A.val() | B.val();
		if(xor.bool()) return A.val() ^ B.val();

		return A.val();
	}
}