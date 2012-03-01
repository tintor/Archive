package simulator;

import java.io.BufferedReader;
import java.io.FileReader;

import logic.Gate;
import assembler.Assembler;

/**
 * @author Marko Tintor
 * @date 06/2006
 */
public class Simulator {
	public final CPU cpu = new CPU();

	//private int cycle;

	public void load(String filename) throws Exception {
		states.clear();
		//cycle = 0;
		Gate.resetAll();
		for(int i = 0; i < cpu.memory.length; i++)
			cpu.memory[i] = 0;

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		while(true) {
			String s = reader.readLine();
			if(s == null) break;
			s = s.trim();
			if(s.length() == 0) continue;

			String[] a = s.split("\\s+");
			String c = a[0].toUpperCase().intern();

			if(c == "SP")
				cpu.SP.set(Integer.parseInt(a[1], 16));
			else if(c == "PC") {
				int pc = Integer.parseInt(a[1], 16);
				cpu.PCH.set(pc >>> 8);
				cpu.PCL.set(pc & 0xF);
			} else if(c == "IVTP")
				cpu.ADRIVT.set(Integer.parseInt(a[1], 16));
			else if(c == "MEMORY") {
				int address = Integer.parseInt(a[1], 16);
				String m = a[2];
				while(m.length() > 0) {
					cpu.memory[address++] = (byte)Integer.parseInt(m.substring(0, 2), 16);
					m = m.substring(2);
				}
			} else if(c == "CODE") {
				boolean r = Assembler.assemble(new FileReader(a[1]), cpu.memory);
				if(!r) throw new Exception("Assembly error!");
			} else
				throw new Exception("Simulation file error!");
		}

		Gate.calculateCombGates();
	}

	public void save(String filename) {
		// TODO
		assert false;
	}

	public int cycle() {
		return states.size();
	}

	public boolean halt() {
		return cpu.hlt.val() == 1;
	}

	static class State {
		int address;
		byte data;
		int[] values;
	}

	private final java.util.Stack<State> states = new java.util.Stack<State>();

	public void restart() {
		while(prevCycle()) {}
		assert states.size() == 0;
		for(int i=0; i<cpu.memory.length; i++)
			assert cpu.memory[i] == 0;
	}

	public void gotoCycle(int c) {
		if(c < 0) return;
		while(c < states.size() && prevCycle()) {}
		while(c > states.size() && nextCycle()) {}
	}

	public boolean prevCycle() {
		if(states.size() == 0) return false;
	
		// restore state
		State s = states.pop();
		if(s.address >= 0) cpu.memory[s.address] = s.data;
		Gate.loadState(s.values);

		if(states.size() == 0) {
			cpu.memLastAddress = -1;
		} else {
			cpu.memLastAddress = states.peek().address;
			cpu.memLastAddress = states.peek().data;
		}
		
		//cycle--;
		return true;
	}

	public boolean nextCycle() {
		if(halt()) return false;
		//cycle++;

		// save state
		State s = new State();
		s.address = cpu.memLastAddress;
		s.data = cpu.memLastData;
		s.values = Gate.saveState();
		states.push(s);
		cpu.memLastAddress = -1;

		Gate.nextCycle();
		return true;
	}

	public void prevInstruction() {
		prevCycle();
		while(cpu.CNT.val() != 0)
			prevCycle();
	}

	public void nextInstruction() {
		if(halt()) return;
		nextCycle();
		while(cpu.CNT.val() != 0 && !halt())
			nextCycle();
	}

	public void executeProgram() {
		while(!halt())
			nextCycle();
	}
}