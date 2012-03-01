package logic;

import gui.Main;

/**
 * Registar sa inkrementovanjem.
 */
public class Register extends SeqGate {
	private Gate load = Gate.zero, clear = Gate.zero, inc = Gate.zero, dec = Gate.zero;
	private Gate dataIn;

	public Register(int bits) {
		super(bits);
	}

	public Register(Gate dataIn, Gate load) {
		super(dataIn.bits);
		assert load.bits == 1;
		this.dataIn = dataIn;
		this.load = load;
	}

	public Register(Gate dataIn, Gate load, Gate inc) {
		super(dataIn.bits);
		assert load.bits == 1 && inc.bits == 1;
		this.dataIn = dataIn;
		this.load = load;
		this.inc = inc;
	}

	public Register(Gate dataIn, Gate load, Gate inc, Gate dec) {
		super(dataIn.bits);
		assert load.bits == 1 && inc.bits == 1 && dec.bits == 1;
		this.dataIn = dataIn;
		this.load = load;
		this.inc = inc;
		this.dec = dec;
	}

	public void attach(Gate dataIn, Gate load) {
		assert dataIn.bits == bits;
		assert load.bits == 1;
		assert this.load == Gate.zero;

		this.dataIn = dataIn;
		this.load = load;
	}

	public void attach(Gate dataIn, Gate load, Gate inc) {
		attach(dataIn, load);
		setInc(inc);
	}

	public void setClear(Gate a) {
		assert a.bits == 1 && this.clear == Gate.zero;
		this.clear = a;
	}

	public void setInc(Gate a) {
		assert a.bits == 1 && this.inc == Gate.zero;
		this.inc = a;
	}

	public void setDec(Gate a) {
		assert a.bits == 1 && this.dec == Gate.zero;
		this.dec = a;
	}

	public @Override
	int func() {
		// ! sekvencijalna mreža zavisi od signala iz prethodnog takta
		assert clear.old() + load.old() + inc.old() + dec.old() <= 1 : "Samo jedan kontrolni signal sme da bude aktivan!";
		if(clear.old() != 0) return 0;
		if(load.old() != 0) {
			if(dataIn == null) {
				System.out.println(this); // FIXME
				if(this == Main.sim.cpu.MAR) System.out.println("MAR");
				if(this == Main.sim.cpu.MDR) System.out.println("MDR");
				if(this == Main.sim.cpu.ACC) System.out.println("ACC");
				if(this == Main.sim.cpu.IR_1) System.out.println("IR_1");
				if(this == Main.sim.cpu.IR_2) System.out.println("IR_2");
				if(this == Main.sim.cpu.IR_3) System.out.println("IR_3");
				if(this == Main.sim.cpu.BH) System.out.println("BH");
				if(this == Main.sim.cpu.BL) System.out.println("BL");
				if(this == Main.sim.cpu.PCH) System.out.println("PCH");
				if(this == Main.sim.cpu.PCL) System.out.println("PCL");
				if(this == Main.sim.cpu.memTimer) System.out.println("memTimer");
				if(this == Main.sim.cpu.R0) System.out.println("R0");
			}
			return dataIn.old(); // FIXME NullPointerException!
		}
		if(inc.old() != 0) return trim(old() + 1);
		if(dec.old() != 0) return trim(old() - 1);
		return old();
	}
}