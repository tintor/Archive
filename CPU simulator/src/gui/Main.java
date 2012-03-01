package gui;

import gui.screen.ALU;
import gui.screen.ControlSignals;
import gui.screen.ControlUnit;
import gui.screen.Indicators;
import gui.screen.InstructionDecode;
import gui.screen.InstructionRegister;
import gui.screen.InterfaceArbitration;
import gui.screen.InterfaceSynchronization;
import gui.screen.InterfaceBus;
import gui.screen.InterruptsExternal;
import gui.screen.InterruptsInternal;
import gui.screen.InterruptsMaskable;
import gui.screen.InterruptsIVT;
import gui.screen.OperandFetch;
import gui.screen.OperationalSignals;
import gui.screen.PC_and_SP;
import gui.screen.PSW_NZVC;
import gui.screen.PSW_LTI;
import gui.screen.Registers;
import gui.util.Action;
import gui.util.GUI;
import gui.util.InputBox;
import gui.util.ViewAction;
import gui.util.XMenu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import logic.Gate;
import logic.SeqGate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import simulator.CPU;
import simulator.Simulator;
import simulator.Util;

/**
 * @author Marko Tintor
 * @date 04/2006
 */

public class Main extends GUI implements PaintListener, KeyListener, MouseMoveListener, MouseListener,
		SelectionListener {
	static Main main;

	SashForm wMainSash, wLeftSash, wMiddleSash, wRightSash;

	ScrolledComposite sc;

	Canvas canvas;

	CLabel screenTitle;

	GC gc;
	public final static Simulator sim = new Simulator();
	final CPU cpu = sim.cpu;

	final Screen[] screens = new Screen[] {  new PC_and_SP(), new InstructionRegister(), new InstructionDecode(), new OperandFetch(),
		new Registers(), new ALU(), new Indicators(), new PSW_NZVC(), new PSW_LTI(), 
		new InterruptsExternal(), new InterruptsInternal(),
		new InterruptsMaskable(), new InterruptsIVT(), new InterfaceArbitration(), new InterfaceSynchronization(), 
		new InterfaceBus(), new ControlUnit(), new OperationalSignals(), new ControlSignals()};

	Screen screen;

	final Action Open = new Action("&Open...\tCtrl+O") {
		public void run() {
			FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
			fileDialog.setText("Open simulation");
			fileDialog.setFilterExtensions(new String[] {"*.sim"});
			String filename = fileDialog.open();
			if(filename == null) return;

			try {
				sim.load(filename);
			} catch(Exception e) {
				MessageBox m = new MessageBox(shell, SWT.OK);
				m.setText("Simulator");
				m.setMessage(e.toString());
				m.open();
				return;
			}
			Table table = (Table)Memory.view.getContent();
			table.removeAll();
			updateDisplay();
		}
	}, Save = new Action("&Save...\tCtrl+S") {
		public void run() {
			assert false;
			sim.save("test2.sim");
		}
	}, Exit = new Action("E&xit\tEsc") {
		public void run() {
			shell.dispose();
		}
	}, NextCycle = new Action("Next Cycle\tPlus") {
		@Override
		protected void run() {
			if(!sim.halt()) {
				sim.nextCycle();
				updateDisplay();
			}
		}
	}, PrevCycle = new Action("Prev Cycle\tMinus") {
		@Override
		protected void run() {
			if(sim.cycle() > 0) {
				sim.prevCycle();
				updateDisplay();
			}
		}
	}, GotoCycle = new Action("Go to Cycle ...\tStar") {
		@Override
		protected void run() {
			String s = inputBox("Go to cycle");
			if(s == null) return;
			try {
				sim.gotoCycle(Integer.parseInt(s));
				updateDisplay();
			} catch(NumberFormatException e) {
				
			}
		}
	}, NextInstruction = new Action("Next Instruction\tPageDown") {
		@Override
		protected void run() {
			if(!sim.halt()) {
				sim.nextInstruction();
				updateDisplay();
			}
		}
	}, PrevInstruction = new Action("Prev Instruction\tPageUp") {
		@Override
		protected void run() {
			if(sim.cycle() > 0) {
				sim.prevInstruction();
				updateDisplay();
			}
		}
	}, ExecuteProgram = new Action("Execute Program\tEnd") {
		@Override
		protected void run() {
			if(!sim.halt()) {
				sim.executeProgram();
				updateDisplay();
			}
		}
	}, Restart = new Action("Restart\tHome") {
		@Override
		protected void run() {
			sim.restart();
			updateDisplay();
		}
	};

	final ViewAction Control = new ViewAction("Control\tF1"), Registers = new ViewAction("Registers and Signals\tF2"),
			/*Signals = new ViewAction("Signals\tF3"),*/ MicroProgram = new ViewAction("MicroProgram\tF3")/*,
			Program = new ViewAction("Program\tF5")*/, Memory = new ViewAction("Memory\tF4")/*,
			Stack = new ViewAction("Stack\tF7")*/;

	final XMenu[] xmenu = {
		new XMenu("&Simulator", Open, null, Exit),
		new XMenu("&Control", NextCycle, PrevCycle, GotoCycle, null, NextInstruction, PrevInstruction,
				null, Restart, ExecuteProgram),
		new XMenu("&View", Control, null, Registers, /*Signals,*/ MicroProgram, /*Program,*/ Memory /*, Stack*/) };

	private final MessageBox haltMessage = new MessageBox(shell);
		
	String inputBox(String title) {
		InputBox ib = new InputBox(shell, SWT.NONE);
		ib.setText(title);
		return ib.open();
	}

	Main() {
		main = this;
		shell.setMaximized(true);
		shell.setText("ETF CPU Simulator");
		shell.setLayout(new FillLayout());

		haltMessage.setText("Simulator");
		haltMessage.setMessage("CPU is halted!");

		XMenu.createMenuBar(shell, xmenu);

		wMainSash = new SashForm(shell, SWT.HORIZONTAL | SWT.SMOOTH);
//		wLeftSash = new SashForm(wMainSash, SWT.VERTICAL | SWT.SMOOTH);
		wMiddleSash = new SashForm(wMainSash, SWT.VERTICAL | SWT.SMOOTH);
		wRightSash = new SashForm(wMainSash, SWT.VERTICAL | SWT.SMOOTH);

		createRegistersView();
//		createSignalsView();
		createMicroProgramView();

		createScreenView();
		createControlView();

//		createProgramView();
		createMemoryView();
//		newStackView();

		wMainSash.setWeights(new int[] { 14, 3 });
		wMiddleSash.setWeights(new int[] { 10, 1 });

		canvas.setFocus();

		Gate.calculateCombGates();
		updateDisplay();
	}

	// for registers
	Font defaultFont;

	void updateDisplay() {
		canvas.redraw();

		// registers
		Table t = (Table)Registers.view.getContent();
		for(TableItem ti : t.getItems()) {
			String a = ((Gate)ti.getData()).toHex();
			if(ti.getText(1).equals(a)) {
				if(ti.getFont() != defaultFont) ti.setFont(defaultFont);
			} else {
				ti.setText(1, a);
				ti.setFont(bold);
			}
		}

		// memory
		byte[] mem = cpu.memory;
		t = (Table)Memory.view.getContent();
		int i = 0;
		for(int adr = 0; adr < mem.length; adr += 8) {
			if(i < t.getItemCount() && (int)(Integer)t.getItem(i).getData() < adr) i++;

			TableItem ti = null;
			if(i < t.getItemCount() && (int)(Integer)t.getItem(i).getData() == adr) {
				ti = t.getItem(i);
			} else if(mem[adr] != 0 || mem[adr + 1] != 0 || mem[adr + 2] != 0 || mem[adr + 3] != 0
					|| mem[adr + 4] != 0 || mem[adr + 5] != 0 || mem[adr + 6] != 0
					|| mem[adr + 7] != 0) {
				ti = new TableItem(t, SWT.NONE, i);
				ti.setData(adr);
				ti.setText(0, Util.shortToHex(adr));
				i++;
			}
			if(ti != null)
				ti.setText(1, Util.byteToHex(mem[adr]) + Util.byteToHex(mem[adr + 1]) + ' '
						+ Util.byteToHex(mem[adr + 2]) + Util.byteToHex(mem[adr + 3]) + ' '
						+ Util.byteToHex(mem[adr + 4]) + Util.byteToHex(mem[adr + 5]) + ' '
						+ Util.byteToHex(mem[adr + 6]) + Util.byteToHex(mem[adr + 7]));
		}

		// control
		org.eclipse.swt.widgets.Label a = (org.eclipse.swt.widgets.Label)Control.view.getContent();
		a.setText("Cycle = " + sim.cycle());

		// microprogram
		t = (Table)MicroProgram.view.getContent();
		int step = sim.cpu.CNT.val();

		// i = step;
		// while((Integer)t.getItem(i).getData() < step) i++;
		// t.setRedraw(true);
		// t.select(i);
		// t.showSelection();
		// t.setRedraw(false);

		for(TableItem ti : t.getItems())
			if(step == (Integer)ti.getData()) {
				ti.setFont(bold);
				t.showItem(ti);
			} else if(ti.getFont() != defaultFont) ti.setFont(defaultFont);

		if(sim.halt())
			haltMessage.open();
	}

	Screen findScreen(String name) {
		for(Screen a : screens)
			if(a.title.equals(name)) return a;
		throw new IllegalArgumentException("invalid screen: " + name);
	}

	void gotoScreen(Screen s) {
		// close current screen
		if(screen != null) {
			screen.origin = sc.getOrigin();
			gc.dispose();
		}

		screen = s;

		// open new screen
		canvas.setBounds(0, 0, screen.width, screen.height);
		screenTitle.setText(screen.title);
		sc.setOrigin(screen.origin);
		gc = new GC(screen.image);
	}

	void addSignal(String name, Gate reg) {
		TableItem ti = newTableItem((Table)Registers.view.getContent(), name, reg.toHex());
		ti.setData(reg);
	}

	void createRegistersView() {
		ViewForm view = Registers.view = newView(wRightSash, "Registers and signals", true);

		final Table table = newTable(view);
		view.setContent(table);
		newColumn(table, "Name");
		newColumn(table, "Value").setAlignment(SWT.RIGHT);

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem ti = table.getItem(table.getSelectionIndex());
				Gate g = (Gate)ti.getData();

				if(g instanceof SeqGate || g == sim.cpu.PC || g == sim.cpu.IR) {
					String a = inputBox("New value for " + ti.getText(0));
					if(a == null || a.length() == 0) return; // TODO
			
					int v;
					try {
						v = Integer.parseInt(a, 16);
					} catch(NumberFormatException ee) {
						return;
					}
					int vv = v;
					int bits = 0;
					while(vv > 0) {
						vv >>>= 1;
						bits++;
					}
					if(bits > g.bits) return; // TODO

					if(g == sim.cpu.PC) {
						sim.cpu.PCH.set(v >>> 8);
						sim.cpu.PCL.set(v & 0xFF);
					} else if(g == sim.cpu.IR) {
						sim.cpu.IR_1.set(v >>> 16);
						sim.cpu.IR_2.set((v >>> 8) & 0xFF);
						sim.cpu.IR_3.set(v & 0xFF);
						
					} else g.set(v);

					Gate.calculateCombGates();
					updateDisplay();
				}
			}
		});

		defaultFont = table.getFont();

		addSignal("CNT", sim.cpu.CNT);

		addSignal("IR", sim.cpu.IR);
		addSignal("PC", sim.cpu.PC);
		addSignal("SP", sim.cpu.SP);
		addSignal("MAR", sim.cpu.MAR);
		addSignal("MDR", sim.cpu.MDR);
		addSignal("ACC", sim.cpu.ACC);
		addSignal("TEMP", sim.cpu.TEMP);
		addSignal("IVTP", sim.cpu.IVTP);
		
		addSignal("ABUS", sim.cpu.ABUS);
		addSignal("DBUS", sim.cpu.DBUS);
		addSignal("cRDBUS", sim.cpu.cRDBUS);
		addSignal("cWRBUS", sim.cpu.cWRBUS);

		addSignal("busHOLD", sim.cpu.busHOLD);
		addSignal("brqSTART", sim.cpu.brqSTART);
		addSignal("brqSTOP", sim.cpu.brqSTOP);

		addSignal("PSWN", sim.cpu.PSWN);
		addSignal("PSWZ", sim.cpu.PSWZ);
		addSignal("PSWC", sim.cpu.PSWC);
		addSignal("PSWV", sim.cpu.PSWV);
		addSignal("PSWL0", sim.cpu.PSWL0);
		addSignal("PSWL1", sim.cpu.PSWL1);
		addSignal("PSWT", sim.cpu.PSWT);
		addSignal("PSWI", sim.cpu.PSWI);

		addSignal("R0", sim.cpu.R0);
		addSignal("R1", sim.cpu.R1);
		addSignal("R2", sim.cpu.R2);
		addSignal("R3", sim.cpu.R3);
	}

