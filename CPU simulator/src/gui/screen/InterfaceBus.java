package gui.screen;

import gui.Screen;

/**
 * @author Marko Tintor
 * @author Đorđe Jevđić
 * @author Branislav Aleksić
 * @date 06/2006
 */
public class InterfaceBus extends Screen {
	public InterfaceBus() {
		super("Interface - bus");

		wire(cpu.mxMAR.bit(0), "85@162 R22");
		wire(cpu.mxMAR.bit(1), "85@136 R22");

		wire(cpu.ldMAR, "84@309 R9");
		wire(cpu.incMAR, "83@278 R10");

		wire(cpu.mxMDR2, "710@80 L17");
		wire(cpu.mxMDR1, "710@109 L17");
		wire(cpu.mxMDR0, "710@136 L17");

		wire(cpu.read, "369@265 R29");
		wire(cpu.brqSTOP, "369@291 R29");
		wire(cpu.read.and(cpu.brqSTOP), "441@280 R36"); // IZLAZ IZ AND GDE JE WRITE I BRQSTOP

		wire(cpu.ldMDR, "428@333 R18 U28 R34");
		wire(cpu.ldMDRx, "515@294 R42"); // IZLAZ IZ OR GDE JE PRETHODNI I LDMDR

		wire(cpu.write, "506@398 R55");
		wire(cpu.busHOLD, "506@419 R55");
		wire(cpu.write.and(cpu.busHOLD), "599@407 R30"); // IZLAZ IZ AND GDE JE WRITE I BUSHOLD

		wire(cpu.write, "530@609 R20");
		wire(cpu.busHOLD, "530@634 R20");
		wire(cpu.write.and(cpu.busHOLD), "593@623 R37"); // IZLAZ IZ AND GDE JE WRITE I BUSHOLD

		wire(cpu.busHOLD, "98@408 R67");

		wire(cpu.read, "62@610 R24");
		wire(cpu.busHOLD, "62@634 R24");
		wire(cpu.read.and(cpu.busHOLD), "127@624 R38"); // IZLAZ IZ AND GDE JE READ I BUSHOLD

		wire(cpu.rd, "174@569 D32");
		wire(cpu.wr, "639@568 D33");
		
		wire(cpu.ABUS, "175@435 D58 L80");
		wire(cpu.ABUS, "175@435 D58 R80");
		wire(cpu.DBUS, "639@435 D58 L80");
		wire(cpu.DBUS, "639@435 D58 R80");
		
		wire(cpu.cRDBUS, "174@666 D43 L80");
		wire(cpu.cRDBUS, "174@666 D43 R80");
		wire(cpu.cWRBUS, "640@666 D43 L80");
		wire(cpu.cWRBUS, "640@666 D43 R80");
		
		wire(cpu.DBUS, "639@568 D33");
		wire(cpu.rd, "174@569 D32");
		wire(cpu.wr, "639@568 D33");

		label(cpu.ABUS, 110, 470);
		label(cpu.DBUS, 580, 470);

		label(cpu.diMAR, 136, 245);
		label(cpu.MAR, 136, 325);
		label(cpu.diMDR, 608, 245);
		label(cpu.MDR, 608, 325);
	}
}