package logic;

public class Bus extends Gate {
	//	private ArrayList<Gate> list = new ArrayList<Gate>();
	private final Gate[] list;
	private boolean highZ;

	public boolean isHighZ() {
		return highZ;
	}

	public Bus(Gate... list) {
		super(list[0].bits);
		assert list.length % 2 == 0;

		this.list = list;
		for(int i = 0; i < list.length; i += 2) {
			assert list[i].bits == bits;
			assert list[i + 1].bits == 1;
		}
	}

	public String toHex() {
		return highZ ? "HighZ" : super.toHex();
	}

	//	public void attach(Gate dataOut, Gate oe) {
	//		assert dataOut.bits == bits && oe.bits == 1;
	//		list.add(dataOut);
	//		list.add(oe);
	//	}

	public @Override
	int func() {
		Gate output = null;
		for(int i = 0; i < list.length; i += 2)
			if(list[i + 1].val == 1) {
				assert output == null : "Multiple outputs enabled on same bus!";
				output = list[i];
			}
		highZ = output == null;
		return highZ ? 0 : output.val;
	}
}