//	void createSignalsView() {
//		ViewForm view = Signals.view = newView(wLeftSash, "Signals", true);
//
//		Composite c = new Composite(view, SWT.NONE);
//		view.setContent(c);
//		c.setLayout(new FormLayout());
//
//		Text text = new Text(c, SWT.NONE);
//		text.setText("<enter signal name>");
//		text.setLayoutData(newFormData(fmin, new FormAttachment(0, 15)));
//
//		Table table = newTable(c);
//		table.setLayoutData(newFormData(new FormAttachment(text), fmax));
//
//		newColumn(table, "Signal");
//		newColumn(table, "Bits").setAlignment(SWT.RIGHT);
//		newColumn(table, "Value").setAlignment(SWT.RIGHT);
//	}

	void createMicroProgramView() {
		ViewForm view = MicroProgram.view = newView(wRightSash, "MicroProgram", true);
		Table table = new Table(view, SWT.SINGLE | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Table table = (Table)e.widget;
				table.getColumn(0).setWidth(table.getClientArea().width);
			}
		});
		view.setContent(table);
		new TableColumn(table, SWT.NONE).setResizable(false);

		int step = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("micro.txt"));
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				line = line.trim();

				Integer ss = 10000;
				if(line.length() > 0) if(line.charAt(0) != '#')
					line = String.format("%02X  ", ss = step++) + line;
				else
					line = line.substring(1).trim();
				newTableItem(table, line).setData(ss);
			}
			table.showItem(table.getItem(0));
		} catch(IOException e) {
			assert false;
		} finally {
			if(reader != null) try {
				reader.close();
			} catch(IOException e) {}
		}
	}

//	void createProgramView() {
//		ViewForm view = Program.view = newView(wRightSash, "Program", true);
//		Table table = newTable(view);
//		view.setContent(table);
//
//		newColumn(table, "Address");
//		newColumn(table, "Instruction");
//	}

	void createMemoryView() {
		ViewForm view = Memory.view = newView(wRightSash, "Memory", true);
		Table table = new Table(view, SWT.SINGLE | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Table table = (Table)e.widget;
				table.getColumn(0).setWidth(55);
				table.getColumn(1).setWidth(table.getClientArea().width - 55);
			}
		});
		view.setContent(table);

		newColumn(table, "Address");
		newColumn(table, "Data").setAlignment(SWT.RIGHT);
	}

//	void newStackView() {
//		ViewForm view = Stack.view = newView(wRightSash, "Stack", true);
//		Table table = newTable(view);
//		view.setContent(table);
//
//		newColumn(table, "Address");
//		newColumn(table, "Source").setAlignment(SWT.CENTER);
//		newColumn(table, "Value").setAlignment(SWT.RIGHT);
//
//		newTableItem(table, "FF00", "PC", "000C");
//		newTableItem(table, "FF02", "FLAGS", "02B1");
//		newTableItem(table, "FF04", "PC", "0009");
//	}

	enum MouseDragMode {
		None, DragingScreen
	}

	// NOTE implement screen draging
	MouseDragMode mouseDragMode = MouseDragMode.None;

	Rectangle dragRect;

	Point lastPointerLocation;

	void createScreenView() {
		ViewForm view = new ViewForm(wMiddleSash, SWT.BORDER);

		screenTitle = new CLabel(view, SWT.NONE);
		screenTitle.setAlignment(SWT.LEFT);
		view.setTopLeft(screenTitle);

		sc = new ScrolledComposite(view, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayout(null);
		canvas = new Canvas(sc, SWT.NONE);
		canvas.setBackground(white);
		sc.setContent(canvas);
		view.setContent(sc);

		gotoScreen(screens[0]);

		ToolBar tb = new ToolBar(view, SWT.FLAT);

		view.setTopRight(tb);

		canvas.addPaintListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
	}

	public void keyPressed(KeyEvent e) {
		Point p;
		switch(e.keyCode) {
		case SWT.ARROW_LEFT:
			p = sc.getOrigin();
			p.x -= 30;
			sc.setOrigin(p);
			return;
		case SWT.ARROW_RIGHT:
			p = sc.getOrigin();
			p.x += 30;
			sc.setOrigin(p);
			return;
		case SWT.ARROW_UP:
			p = sc.getOrigin();
			p.y -= 30;
			sc.setOrigin(p);
			return;
		case SWT.ARROW_DOWN:
			p = sc.getOrigin();
			p.y += 30;
			sc.setOrigin(p);
			return;
		}
	}

	public void keyReleased(KeyEvent e) {}

	public void mouseDoubleClick(MouseEvent e) {}

	public void mouseDown(MouseEvent e) {
		if(screen.mouseDown(e)) return;
	}

	public void mouseUp(MouseEvent e) {
		if(screen.mouseUp(e)) return;
	}

	public void mouseMove(MouseEvent e) {
		screen.mouseMove(e);

		// canvas.setCursor(hand);
		//
		// Point p = sc.getOrigin();
		// sc.setOrigin(p.x + start.x - e.x, p.y + start.y - e.y);
		//
		// Point s = display.getCursorLocation();
		// if(s.x < min.x) s.x = min.x;
		// if(s.x > max.x) s.x = max.x;
		// if(s.y < min.y) s.y = min.y;
		// if(s.y > max.y) s.y = max.y;
		// display.setCursorLocation(s);
	}

	public void paintControl(PaintEvent e) {
		screen.paint(gc);
		e.gc.drawImage(screen.image, 0, 0);
	}

	// drawAndGate(gc, 200, 200, 1);
	// duzina 44, sirina 36
	void drawAndGate(GC gc, int x, int y, int dir) {
		Transform a = new Transform(display);
		a.translate(x, y);
		a.rotate(-90 * dir);
		gc.setTransform(a);

		gc.setForeground(black);

		gc.drawPolygon(new int[] { -11, -16, -7, -14, -2, -9, 0, -3, 0, 3, -2, 8, -6, 14, -12, 16, -17,
			18, -44, 18, -44, -18, -17, -18 });
		// x: od -44 do 0
		// y: od -18 do 18

		// -225, -303-26, -133, -253-26, -49, -149-26, 0, -32-26, 0, 86-26, -49,
		// 194-26, -125, 298-26, -236, 352-26, -344, 380-26, -870, 380-26, -870,
		// -328-26, -338, -330-26
		// x: od -870 do 0
		// y: od -354 do 354
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget.getData() != null && e.widget.getData() instanceof Screen)
			gotoScreen((Screen)e.widget.getData());
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	void createControlView() {
		ViewForm view = Control.view = newView(wMiddleSash, "Control", true);
		Composite c = new Composite(view, SWT.NONE);
		view.setContent(c);
		c.setLayout(new RowLayout());

		ToolBar tb = new ToolBar(view, SWT.FLAT | SWT.MULTI | SWT.WRAP);
		for(Screen s : screens) {
			ToolItem b = new ToolItem(tb, SWT.PUSH);
			b.setText(s.title);
			b.setData(s);
			b.addSelectionListener(this);
		}
		view.setTopCenter(tb);

		org.eclipse.swt.widgets.Label a = new org.eclipse.swt.widgets.Label(view, 0);
		view.setContent(a);
	}

	static class TableResizer extends ControlAdapter {
		public void controlResized(ControlEvent e) {
			Table table = (Table)e.widget;
			int w = table.getClientArea().width / table.getColumnCount();
			table.setRedraw(false);
			for(TableColumn c : table.getColumns())
				c.setWidth(w);
			table.setRedraw(true);
		}
	}

	final TableResizer tableResizer = new TableResizer();

	Table newTable(Composite parent) {
		Table table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addControlListener(tableResizer);
		return table;
	}

	TableColumn newColumn(Table table, String name) {
		TableColumn c = new TableColumn(table, SWT.NONE);
		c.setText(name);
		c.setResizable(true);
		return c;
	}

	public static void main(String[] args) {
		new Main().run();
	}
}